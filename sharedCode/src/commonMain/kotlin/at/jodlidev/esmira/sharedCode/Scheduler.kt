package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.*
import io.ktor.util.date.GMTDate
import io.ktor.util.date.Month
import io.ktor.util.date.plus
import kotlin.math.ceil
import kotlin.math.max
import kotlin.random.Random

/**
 * Created by JodliDev on 12.06.2020.
 * In general:
 * As soon as the actions of an alarm has been issued, the Alarm is deleted
 * alarms reschedule itself (if needed) when its actions are issued
 *
 * On Android:
 * Alarms will be scheduled directly and actions will be issued by the system when the alarm is due
 * that means that when checkMissedAlarms() finds alarms that are due, we know that neither the notifications nor the actions have been issued
 * a service checks for time changes and reboots and calls reactToBootOrTimeChange() which reschedules all alarms on time change and issues actions for alarms that have been missed while the phone was shut down
 *
 * On IOS:
 * we use pendingNotifications to schedule alarms (done in ActionTrigger.execAsPostponedNotifications())
 * no code will run when the notification is triggered
 * The actions that belong to an Alarm (which is scheduled by a pendingNotification) will be issued when the user reacts to the notification (which is issued when the Alarm is due)
 * if the user opens ESMira directly without reacting to the notification, checkMissedAlarms() will issue all actions (except notifications)
 * We also have a service that runs every day and makes sure that alarms are scheduled for the next day (by calling scheduleAhead())
 * At app startup we check if the phone was rebooted since last time and call reactToBootOrTimeChange()
 * Apart from that, we assume that notifications are never missed by the system. So we dont issue notifications from checkMissedAlarms()
 */
object Scheduler {
	internal const val TREAT_AS_MISSED_LEEWAY_MS = 200
	internal const val ONE_DAY_MS: Long = 86400000 //1000*60*60*24
	const val MIN_SCHEDULE_DISTANCE = 60000L //1000*60 = 1 min
	internal const val IOS_DAYS_TO_SCHEDULE_AHEAD_MS = 4 * ONE_DAY_MS
	internal const val IOS_MAX_TIMES_TO_SCHEDULE_AHEAD = 4
	private const val IOS_MAX_ALARM_COUNT = 55 //on iOS alarms are limited to 64 and we need a few free slots for possible eventTrigger with timers
	
