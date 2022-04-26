import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlinx.serialization.decodeFromString

/**
 * Created by JodliDev on 13.04.2022.
 */
abstract class BaseTest {
	internal val studyWebId = 1000L
	internal val testUrl = "https://www.example.com"
	internal val testAccessKey = "accessKey"
	private var baseStudy: Study? = null
	
	fun getBaseStudyId(): Long {
		if(baseStudy == null) {
			baseStudy = createStudy()
			baseStudy!!.save()
		}
		return baseStudy!!.id
	}
	
	open fun reset() {
		baseStudy = null
	}
	
	
	inline fun <reified T>createObj(json: String = "{}"): T {
		return DbLogic.getJsonConfig().decodeFromString(json)
	}
	
	
//	fun createAlarmFromSignalTime(signalTimeJson: String = "{}", actionTriggerId: Long = -1): Alarm {
//		val signalTime = createObj<SignalTime>(signalTimeJson)
//		return Alarm.createFromSignalTime(signalTime, actionTriggerId, NativeLink.getNowMillis())
//	}
	fun createAlarmFromSignalTime(signalTimeJson: String = "{}", actionTriggerId: Long = -1): Alarm {
		val signalTime = createObj<SignalTime>(signalTimeJson)
		return Alarm(signalTime, actionTriggerId, NativeLink.getNowMillis(), 1)
	}
	
	fun createActionTrigger(actionTriggerJson: String = "{}", questionnaireJson: String = "{}"): ActionTrigger {
		val questionnaire = createObj<Questionnaire>(questionnaireJson)
		questionnaire.studyId = getBaseStudyId() //because execActions() and issueReminder() need its study
		questionnaire.save(true)
		val a = createObj<ActionTrigger>(actionTriggerJson)
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
		val ov = createObj<ObservedVariable>(json)
		ov.variableName = variableName
		return ov
	}
	
	fun createScheduleForSaving(json: String = "{}"): Schedule {
		val schedule = createObj<Schedule>(json)
		schedule.bindParent(createActionTrigger())
		return schedule
	}
	
	fun createStudy(json: String = """{"id":$studyWebId}"""): Study {
		return Study.newInstance(testUrl, testAccessKey, json)
	}
}