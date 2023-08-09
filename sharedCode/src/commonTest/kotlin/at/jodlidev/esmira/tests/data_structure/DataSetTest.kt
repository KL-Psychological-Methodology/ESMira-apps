package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class DataSetTest : BaseCommonTest() {
	
	@Test
	fun synced() {
		val dataSet = createDataSet()
		dataSet.id = 123
		dataSet.synced = UploadData.States.SYNCED
		
		assertSqlWasUpdated(DataSet.TABLE, DataSet.KEY_SYNCED, UploadData.States.SYNCED.ordinal)
	}
	
	@Test
	fun setResponses() {
		val dataSet = createDataSet()
		assertFails {
			dataSet.setResponses("test")
		}
		dataSet.setResponses("""{"var1": 234, "var2": "test", "var3": true}""")
	}
	
	@Test
	fun addResponseData() { //TODO is at the wrong place
		val questionnaire = createJsonObj<Questionnaire>(
			"""{"pages": [{"inputs": [{"name": "boolean123"}, {"name": "string123"}]}, {"inputs": [{"name": "integer123"}, {"name": "long123"}]}]}"""
		)
		questionnaire.studyId = getBaseStudyId()
		val dataSet = createDataSet()
		dataSet.studyId = getBaseStudyId()
		
		questionnaire.pages[0].inputs[0].setValue(true.toString())
		questionnaire.pages[0].inputs[1].setValue("string")
		questionnaire.pages[1].inputs[0].setValue("5")
		questionnaire.pages[1].inputs[1].setValue("6")
		questionnaire.saveQuestionnaire(NativeLink.getNowMillis())
		
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf("boolean123"))
		assertNotEquals(-1, value.indexOf("string123"))
		assertNotEquals(-1, value.indexOf("integer123"))
		assertNotEquals(-1, value.indexOf("long123"))
	}
	
	@Test
	fun saveQuestionnaire() {
		val questionnaire = createJsonObj<Questionnaire>(
			"""{"sumScores": [{"addList": ["test1", "test2"], "subtractList": ["test3", "test4"]}]}"""
		)
		questionnaire.studyId = getBaseStudyId()
		val dataSet = createDataSet()
		dataSet.studyId = getBaseStudyId()
		
		val test1 = 6
		val test2 = 7
		val test3 = 8
		val test4 = 9
		val sumScoreValue = test1 + test2 - test3 - test4
		
		dataSet.addResponseData("test1", test1)
		dataSet.addResponseData("test2", test2)
		dataSet.addResponseData("test3", test3)
		dataSet.addResponseData("test4", test4)
		
		questionnaire.saveQuestionnaire(NativeLink.getNowMillis())
		
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertEquals(-1, value.indexOf("\"sumScore\":$sumScoreValue"))
		
		assertSqlWasSelected(EventTrigger.TABLE_JOINED, 1, DataSet.EventTypes.questionnaire.toString())
		assertEquals(1, postponedActions.syncDataSetsCount)
	}
	
	@Test
	fun createShortDataSet() {
		val study = createStudy("""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.EventTypes.actions_executed}": true}}""")
		DataSet.createShortDataSet(DataSet.EventTypes.actions_executed, study)
		assertSqlWasSaved(DataSet.TABLE, DataSet.KEY_TYPE, DataSet.EventTypes.actions_executed.toString())
	}
	
	@Test
	fun createScheduleChangedDataSet() {
		val schedule = createJsonObj<Schedule>()
		schedule.bindParent(createActionTrigger())
		DataSet.createScheduleChangedDataSet(schedule)
		
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf("newSchedule"))
	}
	
	@Test
	fun createActionSentDataSet() {
		val study = createStudy("""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.EventTypes.notification}": true}}""")
		study.save()
		
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.studyId = study.id
		val now = NativeLink.getNowMillis()
		DataSet.createActionSentDataSet(DataSet.EventTypes.notification, questionnaire, now)
		
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf("\"actionScheduledTo\":\"$now\""))
	}
	
	@Test
	fun createAlarmExecuted() {
		val study = createStudy("""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.EventTypes.actions_executed}": true}}""")
		study.save()
		
		val now = NativeLink.getNowMillis()
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.title = "test123"
		questionnaire.studyId = study.id
		
		//without questionnaire:
		DataSet.createAlarmExecuted(null, questionnaire.studyId, now)
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf("\"actionScheduledTo\":\"$now\""))
		
		//with questionnaire:
		DataSet.createAlarmExecuted(questionnaire, questionnaire.studyId, now)
		assertSqlWasSaved(DataSet.TABLE, DataSet.KEY_QUESTIONNAIRE_NAME, "test123")
	}
}