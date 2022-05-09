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
		alarmAndroid.label = "android $daySpacing days"
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
			alarmIOS.label = "iOS $daySpacing days; number $i"
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
			"UPDATE ${DbLogic.User.TABLE} SET ${DbLogic.User.KEY_NOTIFICATIONS_MISSED} = ${DbLogic.User.KEY_NOTIFICATIONS_MISSED} + 1"
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
				signalTime.bindParent(-1, schedule)
				
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
	
	@Test
	fun scheduleSignalTime() {
		val timestampMidnight = NativeLink.getMidnightMillis(1114313512000) //2005-04-24 03:31:52
		val randomBlockSize = 1000*60*60
		val minutesBetween = 60
		
		var scheduleId = 1L
		for(manualDelayDays in -1 until 10) {
			for(dailyRepeatRate in 1 until 10) {
				for(loopFrequency in 0 until 10) {
					val random = loopFrequency != 0
					val frequency = if(random) loopFrequency else 1
					
					val period = frequency * (minutesBetween * 1000 * 60 + randomBlockSize)
					val startTimeOfDay = Random.nextInt(0, (ONE_DAY_MS - period).toInt())
					val endTimeOfDay = startTimeOfDay + period
					val timestampNow = timestampMidnight + startTimeOfDay - Scheduler.MIN_SCHEDULE_DISTANCE
					
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
					signalTime.bindParent(-1, schedule)
					
					
					Scheduler.scheduleSignalTime(signalTime, -1, timestampNow, manualDelayDays)
					
					
					//check alarm:
					
					val alarms = DbLogic.getAlarms(schedule) //only get alarms from this loop (schedule.id)
					val errorInfo = "\nnow: $timestampNow,\n" +
						"midnight: $timestampMidnight,\n" +
						"startTimeOfDay: $startTimeOfDay,\n" +
						"endTimeOfDay: $endTimeOfDay,\n" +
						"frequency: $frequency,\n" +
						"minutesBetween: $minutesBetween,\n" +
						"dailyRepeatRate: $dailyRepeatRate,\n" +
						"random: $random,\n" +
						"manualDelayDays: $manualDelayDays\n"
					
					assertEquals(
						frequency,
						alarms.size,
						"Failed with $errorInfo"
					)
					val delay = if(manualDelayDays == -1) ONE_DAY_MS * dailyRepeatRate else manualDelayDays * ONE_DAY_MS
					val min = timestampMidnight + startTimeOfDay + delay
					val max = timestampMidnight + endTimeOfDay + delay
					val lastTimeStamp = 0L
					for((i, alarm) in alarms.withIndex()) {
						if(random) {
							val timestamp = alarm.timestamp
							assertTrue(
								timestamp in min..max,
								"$timestamp for alarm $i is not between $min and $max. Settings: $errorInfo"
							)
							assertTrue(
								timestamp > lastTimeStamp + minutesBetween,
								"$timestamp needs to be greater than ${lastTimeStamp + minutesBetween}. Settings: $errorInfo"
							)
						}
						else {
							assertEquals(
								timestampMidnight + startTimeOfDay + delay,
								alarm.timestamp,
								"Failed alarm number $i. Settings: $errorInfo"
							)
						}
					}
				}
			}
		}
	}
	
	@Test
	fun scheduleEventTrigger() {
		val now = 626637180000
		Scheduler.scheduleEventTrigger(createJsonObj<EventTrigger>("""{
				"delaySec": 5
			}"""), now)
		Scheduler.scheduleEventTrigger(createJsonObj<EventTrigger>("""{
				"randomDelay": true
				"delayMinimumSec": 10
				"delaySec": 15
			}"""), now)
		
		
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
	fun considerScheduleOptions() {
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
				Scheduler.considerScheduleOptions(
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
		
		val alarmReminder = Alarm.createAsReminder(now, questionnaire.id, -1, "", 0, 1)
		
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