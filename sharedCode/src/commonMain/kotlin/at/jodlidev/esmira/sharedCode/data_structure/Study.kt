package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.*
import at.jodlidev.esmira.sharedCode.Updater
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfo
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticBox
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_timed
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perValue
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 04.05.2020.
 */


@Serializable
class Study internal constructor(
	@SerialName("id") val webId: Long
) {
	enum class STATES {
		Pending, Joined, HasLeft
	}
	
	internal constructor(c: SQLiteCursor) : this(webId = c.getLong(1)) {
		exists = true
		fromJson = false
		
		id = c.getLong(0)
		state = STATES.values()[c.getInt(2)]
		serverUrl = c.getString(3)
		accessKey = c.getString(4)
		version = c.getInt(5)
		subVersion = c.getInt(6)
		lang = c.getString(7)
		joined = c.getLong(8)
		title = c.getString(9)
		studyDescription = c.getString(10)
		contactEmail = c.getString(11)
		informedConsentForm = c.getString(12)
		postInstallInstructions = c.getString(13)
		publicChartsJsonString = c.getString(14)
		personalChartsJsonString = c.getString(15)
		msgTimestamp = c.getLong(16)
		publicStatisticsNeeded = c.getBoolean(17)
		sendMessagesAllowed = c.getBoolean(18)
		eventUploadSettingsString = c.getString(19)
	}
	
	@Transient
	var exists = false
	@Transient
	var fromJson = true
	@Transient
	var json: String = "" //only used to keep the original json-code between instances
	
	var publishedAndroid = true //not in db, only known when directly from server
	var publishedIOS = true //not in db, only known when directly from server
	
	@Transient
	var id: Long = -1
	@Transient
	var state = STATES.Pending
	@Transient
	lateinit var serverUrl: String
	@Transient
	lateinit var accessKey: String
	
	var version: Int = -1
	var subVersion: Int = -1
	var serverVersion: Int = -1
	var lang: String = ""
	
	@Transient
	var joined: Long = 0 //is automatically set to CURRENT_TIMESTAMP by sql
	
	var title: String = "Error"
	var studyDescription: String = "" //can be empty
	@Suppress("unused")
	var contactEmail: String = "" //can be empty
	@Suppress("unused")
	var informedConsentForm: String = "" //can be empty
	@Suppress("unused")
	var postInstallInstructions: String = "" //can be empty
	
	@Transient
	var msgTimestamp: Long = 0
	@Transient
	var publicStatisticsNeeded: Boolean = false
	
	var sendMessagesAllowed: Boolean = true
	
	@SerialName("eventUploadSettings")
	@Serializable(with = JsonToStringSerializer::class)
	private var eventUploadSettingsString = "{}"
	
	fun isEventUploaded(name: String): Boolean {
		val settings: Map<String, Boolean> = DbLogic.getJsonConfig().decodeFromString(eventUploadSettingsString)
		
		return settings[name] ?: defaultSettings[name] ?: true
	}
	
	@SerialName("questionnaires")
	private lateinit var _jsonQuestionnaires: List<Questionnaire>
	
	
	@Transient
	private lateinit var _questionnaires: List<Questionnaire>
	
	val questionnaires: List<Questionnaire> get() {
		if(!this::_questionnaires.isInitialized) {
			_questionnaires = if(this::_jsonQuestionnaires.isInitialized) _jsonQuestionnaires else loadQuestionnairesDB()
		}
		return _questionnaires
	}
	
	@Suppress("unused")
	val availableQuestionnaires: List<Questionnaire> get() {
		val r = ArrayList<Questionnaire>()
		for(q in questionnaires) {
			if(q.canBeFilledOut())
				r.add(q)
		}
		return r
	}
	
	private var publicStatistics: StatisticBox =
		StatisticBox(
			HashMap(),
			""
		) //only used by JSON
	@Transient
	private lateinit var publicChartsJsonString: String //will be set in completeJSON() or from db
	val publicCharts: List<ChartInfo> get() {
		return try {
			DbLogic.getJsonConfig().decodeFromString(publicChartsJsonString)
		}
		catch(e: Exception) {
			ErrorBox.error("Study", "Could not load public charts from:\n$publicChartsJsonString", e)
			ArrayList()
		}
	}
	
	private var personalStatistics: StatisticBox =
		StatisticBox(
			HashMap(),
			""
		) //only used by JSON
	@Transient
	private lateinit var personalChartsJsonString: String //will be set in finishJSON() or from db
	val personalCharts: List<ChartInfo> get() {
		return try {
			DbLogic.getJsonConfig().decodeFromString(personalChartsJsonString)
		}
		catch(e: Exception) {
			ErrorBox.error("Study", "Could not load personal charts from study \"$title\"", e)
			ErrorBox.log("Study", personalChartsJsonString)
			ArrayList()
		}
	}
	
	@Transient
	private lateinit var _observedVariables: List<ObservedVariable>
	@Suppress("unused")
	val observedVariables: List<ObservedVariable> get() {
		if(!this::_observedVariables.isInitialized) {
			if(fromJson) {
				val list = ArrayList<ObservedVariable>()
				for((key, subList) in personalStatistics.observedVariables) {
					for((i, ov) in subList.withIndex()) {
						ov.finishJSON(this, i, key)
						list.add(ov)
					}
				}
				_observedVariables = list
			}
			else {
				val c = NativeLink.sql.select(
					ObservedVariable.TABLE,
					ObservedVariable.COLUMNS,
					"${ObservedVariable.KEY_STUDY_ID} = ?", arrayOf(id.toString()),
					null,
					null,
					null,
					null
				)
				val list = ArrayList<ObservedVariable>()
				while (c.moveToNext()) {
					list.add(ObservedVariable(c))
				}
				c.close()
				_observedVariables = list
			}
		}
		return _observedVariables
	}
	
	@Transient
	private lateinit var _actionTriggers: List<ActionTrigger>
	@Suppress("unused")
	val actionTriggers: List<ActionTrigger> get() { //only loaded from db
		if(!this::_actionTriggers.isInitialized) {
			val c = NativeLink.sql.select(
				ActionTrigger.TABLE,
				ActionTrigger.COLUMNS,
				"${ActionTrigger.KEY_STUDY_ID} = ? AND ${ActionTrigger.KEY_ENABLED} = 1", arrayOf(id.toString()),
				null,
				null,
				ActionTrigger.KEY_QUESTIONNAIRE_ID,
				null
			)
			val actionTriggers: MutableList<ActionTrigger> = ArrayList()
			while(c.moveToNext()) {
				actionTriggers.add(ActionTrigger(c))
			}
			c.close()
			
			_actionTriggers = actionTriggers
		}
		return _actionTriggers
	}
	
	@Transient
	private lateinit var _editableSignalTimes: List<SignalTime>
	@Suppress("unused")
	val editableSignalTimes: List<SignalTime> get() { //only loaded from db
		return if(this::_editableSignalTimes.isInitialized)
			this._editableSignalTimes
		else {
			val list = ArrayList<SignalTime>()
			
			for(trigger: ActionTrigger in actionTriggers) {
				for(schedule: Schedule in trigger.schedules) {
					if(!schedule.userEditable)
						continue
					for(signalTime: SignalTime in schedule.signalTimes) {
						list.add(signalTime)
					}
				}
			}
			this._editableSignalTimes = list
			list
		}
	}
	
	
	@Suppress("unused")
	fun saveSchedules(rescheduleNow: Boolean): Boolean {
		//make sure that there are no errors:
		for(trigger in actionTriggers) {
			if(trigger.schedulesAreFaulty())
				return false
		}
		
		//now we can save stuff:
		for(trigger in actionTriggers) {
			trigger.saveScheduleTimeFrames(rescheduleNow)
		}
		
		return true
	}
	
	private fun loadQuestionnairesDB(): List<Questionnaire> {
		val c = NativeLink.sql.select(
			Questionnaire.TABLE,
			Questionnaire.COLUMNS,
			"${Questionnaire.KEY_STUDY_ID} = ?", arrayOf(id.toString()),
			null,
			null,
			null,
			null
		)
		val questionnaires: MutableList<Questionnaire> = ArrayList()
		while(c.moveToNext()) {
			questionnaires.add(Questionnaire(c))
		}
		c.close()
		return questionnaires
	}
	
	private fun finishJSON(serverUrl: String, accessKey: String) {
		fromJson = true
		this.serverUrl = serverUrl
		this.accessKey = accessKey
		
		publicChartsJsonString = publicStatistics.charts
		personalChartsJsonString = personalStatistics.charts
		
		var publicStatisticsNeeded = publicChartsJsonString.length > 2
		if(!publicStatisticsNeeded) {
			for(chart in personalCharts) {
				if(chart.displayPublicVariable) {
					publicStatisticsNeeded = true
					break;
				}
			}
		}
		this.publicStatisticsNeeded = publicStatisticsNeeded
	}
	
	@Suppress("unused")
	fun hasSchedules(): Boolean {
		for(q in questionnaires) {
			if(q.hasSchedules())
				return true
		}
		return false
	}
	@Suppress("unused")
	fun hasEditableSchedules(): Boolean {
		for(q in questionnaires) {
			if(q.isActive() && q.hasEditableSchedules())
				return true
		}
		return false
	}
	
	@Suppress("unused")
	fun hasEvents(): Boolean {
		for(q in questionnaires) {
			if(q.hasEvents())
				return true
		}
		return false
	}
	
	@Suppress("unused")
	fun hasDelayedEvents(): Boolean {
		for(q in questionnaires) {
			if(q.hasDelayedEvents())
				return true
		}
		return false
	}
	
	@Suppress("unused")
	fun hasNotifications(): Boolean {
		for(q in questionnaires) {
			if(q.hasNotifications())
				return true
		}
		return false
	}
	
	@Suppress("unused")
	fun hasScreenTracking(): Boolean {
		for(q in questionnaires) {
			if(q.hasScreenTracking())
				return true
		}
		return false
	}
	
	@Suppress("unused")
	fun usesPostponedActions(): Boolean {
		for(q in questionnaires) {
			if(q.usesPostponedActions())
				return true
		}
		return false
	}

	fun isActive(): Boolean {
		for(q in questionnaires) {
			if(q.isActive()) //also checks for duration of the study itself
				return true
		}
		return false
	}
	
	@Suppress("unused")
	fun hasNotYetActiveQuestionnaires(): Boolean {
		for(q in questionnaires) {
			if(q.willBeActiveIn(this) > 0)
				return true
		}
		return false
	}
	
	@Suppress("unused")
	fun hasInformedConsent(): Boolean {
		return informedConsentForm.isNotEmpty()
	}
	
	@Suppress("unused")
	fun needsPermissionScreen(): Boolean {
		return hasInformedConsent() || usesPostponedActions() || hasNotifications() || hasScreenTracking()
	}
	
	@Suppress("unused")
	fun needsJoinedScreen(): Boolean {
		return postInstallInstructions.isNotEmpty() || hasSchedules()
	}
	
	private fun getOldLeftStudy(): Study? {
		val c = NativeLink.sql.select(
			TABLE,
			COLUMNS,
			"$KEY_WEB_ID = ? AND $KEY_SERVER_URL = ? AND $KEY_STATE = ${STATES.HasLeft.ordinal}", arrayOf(webId.toString(), serverUrl),
			null,
			null,
			null,
			"1"
		)
		var r: Study? = null
		if(c.moveToFirst())
			r = Study(c)
		c.close()
		return r
	}
	fun updateWith(newStudy: Study) {
		if(newStudy.fromJson)
			newStudy.finishJSON(serverUrl, accessKey)
//		val newStudy = checkUpdate(study)
		
		if(newStudy.id == -1L) //ObservableVariable.finishJSON() needs a studyId and will be called when getting study.observedVariables
			newStudy.id = id

		try {
			this.version = newStudy.version
			this.subVersion = newStudy.subVersion
			this.title = newStudy.title
			this.studyDescription = newStudy.studyDescription
			this.contactEmail = newStudy.contactEmail
			this.informedConsentForm = newStudy.informedConsentForm
			this.postInstallInstructions = newStudy.postInstallInstructions
			this.personalChartsJsonString = newStudy.personalChartsJsonString
			this.publicChartsJsonString = newStudy.publicChartsJsonString
			this.publicStatisticsNeeded = newStudy.publicStatisticsNeeded
			this.sendMessagesAllowed = newStudy.sendMessagesAllowed
			this.eventUploadSettingsString = newStudy.eventUploadSettingsString
			this._jsonQuestionnaires = newStudy.questionnaires
			if(this::_questionnaires.isInitialized)
				this._questionnaires = newStudy.questionnaires
			this._observedVariables = newStudy.observedVariables
		}
		catch(e: Throwable) {
			ErrorBox.error("Updating study", "Could not update study!", e)
			return
		}
		save()
		DataSet.createShortDataSet(DataSet.TYPE_STUDY_UPDATED, this)
	}
	
	@Suppress("unused")
	fun join() {
		ErrorBox.log("Study", "Joining study $title ($webId)")
		state = STATES.Joined
		save()
		NativeLink.postponedActions.updateStudiesRegularly()
	}
	
	fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_WEB_ID, webId)
		values.putString(KEY_SERVER_URL, serverUrl)
		values.putString(KEY_ACCESS_KEY, accessKey)
		values.putInt(KEY_VERSION, version)
		values.putInt(KEY_SUB_VERSION, subVersion)
		values.putString(KEY_LANG, lang)
		values.putInt(KEY_STATE, state.ordinal)
		values.putLong(KEY_LAST_MSG_TIMESTAMP, msgTimestamp)
		values.putString(KEY_TITLE, title)
		values.putString(KEY_DESC, studyDescription)
		values.putString(KEY_EMAIL, contactEmail)
		values.putString(KEY_CONSENT, informedConsentForm)
		values.putString(KEY_INSTRUCTIONS, postInstallInstructions)
		values.putString(KEY_PUBLIC_CHARTS_JSON, publicChartsJsonString)
		values.putString(KEY_PERSONAL_CHARTS_JSON, personalChartsJsonString)
		values.putBoolean(KEY_LOAD_PUBLIC_STATISTICS, publicStatisticsNeeded)
		values.putBoolean(KEY_SEND_MESSAGES_ALLOWED, sendMessagesAllowed)
		values.putString(KEY_UPLOAD_SETTINGS, eventUploadSettingsString)
		
		if(exists) {
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
			
			if(this::_jsonQuestionnaires.isInitialized) { //Meaning that we want to override it
				val dbQuestionnaires = loadQuestionnairesDB()
				if(dbQuestionnaires.size != _jsonQuestionnaires.size) { //if true, too much has changed. We just recreate everything
					for(q in dbQuestionnaires) {
						q.delete()
					}
					NativeLink.notifications.fireSchedulesChanged(this)
				}
				else { //make sure that new questionnaires will be saved with same internal id:
					for(i in _jsonQuestionnaires.indices) {
						val newQuestionnaires = _jsonQuestionnaires[i]
						newQuestionnaires.id = dbQuestionnaires[i].id
						newQuestionnaires.exists = true
					}
				}
				_questionnaires = _jsonQuestionnaires
			}
		}
		else {
			id = db.insert(TABLE, values)
			val oldStudy = getOldLeftStudy()
			if(oldStudy != null) {
				val observedValues = db.getValueBox()
				observedValues.putLong(ObservedVariable.KEY_STUDY_ID, id)
				db.update(ObservedVariable.TABLE, observedValues, "${ObservedVariable.KEY_STUDY_ID} = ?", arrayOf(oldStudy.id.toString()))
				
				val dailyValues = db.getValueBox()
				dailyValues.putLong(ObservedVariable.KEY_STUDY_ID, id)
				db.update(StatisticData_timed.TABLE, dailyValues, "${StatisticData_timed.KEY_STUDY_ID} = ?", arrayOf(oldStudy.id.toString()))
				oldStudy.delete()
				DataSet.createShortDataSet(DataSet.TYPE_REJOIN, oldStudy)
			}
		}
		for(q in questionnaires) {
			q.bindParent(this)
			q.save(state == STATES.Joined, db)
		}
		for(observedVariable in observedVariables) {
			observedVariable.studyId = id //this is not needed because a newly created ObservedVariable already gets this study-object. But I feel safer leaving it in there
			observedVariable.save()
		}
		if(!exists) {
			DataSet.createShortDataSet(DataSet.TYPE_JOIN, this)
			exists = true
		}
	}

	fun saveMsgTimestamp(t: Long) {
		msgTimestamp = t
		if(exists) {
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putLong(KEY_LAST_MSG_TIMESTAMP, msgTimestamp)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		}
	}

	fun delete() {
		val db = NativeLink.sql
		for(q in loadQuestionnairesDB()) {
			q.delete()
		}
		db.delete(Message.TABLE, "${Message.KEY_STUDY_ID} = ?", arrayOf(id.toString()))
		db.delete(ObservedVariable.TABLE, "${ObservedVariable.KEY_STUDY_ID} = ?", arrayOf(id.toString()))
		db.delete(StatisticData_timed.TABLE, "${StatisticData_timed.KEY_STUDY_ID} = ?", arrayOf(id.toString()))
		db.delete(StatisticData_perValue.TABLE, "${StatisticData_perValue.KEY_STUDY_ID} = ?", arrayOf(id.toString()))
		db.delete(DataSet.TABLE, "${DataSet.KEY_STUDY_ID} = ? AND ${DataSet.KEY_SYNCED} IS NOT ${DataSet.STATE_NOT_SYNCED}", arrayOf(id.toString()))
		db.delete(TABLE, "$KEY_ID = ?", arrayOf(id.toString()))
	}

	fun leaveAfterCheck() {
		if(state != STATES.HasLeft && !isActive() && !hasNotYetActiveQuestionnaires()) {
			ErrorBox.log("Study", "Leaving study \"$title\" because it is not active anymore")
			leave()
		}
	}
	
	@Suppress("unused") fun leave() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		state = STATES.HasLeft
		values.putInt(KEY_STATE, state.ordinal)
		db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))

		if(!DataSet.createShortDataSet(DataSet.TYPE_LEAVE, this))
			execLeave()
	}

	internal fun execLeave() {
		//Note: we are not cleaning StudyToken when there are unsynced datasets.
		//That means server_tokens can accumulate over time
		if(!DbLogic.hasUnsyncedDataSetsAfterQuit(id))
			NativeLink.sql.delete(StudyToken.TABLE, "${StudyToken.KEY_STUDY_ID} = ?", arrayOf(id.toString()))

		if(observedVariables.isEmpty()) {
			delete()
		}
		else {
			for(q in questionnaires) {
				q.delete()
			}
		}
	}

	fun alreadyExists(): Boolean {
		val c = NativeLink.sql.select(
			TABLE,
			arrayOf(KEY_WEB_ID, KEY_SERVER_URL),
			"$KEY_WEB_ID = ? AND $KEY_SERVER_URL = ? AND $KEY_STATE = ${STATES.Joined.ordinal}", arrayOf(webId.toString(), serverUrl),
			null,
			null,
			null,
			"1"
		)
		val r = c.moveToFirst()
		c.close()
		return r
	}
	
	companion object {
		const val TABLE = "studies"
		const val KEY_ID = "_id"
		const val KEY_WEB_ID = "web_id" //this is the same id as the web-version
		const val KEY_SERVER_URL = "server_url"
		const val KEY_ACCESS_KEY = "accessKey"
		const val KEY_VERSION = "version"
		const val KEY_SUB_VERSION = "subVersion"
		const val KEY_LANG = "study_lang"
		const val KEY_JOINED = "joined"
		const val KEY_STATE = "state"
		const val KEY_LAST_MSG_TIMESTAMP = "msgTimestamp"
		const val KEY_TITLE = "title"
		const val KEY_DESC = "description"
		const val KEY_EMAIL = "email"
		const val KEY_CONSENT = "consent"
		const val KEY_INSTRUCTIONS = "instructions"
		const val KEY_LOAD_PUBLIC_STATISTICS = "load_publicStatistics"
		const val KEY_PUBLIC_CHARTS_JSON = "public_charts_json"
		const val KEY_PERSONAL_CHARTS_JSON = "personal_charts_json"
		const val KEY_SEND_MESSAGES_ALLOWED = "sendMessagesAllowed"
		const val KEY_UPLOAD_SETTINGS = "uploadSettingsString"
		
		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_WEB_ID,
			KEY_STATE,
			KEY_SERVER_URL,
			KEY_ACCESS_KEY,
			KEY_VERSION,
			KEY_SUB_VERSION,
			KEY_LANG,
			"strftime('%s', $KEY_JOINED)",
			KEY_TITLE,
			KEY_DESC,
			KEY_EMAIL,
			KEY_CONSENT,
			KEY_INSTRUCTIONS,
			KEY_PUBLIC_CHARTS_JSON,
			KEY_PERSONAL_CHARTS_JSON,
			KEY_LAST_MSG_TIMESTAMP,
			KEY_LOAD_PUBLIC_STATISTICS,
			KEY_SEND_MESSAGES_ALLOWED,
			KEY_UPLOAD_SETTINGS
		)
		
		val defaultSettings = hashMapOf(
			"rejoined" to false,
			"invitation" to false,
			"invitation_missed" to false,
			"notification" to false,
			"message" to false,
			"study_message" to false,
			"schedule_changed" to true,
			"study_updated" to false,
			"statistic_viewed" to false
		)
		
		@Suppress("unused") fun newInstance(serverUrl: String, accessKey: String, json: String, checkUpdate: Boolean = true): Study {
			val study = DbLogic.getJsonConfig().decodeFromString<Study>(json)
			study.json = json
			study.finishJSON(serverUrl, accessKey)
			
			return if(checkUpdate) checkUpdate(study) else study
		}
		private fun checkUpdate(study: Study): Study {
			if(study.serverVersion > Updater.EXPECTED_SERVER_VERSION)
				NativeLink.dialogOpener.updateNeeded()
			else if(study.serverVersion != Updater.EXPECTED_SERVER_VERSION)
				return Updater.updateStudy(study)
			
			return study
		}
		
		@Suppress("unused") fun getFilteredStudyList(json: String, url: String, accessKey: String, studyWebId: Long = 0, qId: Long = 0): List<Study> {
			val jsonList = DbLogic.getJsonConfig().decodeFromString<List<JsonObject>>(json)
			val list = ArrayList<Study>()
			
			
			val searchQuestionnaire = qId != 0L
			for(jsonStudy in jsonList) {
				try {
					val study = newInstance(url, accessKey, jsonStudy.toString())
					if(((isAndroid() && study.publishedAndroid) || (isIOS() && study.publishedIOS)) && !study.alreadyExists() && study.isActive()) {
						if(study.webId == studyWebId)
							return arrayListOf(study)
						else {
							if(searchQuestionnaire) {
								for(questionnaire in study.questionnaires) {
									if(questionnaire.internalId == qId)
										return arrayListOf(study)
								}
							}
							else
								list.add(study)
						}
					}
				}
				catch(e: Exception) {
					ErrorBox.warn("New Study list", "Format error: $jsonStudy", e)
				}
			}
			println(list)
			return list
		}
	}
}