package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class SignalTimeTest : BaseCommonTest() {
	
	@Test
	fun testFaultySignalTime() {
		val signalTime = createJsonObj<SignalTime>("""{"random": false, "randomFixed": true, "frequency": 5}""")
		
		assertFalse(signalTime.random)
		assertFalse(signalTime.randomFixed)
		assertEquals(1, signalTime.frequency)
	}
	
	@Test
	fun isFaulty() {
		assertFalse(createJsonObj<SignalTime>("""{"random": false}""").isFaulty())
		
		assertFalse(createJsonObj<SignalTime>(
			"""{"random": true, "frequency": 1, "minutesBetween": 2, "startTimeOfDay": 60000, "endTimeOfDay": 180000}"""
		).isFaulty())
		
		assertTrue(createJsonObj<SignalTime>(
			"""{"random": true, "frequency": 1, "minutesBetween": 2, "startTimeOfDay": 60000, "endTimeOfDay": 179999}"""
		).isFaulty())
		
		assertFalse(createJsonObj<SignalTime>(
			"""{"random": true, "frequency": 3, "minutesBetween": 2, "startTimeOfDay": 60000, "endTimeOfDay": 540000}"""
		).isFaulty())
		
		assertTrue(createJsonObj<SignalTime>(
			"""{"random": true, "frequency": 3, "minutesBetween": 2, "startTimeOfDay": 60000, "endTimeOfDay": 539999}"""
		).isFaulty())
	}
	
	@Test
	fun isDifferent() {
		val signalTime = createJsonObj<SignalTime>(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)
		
		assertTrue(signalTime.isDifferent(createJsonObj(
			"""{"label":"qwe2", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createJsonObj(
			"""{"label":"qwe", "random":false, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createJsonObj(
			"""{"label":"qwe", "random":true, "randomFixed":true, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createJsonObj(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":1, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createJsonObj(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":31, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createJsonObj(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1100, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createJsonObj(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 62000}"""
		)))
		
		//startTimeOfDay and endTimeOfDay become originalStartTimeOfDay and originalEndTimeOfDay after save:
		
		signalTime.save()
		val signalTimeSame = createJsonObj<SignalTime>(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)
		signalTimeSame.save()
		val signalTimeDifferent1 = createJsonObj<SignalTime>(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 6000}"""
		)
		signalTimeDifferent1.save()
		val signalTimeDifferent2 = createJsonObj<SignalTime>(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1040, "endTimeOfDay": 60000}"""
		)
		signalTimeDifferent2.save()
		
		assertFalse(signalTime.isDifferent(signalTimeSame))
		assertTrue(signalTime.isDifferent(signalTimeDifferent1))
		assertTrue(signalTime.isDifferent(signalTimeDifferent2))
	}
	
	@Test
	fun setStart() {
		//we have to be careful because of the users timezone:
		val targetDate = NativeLink.getMidnightMillis(1114313512000) + 12712000 // 2005-04-24 03:31:52
		
		val oneDay = 1000*60*60*24
		val signalTime1 = createJsonObj<SignalTime>("""{"random": true, "endTimeOfDay": 12711999}""") //03:51:51:0999
		signalTime1.setStart(targetDate)
		
		assertTrue(signalTime1.timeHasChanged)
		assertEquals(12712000, signalTime1.startTimeOfDay) //03:31:52
		assertEquals(12711999 + oneDay, signalTime1.endTimeOfDay)
		
		
		val signalTime2 = createJsonObj<SignalTime>("""{"random": true, "endTimeOfDay": 12712001}""") //03:51:52:0001
		signalTime2.setStart(targetDate)
		assertEquals(12712001, signalTime2.endTimeOfDay)
	}
	
	@Test
	fun setEnd() {
		//we have to be careful because of the users timezone:
		val targetDate = NativeLink.getMidnightMillis(1114313512000) + 12712000 // 2005-04-24 03:31:52
		
		val oneDay = 1000*60*60*24
		val signalTime1 = createJsonObj<SignalTime>("""{"random": true, "startTimeOfDay": 12711999}""") //03:51:51:0999
		signalTime1.setEnd(targetDate)
		
		assertTrue(signalTime1.timeHasChanged)
		assertEquals(12712000, signalTime1.endTimeOfDay) //03:31:52
		
		
		val signalTime2 = createJsonObj<SignalTime>("""{"random": true, "startTimeOfDay": 12712001}""") //03:51:52:0001
		signalTime2.setEnd(targetDate)
		assertEquals(12712000 + oneDay, signalTime2.endTimeOfDay) //03:31:52 + one day
	}
	
	@Test
	fun getStart() {
		val signalTime = createJsonObj<SignalTime>("""{"random": true, "startTimeOfDay": 1234}""")
		assertEquals(NativeLink.getMidnightMillis() + 1234, signalTime.getStart())
	}
	
	@Test
	fun getEnd() {
		val signalTime = createJsonObj<SignalTime>("""{"random": true, "endTimeOfDay": 96354}""")
		assertEquals(NativeLink.getMidnightMillis() + 96354, signalTime.getEnd())
	}
	
	@Test
	fun getFormattedStart() {
		//we have to be careful because of the users timezone:
		val targetDate = NativeLink.getMidnightMillis(1114313512000) + 12712000 // 2005-04-24 03:31:52
		
		val signalTime = createJsonObj<SignalTime>("""{"random": true, "startTimeOfDay": 12712000}""") // 03:31:52
		assertEquals(NativeLink.formatTime(targetDate), signalTime.getFormattedStart()) // 2005-04-24 03:31:52
	}
	
	@Test
	fun getFormattedEnd() {
		//we have to be careful because of the users timezone:
		val targetDate = NativeLink.getMidnightMillis(1114313512000) + 12712000 // 2005-04-24 03:31:52
		
		val signalTime = createJsonObj<SignalTime>("""{"random": true, "endTimeOfDay": 12712000}""") // 03:31:52
		assertEquals(NativeLink.formatTime(targetDate), signalTime.getFormattedEnd()) // 2005-04-24 03:31:52
	}
	
	@Test
	fun save() {
		val signalTime = createJsonObj<SignalTime>("""{"startTimeOfDay":123}""")
		signalTime.save()
		assertSqlWasSaved(SignalTime.TABLE, SignalTime.KEY_START_TIME_OF_DAY, 123)
		signalTime.save()
		assertSqlWasUpdated(SignalTime.TABLE, SignalTime.KEY_START_TIME_OF_DAY, 123)
	}
	
	@Test
	fun saveTimeFrames() {
		val schedule = createJsonObj<Schedule>()
		schedule.bindParent(createActionTrigger())
		val signalTime = createJsonObj<SignalTime>("""{"startTimeOfDay":123}""")
		signalTime.exists = true
		signalTime.id = 123
		signalTime.timeHasChanged = true
		signalTime.bindParent(schedule.getQuestionnaire().id, schedule)
		
		signalTime.saveTimeFrames(schedule, true)
		assertSqlWasUpdated(SignalTime.TABLE, SignalTime.KEY_START_TIME_OF_DAY, signalTime.startTimeOfDay)
		assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_SIGNAL_TIME_ID, 123L)
	}
}