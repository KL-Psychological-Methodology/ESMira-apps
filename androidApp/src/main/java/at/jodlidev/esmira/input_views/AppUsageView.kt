package at.jodlidev.esmira.input_views

import android.app.usage.UsageEvents
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

import android.app.usage.UsageStatsManager
import android.os.Build
import android.widget.TextView
import java.util.concurrent.TimeUnit
import androidx.annotation.RequiresApi
import at.jodlidev.esmira.ScreenTrackingReceiver
import at.jodlidev.esmira.sharedCode.NativeLink
import kotlin.collections.HashMap


/**
 * Created by JodliDev on 16.12.2021.
 */
class AppUsageView(context: Context) : TextElView(context, R.layout.view_input_app_usage) {
	class UsageStatsInfo(
		val count: Int,
		val totalTime: Long
	)
	private class AppUsageCounter(
		val from: Long,
		val to: Long,
		val startEventCode: Int,
		val endEventCode: Int,
		val unexpectedEndEventCode: Int
	) {
		private var count = 0
		private var totalTime = 0L
		
		private var enableTimestamp = 0L
		private var hasEvent = false
		
		fun addEvent(event: UsageEvents.Event) {
			when(event.eventType) {
				startEventCode -> {
					++count
					enableTimestamp = event.timeStamp
					hasEvent = true
				}
				endEventCode -> {
					if(enableTimestamp != 0L) {
						totalTime += event.timeStamp - enableTimestamp
						enableTimestamp = 0L
						hasEvent = true
					}
					else if(!hasEvent)//measures the time from filling out the last questionnaire to turning of the screen
						totalTime += event.timeStamp - from
					
				}
				UsageEvents.Event.DEVICE_SHUTDOWN, unexpectedEndEventCode -> {
					if(enableTimestamp != 0L) {
						totalTime += event.timeStamp - enableTimestamp
						enableTimestamp = 0L
						hasEvent = true
					}
				}
			}
		}
		
		fun getResults(): UsageStatsInfo{
			if(enableTimestamp != 0L)
				totalTime += to - enableTimestamp
			
			return UsageStatsInfo(count, totalTime)
		}
	}
	private var appUsageElement: TextView = findViewById(R.id.appUsageTime)
	private var appUsageCountElement: TextView = findViewById(R.id.appUsageCount)
	private var appUsageCountLabelElement: TextView = findViewById(R.id.appUsageCountLabel)
	private var headerElement: TextView = findViewById(R.id.header)
	private var packageNameElement: TextView = findViewById(R.id.packageName)
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
			return
		}
		
		val to = NativeLink.getMidnightMillis()
		val from = to - 86400000
		
		val yesterdayAppUsageTime: Long
		val yesterdayAppUsageCount: Int
		
		//general screen time:
		if(input.packageId == "") {
			val yesterdayPair = countTotalEvents(from, to)
			yesterdayAppUsageCount = yesterdayPair.count
			yesterdayAppUsageTime = yesterdayPair.totalTime
			
			headerElement.text = context.getString(R.string.colon_total_screenTime)
			packageNameElement.visibility = GONE
		}
		//specific app usage time:
		else {
			val packageId = input.packageId
			
			val packageUsages = getAllPackageUsages(from, to)
			yesterdayAppUsageCount = packageUsages[packageId]?.count ?: -1
			yesterdayAppUsageTime = packageUsages[packageId]?.totalTime ?: -1L
			
			headerElement.text = context.getString(R.string.colon_app_usage)
			packageNameElement.text = packageId
			packageNameElement.visibility = VISIBLE
		}
		input.value = yesterdayAppUsageTime.toString()
		input.additionalValues["usageCount"] = yesterdayAppUsageCount.toString()
		
		
		if(yesterdayAppUsageTime == 0L || yesterdayAppUsageTime == -1L) {
			appUsageElement.text = context.getString(R.string.no_data)
		}
		else {
			val hours = TimeUnit.MILLISECONDS.toHours(yesterdayAppUsageTime)
			val minutes = TimeUnit.MILLISECONDS.toMinutes(yesterdayAppUsageTime) % 60
			val seconds = TimeUnit.MILLISECONDS.toSeconds(yesterdayAppUsageTime) % 60
			
			
			appUsageElement.text = context.getString(
				R.string.time_format_android,
				hours.toString().padStart(2, '0'),
				minutes.toString().padStart(2, '0'),
				seconds.toString().padStart(2, '0')
			)
		}
		
		if(yesterdayAppUsageCount == -1 || yesterdayAppUsageCount == 0) {
			appUsageCountElement.visibility = GONE
			appUsageCountLabelElement.visibility = GONE
		}
		else {
			appUsageCountElement.visibility = VISIBLE
			appUsageCountLabelElement.visibility = VISIBLE
			appUsageCountElement.text = yesterdayAppUsageCount.toString()
		}
		
		
		super.bindData(input, questionnaire)
	}
	
	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	private fun getUsageStatsManager(): UsageStatsManager {
		val systemService = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) Context.USAGE_STATS_SERVICE else "usagestats"
		return context.getSystemService(systemService) as UsageStatsManager
	}
	private fun countTotalEvents(from: Long, to: Long): UsageStatsInfo {
		return UsageStatsInfo(-1, -1L)
//		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//			return UsageStatsInfo(-1, -1L)
//
//		val usageStatsManager = getUsageStatsManager()
//
//		return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//			val counter = AppUsageCounter(from, to, UsageEvents.Event.SCREEN_INTERACTIVE, UsageEvents.Event.SCREEN_NON_INTERACTIVE, UsageEvents.Event.DEVICE_SHUTDOWN)
//			val events = usageStatsManager.queryEvents(from, to)
//			val event: UsageEvents.Event = UsageEvents.Event()
//
//			while(events.getNextEvent(event)) {
//				counter.addEvent(event)
//			}
//
//			counter.getResults()
//		}
//		else
//			ScreenTrackingReceiver.getData(context)
	}
	
	
	
	//usageStatsManager.queryAndAggregateUsageStats() seems to be very unreliable. Counting events manually works better
	// Thanks to: https://stackoverflow.com/a/50647945/10423612
	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	private fun getAllPackageUsages(from: Long, to: Long): Map<String, UsageStatsInfo> {
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			return HashMap()
		val usageStatsManager = getUsageStatsManager()
		val counterList = HashMap<String, AppUsageCounter>()
		
		// Query the list of events that has happened within that time frame
		val systemEvents = usageStatsManager.queryEvents(from, to)
		while(systemEvents.hasNextEvent()) {
			val event = UsageEvents.Event()
			systemEvents.getNextEvent(event)
			
			if(!counterList.containsKey(event.packageName)) {
				counterList[event.packageName] = AppUsageCounter(
					from,
					to,
					UsageEvents.Event.MOVE_TO_FOREGROUND,
					UsageEvents.Event.MOVE_TO_BACKGROUND,
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) UsageEvents.Event.ACTIVITY_STOPPED else 23
				)
			}
			
			counterList[event.packageName]?.addEvent(event)
		}
		
		val returnList = HashMap<String, UsageStatsInfo>()
		for((packageName, counter) in counterList) {
			returnList[packageName] = counter.getResults()
		}
		return returnList
	}
}