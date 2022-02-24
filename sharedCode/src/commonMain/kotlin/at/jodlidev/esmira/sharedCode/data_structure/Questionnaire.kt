package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 04.05.2020.
 */
@Serializable
class Questionnaire internal constructor() {
	@Transient var exists = false
	@Transient var fromJson = true
	
	@Transient var id: Long = 0
	@Transient var studyId: Long = -1
	@Transient var studyWebId: Long = -2
	@Transient var enabled = true
	@Transient var lastNotificationUtc: Long = 0
	@Transient var lastCompletedUtc: Long = 0
	
	var title: String = "Error"
	var internalId: Long = -1
	private var durationPeriodDays: Int = 0
	private var durationStartingAfterDays: Int = 0
	private var durationStart: Long = 0
	private var durationEnd: Long = 0
	var publishedAndroid = true
	var publishedIOS = true

	var completableOnce = false
	var completableOncePerNotification = false
	var completableMinutesAfterNotification = 0
	var limitCompletionFrequency = false
	var completionFrequencyMinutes = 60
	var completableAtSpecificTime = false
	var completableAtSpecificTimeStart = -1
	var completableAtSpecificTimeEnd = -1

	
	@SerialName("actionTriggers") private var jsonActionTriggers: List<ActionTrigger> = ArrayList()
	@Transient private lateinit var _actionTriggers: List<ActionTrigger>
	val actionTriggers: List<ActionTrigger> get() {
		if(!this::_actionTriggers.isInitialized) {
			_actionTriggers = if(fromJson)
				jsonActionTriggers
			else
				loadActionTriggerDB()
		}
		return _actionTriggers
	}
	
	
	@SerialName("pages") @Serializable(with = JsonToStringSerializer::class) var pagesString = "[]"
	@Transient private lateinit var _pages: List<Page>
	@Suppress("unused") val pages: List<Page> get() {
		if(!this::_pages.isInitialized) {
			_pages = try {
				DbLogic.getJsonConfig().decodeFromString(pagesString)
			}
			catch(e: Throwable) {
				ErrorBox.warn("Questionnaire", "Questionnaire $title is faulty\n$pagesString", e)
				ArrayList()
			}
		}
		return _pages
	}
	
	@SerialName("sumScores") @Serializable(with = JsonToStringSerializer::class) var sumScoresString: String = "[]"
	@Transient private lateinit var _sumScores: List<SumScore>
	val sumScores: List<SumScore> get() {
		if(!this::_sumScores.isInitialized) {
			_sumScores = try {
				DbLogic.getJsonConfig().decodeFromString(sumScoresString)
			}
			catch(e: Throwable) {
				ErrorBox.warn("Questionnaire", "Questionnaire $title is faulty\n$sumScoresString", e)
				ArrayList()
			}
		}
		return _sumScores
	}


	internal constructor(c: SQLiteCursor): this() {
		id = c.getLong(0)
		studyId = c.getLong(1)
		studyWebId = c.getLong(2)
		enabled = c.getBoolean(3)
		lastNotificationUtc = c.getLong(4)
		lastCompletedUtc = c.getLong(5)
		
		durationPeriodDays = c.getInt(6)
		durationStartingAfterDays = c.getInt(7)
		durationStart = c.getLong(8)
		durationEnd = c.getLong(9)
		completableOnce = c.getBoolean(10)
		completableOncePerNotification = c.getBoolean(11)
		completableMinutesAfterNotification = c.getInt(12)
		limitCompletionFrequency = c.getBoolean(13)
		completionFrequencyMinutes = c.getInt(14)
		completableAtSpecificTime = c.getBoolean(15)
		completableAtSpecificTimeStart = c.getInt(16)
		completableAtSpecificTimeEnd = c.getInt(17)
		title = c.getString(18)
		internalId = c.getLong(19)
		pagesString = c.getString(20)
		sumScoresString = c.getString(21)
		publishedAndroid = c.getBoolean(22)
		publishedIOS = c.getBoolean(23)
		exists = true
		fromJson = false
	}
	
