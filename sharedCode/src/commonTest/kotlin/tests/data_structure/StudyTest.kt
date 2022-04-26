package tests.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */

class StudyTest : BaseDataStructureTest() {
	private val timestamp = 626637180000
	private val testId = 5L
	
	@Test
	fun isEventUploaded() {
		assertEquals(true, createStudy().isEventUploaded(DataSet.TYPE_JOIN)) //default value
		assertEquals(true, createStudy().isEventUploaded(DataSet.TYPE_QUESTIONNAIRE)) //default value
		assertEquals(true, createStudy().isEventUploaded(DataSet.TYPE_QUIT)) //default value
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.TYPE_INVITATION}": true}}"""
		).isEventUploaded(DataSet.TYPE_INVITATION))
		assertEquals(false, createStudy(
			"""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.TYPE_SCHEDULE_CHANGED}": false}}"""
		).isEventUploaded(DataSet.TYPE_SCHEDULE_CHANGED))
		assertEquals(false, createStudy().isEventUploaded(DataSet.TYPE_ALARM_EXECUTED)) //default value
	}
	
	@Test
	fun questionnaires() {
		assertEquals(0, createStudy().questionnaires.size)
		assertEquals(3, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{}, {}, {}]}"""
		).questionnaires.size)
	}
	
	@Test
	fun availableQuestionnaires() {
		assertEquals(0, createStudy().availableQuestionnaires.size)
		assertEquals(3, createStudy(
			"""{"id":$studyWebId, "questionnaires": [
					{"pages": [{"inputs": [{}]}]},
					{"pages": [{"inputs": [{}]}]},
					{"pages": [{"inputs": [{}]}]}
				]}"""
		).availableQuestionnaires.size)
	}
	
	@Test
	fun publicCharts() {
		assertEquals(0, createStudy().publicCharts.size)
		assertEquals(3, createStudy(
			"""{"id":$studyWebId, "publicStatistics": {"charts": [{}, {}, {}]}}"""
		).publicCharts.size)
	}
	
	@Test
	fun personalCharts() {
		assertEquals(0, createStudy().personalCharts.size)
		assertEquals(3, createStudy(
			"""{"id":$studyWebId, "personalStatistics": {"charts": [{}, {}, {}]}}"""
		).personalCharts.size)
	}
	@Test
	fun observedVariables() {
		val study = createStudy(
			"""{"id":$studyWebId, "personalStatistics": {"charts": [], "observedVariables": {"test2":[{}, {}]}}}"""
		)
		assertEquals(2, study.observedVariables.size)
		study.save()
		
		val dbStudy =  DbLogic.getStudy(study.id)!!
		assertEquals(2, dbStudy.observedVariables.size)
	}
	
	@Test
	fun actionTriggers() {
		val newStudy = createStudy()
		newStudy.id = testId
		assertEquals(0, newStudy.enabledActionTriggers.size)
		
		mockTools.assertSqlWasSelected(ActionTrigger.TABLE, 0, testId.toString())
	}
	
	//editableSignalTimes() is tested in DataStructureSharedTests.study_do_editableSignalTimes()
	
	@Test
	fun saveSchedules() {
		val study = createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"schedules": [{"signalTimes": [{"startTimeOfDay": 3600000, "endTimeOfDay": 10800000, "random": true}]}]}]}]}"""
		)
		study.save() //enabledActionTriggers is needed in saveSchedules and is only loaded from db so we need to save first
		val signalTime = study.enabledActionTriggers[0].schedules[0].signalTimes[0]
		
		signalTime.timeHasChanged = true
		signalTime.startTimeOfDay = 0
		signalTime.endTimeOfDay = 1000*60*60*3
		
		assertEquals(true, study.saveSchedules(true))
		
		val selectData = mockTools.getSqlSelectMap()
		val savedData = mockTools.getSqlSavedMap()
		val updateData = mockTools.getSqlUpdateMap()
		
		assertEquals(true, selectData.containsKey(Alarm.TABLE)) //because rescheduleNow was true
		assertEquals(true, savedData.containsKey(Alarm.TABLE)) //because rescheduleNow was true
		
		assertEquals(true, updateData.containsKey(SignalTime.TABLE))
		
		//canceled / faulty save:
		signalTime.startTimeOfDay = 0
		signalTime.endTimeOfDay = 0
		assertEquals(false, study.saveSchedules(false))
	}
	
	@Test
	fun hasSchedules() {
		assertEquals(false, createStudy().hasSchedules())
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"schedules": [{}]}]}]}"""
		).hasSchedules())
		
	}
	
	//hasEditableSchedules() is tested in DataStructureSharedTests.study_do_editableSchedules()
	
	@Test
	fun hasEvents() {
		assertEquals(false, createStudy().hasEvents())
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"eventTriggers": [{}]}]}]}"""
		).hasEvents())
	}
	
	@Test
	fun hasDelayedEvents() {
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"eventTriggers": [{"delaySec": 1}]}]}]}"""
		).hasDelayedEvents())
		
		assertEquals(false, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"eventTriggers": [{"delaySec": 0}]}]}]}"""
		).hasDelayedEvents())
	}
	
	@Test
	fun hasNotifications() {
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"actions": [{}]}]}]}"""
		).hasNotifications())
		assertEquals(false, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"actions": []}]}]}"""
		).hasNotifications())
	}
	
	@Test
	fun hasScreenTracking() {
		assertEquals(false, createStudy().hasScreenTracking())
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"pages":[{"inputs": [{"responseType": "app_usage"}]}]}]}"""
		).hasScreenTracking())
	}
	
	@Test
	fun usesPostponedActions() {
		//study.usesPostponedActions is very similar to hasDelayedEvents
		
		assertEquals(false, createStudy().usesPostponedActions())
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"eventTriggers": [{"delaySec": 10}]}]}]}"""
		).usesPostponedActions())
	}
	
	@Test
	fun isActive() {
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{}]}"""
		).isActive())
		
		
		val study = createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"completableOnce": true}]}"""
		)
		study.questionnaires[0].lastCompleted = timestamp
		assertEquals(false, study.isActive())
	}
	
	@Test
	fun hasNotYetActiveQuestionnaires() {
		assertEquals(false, createStudy().hasNotYetActiveQuestionnaires())
	}
	
	@Test
	fun hasInformedConsent() {
		assertEquals(false, createStudy().hasInformedConsent())
		
		val newStudy = createStudy()
		newStudy.informedConsentForm = "informedConsentForm"
		assertTrue(newStudy.hasInformedConsent())
	}
	
	@Test
	fun needsPermissionScreen() {
		assertTrue(createStudy(
			"""{"id":$studyWebId, "informedConsentForm": "Test123"}"""
		).needsPermissionScreen())
		assertTrue(createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"eventTriggers": [{"delaySec": 10}]}]}]}"""
		).needsPermissionScreen())
		assertTrue(createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"actions": [{}]}]}]}"""
		).needsPermissionScreen())
		assertTrue(createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"pages":[{"inputs": [{"responseType": "app_usage"}]}]}]}"""
		).needsPermissionScreen())
		assertFalse(createStudy().needsPermissionScreen())
	}
	
	@Test
	fun needsJoinedScreen() {
		assertFalse(createStudy().needsJoinedScreen())
		assertTrue(createStudy(
			"""{"id":$studyWebId, "postInstallInstructions": "test123"}"""
		).needsJoinedScreen())
	}
	
	//getOldLeftStudy() is tested in DataStructureSharedTests.study_do_getOldLeftStudy()
	
	@Test
	fun updateWith() {
		val dialogOpener = mockTools.getDialogOpener()
		val notifications = mockTools.getNotifications()
		
		val oldStudy = createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"schedules": [{"signalTimes": [{"startTimeOfDay": 1000}]}]}]}]}"""
		)
		oldStudy.id = 123
		
		
		//nothing has changed
		oldStudy.updateWith(createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"schedules": [{"signalTimes": [{"startTimeOfDay": 1000}]}]}]}]}"""
		))
		assertEquals(0, dialogOpener.errorReportCount)
		assertEquals(0, notifications.fireSchedulesChangedList.size)
		
		
		//signalTime has changed
		oldStudy.updateWith(createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"schedules": [{"signalTimes": [{"startTimeOfDay": 2000}]}]}]}]}"""
		))
		assertEquals(0, dialogOpener.errorReportCount)
		assertNotEquals(0, notifications.fireSchedulesChangedList.size)
		
		
		//questionnaire.size has changed
		val oldChangedEvents = notifications.fireSchedulesChangedList.size
		oldStudy.updateWith(createStudy(
			"""{"id":$studyWebId, "questionnaires": [{}, {"actionTriggers": [{"schedules": [{"signalTimes": [{"startTimeOfDay": 1000}]}]}]}]}"""
		))
		assertEquals(0, dialogOpener.errorReportCount)
		assertNotEquals(oldChangedEvents, notifications.fireSchedulesChangedList.size)
	}
	
	@Test
	fun join() {
		val study = createStudy()
		study.join()
		
		val postponedActions = mockTools.getPostponedActions()
		assertEquals(1, postponedActions.updateStudiesRegularlyCount)
		
		mockTools.assertSqlWasSaved(DataSet.TABLE, DataSet.KEY_TYPE, DataSet.TYPE_JOIN)
	}
	
	//save() is tested in DataStructureSharedTests.study_create_and_delete()
	
	@Test
	fun saveMsgTimestamp() {
		val study = createStudy()
		study.exists = true
		study.saveMsgTimestamp(timestamp)
		
		mockTools.assertSqlWasUpdated(Study.TABLE, Study.KEY_LAST_MSG_TIMESTAMP, timestamp)
	}
	
	//delete() is tested in DataStructureSharedTests.study_create_and_delete()
	
	@Test
	fun leaveAfterCheck() {
		val study = createStudy("""{"id":$studyWebId, "questionnaires": [{"pages": [{"inputs":[{}]}]}]}""")
		study.leaveAfterCheck()
		assertEquals(Study.STATES.Pending, study.state)
		
		val emptyStudy = createStudy()
		emptyStudy.leaveAfterCheck()
		assertEquals(Study.STATES.HasLeft, emptyStudy.state)
	}
	
	@Test
	fun leave() {
		createStudy().leave()
		mockTools.assertSqlWasUpdated(Study.TABLE, Study.KEY_STATE, Study.STATES.HasLeft.ordinal)
	}
	
	@Test
	fun execLeave() {
		val study1 = createStudy()
		study1.id = 5
		study1.execLeave()
		mockTools.assertSqlWasDeleted(Study.TABLE, 0, study1.id.toString())
		
		val study2 = createStudy("""{"id":$studyWebId, "personalStatistics": {"charts": [], "observedVariables": {"test1":[{}]}}}""")
		study2.id = 6
		study2.execLeave()
		assertFails {
			mockTools.assertSqlWasDeleted(Study.TABLE, 0, study2.id.toString())
		}
	}
	
	//alreadyExists() is tested in DataStructureSharedTests.study_do_alreadyExists()
}