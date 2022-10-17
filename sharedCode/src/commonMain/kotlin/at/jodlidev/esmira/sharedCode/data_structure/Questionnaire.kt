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
class Questionnaire {
	@Transient var exists = false
	@Transient var fromJsonOrUpdated = true
	
	@Transient var id: Long = 0
	@Transient var studyId: Long = -1
	@Transient var studyWebId: Long = -2
	@Transient var enabled = true
	@Transient var lastNotification: Long = 0
	@Transient var lastCompleted: Long = 0
	
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
	var limitToGroup = 0
	var minDataSetsForReward = 0

	
	@SerialName("actionTriggers") private var jsonActionTriggers: List<ActionTrigger> = ArrayList()
	@Transient private lateinit var _actionTriggers: List<ActionTrigger>
	val actionTriggers: List<ActionTrigger> get() {
		if(!this::_actionTriggers.isInitialized) {
			_actionTriggers = if(fromJsonOrUpdated)
				jsonActionTriggers
			else
				loadActionTriggerDB()
		}
		return _actionTriggers
	}
	
	
	@SerialName("pages") @Serializable(with = JsonToStringSerializer::class) var pagesString = "[]"
	@Transient private lateinit var _pages: List<Page>
	val pages: List<Page> get() {
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


	internal constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		studyId = c.getLong(1)
		studyWebId = c.getLong(2)
		enabled = c.getBoolean(3)
		lastNotification = c.getLong(4)
		lastCompleted = c.getLong(5)
		
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
		limitToGroup = c.getInt(18)
		title = c.getString(19)
		internalId = c.getLong(20)
		pagesString = c.getString(21)
		sumScoresString = c.getString(22)
		publishedAndroid = c.getBoolean(23)
		publishedIOS = c.getBoolean(24)
		minDataSetsForReward = c.getInt(25)
		exists = true
		fromJsonOrUpdated = false
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
		values.putInt(KEY_LIMIT_TO_GROUP, limitToGroup)
		values.putString(KEY_TITLE, title)
		values.putLong(KEY_INTERNAL_ID, internalId)
		values.putString(KEY_PAGES, pagesString)
		values.putString(KEY_SUMSCORES, sumScoresString)
		values.putBoolean(KEY_PUBLISHED_ANDROID, publishedAndroid)
		values.putBoolean(KEY_PUBLISHED_IOS, publishedIOS)
		values.putInt(KEY_MIN_DATASETS_FOR_REWARD, minDataSetsForReward)
		
		if(exists) {
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
			
			if(fromJsonOrUpdated) {
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
				else { //enable update check for triggers (and copy id over):
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
	
	fun saveQuestionnaire(formStarted: Long) {
		val dataSet = DataSet(DataSet.TYPE_QUESTIONNAIRE, this)
		
		for(page in pages) {
			for(input in page.inputs) {
				input.fillIntoDataSet(dataSet)
			}
		}
		dataSet.saveQuestionnaire(this, formStarted)
		updateLastCompleted(true) //this needs to be after we store last_notification in dataset
		execMissingAlarms() //for iOS when the notification was ignored and the app was opened directly


		NativeLink.notifications.removeQuestionnaireBing(this)
		
		
		//re adding alarms in case filling out the questionnaire changed things:
		Scheduler.remove(this)
		scheduleIfNeeded()
		Scheduler.scheduleAhead()
		
		//remove all reminder for this questionnaire until the next Bing
		//Note: we have to be careful because on iOS all reminders are prescheduled.
		var nextAlarm = DbLogic.getNextAlarm(this)
		while(nextAlarm?.type == Alarm.TYPES.Reminder) {
			nextAlarm.delete()
			nextAlarm = DbLogic.getNextAlarm(this)
		}
	}
	fun checkQuestionnaire(pageI: Int): Int {
		val page = pages[pageI]
		for((i, input) in page.inputs.withIndex()) {
			if(input.needsValue())
				return i
		}
		return -1
	}
	
	fun updateLastNotification(timestamp: Long = NativeLink.getNowMillis()) { //we need this because we don't want to recreate all triggers again
		lastNotification = timestamp
		if(exists) {
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putLong(KEY_LAST_NOTIFICATION, lastNotification)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		}
	}
	
	fun updateLastCompleted(reset_last_notification: Boolean) { //we need this because we dont want to recreate all triggers again
		lastCompleted = NativeLink.getNowMillis()
		if(exists) {
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putLong(KEY_LAST_COMPLETED, lastCompleted)
			if(reset_last_notification) {
				lastNotification = 0
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
		for(alarm in DbLogic.getAlarmsFrom(this)) {
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
			alarm.exec(fireNotifications = NativeLink.smartphoneData.phoneType != PhoneType.IOS)
		}
	}
	
	private fun scheduleIfNeeded() {
		for(actionTrigger in actionTriggers) {
			actionTrigger.scheduleIfNeeded()
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
	fun hasScreenOrAppTracking(): Boolean {
		for(page in pages) {
			if(page.hasScreenOrAppTracking())
				return true
		}
		return false
	}
	fun hasEditableSchedules(): Boolean {
		for(trigger in actionTriggers) {
			for(schedule in trigger.schedules) {
				if(schedule.userEditable)
					return true
			}
		}
		return false
	}
	private fun hasQuestionnaire(): Boolean {
		return pages.isNotEmpty()
	}
	
	fun isActive(now: Long = NativeLink.getNowMillis()): Boolean { //if study is active in general
		val study: Study? = DbLogic.getStudy(studyId) //study can be null when we test for a study that we have not joined yet
		
		val durationCheck = study == null || (
			(durationPeriodDays == 0 || now <= study.joined + durationPeriodDays * (1000*60*60*24))
				&& (durationStartingAfterDays == 0 || now >= study.joined + durationStartingAfterDays * (1000*60*60*24))
			)
			
		
		return durationCheck
			&& (limitToGroup == 0 || study == null || limitToGroup == study.group)
			&& ((durationStart == 0L || now >= durationStart)
			&& (durationEnd == 0L || now <= durationEnd))
			&& (!completableOnce || lastCompleted == 0L)
	}
	
	fun willBeActiveIn(study: Study? = DbLogic.getStudy(studyId), now: Long = NativeLink.getNowMillis()): Long {
		val joined = study?.joined ?: 0
		val group = study?.group ?: 0
		
		if(limitToGroup != 0 && limitToGroup != group)
			return 0
		
		val durationValue = durationStart - now
		val startingAfterDaysValue = joined + durationStartingAfterDays.toLong() * (1000*60*60*24) - now
		
		return when {
			durationValue <= 0 ->
				startingAfterDaysValue.coerceAtLeast(0) // durationValue is negative, so we ignore it
			startingAfterDaysValue <= 0 ->
				durationValue.coerceAtLeast(0) // startingAfterDaysValue is negative, so we ignore it
			else ->
				durationValue.coerceAtMost(startingAfterDaysValue)
		}.coerceAtLeast(0)
	}
	
	fun canBeFilledOut(now: Long = NativeLink.getNowMillis()): Boolean { //if there are any questionnaires at the current time
		val fromMidnight = now - NativeLink.getMidnightMillis(now)
		
		val lastNotification = (DbLogic.getLastAlarmBefore(now, id)?.timestamp ?: 0).coerceAtLeast(lastNotification)
		val oncePerNotification = (!completableOncePerNotification ||
				(lastNotification != 0L && lastNotification >= lastCompleted &&
						(completableMinutesAfterNotification == 0 || now - lastNotification <= completableMinutesAfterNotification * 60 * 1000)
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
		val completionFrequency = (!limitCompletionFrequency || (now >= lastCompleted + completionFrequencyMinutes * 60 * 1000))

		return hasQuestionnaire() && isActive() &&
				oncePerNotification &&
				completionFrequency &&
				specificTime &&
				(NativeLink.smartphoneData.phoneType != PhoneType.IOS || publishedIOS) &&
				(NativeLink.smartphoneData.phoneType != PhoneType.Android || publishedAndroid)
	}
	fun questionnairePageHasRequired(index: Int): Boolean {
		if(index >= pages.size)
			return false
		for(input in pages[index].inputs) {
			if(input.required)
				return true
		}
		return false
	}
	
	companion object {
		const val TABLE = "questionnaires"
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
		const val KEY_LIMIT_TO_GROUP = "limitToGroup"

		const val KEY_TITLE = "title"
		const val KEY_INTERNAL_ID = "internal_id"
		const val KEY_PAGES = "pages"
		const val KEY_SUMSCORES = "sumScores"
		const val KEY_PUBLISHED_ANDROID = "publishedAndroid"
		const val KEY_PUBLISHED_IOS = "publishedIOS"
		const val KEY_MIN_DATASETS_FOR_REWARD = "minDataSetsForReward"
		
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
			KEY_LIMIT_TO_GROUP,
			KEY_TITLE,
			KEY_INTERNAL_ID,
			KEY_PAGES,
			KEY_SUMSCORES,
			KEY_PUBLISHED_ANDROID,
			KEY_PUBLISHED_IOS,
			KEY_MIN_DATASETS_FOR_REWARD
		)
	}
}