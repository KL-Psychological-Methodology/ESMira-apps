package tests

import BaseCommonTest
import at.jodlidev.esmira.sharedCode.*
import at.jodlidev.esmira.sharedCode.data_structure.*
import io.ktor.util.date.*
import kotlin.random.Random
import kotlin.test.*

/**
 * Created by JodliDev on 26.04.2022.
 */
class SchedulerTest : BaseCommonTest() {
	private val ONE_DAY_MS: Long = 1000*60*60*24
	private var scheduleId: Long = 0
	
	private fun assertCheckMissedAlarms(questionnaire: Questionnaire, daySpacing: Int) {
		val schedule = createScheduleForSaving("""{"dailyRepeatRate": $daySpacing, "signalTimes": [{}]}""")
		schedule.saveAndScheduleIfExists() //will create an additional alarm for signalTimes - but it will be ignored because its in the future
		
		val now = NativeLink.getNowMillis()
		val missedCount = Random.nextInt(2, 10)
		val days = (daySpacing * missedCount).toLong()
		val timestamp = now - ONE_DAY_MS*days - Scheduler.TREAT_AS_MISSED_LEEWAY_MS - 1
		
		
		//test Android:
		
		setPhoneType(PhoneType.Android)
		
		//on Android there is always only one alarm
		val alarmAndroid = createAlarmFromSignalTime(timestamp = timestamp)
		alarmAndroid.scheduleId = schedule.id
		alarmAndroid.questionnaireId = questionnaire.id //we need a questionnaire or DbLogic.reportMissedInvitation() will not be called
		alarmAndroid.save()
		
		println("Testing for Android with dailyRepeatRate:$daySpacing and $missedCount missed days")
		assertCheckMissedAlarmsAfterPrep(missedCount)
		
		
		//test iOS:
		
		setPhoneType(PhoneType.IOS)
		
		val spacing = daySpacing*ONE_DAY_MS
		val actionTrigger = createActionTrigger()
		actionTrigger.save(true)
		
		//on iOS every alarm is pre scheduled:
		for(i in 0 .. missedCount) {
			val alarmIOS = createAlarmFromSignalTime(timestamp = timestamp - spacing*i)
			alarmIOS.scheduleId = schedule.id
			alarmIOS.actionTriggerId = actionTrigger.id //alarm.save() will call scheduleReminder() on iOS which needs an actionTrigger
			alarmIOS.questionnaireId = questionnaire.id //we need a questionnaire or DbLogic.reportMissedInvitation() will not be called
			alarmIOS.save()
		}
		
		println("Testing for iOS with dailyRepeatRate:$daySpacing and $missedCount missed days")
		assertCheckMissedAlarmsAfterPrep(missedCount)
	}
	
	private fun assertCheckMissedAlarmsAfterPrep(missedCount: Int) {
		val isAndroid = NativeLink.smartphoneData.phoneType == PhoneType.Android
		
		dialogOpener.reset()
		postponedActions.reset()
		
		countQueries(
			missedCount,
			"UPDATE ${DbUser.TABLE} SET ${DbUser.KEY_NOTIFICATIONS_MISSED} = ${DbUser.KEY_NOTIFICATIONS_MISSED} + 1"
		) {
			Scheduler.checkMissedAlarms(true)
		}
		assertEquals(if(isAndroid) 1 else missedCount+1, postponedActions.cancelList.size) //done in exec()
		assertEquals(
			if(isAndroid) 1 else 0,
			dialogOpener.notificationsBrokenCount
		)
	}
	@Test
	fun checkMissedAlarms() {
		val now = NativeLink.getNowMillis()
		
		//create alarms that will not count:
		//Scheduler.checkMissedAlarms() uses a different now. So we need to account for execution time
		createAlarmFromSignalTime(timestamp = now+1000*60).save()
		createAlarmFromSignalTime(timestamp = now+2000*60).save()
		createAlarmFromSignalTime(timestamp = now+3000*60).save()
		createAlarmFromSignalTime(timestamp = now+4000*60).save()
		
		//Preparation:
		// durationEnd: we dont want questionnaire to be active or alarm.exec() will crash
		val questionnaire = createJsonObj<Questionnaire>("""{"durationEnd": ${now-1000}}""")
		questionnaire.studyId = getBaseStudyId() //DbLogic.reportMissedInvitation() needs a study when creating a DataSet
		questionnaire.save(true)
		
		//Tests:
		for(days in 1 until 10) {
			assertCheckMissedAlarms(questionnaire, days)
		}
	}
	
