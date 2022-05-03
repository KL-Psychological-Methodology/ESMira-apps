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
		dataSet.synced = DataSet.STATES.SYNCED
		
		assertSqlWasUpdated(DataSet.TABLE, DataSet.KEY_SYNCED, DataSet.STATES.SYNCED.ordinal)
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
	fun addResponseData() {
		val questionnaire = createJsonObj<Questionnaire>()
		val dataSet = createDataSet()
		dataSet.studyId = getBaseStudyId()
		
		dataSet.addResponseData("boolean123", true)
		dataSet.addResponseData("string123", "string")
		dataSet.addResponseData("integer123", 5)
		dataSet.addResponseData("long123", 5L)
		dataSet.saveQuestionnaire(questionnaire, NativeLink.getNowMillis())
		
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
		
		dataSet.saveQuestionnaire(questionnaire, NativeLink.getNowMillis())
		
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertEquals(-1, value.indexOf("\"sumScore\":$sumScoreValue"))
		
		assertSqlWasSelected(EventTrigger.TABLE_JOINED, 1, DataSet.TYPE_QUESTIONNAIRE)
		assertEquals(1, postponedActions.syncDataSetsCount)
	}
	
	@Test
	fun createShortDataSet() {
		val study = createStudy("""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.TYPE_ALARM_EXECUTED}": true}}""")
		DataSet.createShortDataSet(DataSet.TYPE_ALARM_EXECUTED, study)
		assertSqlWasSaved(DataSet.TABLE, DataSet.KEY_TYPE, DataSet.TYPE_ALARM_EXECUTED)
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
		val study = createStudy("""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.TYPE_NOTIFICATION}": true}}""")
		study.save()
		
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.studyId = study.id
		val now = NativeLink.getNowMillis()
		DataSet.createActionSentDataSet(DataSet.TYPE_NOTIFICATION, questionnaire, now)
		
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf("\"actionScheduledTo\":\"$now\""))
	}
	
	@Test
	fun createAlarmExecuted() {
		val study = createStudy("""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.TYPE_ALARM_EXECUTED}": true}}""")
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