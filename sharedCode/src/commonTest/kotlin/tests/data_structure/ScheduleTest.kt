package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
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
	fun saveTimeFrames() {
		val oneDay = 1000*60*60*24
		val later = NativeLink.getNowMillis() + oneDay + 1000*60
		val soon = NativeLink.getNowMillis()
		
		val schedule = createJsonObj<Schedule>("""{"signalTimes":[{}]}""")
		schedule.bindParent(createActionTrigger())
		val signalTime = schedule.signalTimes[0]
		signalTime.bindParent(-1, schedule)
		signalTime.exists = true // signalTimes.saveTimeFrames() will be called which only calls postponedActions.cancel() if it exists
		signalTime.timeHasChanged = true
		
		
		//without rescheduleNow
		Alarm.createFromSignalTime(signalTime, schedule.getActionTriggerId(), soon)
		schedule.saveTimeFrames()
		assertEquals(0, postponedActions.cancelList.size)
		
		
		//with automatic rescheduleNow
		reset() // mockDatabase does not order selects. So saveTimeFrames() would get the wrong alarm
		Alarm.createFromSignalTime(signalTime, schedule.getActionTriggerId(), later)
		schedule.bindParent(createActionTrigger()) //add questionnaire and study to db again
		schedule.saveTimeFrames()
		assertNotEquals(0, postponedActions.cancelList.size)
	}
	
	@Test
	fun getInitialDelay() {
		val oneDay = 1000*60*60*24
		val now = NativeLink.getNowMillis()
		val actionTrigger = createActionTrigger(questionnaireJson = """{"durationStart": ${now + oneDay*2}}""")
		val schedule = createJsonObj<Schedule>()
		schedule.bindParent(actionTrigger)
		
		schedule.skipFirstInLoop = false
		assertEquals(2, schedule.getInitialDelayDays())
		
		schedule.skipFirstInLoop = true
		schedule.dailyRepeatRate = 1
		assertEquals(2, schedule.getInitialDelayDays())
		schedule.dailyRepeatRate = 3
		assertEquals(3, schedule.getInitialDelayDays())
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