	@Test
	fun reactToBootOrTimeChange() {
		val pastAlarmsNum = 9
		val futureAlarmsNum = 15
		val now = NativeLink.getNowMillis() + Scheduler.MIN_SCHEDULE_DISTANCE
		
		// durationEnd: we dont want questionnaire to be active or alarm.exec() will crash
		val questionnaire = createJsonObj<Questionnaire>("""{"durationEnd": ${now-1000}}""")
		questionnaire.studyId = getBaseStudyId() //DbLogic.reportMissedInvitation() needs a study when creating a DataSet
		questionnaire.save(true)
		
		for(i in 0 until pastAlarmsNum) {
			createAlarmFromSignalTime(timestamp = now-1).save()
		}
		for(i in 0 until futureAlarmsNum) {
			createAlarmFromSignalTime(timestamp = now+1000*60).save()
		}
		postponedActions.reset()
		
		//test reboot (only used on Android):
		Scheduler.reactToBootOrTimeChange(false)
		assertEquals(pastAlarmsNum, postponedActions.cancelList.size) //done in exec()
		assertEquals(futureAlarmsNum, postponedActions.scheduleAlarmList.size) //done in schedule()
		
		//test time change:
		//pastAlarmsNum are deleted now
		postponedActions.reset()
		Scheduler.reactToBootOrTimeChange(true)
		assertEquals(futureAlarmsNum, postponedActions.cancelList.size)
		assertEquals(futureAlarmsNum, postponedActions.scheduleAlarmList.size) //done in schedule()
	}
	
