package tests.database

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.PhoneType
import at.jodlidev.esmira.sharedCode.Scheduler
import at.jodlidev.esmira.sharedCode.data_structure.*
import at.jodlidev.esmira.sharedCode.data_structure.Input
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Created by JodliDev on 05.04.2022.
 */
abstract class DataStructureSharedTests : BaseDatabaseTests() {
	open fun actionTrigger_trigger_scheduleChanged_notification() {
		var fireScheduleChangedCount = notifications.fireSchedulesChangedList.size
		
		val emptyActionTrigger = createActionTrigger()
		emptyActionTrigger.save(true)
		assertEquals(fireScheduleChangedCount, notifications.fireSchedulesChangedList.size)
		val id = emptyActionTrigger.id
		
		//new schedules:
		val actionTriggerWithSchedules1 = createActionTrigger("""{"schedules": [{}]}""")
		actionTriggerWithSchedules1.id = id
		actionTriggerWithSchedules1.exists = true
		actionTriggerWithSchedules1.save(true)
		assertEquals(++fireScheduleChangedCount, notifications.fireSchedulesChangedList.size)
		
		//no change:
		val actionTriggerWithSchedules2 = createActionTrigger("""{"schedules": [{}]}""")
		actionTriggerWithSchedules2.id = id
		actionTriggerWithSchedules2.exists = true
		actionTriggerWithSchedules2.save(true)
		assertEquals(fireScheduleChangedCount, notifications.fireSchedulesChangedList.size)
		
		//new eventTrigger:
		val actionTriggerWithEventTrigger = createActionTrigger("""{"schedules": [{}], "eventTriggers": [{}]}""")
		actionTriggerWithEventTrigger.id = id
		actionTriggerWithEventTrigger.exists = true
		actionTriggerWithEventTrigger.save(true)
		assertEquals(++fireScheduleChangedCount, notifications.fireSchedulesChangedList.size)
	}
	
	open fun alarm_do_scheduleAhead() {
		setPhoneType(PhoneType.IOS)
		val postponedActions = postponedActions
		val scheduleAheadDays = Scheduler.IOS_DAYS_TO_SCHEDULE_AHEAD_MS/Scheduler.ONE_DAY_MS
		
		val actionTrigger = createActionTrigger()
		actionTrigger.save(true)
		
		//schedule ahead from an alarm before threshold (Scheduler.IOS_DAYS_TO_SCHEDULE_AHEAD_MS)
		val alarm1 = createAlarmFromSignalTime(actionTriggerId = actionTrigger.id)
		alarm1.signalTime!!.bindParent(-1, createObj())
		alarm1.timestamp = NativeLink.getNowMillis() + Scheduler.IOS_DAYS_TO_SCHEDULE_AHEAD_MS - 1
		alarm1.scheduleAhead()
		assertEquals(1, postponedActions.scheduleAlarmList.size)
		
		
		//schedule ahead from an alarm after threshold (Scheduler.IOS_DAYS_TO_SCHEDULE_AHEAD_MS)
		val alarm2 = createAlarmFromSignalTime(actionTriggerId = actionTrigger.id)
		alarm2.signalTime!!.bindParent(-1, createObj())
		alarm2.timestamp = NativeLink.getNowMillis() + Scheduler.IOS_DAYS_TO_SCHEDULE_AHEAD_MS + 1000
		alarm2.scheduleAhead()
		assertEquals(1, postponedActions.scheduleAlarmList.size) //nothing changed
		
		//schedule ahead from an alarm after threshold (Scheduler.IOS_DAYS_TO_SCHEDULE_AHEAD_MS)
		val alarm3 = createAlarmFromSignalTime(actionTriggerId = actionTrigger.id)
		alarm3.signalTime!!.bindParent(-1, createObj(
			"""{"dailyRepeatRate": ${scheduleAheadDays+1}}"""
		))
		alarm3.timestamp = NativeLink.getNowMillis() + Scheduler.IOS_DAYS_TO_SCHEDULE_AHEAD_MS // will be ignored because dailyRepeatRate is greater
		alarm3.scheduleAhead()
		assertEquals(2, postponedActions.scheduleAlarmList.size)
	}
	
