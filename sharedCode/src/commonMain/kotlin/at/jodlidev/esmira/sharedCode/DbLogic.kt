package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.*
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_timed
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perValue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Created by JodliDev on 05.05.2020.
 */

object DbLogic {
	const val DATABASE_NAME = "data"
	const val DATABASE_VERSION = Updater.DATABASE_VERSION
	const val ADMIN_PASSWORD: String = "Jaynestown"
	
	
	fun getJsonConfig(): Json { //TODO: Where does this function fit best..?
		return Json {ignoreUnknownKeys = true}
	}
	inline fun <reified T>createJsonObj(json: String = "{}"): T { //used for testing
		return DbLogic.getJsonConfig().decodeFromString(json)
	}
	
	fun getVersion(): String {
		return "${Updater.LIBRARY_VERSION}-${Updater.DATABASE_VERSION}-${Updater.EXPECTED_SERVER_VERSION}"
	}
	
	fun createTables(db: SQLiteInterface) {
		println("Creating tables")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${DbUser.TABLE} (
			${DbUser.KEY_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
			${DbUser.KEY_NOTIFICATIONS_SETUP} INTEGER,
			${DbUser.KEY_IS_DEV} INTEGER,
			${DbUser.KEY_WAS_DEV} INTEGER,
			${DbUser.KEY_NOTIFICATIONS_MISSED} INTEGER DEFAULT 0,
			${DbUser.KEY_APP_LANG} TEXT,
			${DbUser.KEY_CURRENT_STUDY} INTEGER DEFAULT 0,
			${DbUser.KEY_UID} TEXT)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${StudyToken.TABLE} (
			${StudyToken.KEY_STUDY_ID} INTEGER PRIMARY KEY,
			${StudyToken.KEY_TOKEN} INTEGER)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${Study.TABLE} (
			${Study.KEY_ID} INTEGER PRIMARY KEY,
			${Study.KEY_WEB_ID} INTEGER,
			${Study.KEY_SERVER_URL} TEXT,
			${Study.KEY_ACCESS_KEY} TEXT,
			${Study.KEY_VERSION} INTEGER,
			${Study.KEY_SUB_VERSION} INTEGER,
			${Study.KEY_LANG} TEXT DEFAULT '',
			${Study.KEY_JOINED_TIMESTAMP} INTEGER DEFAULT 0,
			${Study.KEY_QUIT_TIMESTAMP} INTEGER DEFAULT 0,
			${Study.KEY_GROUP} INTEGER DEFAULT 0,
			${Study.KEY_STATE} INTEGER DEFAULT ${Study.STATES.Pending.ordinal},
			${Study.KEY_LAST_MSG_TIMESTAMP} INTEGER DEFAULT 0,
			${Study.KEY_TITLE} TEXT,
			${Study.KEY_DESC} TEXT,
			${Study.KEY_EMAIL} TEXT,
			${Study.KEY_CONSENT} TEXT,
			${Study.KEY_LOAD_PUBLIC_STATISTICS} INTEGER,
			${Study.KEY_SEND_MESSAGES_ALLOWED} INTEGER,
			${Study.KEY_UPLOAD_SETTINGS} TEXT DEFAULT '{}',
			${Study.KEY_PUBLIC_CHARTS_JSON} TEXT,
			${Study.KEY_PERSONAL_CHARTS_JSON} TEXT,
			${Study.KEY_ENABLE_REWARD_SYSTEM} INTEGER DEFAULT 0,
			${Study.KEY_REWARD_VISIBLE_AFTER} INTEGER,
			${Study.KEY_REWARD_EMAIL_CONTENT} TEXT,
			${Study.KEY_REWARD_INSTRUCTIONS} TEXT,
			${Study.KEY_CACHED_REWARD_CODE} TEXT DEFAULT '',
			${Study.KEY_INSTRUCTIONS} TEXT)""")
			
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${Message.TABLE} (
			${Message.KEY_ID} INTEGER PRIMARY KEY,
			${Message.KEY_STUDY_ID} INTEGER,
			${Message.KEY_SENT} INTEGER,
			${Message.KEY_CONTENT} TEXT,
			${Message.KEY_IS_NEW} INTEGER,
			${Message.KEY_FROM_SERVER} INTEGER,
			FOREIGN KEY(${Message.KEY_STUDY_ID}) REFERENCES ${Study.TABLE} (${Study.KEY_ID}))""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${ObservedVariable.TABLE} (
			${ObservedVariable.KEY_ID} INTEGER PRIMARY KEY,
			${ObservedVariable.KEY_STUDY_ID} INTEGER,
			${ObservedVariable.KEY_INDEX} INTEGER,
			${ObservedVariable.KEY_VARIABLE_NAME} TEXT,
			${ObservedVariable.KEY_CONDITIONS_JSON} TEXT,
			${ObservedVariable.KEY_CONDITION_TYPE} INTEGER,
			${ObservedVariable.KEY_STORAGE_TYPE} INTEGER,
			${ObservedVariable.KEY_TIME_INTERVAL} INTEGER DEFAULT ${StatisticData_timed.ONE_DAY},
			FOREIGN KEY(${ObservedVariable.KEY_STUDY_ID}) REFERENCES ${Study.TABLE} (${Study.KEY_ID}))""")
			
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${StatisticData_timed.TABLE} (
			${StatisticData_timed.KEY_ID} INTEGER PRIMARY KEY,
			${StatisticData_timed.KEY_STUDY_ID} INTEGER,
			${StatisticData_timed.KEY_TIMESTAMP} INTEGER,
			${StatisticData_timed.KEY_OBSERVED_ID} INTEGER,
			${StatisticData_timed.KEY_SUM} REAL,
			${StatisticData_timed.KEY_COUNT} INTEGER,
			FOREIGN KEY(${StatisticData_timed.KEY_STUDY_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${StatisticData_timed.KEY_OBSERVED_ID}) REFERENCES ${ObservedVariable.TABLE}(${ObservedVariable.KEY_ID}) ON DELETE CASCADE)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${StatisticData_perValue.TABLE} (
			${StatisticData_perValue.KEY_ID} INTEGER PRIMARY KEY,
			${StatisticData_perValue.KEY_STUDY_ID} INTEGER,
			${StatisticData_perValue.KEY_OBSERVED_ID} INTEGER,
			${StatisticData_perValue.KEY_VALUE} TEXT,
			${StatisticData_perValue.KEY_COUNT} INTEGER,
			FOREIGN KEY(${StatisticData_perValue.KEY_STUDY_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${StatisticData_perValue.KEY_OBSERVED_ID}) REFERENCES ${ObservedVariable.TABLE}(${ObservedVariable.KEY_ID}) ON DELETE CASCADE)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${Questionnaire.TABLE} (
			${Questionnaire.KEY_ID} INTEGER PRIMARY KEY,
			${Questionnaire.KEY_TITLE} TEXT,
			${Questionnaire.KEY_INTERNAL_ID} INTEGER,
			${Questionnaire.KEY_STUDY_ID} INTEGER,
			${Questionnaire.KEY_STUDY_WEB_ID} INTEGER,
			${Questionnaire.KEY_ENABLED} INTEGER,
			${Questionnaire.KEY_LAST_NOTIFICATION} INTEGER DEFAULT 0,
			${Questionnaire.KEY_LAST_COMPLETED} INTEGER DEFAULT 0,
			${Questionnaire.KEY_COMPLETE_REPEAT_TYPE} INTEGER,
			${Questionnaire.KEY_COMPLETE_REPEAT_MINUTES} INTEGER,
			${Questionnaire.KEY_DURATION_PERIOD_DAYS} INTEGER,
			${Questionnaire.KEY_DURATION_STARTING_AFTER_DAYS} INTEGER,
			${Questionnaire.KEY_DURATION_START} INTEGER,
			${Questionnaire.KEY_DURATION_END} INTEGER,
			${Questionnaire.KEY_TIME_CONSTRAINT_TYPE} INTEGER,
			${Questionnaire.KEY_TIME_CONSTRAINT_START} INTEGER,
			${Questionnaire.KEY_TIME_CONSTRAINT_END} INTEGER,
			${Questionnaire.KEY_TIME_CONSTRAINT_PERIOD} INTEGER,
			${Questionnaire.KEY_COMPLETABLE_ONCE} INTEGER,
			${Questionnaire.KEY_COMPLETABLE_ONCE_PER_NOTIFICATION} INTEGER,
			${Questionnaire.KEY_COMPLETABLE_MINUTES_AFTER_NOTIFICATION} INTEGER,
			${Questionnaire.KEY_LIMIT_COMPLETION_FREQUENCY} INTEGER,
			${Questionnaire.KEY_COMPLETION_FREQUENCY_MINUTES} INTEGER,
			${Questionnaire.KEY_COMPLETABLE_AT_SPECIFIC_TIME} INTEGER,
			${Questionnaire.KEY_COMPLETABLE_AT_SPECIFIC_TIME_START} INTEGER,
			${Questionnaire.KEY_COMPLETABLE_AT_SPECIFIC_TIME_END} INTEGER,
			${Questionnaire.KEY_LIMIT_TO_GROUP} INTEGER,
			${Questionnaire.KEY_MIN_DATASETS_FOR_REWARD} INTEGER,
			${Questionnaire.KEY_PAGES} TEXT,
			${Questionnaire.KEY_SUMSCORES} TEXT,
			${Questionnaire.KEY_PUBLISHED_ANDROID} INTEGER DEFAULT 1,
			${Questionnaire.KEY_PUBLISHED_IOS} INTEGER DEFAULT 1,
			FOREIGN KEY(${Questionnaire.KEY_STUDY_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}) ON DELETE CASCADE)""")
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${QuestionnaireCache.TABLE} (
			${QuestionnaireCache.KEY_ID} INTEGER PRIMARY KEY,
			${QuestionnaireCache.KEY_QUESTIONNAIRE_ID} INTEGER,
			${QuestionnaireCache.KEY_BACKUP_FROM} INTEGER,
			${QuestionnaireCache.KEY_CACHE_VALUE} TEXT,
			FOREIGN KEY(${QuestionnaireCache.KEY_QUESTIONNAIRE_ID}) REFERENCES ${Questionnaire.TABLE}(${Questionnaire.KEY_ID}) ON DELETE CASCADE)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${ActionTrigger.TABLE} (
			${ActionTrigger.KEY_ID} INTEGER PRIMARY KEY,
			${ActionTrigger.KEY_ENABLED} INTEGER,
			${ActionTrigger.KEY_ACTIONS} TEXT,
			${ActionTrigger.KEY_STUDY_ID} INTEGER,
			${ActionTrigger.KEY_QUESTIONNAIRE_ID} INTEGER,
			FOREIGN KEY(${ActionTrigger.KEY_STUDY_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${ActionTrigger.KEY_QUESTIONNAIRE_ID}) REFERENCES ${Questionnaire.TABLE}(${Questionnaire.KEY_ID}) ON DELETE CASCADE)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${EventTrigger.TABLE} (
			${EventTrigger.KEY_ID} INTEGER PRIMARY KEY,
			${EventTrigger.KEY_ACTION_TRIGGER_ID} INTEGER,
			${EventTrigger.KEY_STUDY_ID} INTEGER,
			${EventTrigger.KEY_QUESTIONNAIRE_ID} INTEGER,
			${EventTrigger.KEY_LABEL} TEXT,
			${EventTrigger.KEY_CUE} TEXT,
			${EventTrigger.KEY_DELAY} INTEGER,
			${EventTrigger.KEY_RANDOM_DELAY} INTEGER,
			${EventTrigger.KEY_DELAY_MIN} INTEGER,
			${EventTrigger.KEY_SKIP_THIS_QUESTIONNAIRE} INTEGER,
			${EventTrigger.KEY_SPECIFIC_QUESTIONNAIRE} INTEGER DEFAULT NULL,
			FOREIGN KEY(${EventTrigger.KEY_ACTION_TRIGGER_ID}) REFERENCES ${ActionTrigger.TABLE}(${ActionTrigger.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${EventTrigger.KEY_SPECIFIC_QUESTIONNAIRE}) REFERENCES ${Questionnaire.TABLE}(${Questionnaire.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${EventTrigger.KEY_QUESTIONNAIRE_ID}) REFERENCES ${Questionnaire.TABLE}(${Questionnaire.KEY_ID}) ON DELETE CASCADE)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${Schedule.TABLE} (
			${Schedule.KEY_ID} INTEGER PRIMARY KEY,
			${Schedule.KEY_ACTION_TRIGGER} INTEGER,
			${Schedule.KEY_QUESTIONNAIRE_ID} INTEGER,
			${Schedule.KEY_LAST_SCHEDULED} INTEGER DEFAULT 0,
			${Schedule.KEY_EDITABLE} INTEGER,
			${Schedule.KEY_REPEAT_RATE} INTEGER,
			${Schedule.KEY_SKIP_FIRST_IN_LOOP} INTEGER,
			${Schedule.KEY_WEEKDAYS} INTEGER,
			${Schedule.KEY_DAY_OF_MONTH} INTEGER,
			FOREIGN KEY(${Schedule.KEY_ACTION_TRIGGER}) REFERENCES ${ActionTrigger.TABLE}(${ActionTrigger.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${Schedule.KEY_QUESTIONNAIRE_ID}) REFERENCES ${Questionnaire.TABLE}(${Questionnaire.KEY_ID}) ON DELETE CASCADE)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${SignalTime.TABLE} (
			${SignalTime.KEY_ID} INTEGER PRIMARY KEY,
			${SignalTime.KEY_SCHEDULE_ID} INTEGER,
			${SignalTime.KEY_QUESTIONNAIRE_ID} INTEGER,
			${SignalTime.KEY_LABEL} TEXT,
			${SignalTime.KEY_RANDOM} INTEGER,
			${SignalTime.KEY_RANDOM_FIXED} INTEGER DEFAULT 0,
			${SignalTime.KEY_FREQUENCY} INTEGER,
			${SignalTime.KEY_MINUTES_BETWEEN} INTEGER,
			${SignalTime.KEY_START_TIME_OF_DAY} INTEGER,
			${SignalTime.KEY_END_TIME_OF_DAY} INTEGER,
			${SignalTime.KEY_ORIGINAL_START_TIME_OF_DAY} INTEGER,
			${SignalTime.KEY_ORIGINAL_END_TIME_OF_DAY} INTEGER,
			FOREIGN KEY(${SignalTime.KEY_SCHEDULE_ID}) REFERENCES ${Schedule.TABLE}(${Schedule.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${SignalTime.KEY_QUESTIONNAIRE_ID}) REFERENCES ${Questionnaire.TABLE}(${Questionnaire.KEY_ID}) ON DELETE CASCADE)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${Alarm.TABLE} (
			${Alarm.KEY_ID} INTEGER PRIMARY KEY,
			${Alarm.KEY_ACTION_TRIGGER_ID} INTEGER,
			${Alarm.KEY_EVENT_TRIGGER_ID} INTEGER DEFAULT NULL,
			${Alarm.KEY_SIGNAL_TIME_ID} INTEGER DEFAULT NULL,
			${Alarm.KEY_SCHEDULE_ID} INTEGER DEFAULT NULL,
			${Alarm.KEY_QUESTIONNAIRE_ID} INTEGER,
			${Alarm.KEY_TIMESTAMP} INTEGER,
			${Alarm.KEY_TYPE} INTEGER,
			${Alarm.KEY_INDEX_NUM} INTEGER,
			${Alarm.KEY_LABEL} TEXT,
			${Alarm.KEY_ONLY_SINGLE_ACTION_INDEX} INTEGER DEFAULT -1,
			${Alarm.KEY_REMINDER_COUNT} INTEGER,
			FOREIGN KEY(${Alarm.KEY_ACTION_TRIGGER_ID}) REFERENCES ${ActionTrigger.TABLE}(${ActionTrigger.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${Alarm.KEY_EVENT_TRIGGER_ID}) REFERENCES ${EventTrigger.TABLE}(${EventTrigger.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${Alarm.KEY_SIGNAL_TIME_ID}) REFERENCES ${SignalTime.TABLE}(${Schedule.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${Alarm.KEY_SCHEDULE_ID}) REFERENCES ${Schedule.TABLE}(${Schedule.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${Alarm.KEY_QUESTIONNAIRE_ID}) REFERENCES ${Questionnaire.TABLE}(${Questionnaire.KEY_ID}) ON DELETE CASCADE)""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${DataSet.TABLE} (
			${DataSet.KEY_ID} INTEGER PRIMARY KEY,
			${DataSet.KEY_STUDY_ID} INTEGER,
			${DataSet.KEY_STUDY_WEB_ID} INTEGER,
			${DataSet.KEY_SERVER_URL} TEXT,
			${DataSet.KEY_ACCESS_KEY} TEXT,
			${DataSet.KEY_QUESTIONNAIRE_NAME} TEXT,
			${DataSet.KEY_QUESTIONNAIRE_INTERNAL_ID} INTEGER,
			${DataSet.KEY_STUDY_VERSION} INTEGER,
			${DataSet.KEY_STUDY_SUB_VERSION} INTEGER,
			${DataSet.KEY_STUDY_LANG} TEXT DEFAULT '',
			${DataSet.KEY_STUDY_GROUP} INTEGER DEFAULT 0,
			${DataSet.KEY_TIMEZONE} TEXT,
			${DataSet.KEY_RESPONSE_TIME} INTEGER,
			${DataSet.KEY_TYPE} TEXT,
			${DataSet.KEY_RESPONSES} INTEGER,
			${DataSet.KEY_SYNCED} INTEGER DEFAULT 0,
			FOREIGN KEY(${DataSet.KEY_STUDY_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}),
			FOREIGN KEY(${DataSet.KEY_STUDY_WEB_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_WEB_ID}))""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${FileUpload.TABLE} (
			${FileUpload.KEY_ID} INTEGER PRIMARY KEY,
			${FileUpload.KEY_STUDY_ID} INTEGER,
			${FileUpload.KEY_STUDY_WEB_ID} INTEGER,
			${FileUpload.KEY_SERVER_URL} TEXT,
			${FileUpload.KEY_IS_TEMPORARY} INTEGER,
			${FileUpload.KEY_FILE_PATH} TEXT,
			${FileUpload.KEY_IDENTIFIER} INTEGER,
			${FileUpload.KEY_TYPE} INTEGER,
			${FileUpload.KEY_TIMESTAMP} INTEGER,
			FOREIGN KEY(${FileUpload.KEY_STUDY_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}))""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${DynamicInputData.TABLE} (
			${DynamicInputData.KEY_QUESTIONNAIRE_ID} INTEGER,
			${DynamicInputData.KEY_VARIABLE} TEXT,
			${DynamicInputData.KEY_ITEM_INDEX} INTEGER,
			${DynamicInputData.KEY_CREATED_TIME} INTEGER,
			FOREIGN KEY(${DynamicInputData.KEY_QUESTIONNAIRE_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}))""")
		
		db.execSQL("""CREATE TABLE IF NOT EXISTS ${ErrorBox.TABLE} (
			${ErrorBox.KEY_ID} INTEGER PRIMARY KEY,
			${ErrorBox.KEY_TIMESTAMP} INTEGER,
			${ErrorBox.KEY_SEVERITY} INTEGER,
			${ErrorBox.KEY_TITLE} TEXT,
			${ErrorBox.KEY_MSG} TEXT,
			${ErrorBox.KEY_REVIEWED} INTEGER DEFAULT 0)""")
		
		db.execSQL("""CREATE TRIGGER IF NOT EXISTS limit_error_rows AFTER INSERT ON ${ErrorBox.TABLE}
			WHEN (SELECT COUNT(*) FROM ${ErrorBox.TABLE}) > ${ErrorBox.MAX_SAVED_ERRORS}
			BEGIN
			DELETE FROM ${ErrorBox.TABLE} WHERE ${ErrorBox.KEY_ID}
			= (SELECT MIN(${ErrorBox.KEY_ID}) FROM ${ErrorBox.TABLE});
			END""")
	}
	
	fun updateFrom(db: SQLiteInterface, oldVersion: Int) {
		Updater.updateSQL(db, oldVersion)
	}
	
	
	
	fun startupApp() {
		ErrorBox.log("startupApp", "Cold starting app (v${NativeLink.smartphoneData.appVersion} / ${NativeLink.smartphoneData.lang})")
		if(hasNewErrors())
			NativeLink.dialogOpener.errorReport()
		else {
			Scheduler.checkMissedAlarms(true)
			Scheduler.scheduleIfNeeded()
			Scheduler.scheduleAhead()
			NativeLink.postponedActions.syncDataSets()
			cleanupFiles();
			
			val newLang = NativeLink.smartphoneData.lang
			val oldLang = DbUser.getLang()
			
			if(!hasNoJoinedStudies()) {
				checkLeaveStudies()
				
				if(newLang != oldLang) {
					ErrorBox.log("startupApp", "Detected change in language to \"$newLang\"")
					Web.updateStudiesAsync(true) { updatedCount ->
						if(updatedCount != -1)
							DbUser.setLang(newLang)
					}
				}
				else
					Web.updateStudiesAsync()
				
				NativeLink.postponedActions.updateStudiesRegularly()
			}
			else if(newLang != oldLang) {
				ErrorBox.log("startupApp", "Detected change in language from \"$oldLang\" to \"$newLang\"")
				DbUser.setLang(newLang)
			}
		}
	}
	
	//
	//Notifications
	//
	fun notificationsAreSetup(): Boolean {
		val c = NativeLink.sql.select(
			DbUser.TABLE,
			arrayOf(DbUser.KEY_NOTIFICATIONS_SETUP),
			null, null,
			null,
			null,
			null,
			"1"
		)
		val r = c.moveToFirst() && c.getBoolean(0)
		c.close()
		return r
	}
	fun setNotificationsToSetup() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putInt(DbUser.KEY_NOTIFICATIONS_SETUP, 1)
		db.update(DbUser.TABLE, values, null, null)
	}
	