	@Test
	fun rescheduleFromSignalTime() {
		val timestamps = arrayOf(
			arrayOf(626637180000, 17L, 53L, 0L),
			arrayOf(1114313512000, 3L, 31L, 52L)
		)
		val nowDate = GMTDate(NativeLink.getNowMillis())

        val study = createStudy("""{"id":1234}""")
		study.save()
		
		val questionnaire = createJsonObj<Questionnaire>()
        questionnaire.studyId = study.id
		questionnaire.save(true)

		var idCount = 1L
		for(dailyRepeatRate in 1 until 7) {
			for((timestamp, hours, minutes, seconds) in timestamps) {
				val targetTimestamp = GMTDate(
					seconds = seconds.toInt(),
					minutes = minutes.toInt(),
					hours = hours.toInt(),
					dayOfMonth = nowDate.dayOfMonth,
					month = nowDate.month,
					year = nowDate.year
				).timestamp
				
				val schedule = createJsonObj<Schedule>("""{"dailyRepeatRate": $dailyRepeatRate}""")
				schedule.id = idCount++
				val signalTime = createJsonObj<SignalTime>("""{"random": true, "randomFixed": true}""")
				signalTime.bindParent(questionnaire.id, schedule)
				
				Scheduler.rescheduleFromSignalTime(signalTime, -1, timestamp)
				
				val alarms = DbLogic.getAlarms(schedule) //only get alarms from this loop (schedule.id)
				assertEquals(1, alarms.size)
				val alarmTimestamp = alarms[0].timestamp
				val alarmDate = GMTDate(alarmTimestamp)
				assertEquals(targetTimestamp + dailyRepeatRate * ONE_DAY_MS, alarmTimestamp, "check for $timestamp with dailyRepeatRate:$dailyRepeatRate failed!")
				assertEquals(hours.toInt(), alarmDate.hours, "check for $timestamp with dailyRepeatRate:$dailyRepeatRate failed!")
				assertEquals(minutes.toInt(), alarmDate.minutes, "check for $timestamp with dailyRepeatRate:$dailyRepeatRate failed!")
				assertEquals(seconds.toInt(), alarmDate.seconds, "check for $timestamp with dailyRepeatRate:$dailyRepeatRate failed!")
			}
		}
		
	}
	fun scheduleAndCheckSignalTimes(
		questionnaireId: Long,
		timestampNow: Long,
		startTimeOfDay: Int,
		endTimeOfDay: Int,
		expectToSkipFirstDay: Boolean,
		alarmStart: Int = startTimeOfDay,
		alarmEnd: Int = endTimeOfDay,
	) {
		val minutesBetween = 30
		val timestampMidnight = NativeLink.getMidnightMillis(timestampNow)
		var loopI = 0
		
		for(manualDelayDays in -1 until 5) {
			for(dailyRepeatRate in 1 until 5) {
				for(loopFrequency in 0 until 5) {
					val random = loopFrequency != 0
					val frequency = if(random) loopFrequency else 1
					
					val schedule = createJsonObj<Schedule>("""{"dailyRepeatRate": $dailyRepeatRate}""")
					schedule.id = scheduleId++
					val signalTime = createJsonObj<SignalTime>(
						"""{
							"frequency": $frequency
							"minutesBetween": $minutesBetween
							"random": $random
							"startTimeOfDay": $startTimeOfDay
							"endTimeOfDay": $endTimeOfDay
						}"""
					)
					signalTime.bindParent(questionnaireId, schedule)
					Scheduler.scheduleSignalTime(signalTime, -1, timestampNow, manualDelayDays)
					
					
					//check alarm:
					
					val alarms = DbLogic.getAlarms(schedule) //only get alarms from this loop (schedule.id)
					val errorInfo = "\nLoop: ${++loopI}\n" +
							"expectToSkipFirstDay: $expectToSkipFirstDay,\n" +
							"now: $timestampNow,\n" +
							"midnight: $timestampMidnight,\n" +
							"startTimeOfDay: $startTimeOfDay,\n" +
							"endTimeOfDay: $endTimeOfDay,\n" +
							"frequency: $frequency,\n" +
							"minutesBetween: $minutesBetween,\n" +
							"dailyRepeatRate: $dailyRepeatRate,\n" +
							"random: $random,\n" +
							"manualDelayDays: $manualDelayDays\n"
					
					val delay = if(manualDelayDays == -1) {
						ONE_DAY_MS * dailyRepeatRate
					}
					else {
						manualDelayDays * ONE_DAY_MS + if(expectToSkipFirstDay) ONE_DAY_MS else 0
					}
					
					val min = timestampMidnight + alarmStart + delay
					val max = timestampMidnight + alarmEnd + delay
					
					assertEquals(
						frequency,
						alarms.size,
						"The wrong number of alarms was scheduled. Failed with $errorInfo"
					)
					
					val lastTimeStamp = 0L
					for((i, alarm) in alarms.withIndex()) {
						if(random) {
							val timestamp = alarm.timestamp
							assertTrue(
								timestamp in min..max,
								"$timestamp is not between $min and $max. Settings: \nalarm: $i$errorInfo"
							)
							assertTrue(
								timestamp > lastTimeStamp + minutesBetween,
								"$timestamp needs to be greater than ${lastTimeStamp + minutesBetween}. Settings: \nalarm: $i$errorInfo"
							)
						}
						else {
							assertEquals(
								min,
								alarm.timestamp,
								"Wrong timestamp. Settings: \nalarm: $i$errorInfo"
							)
						}
					}
				}
			}
		}
	}
	
	@Test
	fun scheduleSignalTime() {
		val study = createStudy("""{"id":1234}""")
		study.save()
		
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.studyId = study.id
		questionnaire.save(true)
		
		scheduleAndCheckSignalTimes(
			questionnaire.id,
			1114306312000, //2005-04-24 03:31:52
			7200000, //02:00
			72000000, //20:00
			true
		)
		scheduleAndCheckSignalTimes(
			questionnaire.id,
			1114306312000, //2005-04-24 03:31:52
			21600000, //06:00
			72000000, //20:00
			false
		)
	}
	