	open fun alarm_do_exec() {
		setPhoneType(PhoneType.Android)
		val scheduleAlarmList = postponedActions.scheduleAlarmList
		val studyNotificationList = notifications.fireStudyNotificationList
		val questionnaireBingList = notifications.fireQuestionnaireBingList
		
		val study = createStudy()
		study.join()
		
		//test questionnaire that will be active later
		val questionnaireActiveSoon = createObj<Questionnaire>("""{"durationStartingAfterDays": 5}""")
		questionnaireActiveSoon.studyId = study.id
		questionnaireActiveSoon.save(true)
		
		val alarmActiveSoon = createAlarmFromSignalTime()
		alarmActiveSoon.questionnaireId = questionnaireActiveSoon.id
		alarmActiveSoon.signalTime?.bindParent(-1, createObj())
		alarmActiveSoon.exec()
		assertEquals(1, scheduleAlarmList.size)
		
		
		//test inactive questionnaire
		val questionnaireInactive = createObj<Questionnaire>("""{"durationEnd": ${NativeLink.getNowMillis() - 1000*60}}""")
		questionnaireInactive.studyId = study.id
		questionnaireInactive.save(true)
		
		val alarmInactive = createAlarmFromSignalTime()
		alarmInactive.questionnaireId = questionnaireActiveSoon.id
		alarmInactive.signalTime?.bindParent(questionnaireInactive.id, createObj())
		alarmInactive.exec()
		assertEquals(1, scheduleAlarmList.size) //no change
		
		
		//test signalTime alarm
		val questionnaireActive = createObj<Questionnaire>("""{"pages": [{"inputs":[{}]}]}""") //questionnaire needs to be active
		questionnaireActive.save(true) //exec() needs a questionnaire from db
		val actionTrigger = createObj<ActionTrigger>("""{"actions": [{"type": ${ActionTrigger.JSON_ACTION_TYPE_NOTIFICATION}}]}""")
		actionTrigger.questionnaireId = questionnaireActive.id
		actionTrigger.save(true)
		
		val alarmSignalTime = createAlarmFromSignalTime(actionTriggerId = actionTrigger.id)
		alarmSignalTime.questionnaireId = questionnaireActive.id
		alarmInactive.signalTime?.bindParent(questionnaireInactive.id, createObj())
		alarmSignalTime.exec()
		assertEquals(2, scheduleAlarmList.size)
		assertEquals(1, studyNotificationList.size)
		
		
		//test eventTrigger alarm
		val eventTrigger = createObj<EventTrigger>()
		eventTrigger.save()
		val alarmEventTrigger = Alarm(eventTrigger, NativeLink.getNowMillis())
		alarmEventTrigger.questionnaireId = questionnaireActive.id
		alarmEventTrigger.actionTriggerId = actionTrigger.id
		alarmEventTrigger.exec()
		assertEquals(2, studyNotificationList.size)
		
		
		//test eventTrigger alarm
		val alarmReminder = Alarm(
			NativeLink.getNowMillis(),
			questionnaireActive.id,
			actionTrigger.id,
			"test",
			0,
			1,
			-1,
			-1)
		alarmReminder.questionnaireId = questionnaireActive.id
		alarmReminder.actionTriggerId = actionTrigger.id
		alarmReminder.exec()
		assertEquals(1, questionnaireBingList.size)
	}
	
	open fun createAndLoadChartInfoCollection() {
		//TODO: no tests
	}
	
