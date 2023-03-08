package tests.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import at.jodlidev.esmira.sharedCode.NativeLink
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */

class StudyTest : BaseCommonTest() {
	private val timestamp = 626637180000
	private val testId = 5L
	
	@Test
	fun create_and_delete() {
		val study = createStudy("""{
				"id":$studyWebId,
				"questionnaires": [
					{"actionTriggers": [
						{"schedules": [
							{"signalTimes": [{}, {}]},
							{"signalTimes": [{}, {}, {}]}
						]},
						{}
					]},
					{"actionTriggers": [
						{"eventTriggers": [{}]},
						{"eventTriggers": [{}, {}]}
					]}
				],
				"personalStatistics": {"charts": [], "observedVariables": {"test1": [{}], "test2":[{}, {}]}}
			}""")
		val oldId = study.id
		study.save()
		
		val dbStudy = DbLogic.getStudy(study.id)
		assertNotEquals(oldId, study.id)
		assertNotEquals(null, dbStudy)
		
		assertEquals(2, dbStudy!!.questionnaires.size)
		assertEquals(2, dbStudy.questionnaires[0].actionTriggers.size)
		assertEquals(2, dbStudy.questionnaires[0].actionTriggers[0].schedules.size)
		assertEquals(2, dbStudy.questionnaires[0].actionTriggers[0].schedules[0].signalTimes.size)
		assertEquals(3, dbStudy.questionnaires[0].actionTriggers[0].schedules[1].signalTimes.size)
		
		assertEquals(2, dbStudy.questionnaires[1].actionTriggers.size)
		assertEquals(1, dbStudy.questionnaires[1].actionTriggers[0].eventTriggers.size)
		assertEquals(2, dbStudy.questionnaires[1].actionTriggers[1].eventTriggers.size)
		assertEquals(3, dbStudy.observedVariables.size)
		
		
		study.delete()
		assertEquals(null, DbLogic.getStudy(study.id))
		for(questionnaire in dbStudy.questionnaires) {
			assertEquals(null, DbLogic.getQuestionnaire(questionnaire.id))
			
			for(actionTrigger in questionnaire.actionTriggers) {
				assertEquals(null, DbLogic.getActionTrigger(actionTrigger.id))
				
				for(schedule in actionTrigger.schedules) {
					assertEquals(null, DbLogic.getSchedule(schedule.id))
					
					for(signalTime in schedule.signalTimes) {
						assertEquals(null, DbLogic.getSignalTime(signalTime.id))
					}
				}
				for(eventTrigger in actionTrigger.eventTriggers) {
					assertEquals(null, DbLogic.getEventTrigger(eventTrigger.id))
				}
			}
		}
	}
	
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
		