	@Test
	fun scheduleFilteredSignalTime() {
		val study = createStudy("""{"id":1234}""")
		study.save()
		
		val completableAtSpecificTimeStart = 25200000 //07:00
		val completableAtSpecificTimeEnd = 75600000 //21:00
		val questionnaire = createJsonObj<Questionnaire>("""{"completableAtSpecificTime": true, "completableAtSpecificTimeStart": $completableAtSpecificTimeStart, "completableAtSpecificTimeEnd": $completableAtSpecificTimeEnd}""")
		questionnaire.studyId = study.id
		questionnaire.save(true)
		
		//Filter has no effect:
		scheduleAndCheckSignalTimes(
			questionnaire.id,
			1114306312000, //2005-04-24 03:31:52
			28800000, //08:00
			72000000, //20:00
			false
		)
		
		//Filter reduces start:
		scheduleAndCheckSignalTimes(
			questionnaire.id,
			1114306312000, //2005-04-24 03:31:52
			7200000, //02:00
			72000000, //20:00
			false,
			25200000 //07:00
		)
		
		//Filter reduces end:
		scheduleAndCheckSignalTimes(
			questionnaire.id,
			1114306312000, //2005-04-24 03:31:52
			28800000, //08:00
			79200000, //22:00
			false,
			28800000, //08:00
			75600000 //21:00
		)
		
		
		//Filter reduces both:
		scheduleAndCheckSignalTimes(
			questionnaire.id,
			1114306312000, //2005-04-24 03:31:52
			3600000, //01:00
			82800000, //23:00
			false,
			25200000, //07:00
			75600000 //21:00
		)
	}
	
	
	@Test
	fun scheduleEventTrigger() {
		val now = 626637180000

        val study = createStudy("""{"id":1234}""")
        study.save()

        val questionnaire = createJsonObj<Questionnaire>()
        questionnaire.studyId = study.id
        questionnaire.save(true)

        val eventTrigger1 = createJsonObj<EventTrigger>("""{
				"delaySec": 5
			}""")
        eventTrigger1.questionnaireId = questionnaire.id

		val eventTrigger2 = createJsonObj<EventTrigger>("""{
				"randomDelay": true
				"delayMinimumSec": 10
				"delaySec": 15
			}""")
		eventTrigger2.questionnaireId = questionnaire.id

		Scheduler.scheduleEventTrigger(eventTrigger1, now)
		Scheduler.scheduleEventTrigger(eventTrigger2, now)
		
		
		val alarms = DbLogic.getAlarms()
		
		assertEquals(2, alarms.size)
		assertEquals(now + 5*1000, alarms[0].timestamp)
		
		
		val randomTimestamp = alarms[1].timestamp
		val expected = now + 10*1000
		assertTrue(
			randomTimestamp in expected .. (expected + 15*1000),
			"$randomTimestamp should be between $expected and ${expected + 15*1000}"
		)
	}
	
	@Test
	fun considerDayOptions_signalTime() {
		val timestamp = 626637180000
		val schedule = createJsonObj<Schedule>()
		val signalTime = createJsonObj<SignalTime>()
		signalTime.bindParent(-1, createJsonObj())
		
		//Check questionnaire that will be active in the future
		val questionnaire1 = createJsonObj<Questionnaire>("""{"durationStart": ${timestamp + 10}}""")
		questionnaire1.save(true)
		signalTime.bindParent(questionnaire1.id, schedule)
		assertNotEquals(-1, Scheduler.considerDayOptions(timestamp, questionnaire1, schedule))
		
		//Check questionnaire that will never be active and should result in -1
		val questionnaire2 = createJsonObj<Questionnaire>("""{"durationEnd": ${timestamp - 10}}""")
		questionnaire2.save(true)
		signalTime.bindParent(questionnaire2.id, schedule)
		assertEquals(-1, Scheduler.considerDayOptions(timestamp, questionnaire2, schedule))
	}
	