	fun bindParent(study: Study) {
		this.studyId = study.id
		this.studyWebId = study.webId
	}
	
	
	private fun loadActionTriggerDB(): List<ActionTrigger> {
		val db = NativeLink.sql
		val c = db.select(
			ActionTrigger.TABLE,
			ActionTrigger.COLUMNS,
			ActionTrigger.KEY_QUESTIONNAIRE_ID + " = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			null
		)
		val list = ArrayList<ActionTrigger>()
		while(c.moveToNext()) {
			list.add(ActionTrigger(this, c))
		}
		c.close()
		return list
	}
	
	@Suppress("unused")
	fun getQuestionnaireTitle(pageIndex: Int): String {
		return if(pages.size > 1)
			"$title ${(pageIndex + 1)}/${pages.size}"
		else
			title
	}
	
	fun save(enabled: Boolean, db: SQLiteInterface = NativeLink.sql) {
		this.enabled = enabled

		val values = db.getValueBox()
		values.putLong(KEY_STUDY_ID, studyId)
		values.putLong(KEY_STUDY_WEB_ID, studyWebId)
		values.putBoolean(KEY_ENABLED, this.enabled)
		values.putInt(KEY_DURATION_PERIOD_DAYS, durationPeriodDays)
		values.putInt(KEY_DURATION_STARTING_AFTER_DAYS, durationStartingAfterDays)
		values.putLong(KEY_DURATION_START, durationStart)
		values.putLong(KEY_DURATION_END, durationEnd)
		values.putBoolean(KEY_COMPLETABLE_ONCE, completableOnce)
		values.putBoolean(KEY_COMPLETABLE_ONCE_PER_NOTIFICATION, completableOncePerNotification)
		values.putInt(KEY_COMPLETABLE_MINUTES_AFTER_NOTIFICATION, completableMinutesAfterNotification)
		values.putBoolean(KEY_LIMIT_COMPLETION_FREQUENCY, limitCompletionFrequency)
		values.putInt(KEY_COMPLETION_FREQUENCY_MINUTES, completionFrequencyMinutes)
		values.putBoolean(KEY_COMPLETABLE_AT_SPECIFIC_TIME, completableAtSpecificTime)
		values.putInt(KEY_COMPLETABLE_AT_SPECIFIC_TIME_START, completableAtSpecificTimeStart)
		values.putInt(KEY_COMPLETABLE_AT_SPECIFIC_TIME_END, completableAtSpecificTimeEnd)
		values.putString(KEY_NAME, title)
		values.putLong(KEY_INTERNAL_ID, internalId)
		values.putString(KEY_PAGES, pagesString)
		values.putString(KEY_SUMSCORES, sumScoresString)
		values.putBoolean(KEY_PUBLISHEDANDROID, publishedAndroid)
		values.putBoolean(KEY_PUBLISHEDIOS, publishedIOS)
		
		if(exists) {
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
			
			if(fromJson) {
				val dbActionTrigger = loadActionTriggerDB()
				if(dbActionTrigger.size != jsonActionTriggers.size) {
					for(trigger in actionTriggers) { //empty will iterate through its schedules - so we need to finish initializing them before
						trigger.bindParent(studyId, this)
						for(schedule in trigger.schedules) {
							schedule.bindParent(trigger)
						}
					}
					empty(db)
					NativeLink.notifications.fireSchedulesChanged(DbLogic.getStudy(studyId)!!)
				}
				else { //make sure that new triggers will be saved with same internal id:
					for(i in jsonActionTriggers.indices) {
						val newTrigger = jsonActionTriggers[i]
						newTrigger.id = dbActionTrigger[i].id
						newTrigger.exists = true
					}
					_actionTriggers = jsonActionTriggers
				}
			}
		}
		else {
			id = db.insert(TABLE, values)
			exists = true
		}
		for(trigger in actionTriggers) {
			trigger.bindParent(studyId, this)
			trigger.save(this.enabled, db)
		}
	}
	
