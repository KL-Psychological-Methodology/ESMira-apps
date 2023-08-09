package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.PhoneType
import at.jodlidev.esmira.sharedCode.Scheduler
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import at.jodlidev.esmira.sharedCode.DbLogic
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by JodliDev on 31.03.2022.
 */
class AlarmTest : BaseCommonTest() {
	
	@Test
	fun actionTrigger() {
		val actionTrigger = createActionTrigger()
		actionTrigger.actionsString = "{},{}"
		actionTrigger.save(true)
		val eventTrigger = createJsonObj<EventTrigger>()
		eventTrigger.actionTriggerId = actionTrigger.id
		eventTrigger.save()
		val alarm = Alarm.createFromEventTrigger(eventTrigger, NativeLink.getNowMillis())
		
		assertEquals(actionTrigger.actionsString, alarm.actionTrigger.actionsString)
	}
	
	@Test
	fun signalTime() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.frequency = 26
		signalTime.save()
		val alarm = Alarm.createFromSignalTime(signalTime, -1, NativeLink.getNowMillis())
		
		assertEquals(signalTime.frequency, alarm.signalTime?.frequency)
	}
	
	@Test
	fun canBeRescheduled() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.frequency = 3
		val alarm = Alarm.createFromSignalTime(signalTime, -1, NativeLink.getNowMillis())
		
		signalTime.random = false
		assertEquals(true, alarm.canBeRescheduled)
		signalTime.random = true
		
		alarm.indexNum = 1
		assertEquals(false, alarm.canBeRescheduled)
		alarm.indexNum = 2
		assertEquals(false, alarm.canBeRescheduled)
		alarm.indexNum = 3
		assertEquals(true, alarm.canBeRescheduled)
		
		
		assertEquals(false, Alarm.createFromEventTrigger(createJsonObj(), NativeLink.getNowMillis()).canBeRescheduled)
	}
	
	@Test
	fun save() {
		val alarm = createAlarmFromSignalTime()
		alarm.save()
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_ID, alarm.id)
		assertEquals(alarm, postponedActions.scheduleAlarmList[0])
	}
	
	@Test
	fun schedule() {
		val alarm = createAlarmFromSignalTime()
		alarm.schedule()
		assertEquals(alarm, postponedActions.scheduleAlarmList[0])
	}
	
	@Test
	fun scheduleAhead() {
		//TODO: needs sorting
		setPhoneType(PhoneType.IOS)
		val scheduleAheadDays = Scheduler.IOS_DAYS_TO_SCHEDULE_AHEAD_MS / Scheduler.ONE_DAY_MS
		
		
		//schedule ahead from now
		postponedActions.reset()
		var study = DbLogic.createJsonObj<Study>(
			"""{"id": 1111, "questionnaires": [{"pages": [{}], "actionTriggers": [{"schedules": [{"signalTimes": [{}]}]}]}]}"""
		)
		study.finishJSON("exampleUrl", "")
		study.join()
		assertEquals(scheduleAheadDays.toInt(), postponedActions.scheduleAlarmList.size)
		
		
		
		//schedule ahead when some alarms already exist
		val alarm = DbLogic.getLastSignalTimeAlarm(study.questionnaires[0].actionTriggers[0].schedules[0].signalTimes[0]) ?: throw Exception("Error")
		alarm.delete() //this alarm should be replaced when scheduling ahead
		postponedActions.reset()
		Scheduler.scheduleAhead()
		assertEquals(1, postponedActions.scheduleAlarmList.size)
		
		
		
		//schedule ahead from now with inactive questionnaire
		postponedActions.reset()
		study = DbLogic.createJsonObj<Study>(
			"""{"id": 2222, "questionnaires": [{"durationPeriodDays": 2, "pages": [{}], "actionTriggers": [{"schedules": [{"signalTimes": [{}]}]}]}]}"""
		)
		study.finishJSON("exampleUrl", "")
		study.join()
		assertEquals(2, postponedActions.scheduleAlarmList.size)
	}
	
	@Test
	fun exec() {
		setPhoneType(PhoneType.Android)
		val scheduleAlarmList = postponedActions.scheduleAlarmList
		val studyNotificationList = notifications.fireStudyNotificationList
		val questionnaireBingList = notifications.fireQuestionnaireBingList
		
		val study = createDbStudy(1111, """[{"pages": [{}]}]""")
//		study.join()
		
		//test questionnaire that will be active later
		val questionnaireActiveSoon = createJsonObj<Questionnaire>("""{"durationStartingAfterDays": 5}""")
		questionnaireActiveSoon.studyId = study.id
		questionnaireActiveSoon.save(true)
		
		val alarmActiveSoon = createAlarmFromSignalTime()
		alarmActiveSoon.questionnaireId = questionnaireActiveSoon.id
		alarmActiveSoon.signalTime?.bindParent(study.questionnaires[0].id, createJsonObj())
		alarmActiveSoon.exec()
		assertEquals(1, scheduleAlarmList.size)
		
		
		//test inactive questionnaire
		val questionnaireInactive = createJsonObj<Questionnaire>("""{"durationEnd": ${NativeLink.getNowMillis() - 1000*60}}""")
		questionnaireInactive.studyId = study.id
		questionnaireInactive.save(true)
		
		val alarmInactive = createAlarmFromSignalTime()
		alarmInactive.questionnaireId = questionnaireInactive.id
		alarmInactive.signalTime?.bindParent(questionnaireInactive.id, createJsonObj())
		alarmInactive.exec()
		assertEquals(1, scheduleAlarmList.size) //no change
		
		
		//test signalTime alarm
		val questionnaireActive = createJsonObj<Questionnaire>("""{"pages": [{"inputs":[{}]}]}""") //questionnaire needs to be active
		questionnaireActive.save(true) //exec() needs a questionnaire from db
		val actionTrigger = createJsonObj<ActionTrigger>("""{"actions": [{"type": ${ActionTrigger.JSON_ACTION_TYPE_NOTIFICATION}}]}""")
		actionTrigger.questionnaireId = questionnaireActive.id
		actionTrigger.save(true)
		
		val alarmSignalTime = createAlarmFromSignalTime(actionTriggerId = actionTrigger.id)
		alarmSignalTime.questionnaireId = questionnaireActive.id
		alarmSignalTime.signalTime?.bindParent(questionnaireActive.id, createJsonObj())
		alarmSignalTime.exec()
		assertEquals(2, scheduleAlarmList.size)
		assertEquals(1, studyNotificationList.size)
		
		
		//test eventTrigger alarm
		val eventTrigger = createJsonObj<EventTrigger>()
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
			0,
			1,
			-1,
			-1)
		alarmReminder.questionnaireId = questionnaireActive.id
		alarmReminder.actionTriggerId = actionTrigger.id
		alarmReminder.exec()
		assertEquals(1, questionnaireBingList.size)
	}
	
	@Test
	fun delete() {
		val alarm = createAlarmFromSignalTime()
		alarm.delete()
		assertEquals(1, postponedActions.cancelList.size)
		assertEquals(1, notifications.removeList.size)
		assertSqlWasDeleted(Alarm.TABLE, 0, alarm.id.toString())
	}
	
	@Test
	fun createFromSignalTime() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.startTimeOfDay = 123
		val alarm = Alarm.createFromSignalTime(signalTime, -1, NativeLink.getNowMillis())
		
		assertEquals(signalTime.startTimeOfDay, alarm.signalTime?.startTimeOfDay)
	}
	
	@Test
	fun createFromEventTrigger() {
		val eventTrigger = createJsonObj<EventTrigger>()
		eventTrigger.id = 123
		val alarm = Alarm.createFromEventTrigger(eventTrigger, NativeLink.getNowMillis())
		
		assertEquals(eventTrigger.id, alarm.eventTriggerId)
	}
	
	@Test
	fun createAsReminder() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.startTimeOfDay = 123
		val alarm = Alarm.createAsReminder(
			1001L,
			11L,
			12L,
			21,
			22,
			13L,
			14L
		)
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_TIMESTAMP, 1001L)
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_QUESTIONNAIRE_ID, 11L)
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_ACTION_TRIGGER_ID, 12L)
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_EVENT_TRIGGER_ID, 13L)
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_SIGNAL_TIME_ID, 14L)
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_ONLY_SINGLE_ACTION_INDEX, 21)
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_REMINDER_COUNT, 22)
	}
}