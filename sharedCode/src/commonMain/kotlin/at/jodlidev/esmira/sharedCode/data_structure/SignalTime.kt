package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.*
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 04.06.2019.
 */
@Serializable
class SignalTime {
	var random = false
	internal var randomFixed = false
	var frequency = 1
	internal var minutesBetween = 60
	internal var startTimeOfDay = 0 //starting from midnight; can be changed by the user
	internal var endTimeOfDay = 0 //starting from midnight; can be changed by the user
	private var originalStartTimeOfDay = 0 //starting from midnight
	private var originalEndTimeOfDay = 0 //starting from midnight
	
	@Transient internal var exists = false
	@Transient internal var id: Long = -1
	@Transient internal var scheduleId: Long = -1
	@Transient internal var questionnaireId: Long = -1
	
	@Transient internal var timeHasChanged: Boolean = false //not in db for manual schedule changes
	
	@Transient private lateinit var _schedule: Schedule
	val schedule: Schedule
		get() {
			if(!this::_schedule.isInitialized)
				_schedule = DbLogic.getSchedule(scheduleId)
					?: throw Exception("SignalTime (id=$id) had an error. Schedule (id=$scheduleId) is null!")
			return _schedule
		}
	@Transient private lateinit var _questionnaire: Questionnaire
	val questionnaire: Questionnaire
		get() {
			if(!this::_questionnaire.isInitialized)
				_questionnaire = DbLogic.getQuestionnaire(questionnaireId)
					?: throw Exception("SignalTime (id=$id) had an error. Questionnaire (id=$questionnaireId) is null!")
			return _questionnaire
		}
	
	constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		scheduleId = c.getLong(1)
		questionnaireId = c.getLong(2)
		random = c.getBoolean(3)
		randomFixed = c.getBoolean(4)
		frequency = c.getInt(5)
		minutesBetween = c.getInt(6)
		startTimeOfDay = c.getInt(7)
		endTimeOfDay = c.getInt(8)
		originalStartTimeOfDay = c.getInt(9)
		originalEndTimeOfDay = c.getInt(10)
		exists = true
	}
	
	constructor(c: SQLiteCursor, schedule: Schedule) : this(c) {
		this._schedule = schedule
	}
	
	init {
		if(!random) {
			randomFixed = false
			frequency = 1
		}
		if(frequency == 0) //Should never happen. But it would break a lot (dividing by Zero and stuff). So we better play safe
			frequency = 1
	}
	
	fun bindParent(questionnaireId: Long, schedule: Schedule) {
		this.scheduleId = schedule.id
		this.questionnaireId = questionnaireId
		this._schedule = schedule
		
	}
	
	fun isFaulty(): Boolean {
		return if(!random)
			false
		else if(frequency <= 1)
			endTimeOfDay - startTimeOfDay < minutesBetween * 60000 //makes sure that user can not have time window that is too small (default 60 min)
		else
			(endTimeOfDay - startTimeOfDay) / frequency < minutesBetween * 60000
	}
	fun isDifferent(other: SignalTime): Boolean {
		val start: Int
		val end: Int
		if(exists) {
			start = originalStartTimeOfDay
			end = originalEndTimeOfDay
		}
		else { //originalStartTimeOfDay and originalEndTimeOfDay will get set when save() was called the first time. So they don't exist yet
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
			startOther = other.startTimeOfDay
			endOther = other.endTimeOfDay
		}
		
		return random != other.random
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
		startTimeOfDay = (timestamp - NativeLink.getMidnightMillis(timestamp)).toInt() % ONE_DAY
		correctStartEndPeriod()
		timeHasChanged = true
	}
	fun setEnd(timestamp: Long) {
		endTimeOfDay = (timestamp - NativeLink.getMidnightMillis(timestamp)).toInt() % ONE_DAY
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
	
	fun hasNoAlarms(): Boolean {
		return DbLogic.getAlarmsFrom(this).isEmpty()
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
		else {
			id = db.insert(TABLE, values)
			exists = true
		}
	}
	
	internal fun saveTimeFrames(schedule: Schedule, rescheduleNow: Boolean = false, db: SQLiteInterface = NativeLink.sql,) {
		if(!exists) {
			ErrorBox.error("SignalTime", "SignalTime(id=$id) does not exist!")
			return
		}
		else if(!timeHasChanged) {
			ErrorBox.log("SignalTime", "SignalTime(id=$id) was not changed. Skipping...")
			return
		}
		
		
		val values = db.getValueBox()
		values.putInt(KEY_START_TIME_OF_DAY, startTimeOfDay)
		values.putInt(KEY_END_TIME_OF_DAY, endTimeOfDay)
		
		db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		
		
		if(rescheduleNow) { //this will reschedule immediately. If false, the changes will take effect after the current Alarm has been issued
			Scheduler.remove(this)
			Scheduler.scheduleSignalTime(this, schedule.getActionTriggerId(), NativeLink.getNowMillis(), schedule.getInitialDelayDays())
		}
		else if(NativeLink.smartphoneData.phoneType == PhoneType.IOS) {
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
				Scheduler.scheduleSignalTime(this, schedule.getActionTriggerId(), NativeLink.getNowMillis(), schedule.getInitialDelayDays())
		}
		DataSet.createScheduleChangedDataSet(schedule)
		Scheduler.scheduleAhead()
	}
	
	companion object {
		const val TABLE = "signalTimes"
		const val KEY_ID = "_id"
		const val KEY_SCHEDULE_ID = "schedule_id"
		const val KEY_QUESTIONNAIRE_ID = "questionnaire_id"
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