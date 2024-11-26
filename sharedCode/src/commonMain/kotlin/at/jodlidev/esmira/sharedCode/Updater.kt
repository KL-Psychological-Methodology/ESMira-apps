package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.*
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perData
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perValue
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_timed
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

/**
 * Created by JodliDev on 08.09.2020.
 */
internal object Updater {
	const val EXPECTED_SERVER_VERSION: Int = 13
	const val DATABASE_VERSION = 49
	const val LIBRARY_VERSION = 19 //this is mainly used for iOS so we can check that changes in the library have been used in the C library
	const val MERLIN_VERSION = 1

	fun updateSQL(db: SQLiteInterface, oldVersion: Int) {
		if(oldVersion >= DATABASE_VERSION)
			return
		println("Updating from $oldVersion to $DATABASE_VERSION")
		
		if(oldVersion <= 14) {
			db.execSQL("ALTER TABLE ${Study.TABLE} ADD COLUMN ${Study.KEY_ACCESS_KEY} TEXT;")
			db.execSQL("ALTER TABLE ${DataSet.TABLE} ADD COLUMN ${DataSet.KEY_ACCESS_KEY} TEXT;")
		}
		if(oldVersion <= 15) {
			db.execSQL("ALTER TABLE ${Questionnaire.TABLE} ADD COLUMN inputs_randomized INTEGER;")
		}
		if(oldVersion <= 16) {
			db.execSQL("ALTER TABLE ${Study.TABLE} ADD COLUMN ${Study.KEY_SUB_VERSION} INTEGER;")
			db.execSQL("ALTER TABLE ${DataSet.TABLE} ADD COLUMN ${DataSet.KEY_STUDY_SUB_VERSION} INTEGER;")
		}
		if(oldVersion <= 17) {
			db.execSQL("ALTER TABLE ${Alarm.TABLE} ADD COLUMN ${Alarm.KEY_INDEX_NUM} INTEGER;")
			db.execSQL("ALTER TABLE ${Alarm.TABLE} ADD COLUMN was_rescheduled INTEGER;")
		}
		if(oldVersion <= 18) {
			db.execSQL("ALTER TABLE ${ObservedVariable.TABLE} ADD COLUMN ${ObservedVariable.KEY_TIME_INTERVAL} INTEGER DEFAULT ${StatisticData_timed.ONE_DAY};")
			
			db.execSQL("""CREATE TABLE IF NOT EXISTS ${StatisticData_timed.TABLE} (
			${StatisticData_timed.KEY_ID} INTEGER PRIMARY KEY,
			${StatisticData_timed.KEY_STUDY_ID} INTEGER,
			${StatisticData_timed.KEY_TIMESTAMP} INTEGER,
			${StatisticData_timed.KEY_OBSERVED_ID} INTEGER,
			${StatisticData_timed.KEY_SUM} REAL,
			${StatisticData_timed.KEY_COUNT} INTEGER,
			FOREIGN KEY(${StatisticData_timed.KEY_STUDY_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}) ON DELETE CASCADE,
			FOREIGN KEY(${StatisticData_timed.KEY_OBSERVED_ID}) REFERENCES ${ObservedVariable.TABLE}(${ObservedVariable.KEY_ID}) ON DELETE CASCADE)""")
			val c = db.select(
				"daily_statistics",
				arrayOf(
					"_id",
					"study_id",
					"observed_id",
					"day_timestamp",
					"variable_sum",
					"variable_count"
				),
				null, null,
				null,
				null,
				null,
				null
			)
			
			while(c.moveToNext()) {
				val values = db.getValueBox()
				values.putLong(StatisticData_timed.KEY_ID, c.getLong(0))
				values.putLong(StatisticData_timed.KEY_STUDY_ID, c.getLong(1))
				values.putLong(StatisticData_timed.KEY_OBSERVED_ID, c.getLong(2))
				values.putLong(StatisticData_timed.KEY_TIMESTAMP, c.getLong(3))
				values.putDouble(StatisticData_timed.KEY_SUM, c.getDouble(4))
				values.putInt(StatisticData_timed.KEY_COUNT, c.getInt(5))
				
				db.insert(StatisticData_timed.TABLE, values)
			}
			c.close()
		}
		if(oldVersion <= 19) {
			db.execSQL("ALTER TABLE ${Questionnaire.TABLE} ADD COLUMN ${Questionnaire.KEY_SUMSCORES} TEXT;")
		}
		if(oldVersion <= 20) {
			db.execSQL("ALTER TABLE ${DbUser.TABLE} ADD COLUMN ${DbUser.KEY_IS_DEV} INTEGER;")
			db.execSQL("ALTER TABLE ${DbUser.TABLE} ADD COLUMN ${DbUser.KEY_WAS_DEV} INTEGER;")
		}
		if(oldVersion <= 21) { //4.9.20 - update group.inputs[][] to group.page[]
			db.execSQL("ALTER TABLE groups ADD COLUMN pages TEXT;")
			
			val c = db.select(
				"groups",
				arrayOf("_id", "inputs_randomized", "inputs"),
				null,
				null,
				null,
				null,
				null,
				null
			)
			while(c.moveToNext()) {
				val id = c.getString(0)
				val randomized = if(c.getInt(1) == 1) "true" else "false"
				val inputJson = c.getString(2)
				
				val jsonInputs: JsonArray
				try {
					jsonInputs = DbLogic.getJsonConfig().decodeFromString<JsonObject>(inputJson).jsonArray
				}
				catch(e: Exception) {
					continue
				}
				
				val pageString = StringBuilder("[")
				var addComma = false
				for(jsonInput in jsonInputs) {
					if(addComma)
						pageString.append(",")
					pageString.append("{\"randomized\":$randomized, \"inputs\": $jsonInput}")
					addComma = true
				}
				pageString.append("]")
				
				val values = db.getValueBox()
				values.putString("pages", pageString.toString())
				db.update("groups", values, "_id = ?", arrayOf(id))
			}
			c.close()
		}
		if(oldVersion <= 22) {
			db.execSQL("ALTER TABLE user ADD COLUMN notifications_missed INTEGER DEFAULT 0;")
		}
		if(oldVersion <= 24) {
			db.execSQL("ALTER TABLE groups ADD COLUMN publishedAndroid INTEGER DEFAULT 1;")
			db.execSQL("ALTER TABLE groups ADD COLUMN publishedIOS INTEGER DEFAULT 1;")
			db.execSQL("ALTER TABLE groups ADD COLUMN durationStartingAfterDays INTEGER;")
		}
		if(oldVersion <= 25) {
			var c = db.select(
				"groups",
				arrayOf("_id", "pages"),
				null,
				null,
				null,
				null,
				null,
				null
			)
			while(c.moveToNext()) {
				val id = c.getString(0)
				var pages = c.getString(1)

				pages = pages.replace("\"number_has_decimal\":", "\"numberHasDecimal\":")

				val values = db.getValueBox()
				values.putString("pages", pages)
				db.update("groups", values, "_id = ?", arrayOf(id))
			}
			c.close()



			c = db.select(
				"studies",
				arrayOf("_id", "public_charts_json", "personal_charts_json"),
				null,
				null,
				null,
				null,
				null,
				null
			)
			while(c.moveToNext()) {
				val id = c.getString(0)
				var publicCharts = c.getString(1)
				var personalCharts = c.getString(2)

				//ChartInfo
				publicCharts = publicCharts.replace("\"description\":", "\"chartDescription\":")
				personalCharts = personalCharts.replace("\"description\":", "\"chartDescription\":")
				publicCharts = publicCharts.replace("\"display_publicVariable\":", "\"displayPublicVariable\":")
				personalCharts = personalCharts.replace("\"display_publicVariable\":", "\"displayPublicVariable\":")
				publicCharts = publicCharts.replace("\"hide_until_completion\":", "\"hideUntilCompletion\":")
				personalCharts = personalCharts.replace("\"hide_until_completion\":", "\"hideUntilCompletion\":")
				publicCharts = publicCharts.replace("\"in_percent\":", "\"inPercent\":")
				personalCharts = personalCharts.replace("\"in_percent\":", "\"inPercent\":")
				publicCharts = publicCharts.replace("\"publicVariable\":", "\"publicVariables\":")
				personalCharts = personalCharts.replace("\"publicVariable\":", "\"publicVariables\":")

				//ChartInfo.AxisData
				publicCharts = publicCharts.replace("\"observed_variable_index\":", "\"observedVariableIndex\":")
				personalCharts = personalCharts.replace("\"observed_variable_index\":", "\"observedVariableIndex\":")

				val values = db.getValueBox()
				values.putString("public_charts_json", publicCharts)
				values.putString("personal_charts_json", personalCharts)
				db.update("studies", values, "_id = ?", arrayOf(id))
			}
			c.close()
		}
		
		if(oldVersion <= 26) {
			db.execSQL("ALTER TABLE dataSets ADD COLUMN group_internal_id INTEGER;")
			db.execSQL("ALTER TABLE groups ADD COLUMN internal_id INTEGER;")
		}
		
		if(oldVersion <= 27) {
			db.execSQL("ALTER TABLE groups ADD COLUMN completableOnce INTEGER;")
			db.execSQL("ALTER TABLE groups ADD COLUMN completableOncePerNotification INTEGER;")
			db.execSQL("ALTER TABLE groups ADD COLUMN completableMinutesAfterNotification INTEGER;")
			db.execSQL("ALTER TABLE groups ADD COLUMN limitCompletionFrequency INTEGER;")
			db.execSQL("ALTER TABLE groups ADD COLUMN completionFrequencyMinutes INTEGER;")
			db.execSQL("ALTER TABLE groups ADD COLUMN completableAtSpecificTime INTEGER;")
			db.execSQL("ALTER TABLE groups ADD COLUMN completableAtSpecificTimeStart INTEGER;")
			db.execSQL("ALTER TABLE groups ADD COLUMN completableAtSpecificTimeEnd INTEGER;")


			val c = db.select(
				"groups",
				arrayOf("_id", "complete_repeat_type", "timeConstraint_type", "timeConstraint_period", "timeConstraint_start", "timeConstraint_end", "complete_repeat_minutes"),
				null,
				null,
				null,
				null,
				null,
				null
			)
			while(c.moveToNext()) {
				val id = c.getString(0)
				val completeRepeatType = c.getInt(1)
				val timeConstraintType = c.getInt(2)
				val timeConstraintPeriodMinutes = c.getInt(3)
				val timeConstraintStart = c.getInt(4)
				val timeConstraintEnd = c.getInt(5)
				val completeRepeatMinutes = c.getInt(6)

				var completableOnce = false
				var completableOncePerNotification = false
				var limitCompletionFrequency = false
				var completableAtSpecificTime = false

				val values = db.getValueBox()
				when(completeRepeatType) {
					1 -> //COMPLETE_REPEAT_TYPE_NO_REPEAT
						completableOnce = true
					2 -> //COMPLETE_REPEAT_TYPE_ONCE_PER_NOTIFICATION
						completableOncePerNotification = true
					3 -> //COMPLETE_REPEAT_TYPE_MINUTES
						limitCompletionFrequency = true
				}
				when(timeConstraintType) {
					1 -> //TIME_CONSTRAINT_TYPE_TIMEPERIOD
						completableAtSpecificTime = true
					2 -> //TIME_CONSTRAINT_TYPE_AFTER_NOTIFICATION
						completableOncePerNotification = true
				}

				values.putBoolean("completableOnce", completableOnce)
				values.putBoolean("completableOncePerNotification", completableOncePerNotification)
				values.putBoolean("limitCompletionFrequency", limitCompletionFrequency)
				values.putBoolean("completableAtSpecificTime", completableAtSpecificTime)

				values.putInt("completableMinutesAfterNotification", timeConstraintPeriodMinutes)
				values.putInt("completableAtSpecificTimeStart", timeConstraintStart)
				values.putInt("completableAtSpecificTimeEnd", timeConstraintEnd)
				values.putInt("completionFrequencyMinutes", completeRepeatMinutes)
				db.update("groups", values, "_id = ?", arrayOf(id))
			}
			c.close()
		}
		
		
		if(oldVersion <= 28) {
			db.execSQL("""CREATE TABLE IF NOT EXISTS ${StudyToken.TABLE} (
				${StudyToken.KEY_STUDY_ID} INTEGER PRIMARY KEY,
				${StudyToken.KEY_TOKEN} INTEGER)""")
		}
		if(oldVersion <= 29) {
			db.execSQL("ALTER TABLE eventTriggers ADD COLUMN specific_group_internalId INTEGER;")
			
			val c = db.select(
				"eventTriggers LEFT JOIN groups ON eventTriggers.specific_group_id = groups._id",
				arrayOf("eventTriggers._id", "groups._id", "groups.internal_id"),
				"eventTriggers.specific_group_id IS NOT NULL",
				null,
				null,
				null,
				null,
				null
			)
			var groupIdCounter = 500L
			
			while(c.moveToNext()) {
				val eventId = c.getLong(0)
				val groupId = c.getLong(1)
				var groupInternalId = c.getLong(2)
				
				
				if(groupInternalId == -1L) {
					groupInternalId = ++groupIdCounter
					val groupValues = db.getValueBox()
					groupValues.putLong("internal_id", groupInternalId)
					db.update("groups", groupValues, "_id = ?", arrayOf(groupId.toString()))
				}
				
				val eventValues = db.getValueBox()
				eventValues.putLong("specific_group_internalId", groupInternalId)
				
				db.update("eventTriggers", eventValues, "_id = ?", arrayOf(eventId.toString()))
			}
		}
		if(oldVersion <= 30) {
			db.execSQL("ALTER TABLE studies ADD COLUMN sendMessagesAllowed INTEGER DEFAULT 0;")
			
			db.execSQL("""CREATE TABLE IF NOT EXISTS ${Message.TABLE} (
			${Message.KEY_ID} INTEGER PRIMARY KEY,
			${Message.KEY_STUDY_ID} INTEGER,
			${Message.KEY_SENT} INTEGER,
			${Message.KEY_CONTENT} TEXT,
			${Message.KEY_IS_NEW} INTEGER,
			${Message.KEY_FROM_SERVER} INTEGER,
			FOREIGN KEY(${Message.KEY_STUDY_ID}) REFERENCES ${Study.TABLE} (${Study.KEY_ID}))""")
		}
		if(oldVersion <= 31) {
			db.execSQL("ALTER TABLE studies ADD COLUMN uploadSettingsString TEXT DEFAULT '{}';")
		}
		if(oldVersion <= 32) {
			db.execSQL("ALTER TABLE studies ADD COLUMN study_lang TEXT DEFAULT '';")
			db.execSQL("ALTER TABLE dataSets ADD COLUMN study_lang TEXT DEFAULT '';")
			db.execSQL("ALTER TABLE user ADD COLUMN app_lang;")
			
			val values = db.getValueBox()
			try {
				values.putString(DbUser.KEY_APP_LANG, NativeLink.smartphoneData.lang)
			}
			catch(e : Throwable) {
				// NativeLink.smartphoneData probably is not ready yet
				println("Could not detect language??")
				e.printStackTrace()
				values.putString(DbUser.KEY_APP_LANG, "")
			}
			db.update(DbUser.TABLE, values, null, null)
		}
		if(oldVersion <= 33) {
			db.execSQL("""CREATE TABLE IF NOT EXISTS fileUploads (
			_id INTEGER PRIMARY KEY,
			study_id INTEGER,
			study_webId INTEGER,
			server_url TEXT,
			isTemporary INTEGER,
			filePath TEXT,
			identifier INTEGER,
			uploadType INTEGER,
			FOREIGN KEY(study_id) REFERENCES studies(_id))""")
		}
		if(oldVersion <= 34) {
			db.execSQL("ALTER TABLE studies ADD COLUMN joinedTimestamp INTEGER DEFAULT 0;")
			
			val c = db.select(
				"studies",
				arrayOf("_id", "strftime('%s', joined)"),
				null,
				null,
				null,
				null,
				null,
				null
			)
			
			while(c.moveToNext()) {
				val id = c.getLong(0)
				val joined = c.getLong(1)*1000
				
				val values = db.getValueBox()
				values.putLong("joinedTimestamp", joined)
				
				db.update("studies", values, "_id = ?", arrayOf(id.toString()))
			}
		}
		if(oldVersion <= 35) {
			db.beginTransaction()
			db.execSQL("ALTER TABLE groups RENAME TO questionnaires;")
			
			db.execSQL("ALTER TABLE action_trigger ADD COLUMN questionnaire_id INTEGER;")
			db.execSQL("UPDATE action_trigger SET questionnaire_id = group_id;")
			
			db.execSQL("ALTER TABLE eventTriggers ADD COLUMN questionnaire_id INTEGER;")
			db.execSQL("UPDATE eventTriggers SET questionnaire_id = group_id;")
			
			db.execSQL("ALTER TABLE eventTriggers ADD COLUMN skip_this_questionnaire INTEGER;")
			db.execSQL("UPDATE eventTriggers SET skip_this_questionnaire = skip_this_group;")
			
			db.execSQL("ALTER TABLE eventTriggers ADD COLUMN specific_questionnaire_internalId INTEGER DEFAULT NULL;")
			db.execSQL("UPDATE eventTriggers SET specific_questionnaire_internalId = specific_group_internalId;")
			
			db.execSQL("ALTER TABLE schedules ADD COLUMN questionnaire_id INTEGER;")
			db.execSQL("UPDATE schedules SET questionnaire_id = group_id;")
			
			db.execSQL("ALTER TABLE signalTimes ADD COLUMN questionnaire_id INTEGER;")
			db.execSQL("UPDATE signalTimes SET questionnaire_id = group_id;")
			
			db.execSQL("ALTER TABLE alarms ADD COLUMN questionnaire_id INTEGER;")
			db.execSQL("UPDATE alarms SET questionnaire_id = group_id;")
			
			db.execSQL("ALTER TABLE dataSets ADD COLUMN questionnaire_internal_id INTEGER;")
			db.execSQL("UPDATE dataSets SET questionnaire_internal_id = group_internal_id;")
			
			db.execSQL("ALTER TABLE dynamicInput_store ADD COLUMN questionnaire_id INTEGER;")
			db.execSQL("UPDATE dynamicInput_store SET questionnaire_id = group_id;")
			
			db.execSQL("ALTER TABLE studies ADD COLUMN randomGroup INTEGER DEFAULT 0;")
			db.execSQL("ALTER TABLE questionnaires ADD COLUMN limitToGroup INTEGER DEFAULT 0;")
			db.execSQL("ALTER TABLE dataSets ADD COLUMN study_group INTEGER DEFAULT 0;")
			db.setTransactionSuccessful()
			db.endTransaction()
		}
		if(oldVersion <= 36) {
			db.execSQL("ALTER TABLE studies ADD COLUMN enableRewardSystem INTEGER DEFAULT 0;")
			db.execSQL("ALTER TABLE studies ADD COLUMN rewardVisibleAfterDays INTEGER;")
			db.execSQL("ALTER TABLE studies ADD COLUMN rewardEmailContent TEXT DEFAULT '';")
			db.execSQL("ALTER TABLE studies ADD COLUMN rewardInstructions TEXT DEFAULT '';")
			db.execSQL("ALTER TABLE studies ADD COLUMN cachedRewardCode TEXT DEFAULT '';")
			db.execSQL("ALTER TABLE questionnaires ADD COLUMN minDataSetsForReward INTEGER;")
		}
		if(oldVersion <= 37) {
			db.execSQL("ALTER TABLE fileUploads ADD COLUMN creationTimestamp INTEGER;")
		}
		if(oldVersion <= 38) {
			db.execSQL("""CREATE TABLE IF NOT EXISTS questionnaireCache (
			_id INTEGER,
			questionnaireId INTEGER,
			inputName TEXT,
			backupFrom LONG,
			cacheValue TEXT,
			FOREIGN KEY(questionnaireId) REFERENCES questionnaire(_id))""")
			db.execSQL("ALTER TABLE user ADD COLUMN current_study INTEGER DEFAULT 0;")
			db.execSQL("ALTER TABLE studies ADD COLUMN quitTimestamp INTEGER DEFAULT 0;")
		}
		if(oldVersion <= 39) {
			db.execSQL("ALTER TABLE dataSets ADD COLUMN server_version INTEGER;")
			db.execSQL("UPDATE dataSets SET questionnaire_name = group_name;")

			db.execSQL("ALTER TABLE fileUploads ADD COLUMN is_synced INTEGER DEFAULT 0;")
			db.execSQL("ALTER TABLE fileUploads ADD COLUMN server_version INTEGER;")

			db.execSQL("ALTER TABLE studies ADD COLUMN serverVersion INTEGER DEFAULT 0;")
		}
		if(oldVersion <= 40) {
			db.execSQL("ALTER TABLE questionnaires ADD COLUMN isBackEnabled INTEGER;")
		}
		if(oldVersion <= 41) {
			db.execSQL("""CREATE TABLE IF NOT EXISTS statistics_perData (
			_id INTEGER PRIMARY KEY,
			study_id INTEGER,
			observed_id INTEGER,
			variable_index INTEGER,
			variable_value INTEGER,
			FOREIGN KEY(study_id) REFERENCES studies(_id) ON DELETE CASCADE,
			FOREIGN KEY(observed_id) REFERENCES observed_variables(_id) ON DELETE CASCADE)""")
		}
		if(oldVersion <= 42) {
			db.execSQL("ALTER TABLE studies ADD COLUMN faultyAccessKey INTEGER DEFAULT 0;")
		}
		if(oldVersion <= 45) {
			try {
				db.execSQL("ALTER TABLE questionnaires ADD COLUMN scriptEndBlock TEXT DEFAULT '';")
			} catch (_: Throwable) {}
			try {
				db.execSQL("ALTER TABLE questionnaires ADD COLUMN virtualInputs TEXT DEFAULT '[]';")
			} catch (_: Throwable) {}
			db.execSQL("DROP TABLE IF EXISTS merlinCache;")
			db.execSQL("DROP TABLE IF EXISTS merlinLogs;")
			db.execSQL("""CREATE TABLE IF NOT EXISTS merlinCache (
			studyId INTEGER,
			globalsString TEXT,
			FOREIGN KEY(studyId) REFERENCES studies(_id) ON DELETE CASCADE)""")
			db.execSQL("""CREATE TABLE IF NOT EXISTS merlinLogs (
			_id INTEGER PRIMARY KEY,
			study_id INTEGER,
			study_webId INTEGER,
			server_url TEXT,
			server_version INTEGER,
			questionnaire_name TEXT,
			time_ms INTEGER,
			log_type INTEGER,
			msg TEXT,
			is_synced INTEGER,
			FOREIGN KEY(study_id) REFERENCES studies(_id))""")

			Web.updateStudiesAsync(true)
		}
		if(oldVersion <= 46) {
			db.execSQL("ALTER TABLE merlinLogs ADD COLUMN context TEXT DEFAULT '';")
		}
		if(oldVersion <= 47) {
			db.execSQL("ALTER TABLE questionnaires ADD COLUMN showInDisabledList INTEGER DEFAULT 1;")
		}
		if(oldVersion <= 48) {
			db.execSQL("""CREATE TABLE IF NOT EXISTS questionnaire_metadata (
			_id INTEGER PRIMARY KEY,
			study_id INTEGER,
			questionnaire_id INTEGER,
			times_completed INTEGER,
			last_completed INTEGER,
			last_notification INTEGER,
			FOREIGN KEY(study_id) REFERENCES studies(_id) ON DELETE CASCADE)""")

			val c = db.select(
				"questionnaires",
				arrayOf("study_id", "internal_id", "last_notification", "last_completed"),
				null,
				null,
				null,
				null,
				null,
				null
			)

			while(c.moveToNext()) {
				val studyId = c.getLong(0)
				val internalId = c.getLong(1)
				val lastNotification = c.getLong(2)
				val lastCompleted = c.getLong(3)

				val values = db.getValueBox()
				values.putLong("study_id", studyId)
				values.putLong("questionnaire_id", internalId)
				values.putInt("times_completed", if(lastCompleted == 0L) 0 else 1)
				values.putLong("last_completed", lastCompleted)
				values.putLong("last_notification", lastNotification)
				db.insert("questionnaire_metadata", values)
			}

			//db.execSQL("ALTER TABLE questionnaires DROP COLUMN last_notification;")
			//db.execSQL("ALTER TABLE questionnaires DROP COLUMN last_completed;")

			db.execSQL("ALTER TABLE dataSets ADD COLUMN timezone_offset INTEGER DEFAULT 0;")
			db.execSQL("ALTER TABLE dataSets ADD COLUMN local_datetime TEXT DEFAULT '';")
		}
	}
	
