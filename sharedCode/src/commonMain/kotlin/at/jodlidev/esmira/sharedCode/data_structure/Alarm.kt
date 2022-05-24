package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.*

/**
 * Created by JodliDev on 03.06.2019.
 * One Alarm for each timed actionTrigger
 */
class Alarm {
	enum class TYPES {
		SignalTime, EventTrigger, Reminder
	}
	var id: Long = 0
	var actionTriggerId: Long = 0
	var questionnaireId: Long = 0
	var signalTimeId: Long = -1
	var scheduleId: Long = -1
	var eventTriggerId: Long = -1
	var timestamp: Long = 0
	var type = TYPES.SignalTime
	var indexNum = 1 //used when frequency > 1 to tell Alarms that belong to the same SignalTime apart
	var reminderCount = 0
	var onlySingleActionIndex = -1
	var label: String = "Error"
//	var wasRescheduled: Boolean = false
	
	var exists = false
	
	private lateinit var _actionTrigger: ActionTrigger //used as cache
	val actionTrigger: ActionTrigger
		get() {
			if(!this::_actionTrigger.isInitialized) {
				val actionTrigger = DbLogic.getActionTrigger(actionTriggerId)
				if(actionTrigger == null) {
					ErrorBox.error(
						"Alarm",
						"Alarm \"$label\" (id=$id, type=$type) had an error : ActionTrigger (id=$actionTriggerId) is null! Removing Alarm to prevent deathloop!\nQuestionnaire=$questionnaireId, Event=$eventTriggerId, Schedule=$scheduleId, SignalTime=$signalTimeId, timestamp=$timestamp"
					)
					delete()
					throw Exception("Alarm \"$label\" (id=$id) had an error : ActionTrigger (id=$actionTriggerId) is null!")
				}
				else
					_actionTrigger = actionTrigger
			}
			return _actionTrigger
		}
	
	private var _signalTime: SignalTime? = null //used as cache
	val signalTime: SignalTime?
		get() {
			if(_signalTime == null) {
				_signalTime = DbLogic.getSignalTime(signalTimeId)
				if(_signalTime == null)
					ErrorBox.error(
						"Alarm",
						"SignalTime is null! ActionTrigger: $actionTriggerId, Alarm: $label (id=$id), Questionnaire: $questionnaireId, SignalTime: $signalTimeId"
					)
			}
			return _signalTime
		}
	
	val canBeRescheduled: Boolean
		get() {
			val signalTime = this.signalTime
			return signalTime != null && (!signalTime.random || indexNum == signalTime.frequency)
		}
	
	constructor(
		timestamp: Long,
		questionnaireId: Long,
		actionTriggerId: Long,
		label: String,
		onlySingleActionIndex: Int,
		reminderCount: Int,
		evenTriggerId: Long = -1,
		signalTimeId: Long = -1
	) {
		this.questionnaireId = questionnaireId
		this.actionTriggerId = actionTriggerId
		this.timestamp = timestamp
		this.type = TYPES.Reminder
		this.label = label
		this.onlySingleActionIndex = onlySingleActionIndex
		this.reminderCount = reminderCount
		
		this.eventTriggerId = evenTriggerId
		this.signalTimeId = signalTimeId
	}
	
	constructor(eventTrigger: EventTrigger, utc_timestamp: Long) {
		questionnaireId = eventTrigger.questionnaireId
		actionTriggerId = eventTrigger.actionTriggerId
		eventTriggerId = eventTrigger.id
		this.timestamp = utc_timestamp
		type = TYPES.EventTrigger
		label = eventTrigger.label
	}
	
	constructor(signalTime: SignalTime, actionTriggerId: Long, timestamp: Long, indexNum: Int) {
		this.questionnaireId = signalTime.questionnaireId
		this.actionTriggerId = actionTriggerId
		this.signalTimeId = signalTime.id
		this.scheduleId = signalTime.scheduleId
		this.timestamp = timestamp
		this.type = TYPES.SignalTime
		this.indexNum = indexNum
		this.label = signalTime.label
		this._signalTime = signalTime
//		wasRescheduled = canBeRescheduled
	}
	
	constructor(c: SQLiteCursor) {
		loadCursor(c, 0)
	}
	
