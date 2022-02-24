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
import android.widget.ImageView
import android.widget.TextView
import java.util.concurrent.TimeUnit
import android.app.usage.UsageStats
import java.util.*


/**
 * Created by JodliDev on 16.12.2021.
 */
class AppUsageView(context: Context) : TextElView(context, R.layout.view_input_app_usage) {
	private var appUsageElement: TextView = findViewById(R.id.appUsageTime)
	private var appUsageCountElement: TextView = findViewById(R.id.appUsageCount)
	private var appNameElement: TextView = findViewById(R.id.appName)
	private var packageIdElement: TextView = findViewById(R.id.packageId)
	private var appIconElement: ImageView = findViewById(R.id.appIcon)
	
	init {
		appUsageElement.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
			override fun afterTextChanged(s: Editable) {
				if(isBound)
					input.value = appUsageElement.text.toString()
			}
		})
	}
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
			return
		}
		
		val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
		val lastQuestionnaireFrom = questionnaire.lastCompletedUtc
		val nowMs = System.currentTimeMillis()
		
		val cal = Calendar.getInstance()
		cal.timeInMillis = nowMs
		cal[Calendar.HOUR_OF_DAY] = 0
		cal[Calendar.MINUTE] = 0
		cal[Calendar.SECOND] = 0
		cal[Calendar.MILLISECOND] = 0
		val todayMs = cal.timeInMillis
		val yesterdayMs = todayMs - 86400000
		
		val todayAppUsageTime: Long
		var todayAppUsageTimeVisible = -1L
		var todayAppUsageCount = 0
		
		val yesterdayAppUsageTime: Long
		var yesterdayAppUsageTimeVisible = -1L
		var yesterdayAppUsageCount = 0
		
		val fromQuestionnaireAppUsageTime: Long
		var fromQuestionnaireAppUsageTimeVisible = -1L
		var fromQuestionnaireAppUsageCount = 0
		
		//general screen time:
		if(input.packageId == "") {
			val todayPair = countTotalEvents(todayMs, nowMs)
			todayAppUsageCount = todayPair.first
			todayAppUsageTime = todayPair.second
			
			val yesterdayPair = countTotalEvents(yesterdayMs, todayMs)
			yesterdayAppUsageCount = yesterdayPair.first
			yesterdayAppUsageTime = yesterdayPair.second
			
			val fromQuestionnairePair = countTotalEvents(lastQuestionnaireFrom, nowMs)
			fromQuestionnaireAppUsageCount = fromQuestionnairePair.first
			fromQuestionnaireAppUsageTime = fromQuestionnairePair.second
			
			
			appNameElement.text = context.getString(R.string.colon_total_screenTime)
			packageIdElement.visibility = GONE
			appIconElement.visibility = GONE
		}
		//specific app usage time:
		else {
			val packageId = input.packageId
			
			val todayTriple = countSpecificAppEvents(packageId, todayMs, nowMs)
			todayAppUsageCount = todayTriple.first
			todayAppUsageTime = todayTriple.second
			todayAppUsageTimeVisible = todayTriple.third
			
			val yesterdayTriple = countSpecificAppEvents(packageId, yesterdayMs, todayMs)
			yesterdayAppUsageCount = yesterdayTriple.first
			yesterdayAppUsageTime = yesterdayTriple.second
			yesterdayAppUsageTimeVisible = yesterdayTriple.third
			
			val fromQuestionnaireTriple = countSpecificAppEvents(packageId, lastQuestionnaireFrom, nowMs)
			fromQuestionnaireAppUsageCount = fromQuestionnaireTriple.first
			fromQuestionnaireAppUsageTime = fromQuestionnaireTriple.second
			fromQuestionnaireAppUsageTimeVisible = fromQuestionnaireTriple.third
			
			
			packageIdElement.visibility = VISIBLE
			packageIdElement.text = packageId
			
			try {
				val packageManager = context.packageManager
				appIconElement.setImageDrawable(packageManager.getApplicationIcon(packageId))
				val app = packageManager.getApplicationInfo(packageId, 0)
				appNameElement.text = packageManager.getApplicationLabel(app)
				appIconElement.visibility = VISIBLE
			}
			catch(e: Throwable) {
				appNameElement.text = context.getString(R.string.not_installed)
				appIconElement.visibility = INVISIBLE
			}
		}
		input.value = lastQuestionnaireFrom.toString()
		input.additionalValues["usageTime"] = fromQuestionnaireAppUsageTime.toString()
		input.additionalValues["visibleTime"] = fromQuestionnaireAppUsageTimeVisible.toString()
		input.additionalValues["usageCount"] = fromQuestionnaireAppUsageCount.toString()
		
		input.additionalValues["todayUsageTime"] = todayAppUsageTime.toString()
		input.additionalValues["todayVisibleTime"] = todayAppUsageTimeVisible.toString()
		input.additionalValues["todayUsageCount"] = todayAppUsageCount.toString()
		
		input.additionalValues["yesterdayUsageTime"] = yesterdayAppUsageTime.toString()
		input.additionalValues["yesterdayVisibleTime"] = yesterdayAppUsageTimeVisible.toString()
		input.additionalValues["yesterdayUsageCount"] = yesterdayAppUsageCount.toString()
		
		isBound = false //in case this view was reused
		
		val hours = TimeUnit.MILLISECONDS.toHours(fromQuestionnaireAppUsageTime)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(fromQuestionnaireAppUsageTime) % 60
		val seconds = TimeUnit.MILLISECONDS.toSeconds(fromQuestionnaireAppUsageTime) % 60
		
		
		appUsageElement.text = context.getString(R.string.time_format_android, hours, minutes, seconds)
		appUsageCountElement.text = fromQuestionnaireAppUsageCount.toString()
		
		
		
		
		//TODO: For testing-->
		val usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(lastQuestionnaireFrom, nowMs)
		
		var totalTime = 0L
		for(entry in usageStatsMap) {
			totalTime += entry.value.totalTimeInForeground
		}
		
		input.additionalValues["usageTimeFromApps"] = totalTime.toString()
		//TODO: <--for testing
		
		
		
		
		
		super.bindData(input, questionnaire)
	}
	
	private fun countSpecificAppEvents(packageId: String, from: Long, to: Long): Triple<Int, Long, Long> {
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
			return Triple(-1, -1, -1)
		}
		val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
		val usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(from, to)
		
		
		return if(usageStatsMap.containsKey(packageId)) {
			val usageStats = usageStatsMap[packageId]!!
			Triple(
				getAppUsageCount(usageStats),
				usageStats.totalTimeInForeground,
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
					usageStats.totalTimeVisible
				else
					-1
			)
		}
		else {
			Triple(-1, -1, -1)
		}
	}
	
	
	private fun getAppUsageCount(usageStats: UsageStats) : Int {
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
			return -1
		}
		
		return try {
			val mLaunchCount = UsageStats::class.java.getDeclaredField("mLaunchCount")
			mLaunchCount.get(usageStats) as Int
		}
		catch(e: NoSuchFieldException) {
			-1
		}
		catch(e: SecurityException) {
			-1
		}
	}
	
	private fun countTotalEvents(from: Long, to: Long): Pair<Int, Long> {
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
			return Pair(-1, -1L)
		}
		
		val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
		
		var count = 0
		var totalTime = 0L
		
		//actual screen time:
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			val events = usageStatsManager.queryEvents(from, to)
			val event: UsageEvents.Event = UsageEvents.Event()
			
			var enableTimestamp = 0L
			var hasEvent = false
			
			
			while(events.getNextEvent(event)) {
				when(event.eventType) {
					UsageEvents.Event.SCREEN_INTERACTIVE -> {
						++count
						enableTimestamp = event.timeStamp
						hasEvent = true
					}
					UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
						if(enableTimestamp != 0L) {
							totalTime += event.timeStamp - enableTimestamp
							enableTimestamp = 0L
							hasEvent = true
						} else if(!hasEvent)//measures the time from filling out the last questionnaire to turning of the screen
							totalTime += event.timeStamp - from
						
					}
					UsageEvents.Event.DEVICE_SHUTDOWN -> {
						if(enableTimestamp != 0L) {
							totalTime += event.timeStamp - enableTimestamp
							enableTimestamp = 0L
							hasEvent = true
						}
					}
				}
			}
			if(enableTimestamp != 0L)
				totalTime += to - enableTimestamp
			else if(!hasEvent) //screen has been on since last time questionnaire was filled out
				totalTime = to - from
			
		}
		//combined app usage time:
		else {
			val usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(from, to)
			
			for(entry in usageStatsMap) {
				totalTime += entry.value.totalTimeInForeground
				count += getAppUsageCount(entry.value)
			}
		}
		return Pair(count, totalTime)
	}
}