package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable

import kotlin.math.abs

/**
 * Created by JodliDev on 15.04.2019.
 */
@Serializable
class ActionTrigger {
	@SerialName("actions") @Serializable(with = JsonToStringSerializer::class) var actionsString: String = "[]"
	
	@Transient var exists = false
	@Transient var fromJsonOrUpdated = true
	@Transient var id: Long = -1
	@Transient var enabled = false
	@Transient var studyId: Long = -1
	
	@Transient var questionnaireId: Long = -1
	@Transient private lateinit var _questionnaire: Questionnaire
	val questionnaire: Questionnaire
		get() {
			if(!this::_questionnaire.isInitialized)
				_questionnaire = DbLogic.getQuestionnaire(questionnaireId)
					?: throw Exception("ActionTrigger (id=$id) had an error. Questionnaire (id=$questionnaireId) is null!")
			return _questionnaire
		}
	
	@SerialName("eventTriggers") private var jsonEventTriggers: List<EventTrigger> = ArrayList()
	@Transient private lateinit var _eventTriggers: List<EventTrigger>
	val eventTriggers: List<EventTrigger> get() {
		if(!this::_eventTriggers.isInitialized) {
			_eventTriggers = if(fromJsonOrUpdated)
				jsonEventTriggers
			else
				loadEventTriggersDB()
		}
		return _eventTriggers
	}
	
