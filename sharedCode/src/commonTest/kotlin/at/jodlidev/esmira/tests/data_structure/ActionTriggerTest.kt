package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by JodliDev on 31.03.2022.
 */
class ActionTriggerTest : BaseCommonTest() {
	private val testId = 6L
	
	@Test
	fun trigger_scheduleChanged_notifications() {
		var fireScheduleChangedCount = notifications.fireSchedulesChangedList.size
		
		val emptyActionTrigger = createActionTrigger("""{"schedules": [{}]}""")
		emptyActionTrigger.save(true)
		assertEquals(fireScheduleChangedCount, notifications.fireSchedulesChangedList.size)
		val id = emptyActionTrigger.id
		
		//new schedules:
		val actionTriggerWithSchedules1 = createActionTrigger("""{"schedules": [{},{}]}""")
		actionTriggerWithSchedules1.id = id
		actionTriggerWithSchedules1.exists = true
		actionTriggerWithSchedules1.save(true)
		assertEquals(++fireScheduleChangedCount, notifications.fireSchedulesChangedList.size)
		
		//no change:
		val actionTriggerWithSchedules2 = createActionTrigger("""{"schedules": [{},{}]}""")
		actionTriggerWithSchedules2.id = id
		actionTriggerWithSchedules2.exists = true
		actionTriggerWithSchedules2.save(true)
		assertEquals(fireScheduleChangedCount, notifications.fireSchedulesChangedList.size)
	}
	
	@Test
	fun eventTriggers() {
		assertEquals(0, createActionTrigger("""{"eventTriggers": []}""").eventTriggers.size)
		assertEquals(2, createActionTrigger("""{"eventTriggers": [{},{}]}""").eventTriggers.size)
		
		val empty = createActionTrigger()
		empty.id = testId
		empty.fromJsonOrUpdated = false
		assertEquals(0, empty.eventTriggers.size)
		assertSqlWasSelected(EventTrigger.TABLE, 0, testId.toString())
	}
	
	@Test
	fun schedules() {
		assertEquals(1, createActionTrigger("""{"schedules": [{}]}""").schedules.size)
		assertEquals(0, createActionTrigger("""{"schedules": []}""").schedules.size)
		
		val empty = createActionTrigger()
		empty.id = testId
		empty.fromJsonOrUpdated = false
		assertEquals(0, empty.schedules.size)
		assertSqlWasSelected(Schedule.TABLE, 0, testId.toString())
	}
	
	@Test
	fun hasSchedules() {
		assertEquals(true, createActionTrigger("""{"schedules": [{},{}]}""").hasSchedules())
		assertEquals(false, createActionTrigger("""{"schedules": []}""").hasSchedules())
		assertEquals(false, createActionTrigger().hasSchedules())
	}
	
	@Test
	fun hasEvents() {
		assertEquals(false, createActionTrigger("""{"eventTriggers": []}""").hasEvents())
		assertEquals(true, createActionTrigger("""{"eventTriggers": [{}]}""").hasEvents())
		assertEquals(false, createActionTrigger().hasEvents())
	}
	
	@Test
	fun hasDelayedEvents() {
		assertEquals(false, createActionTrigger("""{"schedules": [{},{}]}""").hasDelayedEvents())
		assertEquals(false, createActionTrigger("""{"eventTriggers": [{},{}]}""").hasDelayedEvents())
		assertEquals(true, createActionTrigger("""{"eventTriggers": [{},{"delaySec": 10}]}""").hasDelayedEvents())
		assertEquals(false, createActionTrigger().hasDelayedEvents())
	}
	
	@Test
	fun hasNotifications() {
		assertEquals(true, createActionTrigger("""{"actions": [{"type": 1}]}""").hasNotifications())
		assertEquals(true, createActionTrigger("""{"actions": [{"type": 2}]}""").hasNotifications())
		assertEquals(true, createActionTrigger("""{"actions": [{"type": 3}]}""").hasNotifications())
		assertEquals(false, createActionTrigger().hasNotifications())
	}
	
	@Test
	fun hasInvitation() {
		assertEquals(true, createActionTrigger("""{"actions": [{"type": 1}]}""").hasInvitation())
		assertEquals(false, createActionTrigger("""{"actions": [{"type": 2}]}""").hasInvitation())
		assertEquals(false, createActionTrigger("""{"actions": [{"type": 3}]}""").hasInvitation())
		assertEquals(false, createActionTrigger().hasInvitation())
	}
	
	@Test
	fun usesPostponedActions() {
		assertEquals(false, createActionTrigger("""{"eventTriggers": [{},{}]}""").usesPostponedActions())
		assertEquals(true, createActionTrigger("""{"eventTriggers": [{},{"delaySec": 10}]}""").usesPostponedActions())
		assertEquals(true, createActionTrigger("""{"schedules": [{},{}]}""").usesPostponedActions())
		assertEquals(false, createActionTrigger().usesPostponedActions())
	}
	