	private val WEEKDAY_CODES = intArrayOf( //to comply with WeekDay-enum in io.ktor.util.date.WeekDay
		2,  //Monday
		4,  //Tuesday
		8,  //Wednesday
		16,  //Thursday
		32,  //Friday
		64, //Saturday
		1  //Sunday
	)
	
	
	fun checkMissedAlarms(missedAlarmsAsBroken: Boolean = false) {
		//on IOS we can assume that all notifications have been issued or noticed by reactToBootOrTimeChange()
		//so actions have to be issued, but notifications can be ignored
		//on IOS when the user hasnt pressed the notification, it is not unlikely that this function will find missed alarms
		//on Android this should never find alarms, because they are dealt with the moment a notification is issued,
		// unless something went wrong or the app was killed

		//on Android, notifications can be a couple minutes late when the phone was in Doze mode.
		//But this function is only called when the app is started. So no Doze.
		//We still make sure that there are a few ms leeway, just in case the user opens the app exactly when a notification is issued
		val now = NativeLink.getNowMillis() - TREAT_AS_MISSED_LEEWAY_MS
		val alarms = DbLogic.getAlarmsBefore(now)
		ErrorBox.log("Scheduler", "Found ${alarms.size} missed alarms")
		
		var openDialog = false
		
		if(NativeLink.smartphoneData.phoneType == PhoneType.Android) {
			val missedSchedules = HashMap<Long, Alarm>()
			
			for(alarm in alarms) {
				ErrorBox.warn("Scheduler", "Action (Alarm: ${alarm.id}) did not fire! Rescheduling alarm")
				openDialog = true
				
				//We dont need to cancel the alarm in here because alarm.exec() calls delete()
				alarm.exec(fireNotifications = NativeLink.smartphoneData.phoneType != PhoneType.IOS)
				if(alarm.type == Alarm.TYPES.SignalTime) {
					if(!missedSchedules.containsKey(alarm.scheduleId) || alarm.timestamp > missedSchedules[alarm.scheduleId]!!.timestamp)
						missedSchedules[alarm.scheduleId] = alarm
				}
			}
			
			//report alarms that were not even scheduled:
			for((scheduleId, alarm) in missedSchedules) {
				val schedule = DbLogic.getSchedule(scheduleId) ?: continue
				
				var timestamp = alarm.timestamp + schedule.dailyRepeatRate*ONE_DAY_MS
				while(timestamp < now) {
					timestamp = considerDayOptions(timestamp, schedule)
					
					val signalTimes = DbLogic.getSignalTimes(schedule)
					val q = DbLogic.getQuestionnaire(alarm.questionnaireId) ?: continue
					
					for(signalTime in signalTimes) {
						for(i in 0 until signalTime.frequency) {
							DbLogic.reportMissedInvitation(q, timestamp)
						}
					}
					timestamp += schedule.dailyRepeatRate*ONE_DAY_MS
				}
			}
		}
		else if(NativeLink.smartphoneData.phoneType == PhoneType.IOS) {
			val ignoredAlarms = HashMap<Long, Alarm>()
			for(alarm in alarms.asReversed()) { //we want to execute the last alarm and ignore the others. So we iterate from the back
				if(ignoredAlarms.containsKey(alarm.actionTriggerId)) {
					ErrorBox.log("Scheduler", "Alarm (id=${alarm.id}, type=${alarm.type}) has already another Alarm. Treat as missed")
					val q = DbLogic.getQuestionnaire(alarm.questionnaireId) ?: continue
					if(alarm.type != Alarm.TYPES.Reminder)
						DbLogic.reportMissedInvitation(q, alarm.timestamp)
					alarm.delete()
				}
				else {
					//Rationale:
					//If an alarm has a questionnaire, we wait for the user to do the questionnaire (which will execute not triggered alarms as well)
					//This way its connected notification will not be removed when the alarm is executed (and deleted)
					//If not, we execute it now (which removes the notification)
					//If the user does not fill out the questionnaire, this alarm will be executed
					// next time we call checkMissedAlarms() and there is another notification by the same actionTrigger
					if(alarm.actionTrigger.hasInvitation(true)) {
						ErrorBox.log(
							"Scheduler",
							"Notification for Alarm (id=${alarm.id}, type=${alarm.type}) was not pressed yet but has an invitation. Ignoring for now."
						)
						alarm.actionTrigger.questionnaire.updateLastNotification(alarm.timestamp)
					}
					else {
						ErrorBox.log("Scheduler", "Notification for Alarm (id=${alarm.id}, type=${alarm.type}) was not pressed. Executing now.")
						//alarm.exec() also calls alarm.delete()
						alarm.exec(fireNotifications = false) //also deletes alarm
					}
					
					ignoredAlarms[alarm.actionTriggerId] = alarm
				}
			}
			openDialog = false
		}
		
		
		if(missedAlarmsAsBroken && openDialog) {
			ErrorBox.log("Scheduler", "Notifications seem to be broken. Opening dialog")
			NativeLink.dialogOpener.notificationsBroken()
		}
	}
	
	fun scheduleIfNeeded() {
		for(schedule in DbLogic.getAllSchedules()) {
			schedule.scheduleIfNeeded()
		}
	}
	
	fun reactToBootOrTimeChange(timeChanged: Boolean) {
		val now: Long = NativeLink.getNowMillis() + MIN_SCHEDULE_DISTANCE
		val alarms: List<Alarm> = DbLogic.getAlarms()
		
		for(alarm: Alarm in alarms) {
			ErrorBox.log("BootOrTimeChange", "Recalculating Alarm (id=${alarm.id}, type=${alarm.type})")
			
			if(alarm.timestamp <= now)
				alarm.exec()
			else {
				if(timeChanged) {
					NativeLink.postponedActions.cancel(alarm)
				}
				alarm.schedule()
			}
		}
	}