	@SerialName("schedules") private var jsonSchedules: List<Schedule> = ArrayList()
	@Transient private lateinit var _schedules: List<Schedule>
	val schedules: List<Schedule> get() {
		if(!this::_schedules.isInitialized) {
			_schedules = if(fromJsonOrUpdated)
				jsonSchedules
			else
				loadSchedulesDB()
		}
		return _schedules
	}
	
	
	constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		enabled = c.getBoolean(1)
		actionsString = c.getString(2)
		studyId = c.getLong(3)
		questionnaireId = c.getLong(4)
		exists = true
		fromJsonOrUpdated = false
	}
	constructor(questionnaire: Questionnaire, c: SQLiteCursor): this(c) {
		this._questionnaire = questionnaire
	}
	
	internal fun bindParent(studyId: Long, questionnaire: Questionnaire) {
		this.studyId = studyId
		this._questionnaire = questionnaire
		this.questionnaireId = questionnaire.id
	}
	
	
	private fun loadEventTriggersDB(): MutableList<EventTrigger> {
		val c = NativeLink.sql.select(
			EventTrigger.TABLE,
			EventTrigger.COLUMNS,
			EventTrigger.KEY_ACTION_TRIGGER_ID + " = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			null)
		val eventTriggers: MutableList<EventTrigger> = ArrayList()
		while(c.moveToNext()) {
			eventTriggers.add(EventTrigger(this, c))
		}
		c.close()
		return eventTriggers
	}
	
	private fun loadSchedulesDB(): List<Schedule> {
		val c = NativeLink.sql.select(
			Schedule.TABLE,
			Schedule.COLUMNS,
			Schedule.KEY_ACTION_TRIGGER + " = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			null
		)
		val schedules: MutableList<Schedule> = ArrayList()
		while(c.moveToNext()) {
			schedules.add(Schedule(this, c))
		}
		c.close()
		return schedules
	}
	
	internal fun hasSchedules(): Boolean {
		return schedules.isNotEmpty()
	}
	internal fun hasEvents(): Boolean {
		return eventTriggers.isNotEmpty()
	}
	internal fun hasDelayedEvents(): Boolean {
		for(event in eventTriggers) {
			if(event.delaySec != 0)
				return true
		}
		return false
	}
	fun hasNotifications(): Boolean {
		val actions = getActionArray()
		for(actionJson in actions) {
			val action = Action(actionJson.jsonObject)
			when(action.type) {
				JSON_ACTION_TYPE_INVITATION,
				JSON_ACTION_TYPE_NOTIFICATION,
				JSON_ACTION_TYPE_MSG ->
					return true
			}
		}
		return if(NativeLink.smartphoneData.phoneType == PhoneType.Android) false else usesPostponedActions()
	}
	fun hasInvitation(nothingElse: Boolean = false): Boolean {
		val actions = getActionArray()
		if(nothingElse && actions.size != 1)
			return false
		
		for(actionJson in actions) {
			val action = Action(actionJson.jsonObject)
			when(action.type) {
				JSON_ACTION_TYPE_INVITATION ->
					return true
			}
		}
		return false
	}
	internal fun usesPostponedActions(): Boolean {
		for(event in eventTriggers) {
			if(event.delaySec != 0)
				return true
		}
		return schedules.isNotEmpty()
	}
	internal fun schedulesAreFaulty(): Boolean {
		for(schedule in schedules) {
			if(schedule.isFaulty())
				return true
		}
		return false
	}
	
	internal fun save(enabled: Boolean, db: SQLiteInterface = NativeLink.sql) {
		val values = db.getValueBox()
		this.enabled = enabled
		values.putBoolean(KEY_ENABLED, enabled)
		values.putString(KEY_ACTIONS, actionsString)
		values.putLong(KEY_STUDY_ID, studyId)
		values.putLong(KEY_QUESTIONNAIRE_ID, questionnaire.id)
		
		if(exists) {
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
			
			if(fromJsonOrUpdated) {
				//we dont check for difference and just overwrite existing eventTriggers:
				//Note: this could trigger an already pending event with different conditions with the new data
				//but that is very unlikely so we don't care
				val dbEventTriggers: List<EventTrigger> = loadEventTriggersDB()
				val dbEventTriggersLength = dbEventTriggers.size
				
				for(i in jsonEventTriggers.indices) {
					val jsonEventTrigger = jsonEventTriggers[i]
					if(i < dbEventTriggersLength) {
						val dbEventTrigger = dbEventTriggers[i]
						jsonEventTrigger.id = dbEventTrigger.id
						jsonEventTrigger.exists = true
					}
				}
				
				//remove not needed events:
				if(dbEventTriggersLength > jsonEventTriggers.size) {
					for(i in jsonEventTriggers.size .. dbEventTriggersLength) {
						dbEventTriggers[i].delete(db)
					}
				}
				_eventTriggers = jsonEventTriggers
				
				//Schedules:
				
				var isDifferent = false
				val dbSchedules = loadSchedulesDB()
				if(dbSchedules.size != jsonSchedules.size)
					isDifferent = true
				else {
					for(i in dbSchedules.indices) {
						if(dbSchedules[i].isDifferent(jsonSchedules[i])) {
							isDifferent = true
							break
						}
					}
				}
				
				if(isDifferent) {
					for(schedule in dbSchedules) {
						schedule.bindParent(this)
						schedule.empty(db)
					}
					db.delete(Schedule.TABLE, Schedule.KEY_ACTION_TRIGGER + " = ?", arrayOf(id.toString()))
					val study = DbLogic.getStudy(studyId)!!
					NativeLink.notifications.fireSchedulesChanged(study)
					
					_schedules = jsonSchedules
					for(schedule in schedules) {
						schedule.bindParent(this)
						schedule.saveAndScheduleIfExists(db)
					}
				}
			}
		}
		else {
			id = db.insert(TABLE, values)
			exists = true
			
			for(schedule in schedules) {
				schedule.bindParent(this)
				schedule.saveAndScheduleIfExists(db)
			}
		}
		
		for(eventTrigger in eventTriggers) { //has to be done after db.insert because we need an id
			eventTrigger.bindParent(questionnaire, this)
			eventTrigger.save(db)
		}
	}
	
	internal fun saveScheduleTimeFrames(rescheduleNow: Boolean = false): Boolean {
		val db = NativeLink.sql
		for(schedule in schedules) {
			schedule.saveTimeFrames(db, rescheduleNow)
		}
		return true
	}
	
	internal fun scheduleIfNeeded() {
		for(schedule in schedules) {
			schedule.scheduleIfNeeded()
		}
	}
	
	private fun getActionArray(): JsonArray {
		return DbLogic.getJsonConfig().decodeFromString(actionsString)
	}
	
	internal fun execActions(label: String, actionScheduledTimestamp: Long, fireNotifications: Boolean = true) {
		val now = NativeLink.getNowMillis()
		if(abs(now - actionScheduledTimestamp) > MAX_ACTIONTIME_DEVIATION)
			ErrorBox.warn(
				"ActionTrigger",
				"Action \"$label\" was ${(now - actionScheduledTimestamp) / 60000} min late ($actionScheduledTimestamp)!"
			)
		val actions = getActionArray()
		for((i, actionJson) in actions.withIndex()) {
			val action = Action(actionJson.jsonObject)
			
			when(action.type) {
				JSON_ACTION_TYPE_INVITATION ->
					issueInvitationNotification(
						label = label,
						actionScheduledTimestamp = actionScheduledTimestamp,
						action = action,
						index = i,
						reminderCount = action.reminderCount,
						isReminder = false,
						now = now,
						fireNotifications = fireNotifications
					)
				JSON_ACTION_TYPE_NOTIFICATION -> {
					if(fireNotifications) {
						NativeLink.notifications.fireStudyNotification(
							label,
							action.msg,
							questionnaire,
							actionScheduledTimestamp
						)
					}
				}
				JSON_ACTION_TYPE_MSG -> {
					DbLogic.getStudy(studyId)?.let { study ->
						Message.addMessage(study.id, action.msg, NativeLink.getNowMillis(), true)
						NativeLink.notifications.fireMessageNotification(study)
						DataSet.createActionSentDataSet(DataSet.TYPE_MSG, questionnaire, actionScheduledTimestamp)
					}
				}
				else ->
					ErrorBox.error("action", "Not implemented: " + action.type)
			}
		}
	}
	
	fun execAsPostponedNotifications(alarm: Alarm) { //for IOS where details of notifications have to be set beforehand
		val actions = getActionArray()
		if(actions.size == 0)
			return
		when(alarm.type) {
			Alarm.TYPES.SignalTime, Alarm.TYPES.EventTrigger -> {
				var msg = "No message"
				
				val timestamp = alarm.timestamp
				ErrorBox.log(
					"Postponed Notification",
					"Scheduling Postponed ${alarm.type} for alarm ${alarm.label} (id=${alarm.id}) starting in ${(timestamp - NativeLink.getNowMillis()) / 60000} min (${
						NativeLink.formatDateTime(timestamp)
					})"
				)
				
				for(actionJson in actions) {
					val action = Action(actionJson.jsonObject)
					msg = when(action.type) {
						JSON_ACTION_TYPE_INVITATION -> //reminder will be scheduled in iteratePostponedReminder() called by Alarm.scheduleReminder() which will be started by Alarm.save()
							action.msg
						
						JSON_ACTION_TYPE_NOTIFICATION, JSON_ACTION_TYPE_MSG ->
							action.msg
						else ->
							"No message"
					}
				}
				
				//fire the actual notification:
				NativeLink.notifications.firePostponed(alarm, msg)
			}
			Alarm.TYPES.Reminder -> {
				val index = alarm.onlySingleActionIndex
				val action = Action(actions[index].jsonObject)
				
				issuePostponedReminder(
					alarm,
					action.msg,
					alarm.reminderCount,
					alarm.onlySingleActionIndex,
					action.reminderDelay
				)
			}
		}
	}
	
	fun iteratePostponedReminder(alarm: Alarm) {
		val actions = getActionArray()
		for((index, actionJson) in actions.withIndex()) {
			val action = Action(actionJson.jsonObject)
			
			if(action.type == JSON_ACTION_TYPE_INVITATION) {
				//fire reminder as additional notifications:
				issuePostponedReminder(
					alarm,
					action.msg,
					action.reminderCount,
					index,
					action.reminderDelay
				)
			}
		}
	}
	
	private fun issuePostponedReminder(
		alarm: Alarm,
		msg: String,
		reminderCount: Int,
		index: Int,
		delayMinutes: Int
	) {
		val timestamp = alarm.timestamp
		val label = alarm.label
		
		if(reminderCount > 0) {
			ErrorBox.log("Postponed Reminder", "Scheduling Postponed Reminder")
			
			//this will eventually call execAsPostponedNotifications() again with a different alarm of type Reminder and reduced reminderCount
			val newAlarm = Scheduler.addReminder(
				questionnaire.id,
				id,
				label,
				index,
				delayMinutes,
				reminderCount - 1,
				timestamp,
				alarm.eventTriggerId,
				alarm.signalTimeId
			)
			
			
			NativeLink.notifications.firePostponed(newAlarm, msg)
		}
	}
	
	internal fun issueReminder(label: String, actionScheduledTimestamp: Long, index: Int, reminder_count: Int) { //for Android
		val actions = getActionArray()
		issueInvitationNotification(label, actionScheduledTimestamp, Action(actions[index].jsonObject), index, reminder_count, true)
	}
	
	private fun issueInvitationNotification(
		label: String,
		actionScheduledTimestamp: Long,
		action: Action,
		index: Int,
		reminderCount: Int,
		isReminder: Boolean,
		now: Long = NativeLink.getNowMillis(),
		fireNotifications: Boolean = true
	) { //for Android
		
		//we have to update now:
		//1.) To make sure that canBeFilledOut() is true when COMPLETE_REPEAT_TYPE_ONCE_PER_NOTIFICATION or TIME_CONSTRAINT_TYPE_AFTER_NOTIFICATION are used
		//2.) If we are late and dont issue a notification, we still have to update so participants can still fill out questionnaires when their notifications are not working properly
		//3.) DataSet.createNotificationSentDataSet() which is called in Notifications.fireQuestionnaireBing() uses lastNotificationUtc EDIT: not anymore
		questionnaire.updateLastNotification(actionScheduledTimestamp)
		
		//there is no point in issuing notifications when the questionnaire should not be reachable:
		if(!questionnaire.canBeFilledOut(now)) {
			ErrorBox.log(
				"Notification",
				"Questionnaire (${questionnaire.title}) is not active at ${NativeLink.formatDateTime(now)}. Skipping notification '$label'"
			)
			return
		}
		val timeout = action.timeoutMinutes
		val reminderDelay = action.reminderDelay
		
		if(timeout != 0 && now > actionScheduledTimestamp + timeout * 60 * 1000 + reminderCount * (reminderDelay * 60 * 1000)) {
			DbLogic.reportMissedInvitation(questionnaire, actionScheduledTimestamp)
		}
		else {
			if(fireNotifications) {
				NativeLink.notifications.fireQuestionnaireBing(
					label,
					action.msg,
					questionnaire,
					if(reminderCount > 0) 0 else timeout, //we only want a timeout on the last reminder
					if(isReminder) DataSet.TYPE_INVITATION_REMINDER else DataSet.TYPE_INVITATION,
					actionScheduledTimestamp
				)
				if(reminderCount > 0) {
					Scheduler.addReminder(
						questionnaire.id,
						id,
						label,
						index,
						reminderDelay,
						reminderCount - 1,
						now
					)
				}
			}
		}
	}
	
	companion object {
		class Action(obj: JsonObject) {
			val type = if(obj.contains(JSON_TYPE)) obj.getValue(JSON_TYPE).jsonPrimitive.int else JSON_ACTION_TYPE_INVITATION
			val reminderCount = if(obj.contains(JSON_REMINDER_COUNT)) obj.getValue(
				JSON_REMINDER_COUNT
			).jsonPrimitive.int else 0
			val msg =  if(obj.contains(JSON_MSG)) obj.getValue(JSON_MSG).jsonPrimitive.content else ""
			
			val timeoutMinutes = if(obj.contains(JSON_TIMEOUT_MINUTES)) obj.getValue(
				JSON_TIMEOUT_MINUTES
			).jsonPrimitive.int else 0
			val reminderDelay = if(obj.contains(JSON_REMINDER_DELAY_MINUTES)) obj.getValue(
				JSON_REMINDER_DELAY_MINUTES
			).jsonPrimitive.int else 5
		}
		const val MAX_ACTIONTIME_DEVIATION = 15 * 60 * 1000
		const val TABLE = "action_trigger"
		const val KEY_ID = "_id"
		const val KEY_ENABLED = "enabled"
		const val KEY_ACTIONS = "actions"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_QUESTIONNAIRE_ID = "group_id"
		private const val JSON_MSG = "msgText"
		private const val JSON_TYPE = "type"
		private const val JSON_TIMEOUT_MINUTES = "timeout"
		private const val JSON_REMINDER_COUNT = "reminder_count"
		private const val JSON_REMINDER_DELAY_MINUTES = "reminder_delay_minu"
		internal const val JSON_ACTION_TYPE_INVITATION = 1
		internal const val JSON_ACTION_TYPE_MSG = 2
		internal const val JSON_ACTION_TYPE_NOTIFICATION = 3
		val COLUMNS = arrayOf(
				KEY_ID,
				KEY_ENABLED,
				KEY_ACTIONS,
				KEY_STUDY_ID,
				KEY_QUESTIONNAIRE_ID
		)
	}
}