package at.jodlidev.esmira.views.inputViews

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.ScreenTrackingReceiver
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Input
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Created by JodliDev on 23.01.2023.
 */




class AppUsageCalculator(context: Context) {
	val context: WeakReference<Context>
	
	init {
		this.context = WeakReference(context)
	}
	
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
	
	private fun getUsageStatsManager(): UsageStatsManager {
		val systemService = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) Context.USAGE_STATS_SERVICE else "usagestats"
		return context.get()?.getSystemService(systemService) as UsageStatsManager
	}
	fun countTotalEvents(from: Long, to: Long): UsageStatsInfo {
		val usageStatsManager = getUsageStatsManager()
		
		return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			val counter = AppUsageCounter(
				from,
				to,
				UsageEvents.Event.SCREEN_INTERACTIVE,
				UsageEvents.Event.SCREEN_NON_INTERACTIVE,
				UsageEvents.Event.DEVICE_SHUTDOWN
			)
			val events = usageStatsManager.queryEvents(from, to)
			val event: UsageEvents.Event = UsageEvents.Event()
			
			while(events.getNextEvent(event)) {
				counter.addEvent(event)
			}
			
			counter.getResults()
		}
		else
			context.get()?.let { ScreenTrackingReceiver.getData(it) } ?: UsageStatsInfo(-1, -1)
	}
	
	
	
	//usageStatsManager.queryAndAggregateUsageStats() seems to be very unreliable. Counting events manually works better
	// Thanks to: https://stackoverflow.com/a/50647945/10423612
	fun getAllPackageUsages(from: Long, to: Long): Map<String, UsageStatsInfo> {
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

@Composable
fun AppUsageView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
	val appUsageCalculator = AppUsageCalculator(LocalContext.current)
	val now = NativeLink.getNowMillis()
	val to = NativeLink.getMidnightMillis()
	val from = to - 86400000
	val displayAppUsage = input.packageId != ""
	
	val yesterdayUsageTime: Long
	val yesterdayUsageCount: Int
	val todayUsageTime: Long
	val todayUsageCount: Int
	
	if(displayAppUsage) {
		val packageUsages = appUsageCalculator.getAllPackageUsages(from, to)
		yesterdayUsageCount = packageUsages[input.packageId]?.count ?: -1
		yesterdayUsageTime = packageUsages[input.packageId]?.totalTime ?: -1L
		
		val packageUsagesToday = appUsageCalculator.getAllPackageUsages(to, now)
		todayUsageCount = packageUsagesToday[input.packageId]?.count ?: -1
		todayUsageTime = packageUsagesToday[input.packageId]?.totalTime ?: -1L
	}
	else { //general screen time:
		val yesterdayPair = appUsageCalculator.countTotalEvents(from, to)
		yesterdayUsageCount = yesterdayPair.count
		yesterdayUsageTime = yesterdayPair.totalTime
		
		val todayPair = appUsageCalculator.countTotalEvents(to, now)
		todayUsageCount = todayPair.count
		todayUsageTime = todayPair.totalTime
	}
	
	save(yesterdayUsageTime.toString(), mapOf(
		Pair("usageCount", yesterdayUsageCount.toString()),
		Pair("usageTimeToday", todayUsageTime.toString()),
		Pair("usageCountToday", todayUsageCount.toString())
	))
	
	AppUsageTableView(yesterdayUsageCount, yesterdayUsageTime, displayAppUsage, input.packageId)
}

@Composable
fun AppUsageTableView(yesterdayUsageCount: Int, yesterdayUsageTime: Long, displayAppUsage: Boolean, packageId: String) {
	val usageTime = if(yesterdayUsageTime == 0L || yesterdayUsageTime == -1L) {
		stringResource(R.string.no_data)
	}
	else {
		val hours = TimeUnit.MILLISECONDS.toHours(yesterdayUsageTime)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(yesterdayUsageTime) % 60
		val seconds = TimeUnit.MILLISECONDS.toSeconds(yesterdayUsageTime) % 60
		
		stringResource(
			R.string.time_format_android,
			hours.toString().padStart(2, '0'),
			minutes.toString().padStart(2, '0'),
			seconds.toString().padStart(2, '0')
		)
	}
	
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			stringResource(if(displayAppUsage) R.string.colon_app_usage else R.string.colon_total_screenTime),
			fontWeight = FontWeight.Bold
		)
		if(displayAppUsage) {
			Text(packageId, fontSize = MaterialTheme.typography.labelLarge.fontSize, modifier = Modifier.padding(start = 10.dp))
		}
		
		Spacer(modifier = Modifier.height(5.dp))
		Row(modifier = Modifier.padding(start = 20.dp)) {
			Text(stringResource(R.string.colon_usageTime))
			Spacer(modifier = Modifier.width(20.dp))
			Text(usageTime)
		}
		if(yesterdayUsageCount > 0) {
			Row(modifier = Modifier.padding(start = 20.dp)) {
				Text(stringResource(R.string.colon_usageCount))
				Spacer(modifier = Modifier.width(20.dp))
				Text(yesterdayUsageCount.toString())
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppUsageTableViewForAppUsage() {
	ESMiraSurface {
		AppUsageTableView(1, 5400000, true, "at.jodlidev.esmira")
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppUsageTableViewForAppUsageWithNoData() {
	ESMiraSurface {
		AppUsageTableView(-1, -1, true, "at.jodlidev.esmira")
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppUsageTableViewForScreenTracking() {
	ESMiraSurface {
		AppUsageTableView(1, 5400000, false, "")
	}
}