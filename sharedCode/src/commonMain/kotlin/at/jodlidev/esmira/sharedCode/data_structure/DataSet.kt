package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import kotlinx.serialization.*
import kotlinx.serialization.json.*


/**
 * Created by JodliDev on 17.04.2019.
 */
@Serializable
class DataSet {
	enum class STATES {
		NOT_SYNCED,
		SYNCED,
		NOT_SYNCED_ERROR,
		NOT_SYNCED_SERVER_ERROR
	}
	@SerialName("dataSetId") var id: Long = 0
	@SerialName("studyId") private var studyWebId: Long = 0
	
	val eventType: String
	private val studyVersion: Int
	private val studySubVersion: Int
	private val studyLang: String
	private val group: Int
	private val accessKey: String
	val questionnaireName: String
	private val questionnaireInternalId: Long
	private val timezone: String
	var responseTime: Long = 0
	@SerialName("responses") private var responseTemp: MutableMap<String, JsonElement> = HashMap() //not in db. Just used to fill data and then create a response-string
	
	@Transient private var questionnaireId: Long = -1 //not in db
	@Transient internal var studyId: Long = 0
	@Transient lateinit var serverUrl: String
	private var token = 0L //joined from StudyToken
	@Transient private var _synced = STATES.NOT_SYNCED
	var synced: STATES //this value will be updated in db immediately
		get() {
			return _synced
		}
		set(v) {
			_synced = v
			reupload = _synced == STATES.NOT_SYNCED_SERVER_ERROR
			if(id != 0L) {
				val db = NativeLink.sql
				val values = db.getValueBox()
				values.putInt(KEY_SYNCED, _synced.ordinal)
				db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
			}
		}
	var reupload = false //not in db. Mainly for server. Is true when synced == STATES.NOT_SYNCED_SERVER_ERROR
	