	fun reportMissedInvitation(questionnaire: Questionnaire, timestamp: Long) {
		DataSet.createActionSentDataSet(DataSet.TYPE_INVITATION_MISSED, questionnaire, timestamp)

		NativeLink.sql.execSQL("UPDATE ${DbUser.TABLE} SET ${DbUser.KEY_NOTIFICATIONS_MISSED} = ${DbUser.KEY_NOTIFICATIONS_MISSED} + 1")
	}
	fun resetMissedInvitations() {
		NativeLink.sql.execSQL("UPDATE ${DbUser.TABLE} SET ${DbUser.KEY_NOTIFICATIONS_MISSED} = 0")
	}
	fun getMissedInvitations(): Int {
		val c = NativeLink.sql.select(
			DbUser.TABLE,
			arrayOf(DbUser.KEY_NOTIFICATIONS_MISSED),
			null, null,
			null,
			null,
			null,
			"1"
		)
		val r = if(c.moveToFirst()) c.getInt(0) else 0
		c.close()
		return r
	}
	
	
	//
	//Studies
	//
	fun checkLeaveStudies() {
		val studies = getJoinedStudies()
		for(s in studies) {
			s.leaveAfterCheck()
		}
	}
	fun hasNoStudies(): Boolean {
		val c = NativeLink.sql.select(
			Study.TABLE,
			arrayOf(Study.KEY_ID),
			null, null,
			null,
			null,
			null,
			"1"
		)
		val r = !c.moveToFirst()
		c.close()
		return r
	}
	fun hasNoJoinedStudies(): Boolean {
		val c = NativeLink.sql.select(
			Study.TABLE,
			arrayOf(Study.KEY_ID),
			"${Study.KEY_STATE} = ${Study.STATES.Joined.ordinal}", null,
			null,
			null,
			null,
			"1"
		)
		val r = !c.moveToFirst()
		c.close()
		return r
	}
	fun hasStudiesWithStatistics(): Boolean {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"LENGTH(${Study.KEY_PUBLIC_CHARTS_JSON}) > 2 OR LENGTH(${Study.KEY_PERSONAL_CHARTS_JSON}) > 2", null,
			null,
			null,
			null,
			"1"
		)
		var r = false
		if(c.moveToFirst())
			r = true
		c.close()
		return r
	}
	fun hasStudiesForMessages(): Boolean {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"(${Study.KEY_STATE} = ${Study.STATES.Joined.ordinal} AND ${Study.KEY_SEND_MESSAGES_ALLOWED} = 1) OR ${Study.KEY_LAST_MSG_TIMESTAMP} IS NOT 0", null,
			null,
			null,
			null,
			"1"
		)
		var r = false
		if(c.moveToFirst())
			r = true
		c.close()
		return r
	}
	fun hasStudiesWithRewards(): Boolean {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"(${Study.KEY_ENABLE_REWARD_SYSTEM} = 1 AND (${Study.KEY_STATE} = ${Study.STATES.Joined.ordinal} OR ${Study.KEY_CACHED_REWARD_CODE} IS NOT NULL))", null,
			null,
			null,
			null,
			"1"
		)
		var r = false
		if(c.moveToFirst())
			r = true
		c.close()
		return r
	}
	
	fun getFirstStudy(): Study? {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			null, null,
			null,
			null,
			Study.KEY_JOINED_TIMESTAMP,
			null
		)
		val study = if(c.moveToFirst()) Study(c) else null
		c.close()
		return study
	}
	fun getStudy(id: Long): Study? {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"${Study.KEY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			null
		)
		val study = if(c.moveToFirst()) Study(c) else null
		c.close()
		return study
	}
	
	fun getStudy(server_url: String, webId: Long): Study? {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"${Study.KEY_WEB_ID} = ? AND ${Study.KEY_SERVER_URL} = ?", arrayOf(webId.toString(), server_url),
			null,
			null,
			null,
			"1"
		)
		val s = if(c.moveToFirst()) Study(c) else null
		c.close()
		return s
	}
	
	fun getJoinedStudies(): List<Study> {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"${Study.KEY_STATE} = ${Study.STATES.Joined.ordinal}", null,
			null,
			null,
			null,
			null
		)
		val studies: MutableList<Study> = ArrayList()
		while(c.moveToNext()) {
			studies.add(Study(c))
		}
		c.close()
		return studies
	}
	
	fun getStudiesWithEditableSchedules(): List<Study> {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			null, null,
			null,
			null,
			null,
			null
		)
		val studies: MutableList<Study> = ArrayList()
		while(c.moveToNext()) {
			val study = Study(c)
			if(study.hasEditableSchedules())
				studies.add(study)
		}
		c.close()
		return studies
	}
	
	fun getStudiesWithStatistics(): List<Study> {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"LENGTH(${Study.KEY_PUBLIC_CHARTS_JSON}) > 2 OR LENGTH(${Study.KEY_PERSONAL_CHARTS_JSON}) > 2", null,
			null,
			null,
			null,
			null
		)
		val studies = ArrayList<Study>()
		while(c.moveToNext()) {
			studies.add(Study(c))
		}
		c.close()
		return studies
	}
	
	fun getStudiesForMessages(): List<Study> {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"(${Study.KEY_STATE} = ${Study.STATES.Joined.ordinal} AND ${Study.KEY_SEND_MESSAGES_ALLOWED} = 1) OR ${Study.KEY_LAST_MSG_TIMESTAMP} IS NOT 0", null,
			null,
			null,
			null,
			null
		)
		val studies = ArrayList<Study>()
		while(c.moveToNext()) {
			studies.add(Study(c))
		}
		c.close()
		return studies
	}
	
	fun getStudiesWithRewards(): List<Study> {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			"${Study.KEY_ENABLE_REWARD_SYSTEM} = 1 AND (${Study.KEY_STATE} = ${Study.STATES.Joined.ordinal} OR ${Study.KEY_CACHED_REWARD_CODE} IS NOT NULL)", null,
			null,
			null,
			null,
			null
		)
		val studies = ArrayList<Study>()
		while(c.moveToNext()) {
			studies.add(Study(c))
		}
		c.close()
		return studies
	}
	
	fun getAllStudies(): List<Study> {
		val c = NativeLink.sql.select(
			Study.TABLE,
			Study.COLUMNS,
			null, null,
			null,
			null,
			null,
			null
		)
		val studies: MutableList<Study> = ArrayList()
		while(c.moveToNext()) {
			studies.add(Study(c))
		}
		c.close()
		return studies
	}
	
	//
	//Messages
	//
	fun getMessages(id: Long): List<Message> {
		val c = NativeLink.sql.select(
			Message.TABLE,
			Message.COLUMNS,
			"${Message.KEY_STUDY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			null
		)
		val messages = ArrayList<Message>()
		while(c.moveToNext()) {
			messages.add(Message(c))
		}
		c.close()
		return messages
	}
	fun countUnreadMessages(id: Long): Int {
		val c = NativeLink.sql.select(
			Message.TABLE,
			arrayOf("COUNT(*)"),
			"${Message.KEY_STUDY_ID} = ? AND ${Message.KEY_IS_NEW} = 1", arrayOf(id.toString()),
			null,
			null,
			null,
			null
		)
		var r = 0
		if(c.moveToFirst())
			r = c.getInt(0)
		
		c.close()
		return r
	}
	fun countUnreadMessages(): Int {
		val c = NativeLink.sql.select(
			Message.TABLE,
			arrayOf("COUNT(*)"),
			"${Message.KEY_IS_NEW} = 1", null,
			null,
			null,
			null,
			null
		)
		var r = 0
		if(c.moveToFirst())
			r = c.getInt(0)
		
		c.close()
		return r
	}
	
	//
	//Questionnaires
	//
	fun getQuestionnaire(id: Long): Questionnaire? {
		val c = NativeLink.sql.select(
			Questionnaire.TABLE,
			Questionnaire.COLUMNS,
			"${Questionnaire.KEY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			"1"
		)
		var r: Questionnaire? = null
		if(c.moveToFirst()) r = Questionnaire(c)
		c.close()
		return r
	}
	
	fun getPinnedQuestionnairesSplitByState(studyId: Long): Pair<List<Questionnaire>, List<Questionnaire>> {
		val c = NativeLink.sql.select(
			Questionnaire.TABLE,
			Questionnaire.COLUMNS,
			"${Questionnaire.KEY_STUDY_ID} = ? AND ${Questionnaire.KEY_COMPLETABLE_ONCE} = 0 AND ${Questionnaire.KEY_LIMIT_COMPLETION_FREQUENCY} = 0 AND ${Questionnaire.KEY_COMPLETABLE_ONCE_PER_NOTIFICATION} = 0",
			arrayOf(studyId.toString()),
			null,
			null,
			null,
			null
		)
		val enabled = ArrayList<Questionnaire>()
		val disabled = ArrayList<Questionnaire>()
		while(c.moveToNext()) {
			val questionnaire = Questionnaire(c)
			if(questionnaire.canBeFilledOut())
				enabled.add(questionnaire)
			else
				disabled.add(questionnaire)
		}
		c.close()
		return Pair(enabled, disabled)
	}
	fun hasPinnedQuestionnaires(studyId: Long): Boolean {
		val c = NativeLink.sql.select(
			Questionnaire.TABLE,
			arrayOf(Questionnaire.KEY_STUDY_ID),
			"${Questionnaire.KEY_STUDY_ID} = ? AND ${Questionnaire.KEY_COMPLETABLE_ONCE} = 0 AND ${Questionnaire.KEY_LIMIT_COMPLETION_FREQUENCY} = 0 AND ${Questionnaire.KEY_COMPLETABLE_ONCE_PER_NOTIFICATION} = 0",
			arrayOf(studyId.toString()),
			null,
			null,
			null,
			"1"
		)
		val r = c.moveToFirst()
		c.close()
		return r
	}
	
	fun getRepeatingQuestionnairesSplitByState(studyId: Long): Pair<List<Questionnaire>, List<Questionnaire>> {
		val c = NativeLink.sql.select(
			Questionnaire.TABLE,
			Questionnaire.COLUMNS,
			"${Questionnaire.KEY_STUDY_ID} = ? AND ${Questionnaire.KEY_COMPLETABLE_ONCE} = 0 AND (${Questionnaire.KEY_LIMIT_COMPLETION_FREQUENCY} = 1 OR ${Questionnaire.KEY_COMPLETABLE_ONCE_PER_NOTIFICATION} = 1)",
			arrayOf(studyId.toString()),
			null,
			null,
			null,
			null
		)
		val enabled = ArrayList<Questionnaire>()
		val disabled = ArrayList<Questionnaire>()
		while(c.moveToNext()) {
			val questionnaire = Questionnaire(c)
			if(questionnaire.canBeFilledOut())
				enabled.add(questionnaire)
			else
				disabled.add(questionnaire)
		}
		c.close()
		return Pair(enabled, disabled)
	}
	fun hasRepeatingQuestionnaires(studyId: Long): Boolean {
		val c = NativeLink.sql.select(
			Questionnaire.TABLE,
			arrayOf(Questionnaire.KEY_STUDY_ID),
			"${Questionnaire.KEY_STUDY_ID} = ? AND ${Questionnaire.KEY_COMPLETABLE_ONCE} = 0 AND (${Questionnaire.KEY_LIMIT_COMPLETION_FREQUENCY} = 1 OR ${Questionnaire.KEY_COMPLETABLE_ONCE_PER_NOTIFICATION} = 1)",
			arrayOf(studyId.toString()),
			null,
			null,
			null,
			"1"
		)
		
		val r = c.moveToFirst()
		c.close()
		return r
	}
	
	fun getOneTimeQuestionnairesSplitByState(studyId: Long): Pair<List<Questionnaire>, List<Questionnaire>> {
		val c = NativeLink.sql.select(
			Questionnaire.TABLE,
			Questionnaire.COLUMNS,
			"${Questionnaire.KEY_STUDY_ID} = ? AND ${Questionnaire.KEY_COMPLETABLE_ONCE} = 1", arrayOf(studyId.toString()),
			null,
			null,
			null,
			null
		)
		val enabled = ArrayList<Questionnaire>()
		val disabled = ArrayList<Questionnaire>()
		while(c.moveToNext()) {
			val questionnaire = Questionnaire(c)
			if(questionnaire.canBeFilledOut())
				enabled.add(questionnaire)
			else
				disabled.add(questionnaire)
		}
		c.close()
		return Pair(enabled, disabled)
	}
	fun hasOneTimeQuestionnaires(studyId: Long): Boolean {
		val c = NativeLink.sql.select(
			Questionnaire.TABLE,
			arrayOf(Questionnaire.KEY_STUDY_ID),
			"${Questionnaire.KEY_STUDY_ID} = ? AND ${Questionnaire.KEY_COMPLETABLE_ONCE} = 1", arrayOf(studyId.toString()),
			null,
			null,
			null,
			"1"
		)
		val r = c.moveToFirst()
		c.close()
		return r
	}
	
	//
	//RandomText
	//
	fun getLastDynamicTextIndex(qId: Long, name: String): DynamicInputData? {
		val c = NativeLink.sql.select(
			DynamicInputData.TABLE,
			DynamicInputData.COLUMNS,
			"${DynamicInputData.KEY_QUESTIONNAIRE_ID} = ? AND ${DynamicInputData.KEY_VARIABLE} = ?", arrayOf(qId.toString(), name),
			null,
			null,
			"${DynamicInputData.KEY_CREATED_TIME} DESC",
			"1"
		)
		val r = if(c.moveToFirst()) DynamicInputData(c) else null
		c.close()
		return r
	}
	
	fun getAvailableListForDynamicText(qId: Long, name: String, length: Int): IntArray {
		val checked = BooleanArray(length)
		val c = NativeLink.sql.select(
			DynamicInputData.TABLE,
			DynamicInputData.COLUMNS,
			"${DynamicInputData.KEY_QUESTIONNAIRE_ID} = ? AND ${DynamicInputData.KEY_VARIABLE} = ?", arrayOf(qId.toString(), name),
			null,
			null,
			null,
			null
		)
		var checkedLength = 0
		while(c.moveToNext()) {
			val t = DynamicInputData(c)
			checked[t.index] = true
			++checkedLength
		}
		c.close()
		
		if(checkedLength == 0 || checkedLength >= length) {
			deleteCheckedRandomTexts(qId, name)
			val r = IntArray(length)
			for(i in length - 1 downTo 0) {
				r[i] = i
			}
			return r
		}
		
		val r = IntArray(length - checkedLength)
		var index = 0
		for(i in 0 until length) {
			if(!checked[i])
				r[index++] = i
		}
		return r
	}
	
	internal fun deleteCheckedRandomTexts(qId: Long, name: String) {
		NativeLink.sql.delete(
			DynamicInputData.TABLE,
			"${DynamicInputData.KEY_QUESTIONNAIRE_ID}=? AND ${DynamicInputData.KEY_VARIABLE}=?",
			arrayOf(qId.toString(), name)
		)
	}
	
	//
	//DataSets
	//
	fun getDataSets(studyId: Long): List<DataSet> {
		val c = NativeLink.sql.select(
			DataSet.TABLE_JOINED,
			DataSet.COLUMNS,
			"${DataSet.TABLE}.${DataSet.KEY_STUDY_ID} = ?", arrayOf(studyId.toString()),
			null,
			null,
			"${DataSet.TABLE}.${DataSet.KEY_RESPONSE_TIME} DESC",
			null
		)
		val list = ArrayList<DataSet>()
		while(c.moveToNext()) {
			list.add(DataSet(c))
		}
		c.close()
		
		return list
	}
	fun hasUnSyncedDataSets(studyId: Long): Boolean {
		var c = NativeLink.sql.select(
			DataSet.TABLE,
			arrayOf(DataSet.KEY_STUDY_ID),
			"${DataSet.KEY_STUDY_ID} = ? AND ${DataSet.KEY_SYNCED} IS NOT ${DataSet.STATES.SYNCED.ordinal}", arrayOf(studyId.toString()),
			null,
			null,
			null,
			null
		)
		var r = c.moveToFirst()
		c.close()
		
		if(!r) {
			c = NativeLink.sql.select(
				FileUpload.TABLE,
				FileUpload.COLUMNS,
				"${FileUpload.KEY_IS_TEMPORARY} IS 0", null,
				null,
				null,
				null,
				null
			)
			r = c.moveToFirst()
			c.close()
		}
		return r
	}
	fun getUnSyncedDataSetCount(): Int {
		var c = NativeLink.sql.select(
			DataSet.TABLE,
			arrayOf("COUNT(*)"),
			"${DataSet.KEY_SYNCED} IS NOT ${DataSet.STATES.SYNCED.ordinal}", null,
			null,
			null,
			null,
			null
		)
		var r = if(c.moveToFirst())
			c.getInt(0)
		else
			0
		c.close()
		
		
		c = NativeLink.sql.select(
			FileUpload.TABLE,
			arrayOf("COUNT(*)"),
			"${FileUpload.KEY_IS_TEMPORARY} IS 0", null,
			null,
			null,
			null,
			null
		)
		r = (if(c.moveToFirst())
			c.getInt(0)
		else
			0).coerceAtLeast(r)
		c.close()
		return r
	}
	fun getQuestionnaireDataSetCount(studyId: Long): Int {
		val c = NativeLink.sql.select(
			DataSet.TABLE,
			arrayOf("COUNT(*)"),
			"${DataSet.KEY_TYPE} = ? AND ${DataSet.KEY_STUDY_ID} = ?", arrayOf(DataSet.TYPE_QUESTIONNAIRE, studyId.toString()),
			null,
			null,
			null,
			null
		)
		val r = if(c.moveToFirst())
			c.getInt(0)
		else
			0
		c.close()
		return r
	}
	
	fun getUnSyncedDataSets(): Map<String, List<DataSet>> { //grouped by serverUrl
		val c = NativeLink.sql.select(
			DataSet.TABLE_JOINED,
			DataSet.COLUMNS,
			"${DataSet.KEY_SYNCED} IS NOT ${DataSet.STATES.SYNCED.ordinal}", null,
			null,
			null,
			"${DataSet.KEY_SYNCED} ASC", //we have to make sure that erroneous entries who lead to crashes dont prevent new entries from getting synced
			null
		)
		val container = HashMap<String, ArrayList<DataSet>>()
		while(c.moveToNext()) {
			val dataSet = DataSet(c)
			if(!container.containsKey(dataSet.serverUrl))
				container[dataSet.serverUrl] = ArrayList()
			container[dataSet.serverUrl]?.add(dataSet)
		}
		c.close()
		return container
	}
	fun getDataSet(id: Long): DataSet? {
		val c = NativeLink.sql.select(
			DataSet.TABLE_JOINED,
			DataSet.COLUMNS,
			"${DataSet.KEY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			"1"
		)
		val r = if(c.moveToFirst()) DataSet(c) else null
		c.close()
		return r
	}
	
	//
	//FileUpload
	//
	
	fun getFileUpload(id: Long): FileUpload? {
		val c = NativeLink.sql.select(
			FileUpload.TABLE,
			FileUpload.COLUMNS,
			"${FileUpload.KEY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			null
		)
		val r = if(c.moveToFirst()) FileUpload(c) else null
		c.close()
		return r
	}
	
	fun getFileUploadByIdentifier(identifier: String): FileUpload? {
		val c = NativeLink.sql.select(
			FileUpload.TABLE,
			FileUpload.COLUMNS,
			"${FileUpload.KEY_IDENTIFIER} = ?", arrayOf(identifier),
			null,
			null,
			null,
			null
		)
		val r = if(c.moveToFirst()) FileUpload(c) else null
		c.close()
		return r
	}
	
	fun cleanupFiles() {
		val files = getTemporaryFileUploads()
		for(file: FileUpload in files) {
			if(file.isTooOld())
				file.delete();
		}
	}
	fun getPendingFileUploads(): List<FileUpload> {
		val c = NativeLink.sql.select(
			FileUpload.TABLE,
			FileUpload.COLUMNS,
			"${FileUpload.KEY_IS_TEMPORARY} IS 0", null,
			null,
			null,
			null,
			null
		)
		val container = ArrayList<FileUpload>()
		while(c.moveToNext()) {
			container.add(FileUpload(c))
		}
		c.close()
		return container
	}
	fun getTemporaryFileUploads(): List<FileUpload> {
		val c = NativeLink.sql.select(
			FileUpload.TABLE,
			FileUpload.COLUMNS,
			"${FileUpload.KEY_IS_TEMPORARY} IS 1", null,
			null,
			null,
			null,
			null
		)
		val container = ArrayList<FileUpload>()
		while(c.moveToNext()) {
			container.add(FileUpload(c))
		}
		c.close()
		return container
	}
	
	//
	//Errors
	//
	fun getErrorCount(): Int {
		val c = NativeLink.sql.select(
			ErrorBox.TABLE,
			arrayOf("COUNT(*)"),
			"${ErrorBox.KEY_SEVERITY} = ${ErrorBox.SEVERITY_ERROR}", null,
			null,
			null,
			null,
			null
		)
		val r = if(c.moveToFirst()) c.getInt(0) else 0
		c.close()
		return r
	}
	
	fun getWarnCount(): Int {
		val c = NativeLink.sql.select(
			ErrorBox.TABLE,
			arrayOf("COUNT(*)"),
			"${ErrorBox.KEY_SEVERITY} = ${ErrorBox.SEVERITY_WARN}", null,
			null,
			null,
			null,
			null
		)
		val r = if(c.moveToFirst()) c.getInt(0) else 0
		c.close()
		return r
	}
	
	fun hasNewErrors(): Boolean {
		val c = NativeLink.sql.select(
			ErrorBox.TABLE,
			arrayOf(ErrorBox.KEY_ID),
			"${ErrorBox.KEY_REVIEWED} = 0 AND ${ErrorBox.KEY_SEVERITY} = ${ErrorBox.SEVERITY_ERROR}", null,
			null,
			null,
			null,
			"1"
		)
		val r = c.moveToFirst()
		c.close()
		return r
	}
	
	fun getErrors(): List<ErrorBox> {
		val c = NativeLink.sql.select(
			ErrorBox.TABLE,
			ErrorBox.COLUMNS,
			null, null,
			null,
			null,
			"${ErrorBox.KEY_ID} DESC",
			ErrorBox.MAX_SAVED_ERRORS.toString()
		)
		val errors = ArrayList<ErrorBox>()
		while(c.moveToNext()) {
			errors.add(ErrorBox(c))
		}
		c.close()
		return errors
	}
	
	fun setErrorsAsReviewed() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putInt(ErrorBox.KEY_REVIEWED, 1)
		db.update(ErrorBox.TABLE, values, null, null)
	}
	
	//
	//Alarms
	// in alarm.exec() alarms are skipped when a newer one is pending. This means that lists that are executed need to be ordered!
	//
	
	fun getLastAlarmBefore(timestamp: Long, questionnaireId: Long): Alarm? {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_TIMESTAMP} <= ? AND ${Alarm.KEY_QUESTIONNAIRE_ID} = ?", arrayOf(timestamp.toString(), questionnaireId.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} DESC",
			"1"
		)
		var r: Alarm? = null
		if(c.moveToFirst())
			r = Alarm(c)
		c.close()
		return r
	}
	
	fun getAlarm(id: Long): Alarm? {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			"1"
		)
		var r: Alarm? = null
		if(c.moveToFirst())
			r = Alarm(c)
		c.close()
		return r
	}
	
	fun getAlarms(): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			null, null,
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	fun getAlarms(schedule: Schedule): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_SCHEDULE_ID} = ?", arrayOf(schedule.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	
	fun getAlarmsBefore(timestamp: Long, questionnaireId: Long): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_TIMESTAMP} <= ? AND ${Alarm.KEY_QUESTIONNAIRE_ID} = ?", arrayOf(timestamp.toString(), questionnaireId.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	fun getAlarmsBefore(timestamp: Long): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_TIMESTAMP} <= ?", arrayOf(timestamp.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	
	fun getAlarmsAfterToday(signalTime: SignalTime): List<Alarm> { //used in Scheduler.scheduleAhead() for IOS where a separate service has to set alarms manually for the next day
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_SIGNAL_TIME_ID} = ? AND ${Alarm.KEY_TIMESTAMP} >= ?", arrayOf(signalTime.id.toString(), (NativeLink.getMidnightMillis() + Scheduler.ONE_DAY_MS).toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	
	fun getLastSignalTimeAlarm(signalTime: SignalTime): Alarm? { //for iOS
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_SIGNAL_TIME_ID} = ? AND ${Alarm.KEY_TYPE} = ${Alarm.TYPES.SignalTime.ordinal}", arrayOf(signalTime.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} DESC",
			"1"
		)
		var r: Alarm? = null
		if(c.moveToFirst())
			r = Alarm(c)
		c.close()
		return r
	}
	
	fun getLastAlarmPerSignalTime(): List<Alarm> {
		//a bit weird since this value will not be used and only "tricks" sql to group by the highest timestamp value
		// but it has the lowest chance of breaking when code changes occur:
		val columns = Alarm.COLUMNS.plus("MAX(${Alarm.KEY_TIMESTAMP})")
		
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			columns,
			"${Alarm.KEY_TYPE} = ${Alarm.TYPES.SignalTime.ordinal}", null,
			Alarm.KEY_SIGNAL_TIME_ID,
			null,
			null,
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	
	fun getQuestionnaireAlarmsWithNotifications(studyId: Long): List<Alarm> {
		//a bit weird since this value will not be used and only "tricks" sql to group by the highest timestamp value
		// but it has the lowest chance of breaking when code changes occur:
		val columns = Alarm.COLUMNS.plus("MIN(${Alarm.KEY_TIMESTAMP})")
		
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			columns,
			null, null,
			Alarm.KEY_QUESTIONNAIRE_ID,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val r = ArrayList<Alarm>()
		while(c.moveToNext()) {
			val alarm = Alarm(c)
			val q = getQuestionnaire(alarm.questionnaireId) ?: continue
			if(q.studyId == studyId && alarm.actionTrigger.hasNotifications()) {
				r.add(alarm)
			}
		}
		c.close()
		return r
	}
	fun getNextAlarmWithNotifications(studyId: Long): Alarm? {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			null, null,
			Alarm.KEY_QUESTIONNAIRE_ID,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		while(c.moveToNext()) {
			val alarm = Alarm(c)
			val q = getQuestionnaire(alarm.questionnaireId) ?: continue
			if(q.studyId == studyId && alarm.actionTrigger.hasNotifications()) {
				c.close()
				return alarm
			}
		}
		c.close()
		return null
	}
	fun getNextAlarm(schedule: Schedule): Alarm? {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_SCHEDULE_ID} = ?", arrayOf(schedule.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			"1"
		)
		
		var r: Alarm? = null
		if(c.moveToFirst())
			r = Alarm(c)
		c.close()
		return r
	}
	fun getNextAlarm(questionnaire: Questionnaire): Alarm? {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_QUESTIONNAIRE_ID} = ?", arrayOf(questionnaire.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			"1"
		)

		var r: Alarm? = null
		if(c.moveToFirst())
			r = Alarm(c)
		c.close()
		return r
	}
	
	fun getReminderAlarmsFrom(questionnaireId: Long): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_QUESTIONNAIRE_ID}=? AND ${Alarm.KEY_TYPE}=${Alarm.TYPES.Reminder.ordinal}", arrayOf(questionnaireId.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}

	fun getAlarmsFrom(questionnaire: Questionnaire): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_QUESTIONNAIRE_ID}=?", arrayOf(questionnaire.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	fun getAlarmsFrom(actionTrigger: ActionTrigger): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_ACTION_TRIGGER_ID}=?", arrayOf(actionTrigger.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	fun getAlarmsFrom(eventTrigger: EventTrigger): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_EVENT_TRIGGER_ID}=?", arrayOf(eventTrigger.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	fun getAlarmsFrom(schedule: Schedule): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_SCHEDULE_ID}=?", arrayOf(schedule.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	fun getAlarmsFrom(signalTime: SignalTime): List<Alarm> {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			Alarm.COLUMNS,
			"${Alarm.KEY_SIGNAL_TIME_ID}=?", arrayOf(signalTime.id.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val alarms = ArrayList<Alarm>()
		while(c.moveToNext()) {
			alarms.add(Alarm(c))
		}
		c.close()
		return alarms
	}
	
	fun countAlarmsFrom(signalTimeId: Long): Int {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			arrayOf("COUNT(*)"),
			"${Alarm.KEY_SIGNAL_TIME_ID}=? AND ${Alarm.KEY_TYPE} = ${Alarm.TYPES.SignalTime.ordinal}", arrayOf(signalTimeId.toString()),
			null,
			null,
			"${Alarm.KEY_TIMESTAMP} ASC",
			null
		)
		val r = if(c.moveToFirst()) c.getInt(0) else 0
		c.close()
		return r
	}
	
	//
	//ActionTrigger
	//
	fun getActionTrigger(action_id: Long): ActionTrigger? {
		val c = NativeLink.sql.select(
			ActionTrigger.TABLE,
			ActionTrigger.COLUMNS,
			"${ActionTrigger.KEY_ID} = ?", arrayOf(action_id.toString()),
			null,
			null,
			null,
			null
		)
		val actionTrigger = if(c.moveToFirst()) ActionTrigger(c) else null
		c.close()
		return actionTrigger
	}
	
	fun getActionTriggers(study_id: Long): List<ActionTrigger> {
		val c = NativeLink.sql.select(
			ActionTrigger.TABLE,
			ActionTrigger.COLUMNS,
			"${ActionTrigger.KEY_STUDY_ID} = ?", arrayOf(study_id.toString()),
			null,
			null,
			ActionTrigger.KEY_QUESTIONNAIRE_ID,
			null
		)
		val schedules: MutableList<ActionTrigger> = ArrayList()
		while(c.moveToNext()) {
			schedules.add(ActionTrigger(c))
		}
		c.close()
		return schedules
	}
	
	//
	//EventTrigger
	//
	fun getEventTrigger(id: Long): EventTrigger? {
		val c = NativeLink.sql.select(
			EventTrigger.TABLE_JOINED,
			EventTrigger.COLUMNS_JOINED,
			"${EventTrigger.EXT_KEY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			"1"
		)
		val r = if(c.moveToFirst()) EventTrigger(c) else null
		c.close()
		return r
	}
	
	fun getEventTriggers(study_id: Long, cue: String): List<EventTrigger> {
		val c = NativeLink.sql.select(
			EventTrigger.TABLE_JOINED,
			EventTrigger.COLUMNS_JOINED,
			"${EventTrigger.EXT_KEY_STUDY_ID}=? AND ${EventTrigger.EXT_KEY_CUE}=?", arrayOf(study_id.toString(), cue),
			null,
			null,
			null,
			null
		)
		val r: MutableList<EventTrigger> = ArrayList()
		while(c.moveToNext()) {
			r.add(EventTrigger(c))
		}
		c.close()
		return r
	}
	
	internal fun triggerEventTrigger(studyId: Long, cue: String, qId: Long) {
		val list: List<EventTrigger> = getEventTriggers(studyId, cue)
		if(list.isNotEmpty()) {
			val q = if(qId == -1L) null else getQuestionnaire(qId)
			for(eventTrigger in list) {
				eventTrigger.triggerCheck(q)
			}
		}
	}
	
	//
	//Schedules
	//
	fun getSchedule(id: Long): Schedule? {
		val c = NativeLink.sql.select(
			Schedule.TABLE,
			Schedule.COLUMNS,
			"${Schedule.KEY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			"1"
		)
		var r: Schedule? = null
		if(c.moveToFirst())
			r = Schedule(c)
		c.close()
		return r
		
	}
	fun getAllSchedules(): List<Schedule> {
		val c = NativeLink.sql.select(
			Schedule.TABLE,
			Schedule.COLUMNS,
			null, null,
			null,
			null,
			null,
			null
		)
		val r = ArrayList<Schedule>()
		while(c.moveToNext()) {
			r.add(Schedule(c))
		}
		c.close()
		return r
	}
	fun hasEditableSchedules(): Boolean {
		val c = NativeLink.sql.select(
			Schedule.TABLE,
			arrayOf(Schedule.KEY_ID),
			"${Schedule.KEY_EDITABLE} = 1", null,
			null,
			null,
			null,
			"1"
		)
		val r = c.moveToFirst()
		c.close()
		return r
	}
	
	//
	//SignalTime
	//
	fun getSignalTime(id: Long): SignalTime? {
		val c = NativeLink.sql.select(
			SignalTime.TABLE,
			SignalTime.COLUMNS,
			"${SignalTime.KEY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			"1"
		)
		var r: SignalTime? = null
		if(c.moveToFirst())
			r = SignalTime(c)
		c.close()
		return r
	}
	fun getSignalTimes(schedule: Schedule): List<SignalTime> {
		val c = NativeLink.sql.select(
			SignalTime.TABLE,
			SignalTime.COLUMNS,
			"${SignalTime.KEY_SCHEDULE_ID} = ?", arrayOf(schedule.id.toString()),
			null,
			null,
			null,
			null
		)
		val r = ArrayList<SignalTime>()
		while(c.moveToNext()) {
			r.add(SignalTime(c))
		}
		c.close()
		return r
	}
	
	fun signalTimeHasAlarms(signalTimeId: Long): Boolean {
		val c = NativeLink.sql.select(
			Alarm.TABLE,
			arrayOf(Alarm.KEY_ID),
			"${Alarm.KEY_SIGNAL_TIME_ID} = ?", arrayOf(signalTimeId.toString()),
			null,
			null,
			null,
			"1")
		
		val r = c.moveToFirst()
		c.close()
		return r
	}
	
	//
	//Observed Variable
	//
	fun getObservedVariables(studyId: Long, key: String): List<ObservedVariable> {
		val c = NativeLink.sql.select(
			ObservedVariable.TABLE,
			ObservedVariable.COLUMNS,
			"${ObservedVariable.KEY_STUDY_ID}=? AND ${ObservedVariable.KEY_VARIABLE_NAME}=?", arrayOf(studyId.toString(), key),
			null,
			null,
			null,
			null
		)
		val r = ArrayList<ObservedVariable>()
		while(c.moveToNext()) {
			r.add(ObservedVariable(c))
		}
		return r
	}
	
	//
	//DailyStatistics
	//
	fun getPersonalStatistics(studyId: Long): Pair<Long, Map<String, MutableList<StatisticData>>> {
		val dataListContainer = HashMap<String, MutableList<StatisticData>>()
		//
		//daily statistics:
		//
		var firstDay = NativeLink.getNowMillis() / 1000
		
		//get db-cursor:
		var c = NativeLink.sql.select(
			StatisticData_timed.TABLE_CONNECTED,
			StatisticData_timed.COLUMNS_CONNECTED,
			"${StatisticData_timed.TABLE}.${StatisticData_timed.KEY_STUDY_ID} = ?", arrayOf(studyId.toString()),
			null,
			null,
			"${StatisticData_timed.TABLE}.${StatisticData_timed.KEY_TIMESTAMP}",
			null
		)
		if(c.moveToFirst()) {
			
			//load data
			do {
				val statistic = StatisticData_timed(c)
				val day = statistic.dayTimestampSec
				val index = statistic.observableIndex
				val variableName = statistic.variableName
				val key = variableName + index
				var dataList: MutableList<StatisticData>
				var lastAddedDay: Long
				
				//create / load lists:
				if(dataListContainer.containsKey(key)) { //load existing list
					dataList = dataListContainer[key]!!
					lastAddedDay = (dataList[dataList.size - 1] as StatisticData_timed?)!!.dayTimestampSec
				}
				else { //create new list
					if(statistic.dayTimestampSec < firstDay)
						firstDay = statistic.dayTimestampSec
					dataList = ArrayList()
					dataListContainer[key] = dataList
					lastAddedDay = Long.MAX_VALUE
				}
				
				//add empty entries if days were skipped:
				while(day - StatisticData_timed.ONE_DAY > lastAddedDay) {
					lastAddedDay += StatisticData_timed.ONE_DAY
					dataList.add(
						StatisticData_timed(
							lastAddedDay,
							variableName,
							index
						)
					)
				}
				
				//add data:
				dataList.add(statistic)
			} while(c.moveToNext())
			
			StatisticData_timed.correctEntriesToFirstDay(dataListContainer, firstDay)
		}
		c.close()
		
		//
		//Frequency distribution data:
		//
		c = NativeLink.sql.select(
			StatisticData_perValue.TABLE_CONNECTED,
			StatisticData_perValue.COLUMNS_CONNECTED,
			"${StatisticData_perValue.TABLE}.${StatisticData_perValue.KEY_STUDY_ID} = ?", arrayOf(studyId.toString()),
			null,
			null,
			StatisticData_perValue.KEY_VALUE,
			null
		)
		if(c.moveToFirst()) {
			do {
				val statistic = StatisticData_perValue(c)
				val key = statistic.variableName + statistic.observableIndex
				var dataList: MutableList<StatisticData>
				if(dataListContainer.containsKey(key)) {
					dataList = dataListContainer[key]!!
				}
				else {
					dataList = ArrayList()
					dataListContainer[key] = dataList
				}
				dataList.add(statistic)
			} while(c.moveToNext())
		}
		c.close()
		
		return Pair(firstDay, dataListContainer)
	}
	
	//
	//Mixed
	//
	
	fun getStudyServerUrls(): MutableList<Pair<String, String>> {
		val c = NativeLink.sql.select(
			Study.TABLE,
			arrayOf(Study.KEY_TITLE, Study.KEY_SERVER_URL),
			null, null,
			null,
			null,
			null,
			null
		)
		val urls: MutableList<Pair<String, String>> = ArrayList()
		while(c.moveToNext()) {
			urls.add(Pair(c.getString(0), c.getString(1)))
		}
		c.close()
		return urls
	}
}