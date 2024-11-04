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
class DataSet: UploadData {
	enum class EventTypes {
		schedule_planned,
		schedule_removed,
		joined,
		questionnaire,
		quit,
		actions_executed, 		//only on iOS
		invitation, 			//only on Android
		invitation_missed,
		reminder,				 //only on Android
		message,
		notification,			 //only on Android
		rejoined,
		schedule_changed,
		statistic_viewed,
		study_message,
		study_updated,
		requested_reward_code
	}
	@SerialName("dataSetId") override var id: Long = 0
	@SerialName("studyId") override val studyWebId: Long
	
	override val timestamp: Long
		get() = responseTime
	override val questionnaireName: String
	@Transient override var serverUrl: String = ""
	override val serverVersion: Int
	
	private val eventType: EventTypes
	override val type: String
		get() = eventType.toString()
	
	private val studyVersion: Int
	private val studySubVersion: Int
	private val studyLang: String
	private val group: Int
	private val accessKey: String
	private val questionnaireInternalId: Long
	private val timezone: String
	private var responseTime: Long = 0
	@SerialName("responses") private var responseTemp: MutableMap<String, JsonElement> = HashMap() //not in db. Just used to fill data and then create a response-string
	
	@Transient private var questionnaireId: Long = -1 //not in db
	@Transient override var studyId: Long = 0
	private var token = 0L //joined from StudyToken
	
