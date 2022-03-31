package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.*
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perValue
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_timed
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

/**
 * Created by JodliDev on 08.09.2020.
 */
internal object Updater {
	const val EXPECTED_SERVER_VERSION: Int = 10
	const val DATABASE_VERSION = 34
	const val LIBRARY_VERSION = 17 //this is mainly used for iOS so we can check that changes in the library have been used in the C library
	
	fun updateSQL(db: SQLiteInterface, oldVersion: Int) {
		if(oldVersion >= DATABASE_VERSION)
			return
		println("Updating from $oldVersion to ${DATABASE_VERSION}")
		
		if(oldVersion <= 6) {
			db.execSQL("ALTER TABLE " + Study.TABLE + " ADD COLUMN " + Study.KEY_LOAD_PUBLIC_STATISTICS + " INTEGER;")
			db.execSQL("ALTER TABLE " + Study.TABLE + " ADD COLUMN " + Study.KEY_PUBLIC_CHARTS_JSON + " TEXT;")
			db.execSQL("ALTER TABLE " + Study.TABLE + " ADD COLUMN " + Study.KEY_PERSONAL_CHARTS_JSON + " TEXT;")
			db.execSQL("ALTER TABLE " + SignalTime.TABLE + " ADD COLUMN " + SignalTime.KEY_ORIGINAL_START_TIME_OF_DAY + " INTEGER DEFAULT 0;")
			db.execSQL("ALTER TABLE " + SignalTime.TABLE + " ADD COLUMN " + SignalTime.KEY_ORIGINAL_END_TIME_OF_DAY + " INTEGER DEFAULT 0;")
			db.execSQL("UPDATE " + SignalTime.TABLE + " SET " + SignalTime.KEY_ORIGINAL_START_TIME_OF_DAY + " = " + SignalTime.KEY_START_TIME_OF_DAY)
			db.execSQL("UPDATE " + SignalTime.TABLE + " SET " + SignalTime.KEY_ORIGINAL_END_TIME_OF_DAY + " = " + SignalTime.KEY_END_TIME_OF_DAY)
			db.execSQL("CREATE TABLE IF NOT EXISTS " + ObservedVariable.TABLE + " (" +
					ObservedVariable.KEY_ID + " INTEGER PRIMARY KEY, " +
					ObservedVariable.KEY_STUDY_ID + " INTEGER, " +
					ObservedVariable.KEY_VARIABLE_NAME + " TEXT, " +
					"FOREIGN KEY(" + ObservedVariable.KEY_STUDY_ID + ") REFERENCES " + Study.TABLE + "(" + Study.KEY_ID + "))")
			db.execSQL("CREATE TABLE IF NOT EXISTS daily_statistics (" +
					"_id INTEGER PRIMARY KEY, " +
					"study_id INTEGER, " +
					"observed_id INTEGER, " +
					"day_timestamp INTEGER, " +
					"variable_sum REAL, " +
					"variable_count INTEGER, " +
					"FOREIGN KEY(study_id) REFERENCES " + Study.TABLE + "(" + Study.KEY_ID + ") ON DELETE CASCADE," +
					"FOREIGN KEY(observed_id) REFERENCES " + ObservedVariable.TABLE + "(" + ObservedVariable.KEY_ID + ") ON DELETE CASCADE)")
		}
		if(oldVersion <= 7) { //changes in EventTrigger-table
			db.execSQL("DROP TABLE IF EXISTS " + EventTrigger.TABLE)
			db.execSQL("CREATE TABLE IF NOT EXISTS " + EventTrigger.TABLE + " (" +
					EventTrigger.KEY_ID + " INTEGER PRIMARY KEY, " +
					EventTrigger.KEY_ACTION_TRIGGER_ID + " INTEGER, " +
					EventTrigger.KEY_STUDY_ID + " INTEGER, " +
					EventTrigger.KEY_QUESTIONNAIRE_ID + " INTEGER, " +
					EventTrigger.KEY_LABEL + " TEXT, " +
					EventTrigger.KEY_CUE + " TEXT, " +
					EventTrigger.KEY_DELAY + " INTEGER, " +
					"FOREIGN KEY(" + EventTrigger.KEY_ACTION_TRIGGER_ID + ") REFERENCES " + ActionTrigger.TABLE + "(" + ActionTrigger.KEY_ID + ") ON DELETE CASCADE," +
					"FOREIGN KEY(" + EventTrigger.KEY_QUESTIONNAIRE_ID + ") REFERENCES " + Questionnaire.TABLE + "(" + Questionnaire.KEY_ID + ") ON DELETE CASCADE)")
		}
		if(oldVersion <= 8) { //changes in Schedule-change behavior and added KEY_SKIP_FIRST_IN_LOOP - 14.01.20
			db.execSQL("ALTER TABLE " + Schedule.TABLE + " ADD COLUMN " + Schedule.KEY_SKIP_FIRST_IN_LOOP + " INTEGER;")
		}
		if(oldVersion <= 9) { //adding conditions and frequency distribution to statistics - 12.02.20
			db.execSQL("ALTER TABLE " + ObservedVariable.TABLE + " ADD COLUMN " + ObservedVariable.KEY_INDEX + " INTEGER;")
			db.execSQL("ALTER TABLE " + ObservedVariable.TABLE + " ADD COLUMN " + ObservedVariable.KEY_CONDITIONS_JSON + " TEXT;")
			db.execSQL("ALTER TABLE " + ObservedVariable.TABLE + " ADD COLUMN " + ObservedVariable.KEY_CONDITION_TYPE + " INTEGER;")
			db.execSQL("ALTER TABLE " + ObservedVariable.TABLE + " ADD COLUMN " + ObservedVariable.KEY_STORAGE_TYPE + " INTEGER;")
			db.execSQL("CREATE TABLE IF NOT EXISTS " + StatisticData_perValue.TABLE + " (" +
					StatisticData_perValue.KEY_ID + " INTEGER PRIMARY KEY, " +
					StatisticData_perValue.KEY_STUDY_ID + " INTEGER, " +
					StatisticData_perValue.KEY_OBSERVED_ID + " INTEGER, " +
					StatisticData_perValue.KEY_VALUE + " TEXT, " +
					StatisticData_perValue.KEY_COUNT + " INTEGER, " +
					"FOREIGN KEY(" + StatisticData_perValue.KEY_STUDY_ID + ") REFERENCES " + Study.TABLE + "(" + Study.KEY_ID + ") ON DELETE CASCADE," +
					"FOREIGN KEY(" + StatisticData_perValue.KEY_OBSERVED_ID + ") REFERENCES " + ObservedVariable.TABLE + "(" + ObservedVariable.KEY_ID + ") ON DELETE CASCADE)")
		}
		if(oldVersion <= 10) { //adding dynamicText, event.random_delay - 17.02.20
			db.execSQL("CREATE TABLE IF NOT EXISTS " + DynamicInputData.TABLE + " (" +
					DynamicInputData.KEY_QUESTIONNAIRE_ID + " INTEGER, " +
					DynamicInputData.KEY_VARIABLE + " TEXT, " +
					DynamicInputData.KEY_ITEM_INDEX + " INTEGER, " +
					DynamicInputData.KEY_CREATED_TIME + " INTEGER, " +
					"FOREIGN KEY(${DynamicInputData.KEY_QUESTIONNAIRE_ID}) REFERENCES ${Study.TABLE}(${Study.KEY_ID}))")
			db.execSQL("ALTER TABLE ${EventTrigger.TABLE} ADD COLUMN ${EventTrigger.KEY_RANDOM_DELAY} INTEGER;")
			db.execSQL("ALTER TABLE ${EventTrigger.TABLE} ADD COLUMN ${EventTrigger.KEY_DELAY_MIN} INTEGER;")
			db.execSQL("ALTER TABLE ${EventTrigger.TABLE} ADD COLUMN ${EventTrigger.KEY_SKIP_THIS_QUESTIONNAIRE} INTEGER;")
		}
		if(oldVersion <= 11) {
			db.execSQL("ALTER TABLE ${Alarm.TABLE} ADD COLUMN ${Alarm.KEY_EVENT_TRIGGER_ID} INTEGER DEFAULT NULL;")
		}
		if(oldVersion <= 12) {
			db.execSQL("ALTER TABLE ${EventTrigger.TABLE} ADD COLUMN ${EventTrigger.KEY_SPECIFIC_QUESTIONNAIRE} INTEGER DEFAULT NULL;")
		}
		if(oldVersion <= 13) {
			db.execSQL("ALTER TABLE ${Alarm.TABLE} ADD COLUMN ${Alarm.KEY_TYPE} INTEGER;")
			db.execSQL("ALTER TABLE ${Alarm.TABLE} ADD COLUMN ${Alarm.KEY_REMINDER_COUNT} INTEGER;")
			db.execSQL("ALTER TABLE ${Alarm.TABLE} ADD COLUMN ${Alarm.KEY_LABEL} TEXT;")
			db.execSQL("ALTER TABLE ${Alarm.TABLE} ADD COLUMN ${Alarm.KEY_ONLY_SINGLE_ACTION_INDEX} INTEGER DEFAULT -1;")
		}
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
			db.execSQL("ALTER TABLE ${DbLogic.User.TABLE} ADD COLUMN ${DbLogic.User.KEY_IS_DEV} INTEGER;")
			db.execSQL("ALTER TABLE ${DbLogic.User.TABLE} ADD COLUMN ${DbLogic.User.KEY_WAS_DEV} INTEGER;")
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
				values.putString(DbLogic.User.KEY_APP_LANG, NativeLink.smartphoneData.lang)
			}
			catch(e : Throwable) {
				// NativeLink.smartphoneData probably is not ready yet
				println("Could not detect language??")
				e.printStackTrace()
				values.putString(DbLogic.User.KEY_APP_LANG, "")
			}
			db.update(DbLogic.User.TABLE, values, null, null)
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