	abstract fun createEmptyFile(content: String): String
	open fun fileUpload_createAndDeleteFile() {
		//check existing file
		val content = "test 123"
		
		val path = createEmptyFile(content)
		val fileUploadExists = FileUpload(createStudy(), path, FileUpload.TYPES.Image)
		fileUploadExists.save()
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		assertEquals(1, DbLogic.getTemporaryFileUploads().size)
		fileUploadExists.setReadyForUpload()
		assertEquals(1, DbLogic.getPendingFileUploads().size)
		assertEquals(0, DbLogic.getTemporaryFileUploads().size)
		
		//check file content
		val byteArray = fileUploadExists.getFile()
		assertEquals(content, byteArray.decodeToString())
		
		fileUploadExists.delete()
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		assertEquals(0, DbLogic.getTemporaryFileUploads().size)
		
		
		//check non existing file
		val fileUploadDoesNotExist = FileUpload(createStudy(), "not/existing/file", FileUpload.TYPES.Image)
		fileUploadDoesNotExist.save()
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		assertEquals(1, DbLogic.getTemporaryFileUploads().size)
		fileUploadExists.setReadyForUpload()
		assertEquals(1, DbLogic.getPendingFileUploads().size)
		assertEquals(0, DbLogic.getTemporaryFileUploads().size)
		
		fileUploadDoesNotExist.delete()
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		assertEquals(1, DbLogic.getTemporaryFileUploads().size)
	}
	
	private fun testDynamicInput(random: Boolean) {
		val json = """{"responseType": "dynamic_input", "random": $random, "subInputs":[{"name": "dyn1"}, {"name": "dyn2"}, {"name": "dyn3"}]}"""
		//we need new inputs every time because dynamicInput is cached
		val questionnaire = createObj<Questionnaire>()
		val previousNames = ArrayList<String>()
		var subInput: Input
		
		val defaultInput = createObj<Input>(json)
		
		// get next input; make sure that it was not selected yet (previousNames); "fill out" questionnaire; repeat
		for(i in 0 until defaultInput.subInputs.size) {
			questionnaire.lastCompleted = NativeLink.getNowMillis() + 1000 //fake filled out questionnaire
			subInput = createObj<Input>(json).getDynamicInput(questionnaire)
			assertEquals(-1, previousNames.indexOf(subInput.name))
			previousNames.add(subInput.name)
		}
		
		subInput = createObj<Input>(json).getDynamicInput(questionnaire)
		assertNotEquals(-1, previousNames.indexOf(subInput.name)) //all subInputs have been used. So now we get one we already had
	}
	open fun input_testDynamicInput() {
		testDynamicInput(false)
		testDynamicInput(true)
	}
	
	open fun observedVariable_finishJSON() {
		val variableName = "Katara"
		
		val study = createStudy()
		val observedVariable1 = createObservedVariable(variableName)
		val firstId = observedVariable1.id
		
		observedVariable1.finishJSON(study, 0, variableName)
		assertEquals(firstId, observedVariable1.id)
		observedVariable1.save()
		assertNotEquals(firstId, observedVariable1.id)
		
		val observedVariable2 = createObservedVariable(variableName)
		observedVariable2.finishJSON(study, 0, variableName)
		assertEquals(observedVariable1.id, observedVariable2.id)
		
		val observedVariable3 = createObservedVariable(variableName)
		observedVariable3.finishJSON(study, 0, "Zuko")
		assertEquals(firstId, observedVariable3.id)
	}
	
	open fun questionnaire_save() {
		val questionnaire = createObj<Questionnaire>("""{"actionTriggers":[{},{}]}""")
		questionnaire.save(true)
		assertEquals(questionnaire.studyId, DbLogic.getQuestionnaire(questionnaire.id)?.studyId)
		assertEquals(2, DbLogic.getActionTriggers(questionnaire.studyId).size)
		assertEquals(0, notifications.fireSchedulesChangedList.size)
		
		questionnaire.title = "new"
		questionnaire.save(true)
		assertEquals("new", DbLogic.getQuestionnaire(questionnaire.id)?.title)
		assertEquals(0, notifications.fireSchedulesChangedList.size)
		
		val questionnaireDifferent = createObj<Questionnaire>("""{"actionTriggers":[{},{},{}]}""")
		questionnaireDifferent.fromJson = true
		questionnaireDifferent.exists = true
		questionnaireDifferent.id = questionnaire.id
		questionnaireDifferent.save(true)
		assertEquals(1, notifications.fireSchedulesChangedList.size)
		assertEquals(3, DbLogic.getActionTriggers(questionnaire.studyId).size)
	}
	