	@Suppress("unused")
	fun saveQuestionnaire(formStarted: Long) {
		val dataSet = DataSet(DataSet.TYPE_QUESTIONNAIRE, this)
		
		for(page in pages) {
			for(input in page.inputs) {
				dataSet.addResponseData(input.name, input.value)
				val additionalName = "${input.name}~"
				for(additionalValue in input.additionalValues) {
					dataSet.addResponseData(additionalName + additionalValue.key, additionalValue.value)
				}
			}
		}
		dataSet.saveQuestionnaire(this, formStarted)
		updateLastCompleted(true) //this needs to be after we store last_notification in dataset
		execMissingAlarms() //for iOS when the notification was ignored and the app was opened directly


		NativeLink.notifications.removeQuestionnaireBing(this)

		var nextAlarm = DbLogic.getNextAlarm(this)

		//remove all reminder for this questionnaire until the next Bing
		//Note: we have to be careful because on iOS all reminders are prescheduled.
		while(nextAlarm?.type == Alarm.TYPES.Reminder) {
			nextAlarm.delete()
			nextAlarm = DbLogic.getNextAlarm(this)
		}

	}
	@Suppress("unused")
	fun checkQuestionnaire(pageI: Int): Int {
		val page = pages[pageI]
		for((i, input) in page.inputs.withIndex()) {
			if(input.needsValue())
				return i
		}
		return -1
	}
	
	fun updateLastNotification(timestamp: Long = NativeLink.getNowMillis()) { //we need this because we don't want to recreate all triggers again
		lastNotificationUtc = timestamp
		if(exists) {
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putLong(KEY_LAST_NOTIFICATION, lastNotificationUtc)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		}
	}
	
	@Suppress("unused") fun updateLastCompleted(reset_last_notification: Boolean) { //we need this because we dont want to recreate all triggers again
		lastCompletedUtc = NativeLink.getNowMillis()
		if(exists) {
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putLong(KEY_LAST_COMPLETED, lastCompletedUtc)
			if(reset_last_notification) {
				lastNotificationUtc = 0
				values.putInt(KEY_LAST_NOTIFICATION, 0)
			}
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		}
	}
	
	fun delete() {
		val db = NativeLink.sql
		empty()
		db.delete(TABLE, "$KEY_ID = ?", arrayOf(id.toString()))
		exists = false

		//remove notifications:
		NativeLink.notifications.removeQuestionnaireBing(this)
		for(alarm in DbLogic.getAlarmsFrom(id)) {
			alarm.delete()
		}
	}
	
	private fun empty(db: SQLiteInterface = NativeLink.sql) {
		Scheduler.remove(this)
		db.delete(ActionTrigger.TABLE, "${ActionTrigger.KEY_QUESTIONNAIRE_ID} = ?", arrayOf(id.toString()))
		db.delete(EventTrigger.TABLE, "${EventTrigger.KEY_QUESTIONNAIRE_ID} = ?", arrayOf(id.toString()))
		db.delete(Schedule.TABLE, "${Schedule.KEY_QUESTIONNAIRE_ID} = ?", arrayOf(id.toString()))
		db.delete(SignalTime.TABLE, "${SignalTime.KEY_QUESTIONNAIRE_ID} = ?", arrayOf(id.toString()))
		db.delete(DynamicInputData.TABLE, "${DynamicInputData.KEY_QUESTIONNAIRE_ID} = ?", arrayOf(id.toString()))
	}
	
	private fun execMissingAlarms() { //for iOS
		val now = NativeLink.getNowMillis()
		val alarms = DbLogic.getAlarmsBefore(now, id)
		
		for(alarm in alarms) {
			ErrorBox.log("Questionnaire", "Alarm \"${alarm.label}\" (${alarm.id}) for questionnaire \"$title\" was not executed. Executing now")
			alarm.exec(fireNotifications = !isIOS())
		}
	}
	