	fun scheduleAhead() { //used in IOS
		if(NativeLink.smartphoneData.phoneType != PhoneType.IOS)
			return
		val alarms = DbLogic.getLastAlarmPerSignalTime()
		if(alarms.isEmpty())
			return
		
		var additionalAlarms = 0
		for(alarm in alarms) {
			additionalAlarms += alarm.actionTrigger.countPostponedReminder()
		}
		val reschedulesPerAlarm = IOS_MAX_TIMES_TO_SCHEDULE_AHEAD.coerceAtMost(IOS_MAX_ALARM_COUNT / (alarms.size + additionalAlarms))
		
		ErrorBox.log("Scheduler", "Found ${alarms.size} different alarms with $additionalAlarms reminder that can be rescheduled. Trying to reschedule $reschedulesPerAlarm times")
		//this loop has a lot of empty runs, but it makes sure that all alarms are scheduled equally even if we hit the max notification limit on iOS
		// overhead should be ok because IOS_MAX_ALARM_COUNT or IOS_MAX_TIMES_TO_SCHEDULE_AHEAD are expected to be small
		for(max in 1 .. reschedulesPerAlarm) {
			for(alarm in alarms) {
				val count1 = DbLogic.countAlarms()
				if(count1 > IOS_MAX_ALARM_COUNT) {
					ErrorBox.warn("Scheduler", "Trying to schedule $count1 alarms which is more than $IOS_MAX_ALARM_COUNT. Aborting!")
					return
				}
				
				val count2 = DbLogic.countAlarmsFrom(alarm.signalTimeId)
				if(count2 >= max) {
					ErrorBox.log("Scheduler", "Alarm (id=${alarm.id}, signalTime=${alarm.signalTimeId}) has $count2/$max alarms. Skipping")
					continue
				}
					
				alarm.scheduleAhead()
			}
		}
	}
	
	
	//takes an Alarm from the past and recreates it for the future
	//on Android, if alarms where issued properly, anchorTimestamp should be close to NativeLink.getTimeMillis()
	internal fun rescheduleFromAlarm(alarm: Alarm) {
//		rescheduleSignalTime(alarm.signalTime ?: return, alarm.actionTriggerId, alarm.timestamp)
		val signalTime = alarm.signalTime ?: return
		
		if(!alarm.canBeRescheduled) {//when frequency > 1, then we want to reschedule only when all the other alarms are done as well
			ErrorBox.log("Scheduler", "Several alarms belong to the same SignalTime. Only rescheduling from the last one. This is ${alarm.indexNum}/${signalTime.frequency}. Skipping")
			return
		}
		
		//Note: We use getLastSignalTimeAlarm() for iOS. On Android no other alarms should exist at this point (the original is deleted in Alarm.exec() )
		val lastAlarm = DbLogic.getLastSignalTimeAlarm(signalTime) ?: alarm
		//we have to use lastAlarm.timestamp to make sure we do not skip a day if this function was executed late:
		rescheduleFromSignalTime(signalTime, lastAlarm.actionTriggerId, lastAlarm.timestamp)
	}
	internal fun rescheduleFromSignalTime(signalTime: SignalTime, actionTriggerId: Long, timestampAnchor: Long) {
		if(signalTime.randomFixed) {
			//this does the same as scheduleSignalTime() but it ignores frequency and reuses the time from the alarm.
			ErrorBox.log("Scheduler", "Creating fixed repeating Alarm")
			
			val questionnaire = DbLogic.getQuestionnaire(signalTime.questionnaireId) ?: return
			val loopMs = ONE_DAY_MS * signalTime.schedule.dailyRepeatRate.toLong()
			
			val now = NativeLink.getNowMillis()
			val timestamp = if(timestampAnchor < now) {
				val calNow = GMTDate(now)
				val calAlarm = GMTDate(timestampAnchor)
				
				GMTDate(
					seconds = calAlarm.seconds,
					minutes = calAlarm.minutes,
					hours = calAlarm.hours,
					dayOfMonth = calNow.dayOfMonth,
					month = calNow.month,
					year = calNow.year
				).timestamp
			}
			else
				timestampAnchor
			
			val baseTimestamp = considerDayOptions(timestamp + loopMs, questionnaire, signalTime.schedule)
			if(baseTimestamp == -1L)
				return
			
			Alarm.createFromSignalTime(signalTime, actionTriggerId, baseTimestamp)
		}
		else
			scheduleSignalTime(signalTime, actionTriggerId, max(timestampAnchor, NativeLink.getNowMillis()))
	}
	