	private fun loadCursor(c: SQLiteCursor, i: Int) {
		id = c.getLong(i)
		questionnaireId = c.getLong(i + 1)
		actionTriggerId = c.getLong(i + 2)
		signalTimeId = if(c.isNull(i + 3)) -1 else c.getLong(i + 3)
		scheduleId = if(c.isNull(i + 4)) -1 else c.getLong(i + 4)
		eventTriggerId = if(c.isNull(i + 5)) -1 else c.getLong(i + 5)
		timestamp = c.getLong(i + 6)
		type = TYPES.values()[c.getInt(i + 7)]
		indexNum = c.getInt(i + 8)
		label = c.getString(i + 9)
		onlySingleActionIndex = c.getInt(i + 10)
		reminderCount = c.getInt(i + 11)
//		wasRescheduled = c.getBoolean(i + 12)
		
		exists = true
	}
	
	private fun getLastAlarm(): Alarm? { //for iOS
		val signalTime = signalTime ?: return null
		val lastAlarm = DbLogic.getLastSignalTimeAlarm(signalTime) ?: return null
		return if(lastAlarm.id != id) lastAlarm else null
	}
	
	fun save() {
		if(exists) {
			ErrorBox.error("Alarm", "Alarm \"$label\" (id=$id) already exists!")
			return
		}
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_QUESTIONNAIRE_ID, questionnaireId)
		values.putLong(KEY_ACTION_TRIGGER_ID, actionTriggerId)
		values.putLong(KEY_SIGNAL_TIME_ID, if(signalTimeId == -1L) null else signalTimeId)
		values.putLong(KEY_SCHEDULE_ID, if(scheduleId == -1L) null else scheduleId)
		values.putLong(KEY_EVENT_TRIGGER_ID, if(eventTriggerId == -1L) null else eventTriggerId)
		values.putLong(KEY_TIMESTAMP, timestamp)
		values.putInt(KEY_TYPE, type.ordinal)
		values.putInt(KEY_INDEX_NUM, indexNum)
		values.putString(KEY_LABEL, label)
		values.putInt(KEY_ONLY_SINGLE_ACTION_INDEX, onlySingleActionIndex)
		values.putInt(KEY_REMINDER_COUNT, reminderCount)
		
		id = db.insert(TABLE, values)
		
		schedule()
		if(NativeLink.smartphoneData.phoneType == PhoneType.IOS)
			scheduleReminder()
	}
