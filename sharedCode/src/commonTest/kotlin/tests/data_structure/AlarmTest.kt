package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.PhoneType
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by JodliDev on 31.03.2022.
 */
class AlarmTest : BaseDataStructureTest() {
	
	@Test
	fun actionTrigger() {
		val actionTrigger = createActionTrigger()
		actionTrigger.actionsString = "{},{}"
		actionTrigger.save(true)
		val eventTrigger = createObj<EventTrigger>()
		eventTrigger.actionTriggerId = actionTrigger.id
		eventTrigger.save()
		val alarm = Alarm.createFromEventTrigger(eventTrigger, NativeLink.getNowMillis())
		
		assertEquals(actionTrigger.actionsString, alarm.actionTrigger.actionsString)
	}
	
	@Test
	fun signalTime() {
		val signalTime = createObj<SignalTime>()
		signalTime.frequency = 26
		signalTime.save()
		val alarm = Alarm.createFromSignalTime(signalTime, -1, NativeLink.getNowMillis())
		
		assertEquals(signalTime.frequency, alarm.signalTime?.frequency)
	}
	
	@Test
	fun canBeRescheduled() {
		val signalTime = createObj<SignalTime>()
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
		
		
		assertEquals(false, Alarm.createFromEventTrigger(createObj(), NativeLink.getNowMillis()).canBeRescheduled)
	}
	
	@Test
	fun save() {
		val alarm = createAlarmFromSignalTime()
		alarm.save()
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_ID, alarm.id)
		assertEquals(alarm, mockTools.getPostponedActions().scheduleAlarmList[0])
	}
	
	@Test
	fun schedule() {
		val alarm = createAlarmFromSignalTime()
		alarm.schedule()
		assertEquals(alarm, mockTools.getPostponedActions().scheduleAlarmList[0])
	}
	
	
	//scheduleAhead() is tested in DataStructureSharedTests.alarm_do_scheduleAhead()
	
	//exec() is tested in DataStructureSharedTests.alarm_do_exec()
	
	@Test
	fun delete() {
		val alarm = createAlarmFromSignalTime()
		alarm.delete()
		assertEquals(1, mockTools.getPostponedActions().cancelList.size)
		assertEquals(1, mockTools.getNotifications().removeList.size)
		mockTools.assertSqlWasDeleted(Alarm.TABLE, 0, alarm.id.toString())
	}
	
	@Test
	fun createFromSignalTime() {
		val signalTime = createObj<SignalTime>()
		signalTime.startTimeOfDay = 123
		val alarm = Alarm.createFromSignalTime(signalTime, -1, NativeLink.getNowMillis())
		
		assertEquals(signalTime.startTimeOfDay, alarm.signalTime?.startTimeOfDay)
	}
	
	@Test
	fun createFromEventTrigger() {
		val eventTrigger = createObj<EventTrigger>()
		eventTrigger.id = 123
		val alarm = Alarm.createFromEventTrigger(eventTrigger, NativeLink.getNowMillis())
		
		assertEquals(eventTrigger.id, alarm.eventTriggerId)
	}
	
	@Test
	fun createAsReminder() {
		val signalTime = createObj<SignalTime>()
		signalTime.startTimeOfDay = 123
		val alarm = Alarm.createAsReminder(
			1001L,
			11L,
			12L,
			"test",
			21,
			22,
			13L,
			14L
		)
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_TIMESTAMP, 1001L)
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_QUESTIONNAIRE_ID, 11L)
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_ACTION_TRIGGER_ID, 12L)
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_EVENT_TRIGGER_ID, 13L)
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_SIGNAL_TIME_ID, 14L)
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_LABEL, "test")
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_ONLY_SINGLE_ACTION_INDEX, 21)
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_REMINDER_COUNT, 22)
	}
}