	private fun calculateRandomPeriod(questionnaire: Questionnaire, signalTime: SignalTime): Int {
		if(questionnaire.completableAtSpecificTime) {
			if(questionnaire.completableAtSpecificTimeStart != -1 && questionnaire.completableAtSpecificTimeEnd != -1) {
				if(questionnaire.completableAtSpecificTimeStart > questionnaire.completableAtSpecificTimeEnd) { //start and end include midnight
					var period = 0
					if(questionnaire.completableAtSpecificTimeStart < signalTime.endTimeOfDay)
						period += signalTime.endTimeOfDay - questionnaire.completableAtSpecificTimeStart
					if(questionnaire.completableAtSpecificTimeEnd > signalTime.startTimeOfDay)
						period += questionnaire.completableAtSpecificTimeEnd - signalTime.startTimeOfDay
					return period
				}
				else {
					var period = 0
					if(questionnaire.completableAtSpecificTimeEnd < signalTime.endTimeOfDay)
						period += signalTime.endTimeOfDay - questionnaire.completableAtSpecificTimeEnd
					if(questionnaire.completableAtSpecificTimeStart > signalTime.startTimeOfDay)
						period += questionnaire.completableAtSpecificTimeStart - signalTime.startTimeOfDay
					return period
				}
			}
			else if(questionnaire.completableAtSpecificTimeStart != -1)
				return questionnaire.completableAtSpecificTimeStart - signalTime.startTimeOfDay
			else if(questionnaire.completableAtSpecificTimeEnd != -1)
				return signalTime.endTimeOfDay - questionnaire.completableAtSpecificTimeEnd
			else
				return signalTime.endTimeOfDay - signalTime.startTimeOfDay
		}
		else
			return signalTime.endTimeOfDay - signalTime.startTimeOfDay
	}
	