	@Test
	fun schedulesAreFaulty() {
		val actionTrigger = createActionTrigger(
			"""{"schedules": [{"signalTimes":[{"startTimeOfDay": 3600000, "endTimeOfDay": 10800000, "random": true}]}]}"""
		)
		assertEquals(false, actionTrigger.schedulesAreFaulty())
		val signalTime = actionTrigger.schedules[0].signalTimes[0]
		signalTime.frequency = 3
		signalTime.startTimeOfDay = 0
		signalTime.endTimeOfDay = (signalTime.minutesBetween * 3 + (signalTime.minutesBetween-1)) * 60000
		
		assertEquals(true, actionTrigger.schedulesAreFaulty())
	}
	
	@Test
	fun save() {
		createActionTrigger(
			"""{"schedules": [{"signalTimes":[{}]}], "eventTriggers": [{},{}]}"""
		).save(true)
		
		val savedData = getSqlSavedMap()
		assertEquals(1, savedData[ActionTrigger.TABLE]?.size)
		assertEquals(1, savedData[Schedule.TABLE]?.size)
		assertEquals(1, savedData[SignalTime.TABLE]?.size)
		assertEquals(2, savedData[EventTrigger.TABLE]?.size)
	}
	
	@Test
	fun execActions() {
		createActionTrigger().execActions("test", NativeLink.getNowMillis())
		assertEquals(0, notifications.fireQuestionnaireBingList.size)
		assertEquals(0, notifications.fireStudyNotificationList.size)
		assertEquals(0, notifications.fireMessageNotificationList.size)
		
		val a1 = createActionTrigger(
			"""{"actions": [{"type": 2}]}""",
			"""{"pages": [{"inputs": [{}]}]}"""
		)
		a1.studyId = getBaseStudyId() //JSON_ACTION_TYPE_MSG needs a study obj
		a1.execActions("test", NativeLink.getNowMillis())
		assertEquals(0, notifications.fireQuestionnaireBingList.size)
		assertEquals(0, notifications.fireStudyNotificationList.size)
		assertEquals(1, notifications.fireMessageNotificationList.size)
		
		val a2 = createActionTrigger(
			"""{"actions": [{"type": 3}, {"type": 2}, {"type": 1}]}""",
			"""{"pages": [{"inputs": [{}]}]}"""
		)
		a2.studyId = getBaseStudyId() //JSON_ACTION_TYPE_MSG needs a study obj
		a2.execActions("test", NativeLink.getNowMillis())
		
		assertEquals(1, notifications.fireQuestionnaireBingList.size)
		assertEquals(1, notifications.fireStudyNotificationList.size)
		assertEquals(1+1, notifications.fireMessageNotificationList.size)
	}
	
	@Test
	fun execAsPostponedNotifications() {
		val actionTriggerEventTrigger = createActionTrigger(
			"""{"eventTriggers":[{}], "actions": [{"type": 2}]}"""
		)
		actionTriggerEventTrigger.execAsPostponedNotifications(
			Alarm.createFromEventTrigger(actionTriggerEventTrigger.eventTriggers[0], NativeLink.getNowMillis())
		)
		assertEquals(1, notifications.firePostponedList.size)
		
		val actionTriggerSchedules = createActionTrigger(
			"""{"schedules": [{"signalTimes":[{}]}], "actions": [{"type": 2}]}"""
		)
		actionTriggerSchedules.execAsPostponedNotifications(
			Alarm.createFromSignalTime(actionTriggerSchedules.schedules[0].signalTimes[0], actionTriggerSchedules.id, NativeLink.getNowMillis())
		)
		assertEquals(1+1, notifications.firePostponedList.size)
		
	}
	
	@Test
	fun iteratePostponedReminder() {
		val actionTrigger = createActionTrigger(
			"""{"eventTriggers":[{}], "actions": [{"type": 3}, {"type": 2}, {"type": 1}]}"""
		)
		actionTrigger.iteratePostponedReminder(
			Alarm.createFromEventTrigger(actionTrigger.eventTriggers[0], NativeLink.getNowMillis())
		)
		assertEquals(1, postponedActions.scheduleAlarmList.size) //1 for alarm
	}
	
	@Test
	fun issueReminder() {
		val actionTrigger = createActionTrigger(
			"""{"schedules": [{"signalTimes":[{}]}], "actions": [{"type": 2, "timeout": 3}]}""",
			"""{"pages": [{"inputs":[{}]}]}"""
		)
		countQueries(
			0,
			"UPDATE ${DbUser.TABLE} SET ${DbUser.KEY_NOTIFICATIONS_MISSED} = ${DbUser.KEY_NOTIFICATIONS_MISSED} + 1"
		) {
			actionTrigger.issueReminder("test", NativeLink.getNowMillis(), 0, 5)
		}
		assertEquals(1, notifications.fireQuestionnaireBingList.size)
		assertEquals(1, postponedActions.scheduleAlarmList.size)
		
		countQueries(
			1,
			"UPDATE ${DbUser.TABLE} SET ${DbUser.KEY_NOTIFICATIONS_MISSED} = ${DbUser.KEY_NOTIFICATIONS_MISSED} + 1"
		) {
			actionTrigger.issueReminder("test", NativeLink.getNowMillis() - 1000*60*60*24, 0, 5)
		}
		assertEquals(1, notifications.fireQuestionnaireBingList.size)
		assertEquals(1, postponedActions.scheduleAlarmList.size)
	}
}