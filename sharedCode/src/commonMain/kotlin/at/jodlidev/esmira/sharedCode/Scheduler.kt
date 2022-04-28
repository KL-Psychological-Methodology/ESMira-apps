package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.*
import io.ktor.util.date.GMTDate
import io.ktor.util.date.Month
import io.ktor.util.date.plus
import kotlin.math.max

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
	internal const val ONE_DAY_MS: Long = 86400000 //1000*60*60*24
	const val MIN_SCHEDULE_DISTANCE = 60000 //1000*60 = 1 min
	internal const val IOS_DAYS_TO_SCHEDULE_AHEAD_MS = 4 * ONE_DAY_MS
	
	private val WEEKDAY_CODES = intArrayOf( //to comply with WeekDay-enum in io.ktor.util.date.WeekDay
		2,  //Monday
		4,  //Tuesday
		8,  //Wednesday
		16,  //Thursday
		32,  //Friday
		64, //Saturday
		1  //Sunday
	)
	private val RANGE_FOR_RANDOM = 0..100
	
	
	@Suppress("unused") fun checkMissedAlarms(missedAlarmsAsBroken: Boolean = false) {
		//on IOS we can assume that all notifications have been issued or noticed by reactToBootOrTimeChange()
		//so actions have to be issued, but notifications can be ignored
		//on IOS when the user hasnt pressed the notification, it is not unlikely that this function will find missed alarms
		//on Android this should never find alarms, because they are dealt with the moment a notification is issued,
		// unless something went wrong or the app was killed

		//on Android, notifications can be a couple minutes late when the phone was in Doze mode.
		//But this function is only called when the app is started. So no Doze.
		//We still make sure that there are a few ms leeway, just in case the user opens the app exactly when a notification is issued
		val now = NativeLink.getNowMillis() - 500
		val alarms = DbLogic.getAlarmsBefore(now)
		ErrorBox.log("Scheduler", "Found ${alarms.size} missed alarms")
		
		var openDialog = false

		if(isAndroid()) {

			val missedSchedules = HashMap<Long, Alarm>()
			
			for(alarm in alarms) {
				ErrorBox.warn("Scheduler", "Action \"${alarm.label}\" (Alarm: ${alarm.id}) did not fire! Rescheduling alarm")
				openDialog = true
				
				NativeLink.postponedActions.cancel(alarm)
				alarm.exec(fireNotifications = !isIOS())
				if(alarm.type == Alarm.TYPES.SignalTime) {
					if(!missedSchedules.containsKey(alarm.scheduleId) || alarm.timestamp > missedSchedules[alarm.scheduleId]!!.timestamp)
						missedSchedules[alarm.scheduleId] = alarm
				}
			}
			
			//estimate and report alarms that were not even scheduled:
			for((scheduleId, alarm) in missedSchedules) {
				val schedule = DbLogic.getSchedule(scheduleId) ?: continue
				
				val rate: Int
				rate = when {
					schedule.dayOfMonth != 0 ->
						max(30, schedule.dailyRepeatRate) //Note: 30 days per month is not exact but we can ignore that
					schedule.weekdays != 0 -> {
						var days = 0
						for(dayNum in WEEKDAY_CODES) {
							if(schedule.weekdays or dayNum != schedule.weekdays)
								++days
						}
						max(7 / days, schedule.dailyRepeatRate)
					}
					else ->
						schedule.dailyRepeatRate
				}
				
				var num = (alarm.timestamp - now) / rate
				
				while(num > 0) {
					val signalTimes = DbLogic.getSignalTimes(schedule)
					val q = DbLogic.getQuestionnaire(alarm.questionnaireId) ?: continue
					
					for(signalTime in signalTimes) {
						for(i in 0 until signalTime.frequency) {
							DbLogic.reportMissedInvitation(q, alarm.timestamp + num*rate) //TODO: untested
						}
					}
					num -= 1
				}
			}
		}
		else if(isIOS()) {
			val ignoredAlarms = HashMap<Long, Alarm>()
			for(alarm in alarms.asReversed()) { //we want to execute the last alarm and ignore the others. So we iterate from the back
				if(ignoredAlarms.containsKey(alarm.actionTriggerId)) {
					ErrorBox.log("Scheduler", "Alarm \"${alarm.label}\" (id=${alarm.id}, type=${alarm.type}) has already another Alarm. Treat as missed")
					val q = DbLogic.getQuestionnaire(alarm.questionnaireId) ?: continue
					if(alarm.type != Alarm.TYPES.Reminder)
						DbLogic.reportMissedInvitation(q, alarm.timestamp)
//					NativeLink.postponedActions.cancel(alarm) //not needed because we have NativeLink.notifications.remove()
					alarm.delete()
				}
				else {
					//Rationale:
					//If an alarm has a questionnaire, we wait for the user to do the questionnaire (which will execute untriggered alarms as well)
					//If not, we execute it now and remove the notification
					if(alarm.actionTrigger.hasInvitation()) {
						ErrorBox.log(
							"Scheduler",
							"Notification for Alarm \"${alarm.label}\" (id=${alarm.id}, type=${alarm.type}) was not pressed yet but has an invitation. Ignoring for now."
						)
						alarm.actionTrigger.questionnaire.updateLastNotification(alarm.timestamp)
					}
					else {
						ErrorBox.log("Scheduler", "Notification for Alarm \"${alarm.label}\" (id=${alarm.id}, type=${alarm.type}) was not pressed. Executing now.")
						alarm.exec(fireNotifications = false) //also deletes alarm
//						NativeLink.postponedActions.cancel(alarm) //not needed because we have NativeLink.notifications.remove()
						NativeLink.notifications.remove(alarm.id.toInt())
					}
					
					ignoredAlarms[alarm.actionTriggerId] = alarm
					openDialog = true
				}
			}
		}
		
		
		if(missedAlarmsAsBroken && openDialog) {
			ErrorBox.log("Scheduler", "Notifications seem to be broken. Opening dialog")
			NativeLink.dialogOpener.notificationsBroken()
		}
	}
	@Suppress("unused") fun reactToBootOrTimeChange(timeChanged: Boolean) {
		val now: Long = NativeLink.getNowMillis() + MIN_SCHEDULE_DISTANCE
		val alarms: List<Alarm> = DbLogic.getAlarms()
		
		for(alarm: Alarm in alarms) {
			ErrorBox.log("BootOrTimeChange", "Recalculating \"${alarm.label}\" (id=${alarm.id}, type=${alarm.type})")
			if(timeChanged) {
				NativeLink.postponedActions.cancel(alarm)
				//TODO: recalculate timestamp (is wrong when there is a timezone-change)
			}
			
			if(alarm.timestamp <= now)
				alarm.exec()
			else
				alarm.schedule()
		}
	}
	@Suppress("unused") fun scheduleAhead() { //used in IOS so a separate service can schedule ahead
		for(alarm in DbLogic.getLastAlarms()) {
			alarm.scheduleAhead()
		}
	}
	
	
	//takes an Alarm from the past and recreates it for the future
	//on Android, if alarms where issued properly, anchorTimestamp should be equal to NativeLink.getTimeMillis()
	internal fun rescheduleSignalTimeFromAlarm(alarm: Alarm) {
//		rescheduleSignalTime(alarm.signalTime ?: return, alarm.actionTriggerId, alarm.timestamp)
		val signalTime = alarm.signalTime ?: return
		//Note: We use getLastSignalTimeAlarm() for iOS. On Android no other alarms schould exist at this point (the original is deleted in Alarm.exec() )
		val lastAlarm = DbLogic.getLastSignalTimeAlarm(signalTime) ?: alarm
		rescheduleSignalTime(signalTime, lastAlarm.actionTriggerId, lastAlarm.timestamp)
	}
	internal fun rescheduleSignalTime(signalTime: SignalTime, actionTriggerId: Long, timestampAnchor: Long) {
		if(signalTime.randomFixed) { //TODO: UNTESTED
			//this does the same as scheduleSignalTime() but it ignores frequency and reuses the time from the alarm.
			
			ErrorBox.log("Scheduler", "Creating fixed repeating Alarm")
			val loopMs = ONE_DAY_MS * signalTime.schedule.dailyRepeatRate.toLong()
			
			val now = NativeLink.getNowMillis()
			val timestamp: Long
			timestamp = if(timestampAnchor < now) {
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
			val baseTimestamp = considerScheduleOptions(timestamp + loopMs, signalTime)
			
			Alarm.createFromSignalTime(signalTime, actionTriggerId, baseTimestamp)
			
		}
		else
			scheduleSignalTime(signalTime, actionTriggerId, max(timestampAnchor, NativeLink.getNowMillis()))
	}
	
	// * We can use this function for single time schedules as well. We can just set frequency=1 and it will fire at startTime_minutes
	// * This function can also deal with random == false and frequency > 1 - but will we ever use it this way..?
	// * randomFixed will be considered in rescheduleSignalTimeFromAlarm() after Alarm was issued for the first time
	//current_day_repeat_rate is either:
	//	1 day when alarm is just set (from Schedule)
	//	day_repeat_rate + "days until next alarm when reading Alarms (from RescheduleReceiver)
	//	day_repeat_rate when normal repeating (from AlarmBox)
	internal fun scheduleSignalTime(signalTime: SignalTime, actionTriggerId: Long, anchorTimestamp: Long, manualDelay: Int = -1) {
		ErrorBox.log("Scheduler", "Creating repeating Alarm")
		val frequency = signalTime.frequency
		val msBetween = signalTime.minutesBetween * 60000
		val period = if(signalTime.random) signalTime.endTimeOfDay - signalTime.startTimeOfDay else 0
		val block = period / frequency
		if(signalTime.random && frequency > 1 && block < msBetween) {
			ErrorBox.error("Scheduler", "${signalTime.label}: $frequency blocks with $msBetween ms do not fit into $period ms")
			return
		}
		
		//
		//Consider options:
		//
		val midnight = NativeLink.getMidnightMillis(anchorTimestamp)
		
		//set beginning time:
		var baseTimestamp = midnight + signalTime.startTimeOfDay
		if(manualDelay != -1) { //is only set when schedules are freshly created
			baseTimestamp += ONE_DAY_MS * manualDelay
			while(baseTimestamp - MIN_SCHEDULE_DISTANCE < anchorTimestamp)
				baseTimestamp += ONE_DAY_MS
		}
		else {
			baseTimestamp += ONE_DAY_MS * signalTime.schedule.dailyRepeatRate
			
			while(baseTimestamp - MIN_SCHEDULE_DISTANCE < anchorTimestamp) //we have added a dailyRepeatRate. So this should never be true
				baseTimestamp += ONE_DAY_MS * signalTime.schedule.dailyRepeatRate
		}
		
		//options:
		baseTimestamp = considerScheduleOptions(baseTimestamp, signalTime)
		
		//
		//Create alarms for each frequency:
		//
		var nextBlock = block //this variable is needed when we need to shorten a loop-block when a random notification was set less then minutes_between to the next block
		for(i in 1 .. frequency) { //currently, frequency is always 1 when random == false.
			var workTimestamp: Long
			if(signalTime.random) {
				val randomBlock = (nextBlock * getRandom()).toInt()
				
				workTimestamp = baseTimestamp + randomBlock //set the actual timing of the notification
				baseTimestamp += nextBlock //prepare timestamp for the next loop
				
				if(nextBlock - randomBlock < msBetween) { //if random is very late in this block, make sure that the next time gets shortened to account for minutesBetween
					val shorten = msBetween - (block - randomBlock)
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
		label: String,
		index: Int,
		delayMinutes: Int,
		count: Int,
		now: Long = NativeLink.getNowMillis(),
		evenTriggerId: Long = -1,
		signalTimeId: Long = -1
	): Alarm {
		ErrorBox.log("Scheduler", "Creating Reminder")
		val timestamp = now + delayMinutes * 60 * 1000
		return Alarm.createAsReminder(timestamp, questionnaireId, actionTriggerId, label, index, count, evenTriggerId, signalTimeId)
	}
	
	internal fun scheduleEventTrigger(eventTrigger: EventTrigger) {
		ErrorBox.log("Scheduler", "Creating postponed EventTrigger Alarm")
		val add: Long = if(eventTrigger.randomDelay) {
			eventTrigger.delayMinimumSec*1000 + (getRandom() * (eventTrigger.delaySec*1000 - eventTrigger.delayMinimumSec*1000)).toLong()
		}
		else
			eventTrigger.delaySec.toLong()*1000
		
		Alarm.createFromEventTrigger(eventTrigger, NativeLink.getNowMillis() + add)
	}
	
	private fun getRandom(): Double {
		return (RANGE_FOR_RANDOM.random()/100.0)
	}
	
	private fun considerScheduleOptions(timestamp: Long, signalTime: SignalTime): Long { //consider day_of_month:
		var cal = GMTDate(timestamp)
		if(signalTime.schedule.dayOfMonth != 0) { //TODO: untested
			cal = if(cal.dayOfMonth < signalTime.schedule.dayOfMonth)
				GMTDate(
					seconds = cal.seconds,
					minutes = cal.minutes,
					hours = cal.hours,
					dayOfMonth = signalTime.schedule.dayOfMonth,
					month = cal.month,
					year = cal.year
				)
			else {
				if(cal.month.ordinal < Month.DECEMBER.ordinal)
					GMTDate(
						seconds = cal.seconds,
						minutes = cal.minutes,
						hours = cal.hours,
						dayOfMonth = signalTime.schedule.dayOfMonth,
						month = Month.from(cal.month.ordinal+1),
						year = cal.year
					)
				else
					GMTDate(
						seconds = cal.seconds,
						minutes = cal.minutes,
						hours = cal.hours,
						dayOfMonth = signalTime.schedule.dayOfMonth,
						month = Month.JANUARY,
						year = cal.year+1
					)
			}
		}
		
		//consider weekdays:
		val schedule = signalTime.schedule
		if(schedule.weekdays != 0) {
			var i = 365
			while(schedule.weekdays or WEEKDAY_CODES[cal.dayOfWeek.ordinal] != schedule.weekdays) {
				cal = cal.plus(ONE_DAY_MS)
				if(--i==0) {
					ErrorBox.error("Scheduler", "Could not find appropriate weekday for ${signalTime.label} (weekdaycode=${schedule.weekdays}!")
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
		for(trigger in questionnaire.actionTriggers) {
			for(alarm in DbLogic.getAlarmsFrom(trigger)) {
				alarm.delete()
			}
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