	fun hasSchedules(): Boolean {
		for(trigger in actionTriggers) {
			if(trigger.hasSchedules())
				return true
		}
		return false
	}
	fun hasEvents(): Boolean {
		for(trigger in actionTriggers) {
			if(trigger.hasEvents())
				return true
		}
		return false
	}
	fun hasDelayedEvents(): Boolean {
		for(trigger in actionTriggers) {
			if(trigger.hasDelayedEvents())
				return true
		}
		return false
	}
	fun hasNotifications(): Boolean {
		for(trigger in actionTriggers) {
			if(trigger.hasNotifications())
				return true
		}
		return false
	}
	fun usesPostponedActions(): Boolean {
		for(trigger in actionTriggers) {
			if(trigger.usesPostponedActions())
				return true
		}
		return false
	}
	fun hasScreenTracking(): Boolean {
		for(page in pages) {
			if(page.hasScreenTracking())
				return true
		}
		return false
	}
	fun hasEditableSchedules(): Boolean {
		val c = NativeLink.sql.select(
			Schedule.TABLE,
			arrayOf(Schedule.KEY_ID),
			"${Schedule.KEY_QUESTIONNAIRE_ID} = ? AND ${Schedule.KEY_EDITABLE} = 1", arrayOf(id.toString()),
			null,
			null,
			null,
			"1"
		)
		val r = c.moveToFirst()
		c.close()
		return r
	}
	private fun hasQuestionnaire(): Boolean {
		return pages.isNotEmpty()
	}
	
	fun isActive(): Boolean { //if study is active in general
		val now = NativeLink.getNowMillis()
		val durationCheck = if(durationPeriodDays != 0 || durationStartingAfterDays != 0) {
			val study: Study? = DbLogic.getStudy(studyId)
			if(study == null) //happens when we test for a study that we have not joined yet
				true
			else {
				val joined = study.joined*1000
				(durationPeriodDays == 0 || now <= joined + durationPeriodDays * (1000*60*60*24))
					&& (durationStartingAfterDays == 0 || now >= joined + durationStartingAfterDays * (1000*60*60*24))
			}
		}
		else true

		return (durationCheck
				&& ((durationStart == 0L || now >= durationStart)
				&& (durationEnd == 0L || now <= durationEnd))
				&& (!completableOnce || lastCompletedUtc == 0L))
//		return (durationCheck
//				&& ((durationStart == 0L || now >= durationStart)
//				&& (durationEnd == 0L || now <= durationEnd))
//				&& (completeRepeatType != COMPLETE_REPEAT_TYPE_NO_REPEAT || lastCompletedUtc == 0L))
	}
	
	fun willBeActiveIn(): Long {
		val joined = DbLogic.getStudy(studyId)?.joined ?: 0
		val now = NativeLink.getNowMillis()
		
		return (durationStart - now).coerceAtLeast(joined*1000 + durationStartingAfterDays.toLong() * (1000*60*60*24) - now).coerceAtLeast(0)
	}
	