	// * We can use this function for single time schedules as well. We can just set frequency=1 and it will fire at startTime_minutes
	// * This function can also deal with random == false and frequency > 1 - but will we ever use it this way..?
	// * randomFixed will be considered in rescheduleSignalTimeFromAlarm() after Alarm was issued for the first time
	//if manualDelay is disabled (-1) then dailyRepeatRate is added to anchorTimestamp
	internal fun scheduleSignalTime(signalTime: SignalTime, actionTriggerId: Long, anchorTimestamp: Long, manualDelayDays: Int = -1) {
		ErrorBox.log("Scheduler", "Creating repeating Alarms")
		val questionnaire = DbLogic.getQuestionnaire(signalTime.questionnaireId) ?: return
		val frequency = signalTime.frequency
		val msBetween = signalTime.minutesBetween * 60000
		val period = if(signalTime.random) calculateRandomPeriod(questionnaire, signalTime) else 0
		val block = period / frequency
		if(signalTime.random && frequency > 1 && block < msBetween) {
			ErrorBox.error("Scheduler", "SignalTime ${signalTime.id}: $frequency blocks with $msBetween ms do not fit into $period ms")
			return
		}
		
		//
		//correct timestamp:
		//
		val midnight = NativeLink.getMidnightMillis(anchorTimestamp)

		//set beginning time:
		var baseTimestamp = midnight + signalTime.startTimeOfDay
		val minDate: Long
		if(manualDelayDays != -1) { //is only set when schedules are freshly created
			baseTimestamp += ONE_DAY_MS * manualDelayDays
			minDate = anchorTimestamp + (ONE_DAY_MS * manualDelayDays).coerceAtLeast(MIN_SCHEDULE_DISTANCE)
			
			// Assuming that anchorTimestamp = 23:58, startTimeOfDay = 00:00 and dailyRepeatRate = 5 (anything greater than 1).
			// When we used getMidnightMillis(), we calculated backwards a whole day, so baseTimestamp is one day short.
			// That means, when we just added ONE_DAY_MS * dailyRepeatRate, we effectively only added 4 days instead of 5.
			// This would not be true if startTimeOfDay = 23:59, so we cant just blindly add a day.
			// This loop fixes it:
			while(baseTimestamp < minDate) {
				baseTimestamp += ONE_DAY_MS
			}
		}
		else
			baseTimestamp += ONE_DAY_MS * signalTime.schedule.dailyRepeatRate
		
		
		//options:
		baseTimestamp = considerDayOptions(baseTimestamp, questionnaire, signalTime.schedule)
		if(baseTimestamp == -1L) {
			ErrorBox.log("Scheduler", "SignalTime ${signalTime.id} will never become active. Canceling")
			return
		}
		
		
		//
		//Create alarms for each frequency:
		//
		var nextBlock = block //this variable is needed when we need to shorten a loop-block when a random notification was set less than minutes_between to the next block
		for(i in 1 .. frequency) { //currently, frequency is always 1 when random == false.
			var workTimestamp: Long
			if(signalTime.random) {
				val randomBlock = (nextBlock * getRandom()).toInt()
				
				workTimestamp = considerHourOptions(baseTimestamp + randomBlock, questionnaire) //set the actual timing of the notification
				baseTimestamp = considerHourOptions(baseTimestamp + nextBlock, questionnaire) //prepare timestamp for the next loop
				
				if(baseTimestamp - workTimestamp < msBetween) { //if random is very late in this block, make sure that the next time gets shortened to account for minutesBetween
					val shorten = msBetween - (baseTimestamp - workTimestamp).toInt()
					baseTimestamp += shorten.toLong() //start the next block later
					nextBlock = block - shorten //make sure that the next block ends at the same time (so we shorten it in the end, to account for the later start)
				}
				else nextBlock = block
			}
			else {
				baseTimestamp += block.toLong() //this has no effect. I will leave it in, in case we ever want to use frequency on non-random schedules
				workTimestamp = baseTimestamp
			}
			
			Alarm.createFromSignalTime(signalTime, actionTriggerId, workTimestamp, i)
		}
	}
	
	internal fun addReminder(
		questionnaireId: Long,
		actionTriggerId: Long,
		index: Int,
		delayMinutes: Int,
		count: Int,
		now: Long = NativeLink.getNowMillis(),
		evenTriggerId: Long = -1,
		signalTimeId: Long = -1
	): Alarm {
		ErrorBox.log("Scheduler", "Creating Reminder")
		val timestamp = now + delayMinutes * 60 * 1000
		return Alarm.createAsReminder(timestamp, questionnaireId, actionTriggerId, index, count, evenTriggerId, signalTimeId)
	}
	
	internal fun scheduleEventTrigger(eventTrigger: EventTrigger, now: Long = NativeLink.getNowMillis()) {
		ErrorBox.log("Scheduler", "Creating postponed EventTrigger Alarm")
		val add: Long = if(eventTrigger.randomDelay) {
			eventTrigger.delayMinimumSec*1000 + (getRandom() * (eventTrigger.delaySec*1000 - eventTrigger.delayMinimumSec*1000)).toLong()
		}
		else
			eventTrigger.delaySec.toLong()*1000
		
		Alarm.createFromEventTrigger(eventTrigger, now + add)
	}
	
	private fun getRandom(): Double {
		return Random.nextDouble(1.0)
	}
	
