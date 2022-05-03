import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.PhoneType
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlinx.serialization.decodeFromString
import mock.MockDialogOpener
import mock.MockNotifications
import mock.MockPostponedActions
import mock.MockSmartphoneData

/**
 * Created by JodliDev on 13.04.2022.
 */
abstract class BaseTest {
	internal val studyWebId = 1000L
	internal val testUrl = "https://www.example.com"
	internal val testAccessKey = "accessKey"
	private var baseStudy: Study? = null
	
	internal val smartphoneData = MockSmartphoneData()
	internal val dialogOpener = MockDialogOpener()
	internal val notifications = MockNotifications()
	internal val postponedActions = MockPostponedActions()
	
	fun getBaseStudyId(): Long {
		if(baseStudy == null) {
			baseStudy = createStudy()
			baseStudy!!.save()
		}
		return baseStudy!!.id
	}
	
	
	fun setPhoneType(phoneType: PhoneType) {
		smartphoneData.currentPhoneType = phoneType
	}
	
	fun setLang(lang: String) {
		smartphoneData.currentLang = lang
	}
	
	open fun reset() {
		baseStudy = null
		dialogOpener.reset()
		notifications.reset()
		postponedActions.reset()
	}
	
	
	inline fun <reified T>createJsonObj(json: String = "{}"): T {
		return DbLogic.getJsonConfig().decodeFromString(json)
	}
	
	fun createAlarmFromSignalTime(
		signalTimeJson: String = "{}",
		actionTriggerId: Long = -1,
		timestamp: Long = NativeLink.getNowMillis()
	): Alarm {
		val signalTime = createJsonObj<SignalTime>(signalTimeJson)
		return Alarm(signalTime, actionTriggerId, timestamp, 1)
	}
	
	fun createActionTrigger(actionTriggerJson: String = "{}", questionnaireJson: String = "{}"): ActionTrigger {
		val questionnaire = createJsonObj<Questionnaire>(questionnaireJson)
		questionnaire.studyId = getBaseStudyId() //because execActions() and issueReminder() need its study
		questionnaire.save(true)
		val a = createJsonObj<ActionTrigger>(actionTriggerJson)
		a.questionnaireId = questionnaire.id
		a.studyId = getBaseStudyId()
		return a
	}
	
	fun createDataSet(): DataSet {
		return DataSet(
			eventType = DataSet.TYPE_QUESTIONNAIRE,
			study = createStudy(),
			questionnaireName = "",
			questionnaireId = -1,
			questionnaireInternalId = -1
		)
	}
	
	fun createMessage(msg: String = "I'm your space stepmom!"): Message {
		return Message(-1, msg, NativeLink.getNowMillis())
	}
	
	fun createObservedVariable(variableName: String, json: String = "{}"): ObservedVariable {
		val ov = createJsonObj<ObservedVariable>(json)
		ov.variableName = variableName
		return ov
	}
	
	fun createScheduleForSaving(json: String = "{}"): Schedule {
		val schedule = createJsonObj<Schedule>(json)
		schedule.bindParent(createActionTrigger())
		return schedule
	}
	
	fun createStudy(json: String = """{"id":$studyWebId}"""): Study {
		return Study.newInstance(testUrl, testAccessKey, json)
	}
}