		assertSqlWasSelected(ActionTrigger.TABLE, 0, testId.toString())
	}
	
	@Test
	fun editableSignalTimes() {
		assertEquals(0, createStudy().editableSignalTimes.size)
		val study = createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"schedules": [{"signalTimes":[{},{},{}]}, {"signalTimes": [{},{}], "userEditable": false}]}]}]}"""
		)
		study.join()
		assertEquals(3, study.editableSignalTimes.size)
	}
	
	@Test
	fun finishJSON() {
		var study: Study = createStudy { it.finishJSON(testUrl, testAccessKey) }
		assertFalse(study.publicStatisticsNeeded)
		assertEquals(0, study.group)
		
		//test publicStatisticsNeeded:
		study = createStudy("""{"id":$studyWebId, "publicStatistics": {"charts": [{}]}}""") {
			it.finishJSON(testUrl, testAccessKey)
		}
		assertTrue(study.publicStatisticsNeeded)
		
		study = createStudy("""{"id":$studyWebId, "personalStatistics": {"charts": [{"displayPublicVariable": true}]}}""") {
			it.finishJSON(testUrl, testAccessKey)
		}
		assertTrue(study.publicStatisticsNeeded)
		
		
		//test randomGroup:
		for(i in 1 until 10) {
			study = createStudy("""{"id":$studyWebId, "randomGroups": $i}""") {
				it.finishJSON(testUrl, testAccessKey)
			}
			assertTrue(study.group in 1 .. i, "Group (${study.group}) is not between 1 and $i")
		}
	}
	
	@Test
	fun saveSchedules() {
		val study = createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers":[{"schedules": [{"signalTimes": [{"startTimeOfDay": 3600000, "endTimeOfDay": 10800000, "random": true}]}]}]}]}"""
		)
		study.join() //enabledActionTriggers is needed in saveSchedules and is only loaded from db so we need to save first
		val signalTime = study.enabledActionTriggers[0].schedules[0].signalTimes[0]
		
		signalTime.timeHasChanged = true
		signalTime.startTimeOfDay = 0
		signalTime.endTimeOfDay = 1000*60*60*3
		
		assertEquals(true, study.saveSchedules(true))
		
		val selectData = getSqlSelectMap()
		val savedData = getSqlSavedMap()
		val updateData = getSqlUpdateMap()
		
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
	
	@Test
	fun hasEditableSchedules() {
		assertFalse(createStudy().hasEditableSchedules())
		val study = createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"schedules": [{"signalTimes": [{},{}], "userEditable": false}, {"signalTimes":[{},{},{}]}]}]}]}"""
		)
		study.join()
		assertTrue(study.hasEditableSchedules())
	}
	
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
		assertEquals(false, createStudy().hasScreenOrAppTracking())
		assertEquals(true, createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"pages":[{"inputs": [{"responseType": "app_usage"}]}]}]}"""
		).hasScreenOrAppTracking())
	}
	
	@Test
	fun daysUntilRewardsAreActive() {
		val oneDay = 86400000
		val study = createStudy("""{"id":$studyWebId, "rewardVisibleAfterDays": 3}""")
		study.joinedTimestamp = NativeLink.getNowMillis()
		assertEquals(3, study.daysUntilRewardsAreActive())
		study.joinedTimestamp -= oneDay * 2
		assertEquals(1, study.daysUntilRewardsAreActive())
		study.joinedTimestamp -= oneDay
		assertEquals(0, study.daysUntilRewardsAreActive())
		study.joinedTimestamp -= oneDay
		assertEquals(0, study.daysUntilRewardsAreActive())
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
	
	@Test
	fun study_do_getOldLeftStudy() {
		val newStudy = createStudy()
		
		assertEquals(null, newStudy.getOldLeftStudy())
		val study = createStudy("""{"id":$studyWebId, "personalStatistics": {"charts": [], "observedVariables": {"test1": [{}]}}}""")
		study.save()
		newStudy.id = study.id
		study.leave()
		
		assertNotEquals(null, newStudy.getOldLeftStudy())
	}
	
	@Test
	fun updateWith() {
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
		
		
		//randomGroups changed:
		oldStudy.group = 5
		oldStudy.updateWith(createStudy(
			"""{"id":$studyWebId, "randomGroups": 8}"""
		))
		assertEquals(5, oldStudy.group)
		
		oldStudy.updateWith(createStudy(
			"""{"id":$studyWebId, "randomGroups": 3}"""
		))
		assertNotEquals(5, oldStudy.group)
		
		oldStudy.updateWith(createStudy(
			"""{"id":$studyWebId, "randomGroups": 0}"""
		))
		assertEquals(0, oldStudy.group)
		
		
		//questionnaire has more elements
		oldStudy.updateWith(createStudy(
			"""{
				"id":$studyWebId,
				"questionnaires": [
					{},
					{
						"actionTriggers": [
							{},
							{},
							{
								"schedules": [
									{},
									{"signalTimes": [{}, {}, {}, {}, {}]}
								],
								"eventTriggers": [{"cueCode": "test"}, {"cueCode": "test"}, {"cueCode": "test"}, {"cueCode": "test"}]
							}
						]
					}
				]}"""
		))
		assertEquals(3, DbLogic.getActionTriggers(oldStudy.id).size)
		assertEquals(2, DbLogic.getAllSchedules().size)
		assertEquals(4, DbLogic.getEventTriggers(oldStudy.id, "test").size)
		
		
		//questionnaire has less elements
		oldStudy.updateWith(createStudy(
			"""{
				"id":$studyWebId,
				"questionnaires": [
					{},
					{
						"actionTriggers": [
							{},
							{
								"schedules": [
									{"signalTimes": [{}, {}, {}, {}]}
								],
								"eventTriggers": [{"cueCode": "test"}, {"cueCode": "test"}, {"cueCode": "test"}]
							}
						]
					}
				]}"""
		))
		assertEquals(2, DbLogic.getActionTriggers(oldStudy.id).size)
		assertEquals(1, DbLogic.getAllSchedules().size)
		assertEquals(3, DbLogic.getEventTriggers(oldStudy.id, "test").size)
	}
	
	@Test
	fun join() {
		val study = createStudy()
		study.join()
		
		assertEquals(1, postponedActions.updateStudiesRegularlyCount)
		
		assertSqlWasSaved(DataSet.TABLE, DataSet.KEY_TYPE, DataSet.TYPE_JOIN)
	}
	
	@Test
	fun saveMsgTimestamp() {
		val study = createStudy()
		study.exists = true
		study.saveMsgTimestamp(timestamp)
		
		assertSqlWasUpdated(Study.TABLE, Study.KEY_LAST_MSG_TIMESTAMP, timestamp)
	}
	
	@Test
	fun saveRewardCode() {
		val study = createStudy()
		study.exists = true
		study.saveRewardCode("code123")
		
		assertSqlWasUpdated(Study.TABLE, Study.KEY_CACHED_REWARD_CODE, "code123")
	}
	
	@Test
	fun leaveAfterCheck() {
		val study = createStudy("""{"id":$studyWebId, "questionnaires": [{"pages": [{"inputs":[{}]}]}]}""")
		study.leaveAfterCheck()
		assertEquals(Study.STATES.Pending, study.state)
		
		val emptyStudy = createStudy()
		emptyStudy.leaveAfterCheck()
		assertEquals(Study.STATES.Quit, emptyStudy.state)
	}
	
	@Test
	fun leave() {
		createStudy().leave()
		assertSqlWasUpdated(Study.TABLE, Study.KEY_STATE, Study.STATES.Quit.ordinal)
	}
	
	@Test
	fun alreadyExists() {
		val study = createStudy()
		val newStudy = createStudy()
		
		assertFalse(newStudy.alreadyExists())
		study.join()
		
		assertTrue(newStudy.alreadyExists())
	}
}