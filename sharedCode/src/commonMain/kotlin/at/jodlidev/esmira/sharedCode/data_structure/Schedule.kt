package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable
import kotlin.math.ceil

/**
 * Created by JodliDev on 29.05.2019.
 */
@Serializable
class Schedule {
	internal constructor(actionTrigger: ActionTrigger, c: SQLiteCursor) {
		initCursor(c)
		this.actionTrigger = actionTrigger
		
		exists = true
		fromJson = false
	}
	internal constructor(c: SQLiteCursor) {
		initCursor(c)
		val aT = DbLogic.getActionTrigger(c.getLong(COLUMNS.size-1))
		if(aT == null)
			ErrorBox.error("Schedule", "ActionTrigger is null (Schedule=$id, actionTrigger=${c.getLong(
				COLUMNS.size-1)})")
		else
			this.actionTrigger = aT
		exists = true
		fromJson = false
	}
	
	var dailyRepeatRate: Int = 1
	var weekdays: Int = 0
	var dayOfMonth: Int = 0
	var userEditable: Boolean = true
	var skipFirstInLoop: Boolean = false
	
	@Transient var exists = false
	@Transient var fromJson = true
	@Transient var id: Long = 0
	@Transient var lastScheduled: Long = 0
	@Transient private lateinit var actionTrigger: ActionTrigger
	
	@SerialName("signalTimes") private var jsonSignalTimes: List<SignalTime> = ArrayList()
	@Transient private lateinit var _signalTimes: List<SignalTime>
	val signalTimes: List<SignalTime> get() {
		if(!this::_signalTimes.isInitialized) {
			if(fromJson)
				_signalTimes = jsonSignalTimes
			else {
				val c = NativeLink.sql.select(
					SignalTime.TABLE,
					SignalTime.COLUMNS,
					"${SignalTime.KEY_SCHEDULE_ID} = ?", arrayOf(id.toString()),
					null,
					null,
					null,
					null
				)
				val signalTimes = ArrayList<SignalTime>()
				while (c.moveToNext()) {
					signalTimes.add(SignalTime(c, this))
				}
				c.close()
				_signalTimes = signalTimes
			}
		}
		return _signalTimes
	}
	
	private fun initCursor(c: SQLiteCursor) {
		id = c.getLong(0)
		lastScheduled = c.getLong(1)
		userEditable = c.getBoolean(2)
		dailyRepeatRate = c.getInt(3)
		skipFirstInLoop = c.getBoolean(4)
		weekdays = c.getInt(5)
		dayOfMonth = c.getInt(6)
	}
	fun bindParent(actionTrigger: ActionTrigger) {
		this.actionTrigger = actionTrigger
	}
	
	internal fun isDifferent(other: Schedule): Boolean {
		if(
			userEditable != other.userEditable ||
			dailyRepeatRate != other.dailyRepeatRate ||
			weekdays != other.weekdays ||
			dayOfMonth != other.dayOfMonth ||
			skipFirstInLoop != other.skipFirstInLoop
		) {
			println("Schedule content is different: $userEditable==${other.userEditable}, $dailyRepeatRate==${other.dailyRepeatRate}, $weekdays==${other.weekdays}, $dayOfMonth==${other.dayOfMonth}, $skipFirstInLoop==${other.skipFirstInLoop}")
			return true
		}
		else {
			val otherSignalTimes = other.signalTimes
			if(otherSignalTimes.size != signalTimes.size)
				return true

			for(i in signalTimes.indices) {
				if(signalTimes[i].isDifferent(otherSignalTimes[i]))
					return true
			}
		}
		return false
	}
	internal fun isFaulty(): Boolean {
		for(signalTime in signalTimes) {
			if(signalTime.isFaulty())
				return true
		}
		return false
	}
	
	internal fun saveAndScheduleIfExists(db: SQLiteInterface = NativeLink.sql) {
		val questionnaireId = actionTrigger.questionnaire.id
		val values = db.getValueBox()
		values.putLong(KEY_ACTION_TRIGGER, actionTrigger.id)
		values.putLong(KEY_LAST_SCHEDULED, 0) //is always 0 when newly created or updated (and emptied in the process)
		values.putBoolean(KEY_EDITABLE, userEditable)
		values.putLong(KEY_QUESTIONNAIRE_ID, questionnaireId)
		values.putInt(KEY_REPEAT_RATE, dailyRepeatRate)
		values.putBoolean(KEY_SKIP_FIRST_IN_LOOP, skipFirstInLoop)
		values.putInt(KEY_WEEKDAYS, weekdays)
		values.putInt(KEY_DAY_OF_MONTH, dayOfMonth)
		
		if(exists) {
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
			empty(db)
		}
		else
			id = db.insert(TABLE, values)
		
		for(signalTime in signalTimes) {
			signalTime.bindParent(questionnaireId, this)
			signalTime.save(db)
		}
		
		//create alarms:
		scheduleIfNeeded()
	}
	
	internal fun scheduleIfNeeded() {
		if(actionTrigger.enabled) {
			val initialDelay = getInitialDelayDays()
			for(signalTime in signalTimes) {
				if(signalTime.hasNoAlarms())
					Scheduler.scheduleSignalTime(signalTime, actionTrigger.id, NativeLink.getNowMillis(), initialDelay)
			}
		}
	}
	
	internal fun saveTimeFrames(db: SQLiteInterface = NativeLink.sql, _rescheduleNow: Boolean = false) {
		val rescheduleNow = if(!_rescheduleNow) {
			val nextAlarm = DbLogic.getNextAlarm(this)
			if(nextAlarm == null)
				true
			else
				nextAlarm.timestamp - NativeLink.getNowMillis() > Scheduler.ONE_DAY_MS //when the next alarm is not in the next 24 hours, we are safe to reschedule
		}
		else
			true
		
		for(signalTime in signalTimes) {
			signalTime.saveTimeFrames(this, rescheduleNow, db)
		}
	}
	
	internal fun empty(db: SQLiteInterface = NativeLink.sql) {
		Scheduler.remove(this)
		db.delete(SignalTime.TABLE, "${SignalTime.KEY_SCHEDULE_ID} = ?", arrayOf(id.toString()))
	}
	
	internal fun getInitialDelayDays():Int { //in days
		return if(skipFirstInLoop) dailyRepeatRate else 0
	}
	
	internal fun getQuestionnaire(): Questionnaire {
		return actionTrigger.questionnaire
	}
	
	internal fun getActionTriggerId(): Long {
		return actionTrigger.id
	}
	
	internal fun toDescString(): String {
		val s = StringBuilder()
		for(signalTime in signalTimes) {
			s.append(signalTime.questionnaire.title)
			s.append(": ")
			s.append(signalTime.getFormattedStart())
			if(signalTime.random) {
				s.append('-')
				s.append(signalTime.getFormattedEnd())
			}
			s.append(", ")
		}
		return s.toString()
	}
	
	companion object {
		const val TABLE = "schedules"
		const val KEY_ID = "_id"
		const val KEY_ACTION_TRIGGER = "action_trigger"
		const val KEY_QUESTIONNAIRE_ID = "questionnaire_id"
		const val KEY_LAST_SCHEDULED = "last_scheduled"
		const val KEY_EDITABLE = "editable"
		const val KEY_REPEAT_RATE = "repeat_rate"
		const val KEY_SKIP_FIRST_IN_LOOP = "skip_first_in_loop"
		const val KEY_WEEKDAYS = "weekdays"
		const val KEY_DAY_OF_MONTH = "dayOfMonth"

		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_LAST_SCHEDULED,
			KEY_EDITABLE,
			KEY_REPEAT_RATE,
			KEY_SKIP_FIRST_IN_LOOP,
			KEY_WEEKDAYS,
			KEY_DAY_OF_MONTH,
			KEY_ACTION_TRIGGER //is usually ignored
		)
		
		fun updateLastScheduled(id: Long, timestamp: Long) {
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putLong(KEY_LAST_SCHEDULED, timestamp)
			db.update(TABLE, values, "$KEY_ID = ? AND $KEY_LAST_SCHEDULED < ?", arrayOf(id.toString(), timestamp.toString()))
		}
	}
}