	private fun considerHourOptions(timestamp: Long, questionnaire: Questionnaire): Long {
		val midnight = NativeLink.getMidnightMillis(timestamp)
		val fromMidnight = timestamp - midnight
		
		
		if(!questionnaire.completableAtSpecificTime)
			return timestamp
		else if(questionnaire.completableAtSpecificTimeStart != -1 && questionnaire.completableAtSpecificTimeEnd != -1) {
			if(questionnaire.completableAtSpecificTimeStart > questionnaire.completableAtSpecificTimeEnd) { //start and end include midnight
				if(fromMidnight < questionnaire.completableAtSpecificTimeStart)
					return midnight + questionnaire.completableAtSpecificTimeStart
				else if(fromMidnight > questionnaire.completableAtSpecificTimeEnd) //this should never happen
					return fromMidnight + questionnaire.completableAtSpecificTimeEnd
			}
			else {
				if(fromMidnight > questionnaire.completableAtSpecificTimeStart && fromMidnight < questionnaire.completableAtSpecificTimeEnd)
					return midnight + questionnaire.completableAtSpecificTimeEnd
			}
		}
		else if(questionnaire.completableAtSpecificTimeStart != -1) {
			if(fromMidnight < questionnaire.completableAtSpecificTimeStart)
				return midnight + questionnaire.completableAtSpecificTimeStart
		}
		else if(questionnaire.completableAtSpecificTimeEnd != -1) {
			if(fromMidnight > questionnaire.completableAtSpecificTimeEnd) //this should never happen
				return midnight + questionnaire.completableAtSpecificTimeEnd
		}
		
		return timestamp
	}
	
	internal fun considerDayOptions(timestamp: Long, questionnaire: Questionnaire, schedule: Schedule): Long {
		val activeInDays = ceil(questionnaire.willBeActiveIn(now = timestamp).toDouble() / ONE_DAY_MS).toInt()
		val newTimestamp = considerDayOptions(timestamp + activeInDays * ONE_DAY_MS, schedule)
		
		return if(questionnaire.isActive(newTimestamp)) newTimestamp else -1
	}
	
	//internal for testing
	internal fun considerDayOptions(timestamp: Long, schedule: Schedule): Long {
		var cal = GMTDate(timestamp)
		if(schedule.dayOfMonth != 0) {
			cal = if(cal.dayOfMonth <= schedule.dayOfMonth)
				GMTDate(
					seconds = cal.seconds,
					minutes = cal.minutes,
					hours = cal.hours,
					dayOfMonth = schedule.dayOfMonth,
					month = cal.month,
					year = cal.year
				)
			else {
				if(cal.month.ordinal < Month.DECEMBER.ordinal)
					GMTDate(
						seconds = cal.seconds,
						minutes = cal.minutes,
						hours = cal.hours,
						dayOfMonth = schedule.dayOfMonth,
						month = Month.from(cal.month.ordinal+1),
						year = cal.year
					)
				else
					GMTDate(
						seconds = cal.seconds,
						minutes = cal.minutes,
						hours = cal.hours,
						dayOfMonth = schedule.dayOfMonth,
						month = Month.JANUARY,
						year = cal.year+1
					)
			}
		}
		
		//consider weekdays:
		if(schedule.weekdays != 0) {
			var i = 365
			while(schedule.weekdays or WEEKDAY_CODES[cal.dayOfWeek.ordinal] != schedule.weekdays) {
				cal = cal.plus(ONE_DAY_MS)
				if(--i==0) {
					ErrorBox.error("Scheduler", "Could not find appropriate weekday for ${schedule.id} (weekdaycode=${schedule.weekdays}!")
					break
				}
			}
		}
		return cal.timestamp
	}
	
	//NOTE: this will cancel all reminder from the whole questionnaire
	private fun removeReminder(questionnaireId: Long) {
		for(alarm in DbLogic.getReminderAlarmsFrom(questionnaireId)) {
			alarm.delete()
		}
	}
	
	internal fun remove(questionnaire: Questionnaire) {
		for(alarm in DbLogic.getAlarmsFrom(questionnaire)) {
			alarm.delete()
		}
		removeReminder(questionnaire.id)
	}
	
	internal fun remove(eventTrigger: EventTrigger) {
		for(alarm in DbLogic.getAlarmsFrom(eventTrigger)) {
			alarm.delete()
		}
		removeReminder(eventTrigger.questionnaireId)
	}
	
	internal fun remove(schedule: Schedule) {
		for(alarm in DbLogic.getAlarmsFrom(schedule)) {
			alarm.delete()
		}
		removeReminder(schedule.getQuestionnaire().id)
	}
	
	internal fun remove(signalTime: SignalTime) {
		for(alarm in DbLogic.getAlarmsFrom(signalTime)) {
			alarm.delete()
		}
		removeReminder(signalTime.questionnaireId)
	}
}