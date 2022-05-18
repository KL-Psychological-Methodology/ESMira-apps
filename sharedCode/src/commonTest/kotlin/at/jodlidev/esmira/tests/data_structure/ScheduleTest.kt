package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.PhoneType
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class ScheduleTest : BaseCommonTest() {
	
	@Test
	fun signalTimes() {
		assertEquals(0, createJsonObj<Schedule>().signalTimes.size)
		assertEquals(3, createJsonObj<Schedule>("""{"signalTimes":[{},{},{}]}""").signalTimes.size)
		
		val schedule = createJsonObj<Schedule>()
		schedule.fromJson = false
		schedule.id = 123
		assertEquals(0, schedule.signalTimes.size)
		assertSqlWasSelected(SignalTime.TABLE, 0, schedule.id.toString())
	}
	
	@Test
	fun isDifferent() {
		val schedule = createJsonObj<Schedule>(
			"""{"userEditable": true, "dailyRepeatRate": 1, "weekdays": 0, "dayOfMonth": 0, "skipFirstInLoop": false}"""
		)
		assertFalse(schedule.isDifferent(createJsonObj<Schedule>(
			"""{"userEditable": true, "dailyRepeatRate": 1, "weekdays": 0, "dayOfMonth": 0, "skipFirstInLoop": false}"""
		)))
		assertTrue(schedule.isDifferent(createJsonObj<Schedule>(
			"""{"userEditable": false, "dailyRepeatRate": 1, "weekdays": 0, "dayOfMonth": 0, "skipFirstInLoop": false}"""
		)))
		assertTrue(schedule.isDifferent(createJsonObj<Schedule>(
			"""{"userEditable": false, "dailyRepeatRate": 2, "weekdays": 0, "dayOfMonth": 0, "skipFirstInLoop": false}"""
		)))
		assertTrue(schedule.isDifferent(createJsonObj<Schedule>(
			"""{"userEditable": false, "dailyRepeatRate": 2, "weekdays": 2, "dayOfMonth": 0, "skipFirstInLoop": false}"""
		)))
		assertTrue(schedule.isDifferent(createJsonObj<Schedule>(
			"""{"userEditable": false, "dailyRepeatRate": 2, "weekdays": 2, "dayOfMonth": 12, "skipFirstInLoop": false}"""
		)))
		assertTrue(schedule.isDifferent(createJsonObj<Schedule>(
			"""{"userEditable": false, "dailyRepeatRate": 2, "weekdays": 2, "dayOfMonth": 12, "skipFirstInLoop": true}"""
		)))
		
		assertTrue(schedule.isDifferent(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {}]}"""
		)))
		
		assertFalse(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {}]}"""
		).isDifferent(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {}]}"""
		)))
		
		assertTrue(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {}]}"""
		).isDifferent(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {},{}]}"""
		)))
		
		assertTrue(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {}, {}]}"""
		).isDifferent(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {}]}"""
		)))
	}
	
	@Test
	fun isFaulty() {
		val onHour = 1000*60*60
		assertFalse(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {}, {}]}"""
		).isFaulty())
		
		assertTrue(createJsonObj<Schedule>(
			"""{"signalTimes": [{}, {"random":true, "startTimeOfDay":${onHour}, "endTimeOfDay":${onHour*2}, "minutesBetween":61}, {}]}"""
		).isFaulty())
	}
	
	@Test
	fun saveAndScheduleIfExists() {
		val schedule = createJsonObj<Schedule>()
		schedule.bindParent(createActionTrigger())
		schedule.saveAndScheduleIfExists()
		
		assertSqlWasSaved(Schedule.TABLE, Schedule.KEY_ID, schedule.id)
	}
	
	@Test
	fun scheduleIfNeeded() {
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.save(true)
		val schedule = createJsonObj<Schedule>("""{"signalTimes": [{}, {}, {}]}""")
		for((i, signalTime) in schedule.signalTimes.withIndex()) {
			signalTime.bindParent(questionnaire.id, schedule)
			signalTime.id = i+1L
		}
		schedule.bindParent(createActionTrigger {it.enabled = true})
		
		//initial schedule:
		schedule.scheduleIfNeeded()
		assertEquals(3, DbLogic.getAlarmsFrom(schedule).size, "not every Alarm was scheduled")
		
		//no change:
		schedule.scheduleIfNeeded()
		assertEquals(3, DbLogic.getAlarmsFrom(schedule).size, "no new Alarm should have been scheduled")
		
		//delete one alarm and schedule it again:
		DbLogic.getAlarmsFrom(schedule)[0].delete()
		assertEquals(2, DbLogic.getAlarmsFrom(schedule).size, "one Alarm should have been deleted")
		schedule.scheduleIfNeeded()
		assertEquals(3, DbLogic.getAlarmsFrom(schedule).size, "one Alarm should have been scheduled again")
	}
	
	@Test
	fun saveTimeFrames() {
		val oneDay = 1000*60*60*24
		val later = NativeLink.getNowMillis() + oneDay + 1000*60
		val soon = NativeLink.getNowMillis()
		
		val schedule = createJsonObj<Schedule>("""{"signalTimes":[{}]}""")
		schedule.bindParent(createActionTrigger())
		val signalTime = schedule.signalTimes[0]
		signalTime.bindParent(-1, schedule)
		signalTime.save()
		signalTime.timeHasChanged = true
		
		
		//with automatic rescheduleNow (next alarm is later than 24 hours)
		//this has to happen
//		reset() // mockDatabase does not order selects. So saveTimeFrames() would get the wrong alarm
		Alarm.createFromSignalTime(signalTime, schedule.getActionTriggerId(), later).save()
		schedule.bindParent(createActionTrigger()) //add questionnaire and study to db again
		schedule.saveTimeFrames()
		assertNotEquals(0, postponedActions.cancelList.size)
		
		
		//without rescheduleNow (next alarm is sooner than 24 hours)
		postponedActions.reset()
		Alarm.createFromSignalTime(signalTime, schedule.getActionTriggerId(), soon).save()
		schedule.saveTimeFrames()
		assertEquals(0, postponedActions.cancelList.size)
		
	}
	
	@Test
	fun getInitialDelayDays() {
		assertEquals(0, createJsonObj<Schedule>("""{"skipFirstInLoop": false}""").getInitialDelayDays())
		assertEquals(1, createJsonObj<Schedule>("""{"skipFirstInLoop": true, "dailyRepeatRate": 1}""").getInitialDelayDays())
		assertEquals(3, createJsonObj<Schedule>("""{"skipFirstInLoop": true, "dailyRepeatRate": 3}""").getInitialDelayDays())
	}
	
	
//	@Test
//	fun toDescString() {
//		//TODO: not tested
//	}
	
	@Test
	fun updateLastScheduled() {
		Schedule.updateLastScheduled(123, 1001)
		assertSqlWasUpdated(Schedule.TABLE, Schedule.KEY_LAST_SCHEDULED, 1001L)
	}
}