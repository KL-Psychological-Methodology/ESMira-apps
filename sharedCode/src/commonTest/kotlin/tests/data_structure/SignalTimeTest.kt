package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class SignalTimeTest : BaseDataStructureTest() {
	
	@Test
	fun testFaultySignalTime() {
		val signalTime = createObj<SignalTime>("""{"random": false, "randomFixed": true, "frequency": 5}""")
		
		assertFalse(signalTime.random)
		assertFalse(signalTime.randomFixed)
		assertEquals(1, signalTime.frequency)
	}
	
	@Test
	fun isFaulty() {
		assertFalse(createObj<SignalTime>("""{"random": false}""").isFaulty())
		
		assertFalse(createObj<SignalTime>(
			"""{"random": true, "frequency": 1, "minutesBetween": 2, "startTimeOfDay": 60000, "endTimeOfDay": 180000}"""
		).isFaulty())
		
		assertTrue(createObj<SignalTime>(
			"""{"random": true, "frequency": 1, "minutesBetween": 2, "startTimeOfDay": 60000, "endTimeOfDay": 179999}"""
		).isFaulty())
		
		assertFalse(createObj<SignalTime>(
			"""{"random": true, "frequency": 3, "minutesBetween": 2, "startTimeOfDay": 60000, "endTimeOfDay": 540000}"""
		).isFaulty())
		
		assertTrue(createObj<SignalTime>(
			"""{"random": true, "frequency": 3, "minutesBetween": 2, "startTimeOfDay": 60000, "endTimeOfDay": 539999}"""
		).isFaulty())
	}
	
	@Test
	fun isDifferent() {
		val signalTime = createObj<SignalTime>(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)
		
		assertTrue(signalTime.isDifferent(createObj(
			"""{"label":"qwe2", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createObj(
			"""{"label":"qwe", "random":false, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createObj(
			"""{"label":"qwe", "random":true, "randomFixed":true, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createObj(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":1, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createObj(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":31, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createObj(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1100, "endTimeOfDay": 60000}"""
		)))
		
		assertTrue(signalTime.isDifferent(createObj(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 62000}"""
		)))
		
		//startTimeOfDay and endTimeOfDay become originalStartTimeOfDay and originalEndTimeOfDay after save:
		
		signalTime.save()
		val signalTimeSame = createObj<SignalTime>(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 60000}"""
		)
		signalTimeSame.save()
		val signalTimeDifferent1 = createObj<SignalTime>(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1000, "endTimeOfDay": 6000}"""
		)
		signalTimeDifferent1.save()
		val signalTimeDifferent2 = createObj<SignalTime>(
			"""{"label":"qwe", "random":true, "randomFixed":false, "frequency":2, "minutesBetween":30, "startTimeOfDay": 1040, "endTimeOfDay": 60000}"""
		)
		signalTimeDifferent2.save()
		
		assertFalse(signalTime.isDifferent(signalTimeSame))
		assertTrue(signalTime.isDifferent(signalTimeDifferent1))
		assertTrue(signalTime.isDifferent(signalTimeDifferent2))
	}
	
	@Test
	fun setStart() {
		val oneDay = 1000*60*60*24
		val signalTime1 = createObj<SignalTime>("""{"random": true, "endTimeOfDay": 12711999}""")
		signalTime1.setStart(1114306312000) // 2005-04-24 03:31:52
		
		assertTrue(signalTime1.timeHasChanged)
		assertEquals(12712000, signalTime1.startTimeOfDay) //03:31:52
		assertEquals(12711999 + oneDay, signalTime1.endTimeOfDay)
		
		
		val signalTime2 = createObj<SignalTime>("""{"random": true, "endTimeOfDay": 12712001}""")
		signalTime2.setStart(1114306312000) // 2005-04-24 03:31:52
		assertEquals(12712001, signalTime2.endTimeOfDay)
	}
	
	@Test
	fun setEnd() {
		val oneDay = 1000*60*60*24
		val signalTime1 = createObj<SignalTime>("""{"random": true, "startTimeOfDay": 12711999}""")
		signalTime1.setEnd(1114306312000) // 2005-04-24 03:31:52
		
		assertTrue(signalTime1.timeHasChanged)
		assertEquals(12712000, signalTime1.endTimeOfDay) //03:31:52
		
		
		val signalTime2 = createObj<SignalTime>("""{"random": true, "startTimeOfDay": 12712001}""")
		signalTime2.setEnd(1114306312000) // 2005-04-24 03:31:52
		assertEquals(12712000 + oneDay, signalTime2.endTimeOfDay) //03:31:52 + one day
	}
	
	@Test
	fun getStart() {
		val signalTime = createObj<SignalTime>("""{"random": true, "startTimeOfDay": 1234}""")
		assertEquals(NativeLink.getMidnightMillis() + 1234, signalTime.getStart())
	}
	
	@Test
	fun getEnd() {
		val signalTime = createObj<SignalTime>("""{"random": true, "endTimeOfDay": 96354}""")
		assertEquals(NativeLink.getMidnightMillis() + 96354, signalTime.getEnd())
	}
	
	@Test
	fun getFormattedStart() {
		val signalTime = createObj<SignalTime>("""{"random": true, "startTimeOfDay": 12712000}""") // 03:31:52
		assertEquals(NativeLink.formatTime(1114306312000), signalTime.getFormattedStart()) // 2005-04-24 03:31:52
	}
	
	@Test
	fun getFormattedEnd() {
		val signalTime = createObj<SignalTime>("""{"random": true, "endTimeOfDay": 12712000}""") // 03:31:52
		assertEquals(NativeLink.formatTime(1114306312000), signalTime.getFormattedEnd()) // 2005-04-24 03:31:52
	}
	
	@Test
	fun save() {
		val signalTime = createObj<SignalTime>("""{"startTimeOfDay":123}""")
		signalTime.save()
		mockTools.assertSqlWasSaved(SignalTime.TABLE, SignalTime.KEY_START_TIME_OF_DAY, 123)
		signalTime.save()
		mockTools.assertSqlWasUpdated(SignalTime.TABLE, SignalTime.KEY_START_TIME_OF_DAY, 123)
	}
	
	@Test
	fun saveTimeFrames() {
		val schedule = createObj<Schedule>()
		schedule.bindParent(createActionTrigger())
		val signalTime = createObj<SignalTime>("""{"startTimeOfDay":123}""")
		signalTime.exists = true
		signalTime.id = 123
		signalTime.timeHasChanged = true
		signalTime.bindParent(schedule.getQuestionnaire().id, schedule)
		
		signalTime.saveTimeFrames(schedule, true)
		mockTools.assertSqlWasUpdated(SignalTime.TABLE, SignalTime.KEY_START_TIME_OF_DAY, signalTime.startTimeOfDay)
		mockTools.assertSqlWasSaved(Alarm.TABLE, Alarm.KEY_SIGNAL_TIME_ID, 123L)
	}
}