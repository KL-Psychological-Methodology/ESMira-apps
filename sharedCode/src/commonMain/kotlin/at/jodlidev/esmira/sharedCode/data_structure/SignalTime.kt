package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.*
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 04.06.2019.
 */
@Serializable
class SignalTime {
	var label: String = ""
	var random = false
	var randomFixed = false
	var frequency = 1
	var minutesBetween = 60
	var startTimeOfDay = 0 //starting from midnight; can be changed by the user
	var endTimeOfDay = 0 //starting from midnight; can be changed by the user
	private var originalStartTimeOfDay = 0 //starting from midnight
	private var originalEndTimeOfDay = 0 //starting from midnight
	
	@Transient var exists = false
	@Transient var id: Long = -1
	@Transient var scheduleId: Long = -1
	@Transient var questionnaireId: Long = -1
	
	@Transient var timeHasChanged: Boolean = false //not in db for manual schedule changes
	
	@Transient private lateinit var _schedule: Schedule
	val schedule: Schedule
		get() {
			if(!this::_schedule.isInitialized)
				_schedule = DbLogic.getSchedule(scheduleId)
					?: throw Exception("SignalTime \"$label\" (id=$id) had an error. Schedule (id=$scheduleId) is null!")
			return _schedule
		}
	
	constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		scheduleId = c.getLong(1)
		questionnaireId = c.getLong(2)
		label = c.getString(3)
		random = c.getBoolean(4)
		randomFixed = c.getBoolean(5)
		frequency = c.getInt(6)
		minutesBetween = c.getInt(7)
		startTimeOfDay = c.getInt(8)
		endTimeOfDay = c.getInt(9)
		originalStartTimeOfDay = c.getInt(10)
		originalEndTimeOfDay = c.getInt(11)
		exists = true
	}
	
	constructor(c: SQLiteCursor, schedule: Schedule) : this(c) {
		this._schedule = schedule
	}
	
	fun bindParent(questionnaireId: Long, schedule: Schedule) {
		this.scheduleId = schedule.id
		this.questionnaireId = questionnaireId
		this._schedule = schedule
		
		if(frequency == 0) //Should never happen. But it would break a lot (dividing by Zero and stuff). So we better play safe
			frequency = 1
	}
	
	fun isFaulty(): Boolean {
		return if(!random)
			false
		else if(frequency <= 1)
			endTimeOfDay - startTimeOfDay < minutesBetween * 60000
		else
			endTimeOfDay - startTimeOfDay < (frequency * minutesBetween + minutesBetween) * 60000
	}
	fun isDifferent(other: SignalTime): Boolean {
		val start: Int
		val end: Int
		if(exists) {
			start = originalStartTimeOfDay
			end = originalEndTimeOfDay
		}
		else { //originalStartTimeOfDay and originalEndTimeOfDay will get set when save() was called the first time. So it doesnt exist yet
			start = startTimeOfDay
			end = endTimeOfDay
		}
		val startOther: Int
		val endOther: Int
		if(other.exists) {
			startOther = other.originalStartTimeOfDay
			endOther = other.originalEndTimeOfDay
		}
		else {
			startOther = startTimeOfDay
			endOther = endTimeOfDay
		}
		
		return label != other.label
			|| random != other.random
			|| randomFixed != other.randomFixed
			|| frequency != other.frequency
			|| minutesBetween != other.minutesBetween
			|| start != startOther
			|| end != endOther
	}
	
	private fun correctStartEndPeriod() {
		if(random && endTimeOfDay < startTimeOfDay)
			endTimeOfDay += ONE_DAY
	}
	
	fun setStart(timestamp: Long) {
		startTimeOfDay = (timestamp - NativeLink.getMidnightMillis()).toInt() % ONE_DAY
		correctStartEndPeriod()
		timeHasChanged = true
	}
	
	fun setEnd(timestamp: Long) {
		endTimeOfDay = (timestamp - NativeLink.getMidnightMillis()).toInt() % ONE_DAY
		correctStartEndPeriod()
		timeHasChanged = true
	}
	fun getStart(): Long {
		return NativeLink.getMidnightMillis() + startTimeOfDay
	}
	
	fun getEnd(): Long {
		return NativeLink.getMidnightMillis() + endTimeOfDay
	}
	
	fun getFormattedStart(): String {
		return NativeLink.formatTime(NativeLink.getMidnightMillis() + startTimeOfDay)
	}
	
	fun getFormattedEnd(): String {
		return NativeLink.formatTime(NativeLink.getMidnightMillis() + endTimeOfDay)
	}
	
	
	fun save(db: SQLiteInterface = NativeLink.sql) {
		if(!exists) {
			correctStartEndPeriod()
			originalEndTimeOfDay = endTimeOfDay
			originalStartTimeOfDay = startTimeOfDay
		}
		
		val values = db.getValueBox()
		values.putLong(KEY_SCHEDULE_ID, scheduleId)
		values.putLong(KEY_QUESTIONNAIRE_ID, questionnaireId)
		values.putString(KEY_LABEL, label)
		values.putBoolean(KEY_RANDOM, random)
		values.putBoolean(KEY_RANDOM_FIXED, randomFixed)
		values.putInt(KEY_FREQUENCY, frequency)
		values.putInt(KEY_MINUTES_BETWEEN, minutesBetween)
		values.putInt(KEY_START_TIME_OF_DAY, startTimeOfDay)
		values.putInt(KEY_END_TIME_OF_DAY, endTimeOfDay)
		values.putInt(KEY_ORIGINAL_START_TIME_OF_DAY, originalStartTimeOfDay)
		values.putInt(KEY_ORIGINAL_END_TIME_OF_DAY, originalEndTimeOfDay)

		if(exists) {
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		}
		else
			id = db.insert(TABLE, values)
	}
	
	fun saveTimeFrames(db: SQLiteInterface = NativeLink.sql, schedule: Schedule, rescheduleNow: Boolean = false) {
		if(!exists) {
			ErrorBox.error("SignalTime", "SignalTime(label=$label, id=$id) does not exist!")
			return
		}
		else if(!timeHasChanged) {
			ErrorBox.log("SignalTime", "SignalTime(label=$label, id=$id) was not changed. Skipping...")
			return
		}
		
		
		val values = db.getValueBox()
		values.putInt(KEY_START_TIME_OF_DAY, startTimeOfDay)
		values.putInt(KEY_END_TIME_OF_DAY, endTimeOfDay)
		
		db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		
		
		if(rescheduleNow) { //this will reschedule immediately. If false, the changes will take effect after the current Alarm has been issued
			Scheduler.remove(this)
			Scheduler.scheduleSignalTime(this, schedule.getActionTriggerId(), NativeLink.getNowMillis(), schedule.getInitialDelay())
		}
		else if(isIOS()) {
			val cancelAlarms = DbLogic.getAlarmsAfterToday(this)
			var searchFirst = true
			for(alarm in cancelAlarms) {
				if(searchFirst) { //we dont want to remove the part of a SignalTime batch so we search for the first one (which works because getAlarmsAfterToday() is ordered)
					if(alarm.indexNum != 1)
						continue
					else
						searchFirst = false
				}
				alarm.delete()
			}
			
			
			val alarm = DbLogic.getLastSignalTimeAlarm(this)
			if(alarm == null)
				Scheduler.scheduleSignalTime(this, schedule.getActionTriggerId(), NativeLink.getNowMillis(), schedule.getInitialDelay())
			else
				alarm.scheduleAhead()
		}
		DataSet.createScheduleChangedDataSet(schedule)
	}
	
	companion object {
		const val TABLE = "signalTimes"
		const val KEY_ID = "_id"
		const val KEY_SCHEDULE_ID = "schedule_id"
		const val KEY_QUESTIONNAIRE_ID = "group_id"
		const val KEY_LABEL = "label"
		const val KEY_RANDOM = "random"
		const val KEY_RANDOM_FIXED = "random_fixed"
		const val KEY_FREQUENCY = "frequency"
		const val KEY_MINUTES_BETWEEN = "minutes_between"
		const val KEY_START_TIME_OF_DAY = "start_timeOfDay"
		const val KEY_END_TIME_OF_DAY = "end_timeOfDay"
		const val KEY_ORIGINAL_START_TIME_OF_DAY = "original_start_timeOfDay"
		const val KEY_ORIGINAL_END_TIME_OF_DAY = "original_end_timeOfDay"
		
		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_SCHEDULE_ID,
			KEY_QUESTIONNAIRE_ID,
			KEY_LABEL,
			KEY_RANDOM,
			KEY_RANDOM_FIXED,
			KEY_FREQUENCY,
			KEY_MINUTES_BETWEEN,
			KEY_START_TIME_OF_DAY,
			KEY_END_TIME_OF_DAY,
			KEY_ORIGINAL_START_TIME_OF_DAY,
			KEY_ORIGINAL_END_TIME_OF_DAY
		)
		
		private const val ONE_DAY = 1000 * 60 * 60 * 24
	}
}