	internal constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		studyId = c.getLong(1)
		studyWebId = c.getLong(2)
		serverUrl = c.getString(3)
		accessKey = c.getString(4)
		questionnaireName = c.getString(5)
		questionnaireInternalId = c.getLong(6)
		studyVersion = c.getInt(7)
		studySubVersion = c.getInt(8)
		studyLang = c.getString(9)
		group = c.getInt(10)
		timezone = c.getString(11)
		responseTime = c.getLong(12)
		eventType = c.getString(13)
		setResponses(c.getString(14))
		_synced = STATES.values()[c.getInt(15)]
		token = c.getLong(16)
		reupload = _synced == STATES.NOT_SYNCED_SERVER_ERROR
	}
	
	
	constructor(
		eventType: String,
		study: Study,
		questionnaireName: String,
		questionnaireId: Long,
		questionnaireInternalId: Long
	) {
		this.questionnaireName = questionnaireName
		this.questionnaireId = questionnaireId
		this.questionnaireInternalId = questionnaireInternalId
		this.eventType = eventType
		this.studyId = study.id
		this.studyWebId = study.webId
		this.studyVersion = study.version
		this.studySubVersion = study.subVersion
		this.serverUrl = study.serverUrl
		this.accessKey = study.accessKey
		this.studyLang = study.lang
		this.group = study.group
		this.responseTime = NativeLink.getNowMillis()
		this.timezone = NativeLink.getTimezone()
	}
	
	constructor(eventType: String, study: Study): this(
		eventType = eventType,
		study = study,
		questionnaireName = "",
		questionnaireId = -1,
		questionnaireInternalId = -1
	)
	
	constructor(eventType: String, questionnaire: Questionnaire): this(
		eventType = eventType,
		study = DbLogic.getStudy(questionnaire.studyId)!!,
		questionnaireName = questionnaire.title,
		questionnaireId = questionnaire.id,
		questionnaireInternalId = questionnaire.internalId
	) {
		studyWebId = questionnaire.studyWebId //not needed because init() sets this too, but if study is not found, it will be helpful for the error report
	}
	
	fun setResponses(json: String) {
		if(json.isNotEmpty())
			responseTemp = DbLogic.getJsonConfig().decodeFromString<Map<String, JsonElement>>(json).toMutableMap()
	}
	
	fun addResponseData(key: String, value: Long) = addResponseData(key, value.toString())
	fun addResponseData(key: String, value: Int) = addResponseData(key, value.toString())
	fun addResponseData(key: String, value: Boolean) = addResponseData(key, value.toString())
	fun addResponseData(key: String, value: String) {
		responseTemp[key] = JsonPrimitive(value)
	}
	
	
	fun saveQuestionnaire(questionnaire: Questionnaire, formStarted: Long) {
		responseTime = NativeLink.getNowMillis()
		addResponseData("formDuration", responseTime - formStarted)
		addResponseData("lastInvitation", questionnaire.lastNotification)
		
		for(score in questionnaire.sumScores) { //needs to happen before we create statistics in case it is used for a statistic
			var sum = 0
			
			for(key in score.addList) {
				if(responseTemp.containsKey(key))
					try {
						sum += responseTemp[key]?.jsonPrimitive?.int ?: 0
					}
					catch(e: Exception) {
						ErrorBox.warn(
							"DataSet",
							"Response \"$key\" is not an Integer: ${responseTemp[key]}",
							e
						)
					}
			}
			for(key in score.subtractList) {
				if(responseTemp.containsKey(key))
					try {
						sum -= responseTemp[key]?.jsonPrimitive?.int ?: 0
					}
					catch(e: Exception) {
						ErrorBox.warn(
							"DataSet",
							"Response \"$key\" is not an Integer: ${responseTemp[key]}",
							e
						)
					}
			}
			addResponseData(score.name, sum)
		}
		
		for((key, _) in responseTemp) {
			for(observedVariable in DbLogic.getObservedVariables(studyId, key)) {
				observedVariable.createStatistic(responseTemp)
			}
		}
		
		save()
	}
	
	private fun save(study: Study? = DbLogic.getStudy(studyId)) {
		if(id != 0L)
			throw RuntimeException("Trying to save an already created DataSet")
		
		if(study?.isEventUploaded(eventType) != false) {
			addResponseData("osVersion", NativeLink.smartphoneData.osVersion)
			addResponseData("model", NativeLink.smartphoneData.model)
			addResponseData("manufacturer", NativeLink.smartphoneData.manufacturer)
			
			val responses = if(responseTemp.isNotEmpty()) DbLogic.getJsonConfig().encodeToString(responseTemp) else ""
			
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putLong(KEY_STUDY_ID, studyId)
			values.putLong(KEY_STUDY_WEB_ID, studyWebId)
			values.putString(KEY_ACCESS_KEY, accessKey)
			values.putString(KEY_SERVER_URL, serverUrl)
			values.putString(KEY_QUESTIONNAIRE_NAME, questionnaireName)
			values.putLong(KEY_QUESTIONNAIRE_INTERNAL_ID, questionnaireInternalId)
			values.putInt(KEY_STUDY_VERSION, studyVersion)
			values.putInt(KEY_STUDY_SUB_VERSION, studySubVersion)
			values.putString(KEY_STUDY_LANG, studyLang)
			values.putInt(KEY_STUDY_GROUP, group)
			values.putString(KEY_TIMEZONE, timezone)
			values.putLong(KEY_RESPONSE_TIME, responseTime)
			values.putString(KEY_TYPE, eventType)
			values.putString(KEY_RESPONSES, responses)
			values.putInt(KEY_SYNCED, _synced.ordinal)
			id = db.insert(TABLE, values)
			
			NativeLink.postponedActions.syncDataSets()
			ErrorBox.log(
				"Send DataSet",
				"Sending \"$eventType\" to $serverUrl($studyWebId) (Questionnaire: $questionnaireName)"
			)
		}
		else
			ErrorBox.log(
				"DataSet not sent",
				"Event \"$eventType\" is not logged"
			)
		DbLogic.triggerEventTrigger(studyId, eventType, questionnaireId)
	}
	
	fun delete() {
		NativeLink.sql.delete(TABLE, "$KEY_ID = ?", arrayOf(id.toString()))
	}
	
	companion object {
		const val TABLE = "dataSets"
		
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_STUDY_WEB_ID = "study_webid"
		const val KEY_SERVER_URL = "server_url"
		const val KEY_ACCESS_KEY = "accessKey"
		const val KEY_QUESTIONNAIRE_NAME = "group_name"
		const val KEY_QUESTIONNAIRE_INTERNAL_ID = "questionnaire_internal_id"
		const val KEY_STUDY_VERSION = "study_version"
		const val KEY_STUDY_SUB_VERSION = "study_subVersion"
		const val KEY_STUDY_LANG = "study_lang"
		const val KEY_STUDY_GROUP = "study_group"
		const val KEY_TIMEZONE = "timezone"
		const val KEY_RESPONSE_TIME = "response_time"
		const val KEY_TYPE = "event_type"
		const val KEY_RESPONSES = "responses"
		const val KEY_SYNCED = "is_synced"
		
		const val TABLE_JOINED = "$TABLE LEFT JOIN ${StudyToken.TABLE} ON $TABLE.$KEY_STUDY_ID=${StudyToken.TABLE}.${StudyToken.KEY_STUDY_ID}"
		
		const val TYPE_JOIN = "joined"
		const val TYPE_QUESTIONNAIRE = "questionnaire"
		const val TYPE_QUIT = "quit"
		const val TYPE_ALARM_EXECUTED = "actions_executed" //only on iOS
		const val TYPE_INVITATION = "invitation" //only on Android
		const val TYPE_INVITATION_MISSED = "invitation_missed"
		const val TYPE_INVITATION_REMINDER = "reminder" //only on Android
		const val TYPE_MSG = "message"
		const val TYPE_NOTIFICATION = "notification" //only on Android
		const val TYPE_REJOIN = "rejoined"
		const val TYPE_SCHEDULE_CHANGED = "schedule_changed"
		const val TYPE_STATISTIC_VIEWED = "statistic_viewed"
		const val TYPE_STUDY_MSG = "study_message"
		const val TYPE_STUDY_UPDATED = "study_updated"
		const val TYPE_REQUESTED_REWARD_CODE = "requested_reward_code"

		val COLUMNS = arrayOf(
			"$TABLE.$KEY_ID",
			"$TABLE.$KEY_STUDY_ID",
			"$TABLE.$KEY_STUDY_WEB_ID",
			"$TABLE.$KEY_SERVER_URL",
			"$TABLE.$KEY_ACCESS_KEY",
			"$TABLE.$KEY_QUESTIONNAIRE_NAME",
			"$TABLE.$KEY_QUESTIONNAIRE_INTERNAL_ID",
			"$TABLE.$KEY_STUDY_VERSION",
			"$TABLE.$KEY_STUDY_SUB_VERSION",
			"$TABLE.$KEY_STUDY_LANG",
			"$TABLE.$KEY_STUDY_GROUP",
			"$TABLE.$KEY_TIMEZONE",
			"$TABLE.$KEY_RESPONSE_TIME",
			"$TABLE.$KEY_TYPE",
			"$TABLE.$KEY_RESPONSES",
			"$TABLE.$KEY_SYNCED",
			"${StudyToken.TABLE}.${StudyToken.KEY_TOKEN}"
		)
		
		fun createShortDataSet(type: String, study: Study) {
			val dataSet = DataSet(type, study)
			dataSet.save(study)
		}
		
		fun createScheduleChangedDataSet(schedule: Schedule) {
			val dataSet = DataSet(TYPE_SCHEDULE_CHANGED, schedule.getQuestionnaire())
			dataSet.addResponseData("newSchedule", schedule.toDescString())
			dataSet.save()
		}
		
		fun createActionSentDataSet(type: String, questionnaire: Questionnaire, scheduledToTimestamp: Long) {
			val dataSet = DataSet(type, questionnaire)
			dataSet.addResponseData("actionScheduledTo", scheduledToTimestamp)
			dataSet.save()
		}
		
		fun createAlarmExecuted(questionnaire: Questionnaire?, studyId: Long, scheduledToTimestamp: Long) {
			if(questionnaire == null) {
				val study = DbLogic.getStudy(studyId) ?: return
				val dataSet = DataSet(TYPE_ALARM_EXECUTED, study)
				dataSet.addResponseData("actionScheduledTo", scheduledToTimestamp)
				dataSet.save(study)
			}
			else {
				val dataSet = DataSet(TYPE_ALARM_EXECUTED, questionnaire)
				dataSet.addResponseData("actionScheduledTo", scheduledToTimestamp)
				dataSet.save()
			}

		}
	}
}