	fun canBeFilledOut(now: Long = NativeLink.getNowMillis()): Boolean { //if there are any questionnaires at the current time
		val fromMidnight = now - NativeLink.getMidnightMillis()
//		val completedCheck = when(completeRepeatType) {
//			COMPLETE_REPEAT_TYPE_NO_REPEAT ->
//				lastCompletedUtc == 0L
//			COMPLETE_REPEAT_TYPE_ONCE_PER_NOTIFICATION ->
//				lastNotificationUtc >= lastCompletedUtc
//			COMPLETE_REPEAT_TYPE_MINUTES ->
//				now >= lastCompletedUtc + completeRepeatMinutes * 60 * 1000
//			COMPLETE_REPEAT_TYPE_ALWAYS ->
//				true
//			else -> {
//				ErrorBox.warn("Questionnaire", "Unsupported complete_repeat_type ($completeRepeatType) in Questionnaire: $name")
//				true
//			}
//		}
//		val timeConstraintCheck = when(timeConstraintType) {
//			TIME_CONSTRAINT_TYPE_TIMEPERIOD ->
//				if(timeConstraintStart != -1 && timeConstraintEnd != -1) {
//					if(timeConstraintStart > timeConstraintEnd)
//						fromMidnight >= timeConstraintStart || fromMidnight <= timeConstraintEnd
//					else
//						fromMidnight in timeConstraintStart .. timeConstraintEnd
//				}
//				else
//					if(timeConstraintStart != -1)
//						fromMidnight >= timeConstraintStart
//					else if(timeConstraintEnd != -1)
//						fromMidnight <= timeConstraintEnd
//					else
//						true
//			TIME_CONSTRAINT_TYPE_AFTER_NOTIFICATION ->
//				timeConstraintPeriodMinutes == 0 && lastNotificationUtc != 0L || now - lastNotificationUtc <= timeConstraintPeriodMinutes * 60 * 1000
//			TIME_CONSTRAINT_TYPE_NONE ->
//				true
//			else -> {
//				ErrorBox.warn("Questionnaire", "Unsupported timeConstraint_type ($timeConstraintType) in Questionnaire: $name")
//				true
//			}
//		}
//		return hasQuestionnaire() && isActive() && completedCheck && timeConstraintCheck && (!isIOS() || publishedIOS) && (!isAndroid() || publishedAndroid)
		
		
		val oncePerNotification = (!completableOncePerNotification ||
				(lastNotificationUtc != 0L && lastNotificationUtc >= lastCompletedUtc &&
						(completableMinutesAfterNotification == 0 || now - lastNotificationUtc <= completableMinutesAfterNotification * 60 * 1000)
						)
				)
		val specificTime = !completableAtSpecificTime ||
				if(completableAtSpecificTimeStart != -1 && completableAtSpecificTimeEnd != -1) {
					if(completableAtSpecificTimeStart > completableAtSpecificTimeEnd)
						fromMidnight >= completableAtSpecificTimeStart || fromMidnight <= completableAtSpecificTimeEnd
					else
						fromMidnight in completableAtSpecificTimeStart .. completableAtSpecificTimeEnd
				}
				else
					if(completableAtSpecificTimeStart != -1)
						fromMidnight >= completableAtSpecificTimeStart
					else if(completableAtSpecificTimeEnd != -1)
						fromMidnight <= completableAtSpecificTimeEnd
					else
						true
		val completionFrequency = (!limitCompletionFrequency || (now >= lastCompletedUtc + completionFrequencyMinutes * 60 * 1000))

		return hasQuestionnaire() && isActive() &&
				oncePerNotification &&
				completionFrequency &&
				specificTime &&
				(!isIOS() || publishedIOS) &&
				(!isAndroid() || publishedAndroid)
	}
	@Suppress("unused") fun questionnairePageHasRequired(index: Int): Boolean {
		if(index >= pages.size)
			return false
		for(input in pages[index].inputs) {
			if(input.required)
				return true
		}
		return false
	}
	