	open fun questionnaire_isActive() {
		val now = NativeLink.getNowMillis()
		val db = NativeLink.sql
		val values = db.getValueBox()
		
		//test durationPeriodDays:
		values.putLong(Study.KEY_JOINED, now)
		db.update(Study.TABLE, values, "${Study.KEY_ID} = ?", arrayOf(getBaseStudyId().toString()))
		var questionnaire = createObj<Questionnaire>("{\"durationPeriodDays\": 2}")
		assertTrue(questionnaire.isActive())
		
		values.putLong(Study.KEY_JOINED, now - 1000*60*60*24*2 + 1)
		db.update(Study.TABLE, values, "${Study.KEY_ID} = ?", arrayOf(getBaseStudyId().toString()))
		assertFalse(questionnaire.isActive())
		
		
		//test durationStartingAfterDays:
		questionnaire = createObj<Questionnaire>("{\"durationStartingAfterDays\": 2}")
		values.putLong(Study.KEY_JOINED, now - 1000*60*60*24*1)
		db.update(Study.TABLE, values, "${Study.KEY_ID} = ?", arrayOf(getBaseStudyId().toString()))
		assertFalse(questionnaire.isActive())
		
		values.putLong(Study.KEY_JOINED, now - 1000*60*60*24*2 + 1)
		db.update(Study.TABLE, values, "${Study.KEY_ID} = ?", arrayOf(getBaseStudyId().toString()))
		assertTrue(questionnaire.isActive())
		
		
		//test durationStart
		questionnaire = createObj<Questionnaire>("{\"durationStart\": ${now}}")
		assertTrue(questionnaire.isActive())
		
		questionnaire = createObj<Questionnaire>("{\"durationStart\": ${now + 1000 * 60}}")
		assertFalse(questionnaire.isActive())
		
		
		//test durationEnd
		questionnaire = createObj<Questionnaire>("{\"durationEnd\": ${now + 1000 * 60}}")
		assertTrue(questionnaire.isActive())
		
		questionnaire = createObj<Questionnaire>("{\"durationEnd\": ${now - 1}}")
		assertFalse(questionnaire.isActive())
		
		
		//test completableOnce
		questionnaire = createObj<Questionnaire>("{\"completableOnce\": true}")
		assertTrue(questionnaire.isActive())
		questionnaire.lastCompleted = now
		assertFalse(questionnaire.isActive())
	}
	
	open fun study_create_and_delete() {
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
		
		assertEquals(3, dbStudy!!.questionnaires.size)
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
	
	open fun study_do_editableSignalTimes() {
		assertEquals(0, createStudy().editableSignalTimes.size)
		val study = createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"schedules": [{"signalTimes":[{},{},{}]}, {"signalTimes": [{},{}], "userEditable": false}]}]}]}"""
		)
		study.join()
		assertEquals(3, study.editableSignalTimes.size)
	}
	
	open fun study_do_editableSchedules() {
		assertFalse(createStudy().hasEditableSchedules())
		val study = createStudy(
			"""{"id":$studyWebId, "questionnaires": [{"actionTriggers": [{"schedules": [{"signalTimes": [{},{}], "userEditable": false}, {"signalTimes":[{},{},{}]}]}]}]}"""
		)
		study.join()
		assertTrue(study.hasEditableSchedules())
	}
	
	open fun study_do_alreadyExists() {
		val study = createStudy()
		val newStudy = createStudy()
		
		assertFalse(newStudy.alreadyExists())
		study.join()
		
		assertTrue(newStudy.alreadyExists())
	}
	
	open fun study_do_getOldLeftStudy() {
		val study = createStudy()
		val newStudy = createStudy()
		
		assertEquals(null, newStudy.getOldLeftStudy())
		study.join()
		study.execLeave()
		
		assertNotEquals(null, newStudy.getOldLeftStudy())
	}
}