	@Test
	fun considerDayOptions_schedule() {
		val timestamp1 = 626637180000 // Thu Nov 09 1989 18:53:00 GMT+0100
		val timestampEndOfYear = 629229180000 // Sat Dec 09 1989 18:53:00 GMT+0100
		
		val rounds = arrayOf(
			//no changes:
			Triple(0, 0, Pair(timestamp1, 626637180000)), //Thu Nov 09 1989 18:53:00 GMT+0100
			//every weekday:
			Triple(127, 0, Pair(timestamp1, 626637180000)), //Thu Nov 09 1989 18:53:00 GMT+0100
			
			//testing only weekdays:
			Triple(1, 0, Pair(timestamp1, 626896380000)), //Sun Nov 12 1989 18:53:00 GMT+0100
			Triple(2, 0, Pair(timestamp1, 626982780000)), //Mon Nov 13 1989 18:53:00 GMT+0100
			Triple(4, 0, Pair(timestamp1, 627069180000)), //Tue Nov 14 1989 18:53:00 GMT+0100
			Triple(8, 0, Pair(timestamp1, 627155580000)), //Wed Nov 15 1989 18:53:00 GMT+0100
			Triple(16, 0, Pair(timestamp1, 626637180000)), //Thu Nov 09 1989 18:53:00 GMT+0100
			Triple(32, 0, Pair(timestamp1, 626723580000)), //Fri Nov 10 1989 18:53:00 GMT+0100
			Triple(64, 0, Pair(timestamp1, 626809980000)), //Sat Nov 11 1989 18:53:00 GMT+0100
			
			//testing only dayOfMonth:
			Triple(0, 8, Pair(timestampEndOfYear, 631821180000)), //Mon Jan 08 1990 18:53:00 GMT+0100
			Triple(0, 8, Pair(timestamp1, 629142780000)), //Fri Dec 08 1989 18:53:00 GMT+0100
			Triple(0, 9, Pair(timestamp1, 626637180000)), //Thu Nov 09 1989 18:53:00 GMT+0100
			Triple(0, 10, Pair(timestamp1, 626723580000)), //Fri Nov 10 1989 18:53:00 GMT+0100
			
			//testing both:
			Triple(34, 11, Pair(timestamp1, 626982780000)), //Sat Nov 11 1989 18:53:00 GMT+0100 > (Mon, Fr) > Mon Nov 13 1989 18:53:00 GMT+0100
			Triple(127, 12, Pair(timestamp1, 626896380000)), //Sun Nov 12 1989 18:53:00 GMT+0100 > (every weekday) > Sun Nov 12 1989 18:53:00 GMT+0100
		
		)
		
		for((weekdayCode, dayOfMonth, timestamps) in rounds) {
			val weekdayJson = if(weekdayCode != 0) """"weekdays": $weekdayCode""" else ""
			val dayOfMonthJson = if(dayOfMonth != 0) """"dayOfMonth":$dayOfMonth""" else ""
			val separator = if(weekdayJson.isNotEmpty() && dayOfMonthJson.isNotEmpty()) "," else ""
			val json = """{$weekdayJson$separator$dayOfMonthJson}"""
			assertEquals(
				timestamps.second,
				Scheduler.considerDayOptions(
					timestamps.first, createJsonObj<Schedule>(json)
				),
				"Failed with weekday: $weekdayCode, dayOfMonth: $dayOfMonth, now: ${timestamps.first}, expected: ${timestamps.second}"
			)
		}
	}
	
	@Test
	fun remove() {
		val now = NativeLink.getNowMillis()
		
		val eventTrigger = createJsonObj<EventTrigger>()
		eventTrigger.id = 5
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.id = 6
		val schedule = createScheduleForSaving()
		schedule.id = 7
		val signalTime = createJsonObj<SignalTime>()
		signalTime.id = 8
		
		val alarmReminder = Alarm.createAsReminder(now, questionnaire.id, -1, 0, 1)
		
		val alarmEventTrigger = Alarm.createFromEventTrigger(eventTrigger, now)
		
		val alarmSignalTime = Alarm.createFromSignalTime(signalTime, -1, now)
		
		signalTime.bindParent(questionnaire.id, createJsonObj())
		val alarmQuestionnaire = Alarm.createFromSignalTime(signalTime, -1, now)
		
		signalTime.bindParent(-1, schedule)
		val alarmSchedule = Alarm.createFromSignalTime(signalTime, -1, now)
		
		Scheduler.remove(eventTrigger)
		assertNull(DbLogic.getAlarm(alarmEventTrigger.id))
		assertNotNull(DbLogic.getAlarm(alarmQuestionnaire.id))
		assertNotNull(DbLogic.getAlarm(alarmReminder.id))
		assertNotNull(DbLogic.getAlarm(alarmSchedule.id))
		assertNotNull(DbLogic.getAlarm(alarmSignalTime.id))
		
		Scheduler.remove(questionnaire)
		assertNull(DbLogic.getAlarm(alarmQuestionnaire.id))
		assertNull(DbLogic.getAlarm(alarmReminder.id))
		assertNotNull(DbLogic.getAlarm(alarmSchedule.id))
		assertNotNull(DbLogic.getAlarm(alarmSignalTime.id))
		
		Scheduler.remove(schedule)
		assertNull(DbLogic.getAlarm(alarmSchedule.id))
		assertNotNull(DbLogic.getAlarm(alarmSignalTime.id))
		
		Scheduler.remove(signalTime)
		assertNull(DbLogic.getAlarm(alarmSignalTime.id))
	}
}