	fun updateStudy(study: Study): Study {
		val v = study.serverVersion
		ErrorBox.log("Updater", "Update from version $v")
		val hasJson = study.json.isNotEmpty()
		var newStudy = study
		
		if(v <= 1 && hasJson) { //4.9.20 - update group.inputs[][] to group.page[]
			val jsonStudy = DbLogic.getJsonConfig().decodeFromString<JsonObject>(newStudy.json)
			val jsonGroups = jsonStudy.getValue("groups").jsonArray
			for((i, jsonGroup) in jsonGroups.withIndex()) {
				val jsonGroupObj = jsonGroup.jsonObject
				val jsonInputs = jsonGroupObj.getValue("inputs").jsonArray
				val randomized = if(jsonGroupObj.getValue("inputsRandomized").jsonPrimitive.boolean) "true" else "false"

				val pageString = StringBuilder("[")
				var addComma = false
				for(jsonInput in jsonInputs) {
					if(addComma)
						pageString.append(",")
					pageString.append("{\"randomized\":$randomized, \"inputs\": $jsonInput}")
					addComma = true
				}
				pageString.append("]")
				newStudy.questionnaires[i].pagesString = pageString.toString()
			}
		}
		
		if(v <= 2 && hasJson) {
			var json = newStudy.json
			
			//Group
			json = json.replace("\"complete_repeat_type\":", "\"completeRepeatType\":")
			json = json.replace("\"complete_repeat_minutes\":", "\"completeRepeatMinutes\":")
			json = json.replace("\"period\":", "\"durationPeriodDays\":")
			json = json.replace("\"startDate\":", "\"durationStart\":")
			json = json.replace("\"endDate\":", "\"durationEnd\":")
			json = json.replace("\"timeConstraint_type\":", "\"timeConstraintType\":")
			json = json.replace("\"timeConstraint_start\":", "\"timeConstraintStart\":")
			json = json.replace("\"timeConstraint_end\":", "\"timeConstraintEnd\":")
			json = json.replace("\"timeConstraint_period\":", "\"timeConstraintPeriodMinutes\":")
			
			//Input
			json = json.replace("\"number_has_decimal\":", "\"numberHasDecimal\":")
			
			//ActionTrigger
			json = json.replace("\"cues\":", "\"eventTriggers\":")
			
			//EventTrigger
			json = json.replace("\"cue_code\":", "\"cueCode\":")
			json = json.replace("\"random_delay\":", "\"randomDelay\":")
			json = json.replace("\"delay\":", "\"delaySec\":")
			json = json.replace("\"delay_min\":", "\"delayMinimumSec\":")
			json = json.replace("\"skip_this_group\":", "\"skipThisGroup\":")
			json = json.replace("\"specific_group\":", "\"specificGroupEnabled\":")
			json = json.replace("\"specific_group_index\":", "\"specificGroupIndex\":")
			
			//Schedule
			json = json.replace("\"repeatRate\":", "\"dailyRepeatRate\":")
			json = json.replace("\"skip_first_in_loop\":", "\"skipFirstInLoop\":")
			
			//SignalTime
			json = json.replace("\"is_random\":", "\"random\":")
			json = json.replace("\"is_random_fixed\":", "\"randomFixed\":")
			json = json.replace("\"random_frequency\":", "\"frequency\":")
			json = json.replace("\"random_minutes_between\":", "\"minutesBetween\":")
			json = json.replace("\"startTime_of_day\":", "\"startTimeOfDay\":")
			json = json.replace("\"endTime_of_day\":", "\"endTimeOfDay\":")
			
			//Statistics
			json = json.replace("\"observed_variables\":", "\"observedVariables\":")
			
			//ChartInfo
			json = json.replace("\"description\":", "\"chartDescription\":") //will also replace Study.description
			json = json.replace("\"display_publicVariable\":", "\"displayPublicVariable\":")
			json = json.replace("\"hide_until_completion\":", "\"hideUntilCompletion\":")
			json = json.replace("\"in_percent\":", "\"inPercent\":")
			json = json.replace("\"publicVariable\":", "\"publicVariables\":")
			
			//ChartInfo.AxisData
			json = json.replace("\"observed_variable_index\":", "\"observedVariableIndex\":")
			
			
			
			newStudy = Study.newInstance(study.serverUrl, study.accessKey, json, false)

			val jsonStudy = DbLogic.getJsonConfig().decodeFromString<JsonObject>(json)
			if(jsonStudy.contains("chartDescription")) {
				newStudy.studyDescription = jsonStudy.getValue("chartDescription").jsonPrimitive.content
			}
			else if(jsonStudy.contains("description")) {
				newStudy.studyDescription = jsonStudy.getValue("description").jsonPrimitive.content
			}
		}

		if(v <= 3 && hasJson) { //remove completeRepeatType and timeConstraintType
			val jsonStudy = DbLogic.getJsonConfig().decodeFromString<JsonObject>(newStudy.json)
			val jsonGroups = jsonStudy.getValue("groups").jsonArray
			val groups = newStudy.questionnaires
			
			for((i, jsonGroup) in jsonGroups.withIndex()) {
				val group = groups[i]
				val jsonGroupObj = jsonGroup.jsonObject
				if(jsonGroupObj.containsKey("completeRepeatType")) {
					when(jsonGroupObj.getValue("completeRepeatType").jsonPrimitive.int) {
						1 -> //COMPLETE_REPEAT_TYPE_NO_REPEAT
							group.completableOnce = true
						2 -> //COMPLETE_REPEAT_TYPE_ONCE_PER_NOTIFICATION
							group.completableOncePerNotification = true
						3 -> //COMPLETE_REPEAT_TYPE_MINUTES
							group.limitCompletionFrequency = true
					}
				}
				if(jsonGroupObj.containsKey("timeConstraintType")) {
					when(jsonGroupObj.getValue("timeConstraintType").jsonPrimitive.int) {
						1 -> //TIME_CONSTRAINT_TYPE_TIMEPERIOD
							group.completableAtSpecificTime = true
						2 -> //TIME_CONSTRAINT_TYPE_AFTER_NOTIFICATION
							group.completableOncePerNotification = true
					}
				}
				
				if(jsonGroupObj.containsKey("timeConstraintPeriodMinutes"))
					group.completableMinutesAfterNotification = jsonGroupObj.getValue("timeConstraintPeriodMinutes").jsonPrimitive.int
				if(jsonGroupObj.containsKey("timeConstraintStart"))
					group.completableAtSpecificTimeStart = jsonGroupObj.getValue("timeConstraintStart").jsonPrimitive.int
				if(jsonGroupObj.containsKey("timeConstraintEnd"))
					group.completableAtSpecificTimeEnd = jsonGroupObj.getValue("timeConstraintEnd").jsonPrimitive.int
				if(jsonGroupObj.containsKey("completeRepeatMinutes"))
					group.completionFrequencyMinutes = jsonGroupObj.getValue("completeRepeatMinutes").jsonPrimitive.int
					
			}
		}

		if(v <= 4 && hasJson) { //replace specificGroupIndex with specificGroupInternalId
			val jsonStudy = DbLogic.getJsonConfig().decodeFromString<JsonObject>(newStudy.json)
			val jsonGroups = jsonStudy.getValue("groups").jsonArray
			val groups = newStudy.questionnaires

			var groupIdCounter = 100L

			for((iGroup, jsonGroup) in jsonGroups.withIndex()) {
				val jsonGroupObj = jsonGroup.jsonObject
				val actionTriggers = groups[iGroup].actionTriggers
				
				val jsonActionTriggers = jsonGroupObj.getValue("actionTriggers").jsonArray
				for((iActionTrigger, jsonActionTrigger) in jsonActionTriggers.withIndex()) {
					val jsonActionTriggerObj = jsonActionTrigger.jsonObject
					val eventTriggers = actionTriggers[iActionTrigger].eventTriggers
					
					if(jsonActionTriggerObj.containsKey("eventTriggers")) {
						val jsonEventTriggers = jsonActionTriggerObj.getValue("eventTriggers").jsonArray
						for((iEventTrigger, jsonEventTrigger) in jsonEventTriggers.withIndex()) {
							val jsonEventTriggerObj = jsonEventTrigger.jsonObject
							val eventTrigger = eventTriggers[iEventTrigger]
							
							if(jsonEventTriggerObj.containsKey("specificGroupEnabled") && jsonEventTriggerObj.containsKey("specificGroupIndex")) {
								val specificGroupEnabled = jsonEventTriggerObj.getValue("specificGroupEnabled").jsonPrimitive.boolean
								val specificGroupIndex = jsonEventTriggerObj.getValue("specificGroupIndex").jsonPrimitive.int
								
								if(specificGroupEnabled && specificGroupIndex < groups.size) {
									val targetGroup = groups[specificGroupIndex]
									
									if(targetGroup.internalId == -1L)
										targetGroup.internalId = ++groupIdCounter
									
									eventTrigger.specificQuestionnaireInternalId = targetGroup.internalId
								}
							}
						}
					}
				}
			}
		}
		
		if(v <= 5) {
			newStudy.sendMessagesAllowed = false
		}
		
		if(v <= 10 && hasJson) {
			var json = study.json
			
			json = json.replace("\"skipThisGroup\":", "\"skipThisQuestionnaire\":")
			json = json.replace("\"specificGroupInternalId\":", "\"specificQuestionnaireInternalId\":")
			
			newStudy = Study.newInstance(study.serverUrl, study.accessKey, json, false)
			
			
			
			val jsonStudy = DbLogic.getJsonConfig().decodeFromString<JsonObject>(newStudy.json)
			if(jsonStudy.containsKey("groups")) {
				val jsonGroups = jsonStudy.getValue("groups").jsonArray
				
				for((i, jsonGroup) in jsonGroups.withIndex()) {
					val jsonGroupObj = jsonGroup.jsonObject
					newStudy.questionnaires[i].title = jsonGroupObj["name"]?.jsonPrimitive?.content ?: newStudy.questionnaires[i].title
				}
			}
		}
		
		return newStudy
	}
}