	@Transient private var _synced = States.NOT_SYNCED
	override var synced: States //this value will be updated in db immediately
		get() {
			return _synced
		}
		set(v) {
			_synced = v
			reupload = _synced == States.NOT_SYNCED_ERROR_DELETABLE
			if(id != 0L) {
				val db = NativeLink.sql
				val values = db.getValueBox()
				values.putInt(KEY_SYNCED, _synced.ordinal)
				db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
			}
		}
	private var reupload = false //not in db. Mainly for server. Is true when synced == States.NOT_SYNCED_ERROR_DELETABLE
	
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
		serverVersion = c.getInt(9)
		studyLang = c.getString(10)
		group = c.getInt(11)
		timezone = c.getString(12)
		responseTime = c.getLong(13)
		eventType = EventTypes.valueOf(c.getString(14))
		setResponses(c.getString(15))
		_synced = States.values()[c.getInt(16)]
		token = c.getLong(17)
		reupload = _synced == States.NOT_SYNCED_ERROR_DELETABLE
	}
	
	
	constructor(
		eventType: EventTypes,
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
		this.serverVersion = study.serverVersion
		this.serverUrl = study.serverUrl
		this.accessKey = study.accessKey
		this.studyLang = study.lang
		this.group = study.group
		this.responseTime = NativeLink.getNowMillis()
		this.timezone = NativeLink.getTimezone()
	}
	
	constructor(eventType: EventTypes, study: Study): this(
		eventType = eventType,
		study = study,
		questionnaireName = "",
		questionnaireId = -1,
		questionnaireInternalId = -1
	)
	
	constructor(eventType: EventTypes, questionnaire: Questionnaire): this(
		eventType = eventType,
		study = DbLogic.getStudy(questionnaire.studyId)!!,
		questionnaireName = questionnaire.title,
		questionnaireId = questionnaire.id,
		questionnaireInternalId = questionnaire.internalId
	)
	
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
	
	
	fun saveQuestionnaire(questionnaire: Questionnaire) {
		responseTime = NativeLink.getNowMillis()
		val formStarted = QuestionnaireCache.getFormStarted(questionnaire.id)
		val pageTimestamps = QuestionnaireCache.getPageTimestamps(questionnaire.id)
		
		val pageDurations = ArrayList<Long>()
		val lastPageTimeStamp = pageTimestamps.fold(formStarted) {last, current ->
			pageDurations.add(current - last)
			current
		}
		pageDurations.add(responseTime - lastPageTimeStamp)
		
		addResponseData("formDuration", responseTime - formStarted)
		addResponseData("pageDurations", pageDurations.joinToString(","))
		addResponseData("lastInvitation", questionnaire.metadata.lastNotification)
		
		for(score in questionnaire.sumScores) { //needs to happen before we create statistics in case it is used for a statistic
			var sum = 0
			
			for(key in score.addList) {
				if(responseTemp.containsKey(key))
					sum += responseTemp[key]?.jsonPrimitive?.intOrNull ?: 0
			}
			for(key in score.subtractList) {
				if(responseTemp.containsKey(key))
					sum -= responseTemp[key]?.jsonPrimitive?.intOrNull ?: 0
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
			values.putInt(KEY_SERVER_VERSION, serverVersion)
			values.putString(KEY_STUDY_LANG, studyLang)
			values.putInt(KEY_STUDY_GROUP, group)
			values.putString(KEY_TIMEZONE, timezone)
			values.putLong(KEY_RESPONSE_TIME, responseTime)
			values.putString(KEY_TYPE, eventType.toString())
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
	
	override fun delete() {
		NativeLink.sql.delete(TABLE, "$KEY_ID = ?", arrayOf(id.toString()))
	}
	
	companion object {
		const val TABLE = "dataSets"
		
		const val KEY_ID = UploadData.KEY_ID
		const val KEY_STUDY_ID = UploadData.KEY_STUDY_ID
		const val KEY_STUDY_WEB_ID = UploadData.KEY_STUDY_WEB_ID
		const val KEY_SERVER_URL = UploadData.KEY_SERVER_URL
		const val KEY_ACCESS_KEY = "accessKey"
		const val KEY_QUESTIONNAIRE_NAME = "questionnaire_name"
		const val KEY_QUESTIONNAIRE_INTERNAL_ID = "questionnaire_internal_id"
		const val KEY_STUDY_VERSION = "study_version"
		const val KEY_STUDY_SUB_VERSION = "study_subVersion"
		const val KEY_SERVER_VERSION = UploadData.KEY_SERVER_VERSION
		const val KEY_STUDY_LANG = "study_lang"
		const val KEY_STUDY_GROUP = "study_group"
		const val KEY_TIMEZONE = "timezone"
		const val KEY_RESPONSE_TIME = "response_time"
		const val KEY_TYPE = "event_type"
		const val KEY_RESPONSES = "responses"
		const val KEY_SYNCED = UploadData.KEY_SYNCED
		
		const val TABLE_JOINED = "$TABLE LEFT JOIN ${StudyToken.TABLE} ON $TABLE.$KEY_STUDY_ID=${StudyToken.TABLE}.${StudyToken.KEY_STUDY_ID}"

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
			"$TABLE.$KEY_SERVER_VERSION",
			"$TABLE.$KEY_STUDY_LANG",
			"$TABLE.$KEY_STUDY_GROUP",
			"$TABLE.$KEY_TIMEZONE",
			"$TABLE.$KEY_RESPONSE_TIME",
			"$TABLE.$KEY_TYPE",
			"$TABLE.$KEY_RESPONSES",
			"$TABLE.$KEY_SYNCED",
			"${StudyToken.TABLE}.${StudyToken.KEY_TOKEN}"
		)
		
		fun createShortDataSet(type: EventTypes, study: Study) {
			val dataSet = DataSet(type, study)
			dataSet.save(study)
		}
		
		fun createScheduleChangedDataSet(schedule: Schedule) {
			val dataSet = DataSet(EventTypes.schedule_changed, schedule.getQuestionnaire())
			dataSet.addResponseData("newSchedule", schedule.toDescString())
			dataSet.save()
		}
		
		fun createActionSentDataSet(type: EventTypes, questionnaire: Questionnaire, scheduledToTimestamp: Long) {
			val dataSet = DataSet(type, questionnaire)
			dataSet.addResponseData("actionScheduledTo", scheduledToTimestamp)
			dataSet.save()
		}
		
		fun createAlarmExecuted(questionnaire: Questionnaire?, studyId: Long, scheduledToTimestamp: Long) {
			if(questionnaire == null) {
				val study = DbLogic.getStudy(studyId) ?: return
				val dataSet = DataSet(EventTypes.actions_executed, study)
				dataSet.addResponseData("actionScheduledTo", scheduledToTimestamp)
				dataSet.save(study)
			}
			else {
				val dataSet = DataSet(EventTypes.actions_executed, questionnaire)
				dataSet.addResponseData("actionScheduledTo", scheduledToTimestamp)
				dataSet.save()
			}

		}
	}
}