	companion object {
		const val TABLE = "groups"
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_STUDY_WEB_ID = "study_webid"
		const val KEY_ENABLED = "enabled"
		const val KEY_LAST_NOTIFICATION = "last_notification"
		const val KEY_LAST_COMPLETED = "last_completed"
		const val KEY_COMPLETE_REPEAT_TYPE = "complete_repeat_type"
		const val KEY_COMPLETE_REPEAT_MINUTES = "complete_repeat_minutes"
		const val KEY_DURATION_PERIOD_DAYS = "period"
		const val KEY_DURATION_STARTING_AFTER_DAYS = "durationStartingAfterDays"
		const val KEY_DURATION_START = "duration_start"
		const val KEY_DURATION_END = "duration_end"
		const val KEY_TIME_CONSTRAINT_TYPE = "timeConstraint_type"
		const val KEY_TIME_CONSTRAINT_START = "timeConstraint_start"
		const val KEY_TIME_CONSTRAINT_END = "timeConstraint_end"
		const val KEY_TIME_CONSTRAINT_PERIOD = "timeConstraint_period"

		const val KEY_COMPLETABLE_ONCE = "completableOnce"
		const val KEY_COMPLETABLE_ONCE_PER_NOTIFICATION = "completableOncePerNotification"
		const val KEY_COMPLETABLE_MINUTES_AFTER_NOTIFICATION = "completableMinutesAfterNotification"
		const val KEY_LIMIT_COMPLETION_FREQUENCY = "limitCompletionFrequency"
		const val KEY_COMPLETION_FREQUENCY_MINUTES = "completionFrequencyMinutes"
		const val KEY_COMPLETABLE_AT_SPECIFIC_TIME = "completableAtSpecificTime"
		const val KEY_COMPLETABLE_AT_SPECIFIC_TIME_START = "completableAtSpecificTimeStart"
		const val KEY_COMPLETABLE_AT_SPECIFIC_TIME_END = "completableAtSpecificTimeEnd"

		const val KEY_NAME = "title"
		const val KEY_INTERNAL_ID = "internal_id"
		const val KEY_PAGES = "pages"
		const val KEY_SUMSCORES = "sumScores"
		const val KEY_PUBLISHEDANDROID = "publishedAndroid"
		const val KEY_PUBLISHEDIOS = "publishedIOS"
		
		private const val TIME_CONSTRAINT_TYPE_NONE = 0
		private const val TIME_CONSTRAINT_TYPE_TIMEPERIOD = 1
		const val TIME_CONSTRAINT_TYPE_AFTER_NOTIFICATION = 2
		
		private const val COMPLETE_REPEAT_TYPE_ALWAYS = 0
		private const val COMPLETE_REPEAT_TYPE_NO_REPEAT = 1
		const val COMPLETE_REPEAT_TYPE_ONCE_PER_NOTIFICATION = 2
		private const val COMPLETE_REPEAT_TYPE_MINUTES = 3
		
		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_STUDY_ID,
			KEY_STUDY_WEB_ID,
			KEY_ENABLED,
			KEY_LAST_NOTIFICATION,
			KEY_LAST_COMPLETED,
			KEY_DURATION_PERIOD_DAYS,
			KEY_DURATION_STARTING_AFTER_DAYS,
			KEY_DURATION_START,
			KEY_DURATION_END,
			KEY_COMPLETABLE_ONCE,
			KEY_COMPLETABLE_ONCE_PER_NOTIFICATION,
			KEY_COMPLETABLE_MINUTES_AFTER_NOTIFICATION,
			KEY_LIMIT_COMPLETION_FREQUENCY,
			KEY_COMPLETION_FREQUENCY_MINUTES,
			KEY_COMPLETABLE_AT_SPECIFIC_TIME,
			KEY_COMPLETABLE_AT_SPECIFIC_TIME_START,
			KEY_COMPLETABLE_AT_SPECIFIC_TIME_END,
			KEY_NAME,
			KEY_INTERNAL_ID,
			KEY_PAGES,
			KEY_SUMSCORES,
			KEY_PUBLISHEDANDROID,
			KEY_PUBLISHEDIOS
		)
//		val COLUMNS = arrayOf(
//			KEY_ID,
//			KEY_STUDY_ID,
//			KEY_STUDY_WEB_ID,
//			KEY_ENABLED,
//			KEY_LAST_NOTIFICATION,
//			KEY_LAST_COMPLETED,
//			KEY_COMPLETE_REPEAT_TYPE,
//			KEY_COMPLETE_REPEAT_MINUTES,
//			KEY_DURATION_PERIOD_DAYS,
//			KEY_DURATION_STARTING_AFTER_DAYS,
//			KEY_DURATION_START,
//			KEY_DURATION_END,
//			KEY_TIME_CONSTRAINT_TYPE,
//			KEY_TIME_CONSTRAINT_START,
//			KEY_TIME_CONSTRAINT_END,
//			KEY_TIME_CONSTRAINT_PERIOD,
//			KEY_NAME,
//			KEY_INTERNAL_ID,
//			KEY_PAGES,
//			KEY_SUMSCORES,
//			KEY_PUBLISHEDANDROID,
//			KEY_PUBLISHEDIOS
//		)
	}
}