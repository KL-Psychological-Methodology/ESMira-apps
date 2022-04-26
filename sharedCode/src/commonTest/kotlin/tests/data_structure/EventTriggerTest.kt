package tests.data_structure

import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class EventTriggerTest : BaseDataStructureTest() {
	
	@Test
	fun triggerCheck() {
		val notifications = mockTools.getNotifications()
		val actionTrigger = createActionTrigger("""{"eventTriggers":[{}], "actions":[{"type": 2}]}""")
		val questionnaire = actionTrigger.questionnaire
		val eventTrigger = actionTrigger.eventTriggers[0]
		
		//eventTrigger.exec() loads actionTrigger from db:
		actionTrigger.save(true)
		eventTrigger.bindParent(questionnaire, actionTrigger)
		
		//check noCondition
		eventTrigger.skipThisQuestionnaire = false
		eventTrigger.specificQuestionnaireInternalId = -1
		eventTrigger.triggerCheck(questionnaire)
		assertEquals(1, notifications.fireMessageNotificationList.size)
		
		//check skipThisQuestionnaire
		eventTrigger.skipThisQuestionnaire = true
		eventTrigger.questionnaireId = questionnaire.id
		eventTrigger.triggerCheck(questionnaire)
		assertEquals(2, notifications.fireMessageNotificationList.size)
		eventTrigger.questionnaireId = questionnaire.id + 1
		eventTrigger.triggerCheck(questionnaire)
		assertEquals(2, notifications.fireMessageNotificationList.size)
		
		//check specificQuestionnaire
		eventTrigger.skipThisQuestionnaire = false
		eventTrigger.specificQuestionnaireInternalId = questionnaire.internalId
		eventTrigger.studyId = questionnaire.studyId
		eventTrigger.triggerCheck(questionnaire)
		assertEquals(3, notifications.fireMessageNotificationList.size)
		eventTrigger.specificQuestionnaireInternalId = questionnaire.internalId+1
		assertEquals(3, notifications.fireMessageNotificationList.size)
	}
	
	@Test
	fun exec() {
		val actionTrigger = createActionTrigger("""{"eventTriggers":[{}]}""")
		val eventTrigger = actionTrigger.eventTriggers[0]
		
		//eventTrigger.exec() loads actionTrigger from db:
		actionTrigger.save(true)
		eventTrigger.bindParent(actionTrigger.questionnaire, actionTrigger)
		
		eventTrigger.cueCode = DataSet.TYPE_QUIT
		eventTrigger.exec(123, true)
		mockTools.assertSqlWasDeleted(Study.TABLE, 0, eventTrigger.studyId.toString())
	}
	
	@Test
	fun save() {
		val eventTrigger = createObj<EventTrigger>()
		eventTrigger.save()
		mockTools.assertSqlWasSaved(EventTrigger.TABLE, EventTrigger.KEY_STUDY_ID, eventTrigger.studyId)
	}
	
	@Test
	fun delete() {
		val eventTrigger = createObj<EventTrigger>()
		eventTrigger.id = 123
		eventTrigger.delete()
		mockTools.assertSqlWasDeleted(EventTrigger.TABLE, 0, eventTrigger.id.toString())
	}
}