//	private fun setWasRescheduled() { //only ued in IOS
//		wasRescheduled = true
//
//		val db = NativeLink.sql
//		val values = db.getValueBox()
//		values.putInt(KEY_WAS_RESCHEDULED,  1)
//		db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
//	}
	
	private fun scheduleReminder() {
		if(type == TYPES.SignalTime || type == TYPES.EventTrigger) {
			actionTrigger.iteratePostponedReminder(this)
		}
	}
	internal fun schedule() {
		if(NativeLink.postponedActions.scheduleAlarm(this)) {
			if(signalTimeId != -1L)
				Schedule.updateLastScheduled(signalTimeId, timestamp)

			ErrorBox.log(
				"Alarm",
				"Scheduled \"$label\" (id=$id, type=$type), starting in: ${(timestamp - NativeLink.getNowMillis()) / 60000} min (${
					NativeLink.formatDateTime(timestamp)
				})"
			)
		}
		else {
			ErrorBox.warn(
				"Alarm",
				"Could not schedule alarm \"$label\" (id=$id, type=$type), starting in: ${(timestamp - NativeLink.getNowMillis()) / 60000} min (${
					NativeLink.formatDateTime(timestamp)
				})"
			)
			delete()
		}
	}
	
	fun scheduleAhead() { //for IOS
		// this function will only schedule one Alarm
		
		if(canBeRescheduled) {
			val signalTime = this.signalTime
			val questionnaire = DbLogic.getQuestionnaire(questionnaireId)
			if(signalTime != null && questionnaire != null) {
				val alarm = DbLogic.getLastSignalTimeAlarm(signalTime) ?: this
				val timestampAnchor = alarm.timestamp
				
				ErrorBox.log(
					"Alarm",
					"Scheduling \"$label\" (id=$id) ahead. Anchor in ${(timestampAnchor - NativeLink.getNowMillis()) / 60000} min (${
						NativeLink.formatDateTime(timestampAnchor)
					})"
				)
				Scheduler.rescheduleFromSignalTime(signalTime, actionTriggerId, timestampAnchor)
			}
		}
	}
	fun exec(fireNotifications: Boolean = true) { //fireNotifications is false on IOS when called from checkMissedAlarms()
		ErrorBox.log(
			"Alarm",
			"Running Alarm $label (id=$id, type=$type, SignalTime=$signalTimeId, ActionTrigger=$actionTriggerId, Questionnaire=$questionnaireId)"
		)
		delete()
		
		val q = DbLogic.getQuestionnaire(questionnaireId)
		
		if(q == null) {
			ErrorBox.error(
				"Alarm",
				"Questionnaire is null! Alarm=$id, SignalTime=$signalTimeId, ActionTrigger=$actionTriggerId, Questionnaire=$questionnaireId"
			)
			return
		}
		else if(!q.isActive()) {
			if(type == TYPES.SignalTime) {
				if(q.willBeActiveIn() > 0) {
					ErrorBox.log(
						"Alarm",
						"Questionnaire \"${q.title}\" is not active yet. Postponing alarm (id=$id, type=$type)."
					)
					Scheduler.rescheduleFromAlarm(this)
				}
				else {
					ErrorBox.log(
						"Alarm",
						"Questionnaire \"${q.title}\" is not active anymore. Canceling alarm (id=$id, type=$type)."
					)
				}
			}
			return
		}
		
		val studyId: Long
		when(type) {
			TYPES.SignalTime -> {
				studyId = q.studyId
				
				actionTrigger.execActions(label, timestamp, fireNotifications)
				
				Scheduler.rescheduleFromAlarm(this)
			}
			TYPES.EventTrigger -> {
				val eventTrigger: EventTrigger? = DbLogic.getEventTrigger(eventTriggerId)
				if(eventTrigger != null)
					eventTrigger.exec(this, fireNotifications)
				else {
					ErrorBox.error("AlarmBox", "Event is null! (Event=${eventTriggerId})")
					return
				}
				studyId = eventTrigger.studyId
			}
			TYPES.Reminder -> {
				studyId = q.studyId
				if(fireNotifications)
					actionTrigger.issueReminder(label, timestamp, onlySingleActionIndex, reminderCount)
			}
		}

		if(NativeLink.smartphoneData.phoneType == PhoneType.IOS)
			DataSet.createAlarmExecuted(q, studyId, timestamp)
	}
	
	fun delete() {
		ErrorBox.log(
			"Alarm",
			"Removing Alarm \"$label\" (id=$id, type=$type, timestamp=$timestamp)"
		)
		NativeLink.postponedActions.cancel(this)
		NativeLink.notifications.remove(id.toInt())
		NativeLink.sql.delete(TABLE, "$KEY_ID = ?", arrayOf(id.toString()))
	}
	
	companion object {
		const val TABLE = "alarms"
		const val KEY_ID = "_id"
		const val KEY_QUESTIONNAIRE_ID = "questionnaire_id"
		const val KEY_ACTION_TRIGGER_ID = "actionTrigger_id"
		const val KEY_SIGNAL_TIME_ID = "signal_time_id"
		const val KEY_SCHEDULE_ID = "schedule_id"
		const val KEY_EVENT_TRIGGER_ID = "event_trigger_id"
		const val KEY_TIMESTAMP = "alarm_timestamp"
		const val KEY_TYPE = "alarm_type"
		const val KEY_INDEX_NUM = "alarm_index"
		const val KEY_LABEL = "alarm_label"
		const val KEY_REMINDER_COUNT = "reminder_count"
		const val KEY_ONLY_SINGLE_ACTION_INDEX = "only_single_action_index"
		
		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_QUESTIONNAIRE_ID,
			KEY_ACTION_TRIGGER_ID,
			KEY_SIGNAL_TIME_ID,
			KEY_SCHEDULE_ID,
			KEY_EVENT_TRIGGER_ID,
			KEY_TIMESTAMP,
			KEY_TYPE,
			KEY_INDEX_NUM,
			KEY_LABEL,
			KEY_ONLY_SINGLE_ACTION_INDEX,
			KEY_REMINDER_COUNT
		)

		internal fun createFromSignalTime(signalTime: SignalTime, actionTriggerId: Long, timestamp: Long, indexNum: Int=1): Alarm {
			val alarm = Alarm(signalTime, actionTriggerId, timestamp, indexNum)
			alarm.save()
			return alarm
		}
		
		internal fun createFromEventTrigger(eventTrigger: EventTrigger, timestamp: Long): Alarm {
			val alarm = Alarm(eventTrigger, timestamp)
			alarm.save()
			return alarm
		}
		
		internal fun createAsReminder(
			timestamp: Long,
			questionnaireId: Long,
			actionTriggerId: Long,
			label: String,
			onlySingleActionIndex: Int,
			reminderCount: Int,
			evenTriggerId: Long = -1,
			signalTimeId: Long = -1
		): Alarm {
			val alarm = Alarm(timestamp, questionnaireId, actionTriggerId, label, onlySingleActionIndex, reminderCount, evenTriggerId, signalTimeId)
			alarm.save()
			return alarm
		}
	}
}