package at.jodlidev.esmira;

import android.content.*
import android.widget.Toast
import androidx.preference.PreferenceManager
import at.jodlidev.esmira.input_views.AppUsageView
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox

/**
 * Created by JodliDev on 14.06.2022.
 */

class ScreenTrackingReceiver : BroadcastReceiver() {
	class ScreenTrackData(context: Context) {
		private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
		private val edit: SharedPreferences.Editor = prefs.edit()
		
		val now = NativeLink.getNowMillis()
		val todayStart = NativeLink.getMidnightMillis()
		val yesterdayStart = todayStart - ONE_DAY
		
		private var _trackedDayStartMarker = prefs.getLong(KEY_TRACKED_DAY_START_MARKER, 0)
		var trackedDayStartMarker: Long
			get() { return _trackedDayStartMarker }
			set(value) {
				_trackedDayStartMarker = value
				edit.putLong(KEY_TRACKED_DAY_START_MARKER, value)
			}
		
		private var _lastScreenOnTime = prefs.getLong(KEY_LAST_SCREEN_ON_TIME, -1)
		var lastScreenOnTime: Long
			get() { return _lastScreenOnTime }
			set(value) {
				_lastScreenOnTime = value
				edit.putLong(KEY_LAST_SCREEN_ON_TIME, value)
			}
		
		private var _todayTime = prefs.getLong(KEY_TODAY_TIME, 0)
		var todayTime: Long
			get() { return _todayTime }
			set(value) {
				_todayTime = value
				edit.putLong(KEY_TODAY_TIME, value)
			}
		
		private var _todayCount = prefs.getInt(KEY_TODAY_COUNT, 0)
		var todayCount: Int
			get() { return _todayCount }
			set(value) {
				_todayCount = value
				edit.putInt(KEY_TODAY_COUNT, value)
			}
		
		private var _yesterdayTime = prefs.getLong(KEY_YESTERDAY_TIME, 0)
		var yesterdayTime: Long
			get() { return _yesterdayTime }
			set(value) {
				_yesterdayTime = value
				edit.putLong(KEY_YESTERDAY_TIME, value)
			}
		
		private var _yesterdayCount = prefs.getInt(KEY_YESTERDAY_COUNT, 0)
		var yesterdayCount: Int
			get() { return _yesterdayCount }
			set(value) {
				_yesterdayCount = value
				edit.putInt(KEY_YESTERDAY_COUNT, value)
			}
		
		fun save() {
			edit.apply()
		}
		
		companion object {
			const val MISSING_LONG = -1L
			const val MISSING_INT = -1
		}
	}
	
	override fun onReceive(context: Context, intent: Intent) {
		CrashExceptionHandler.init(context)
		val action: String? = intent.action
		if(action == null) {
			ErrorBox.warn("Service_screenTimeMeasurement", "no action")
			return
		}
		
		newEvent(context, action)
	}
	
	
	companion object {
		private const val KEY_YESTERDAY_TIME = "yesterdayTime"
		private const val KEY_YESTERDAY_COUNT = "yesterdayCount"
		private const val KEY_TODAY_TIME = "todayTime"
		private const val KEY_TODAY_COUNT = "todayCount"
		private const val KEY_LAST_SCREEN_ON_TIME = "lastScreenOnTime"
		private const val KEY_TRACKED_DAY_START_MARKER = "measureTodayStart"
		private const val ONE_DAY = 1000 * 60 * 60 * 24
		
		private fun checkDay(data: ScreenTrackData): Boolean {
			val todayStart = NativeLink.getMidnightMillis()
			val yesterdayStart = todayStart - ONE_DAY
			
			when(data.trackedDayStartMarker) {
				in yesterdayStart until todayStart -> { //current data is from yesterday
					ErrorBox.log("ScreenTrackingReceiver", "Day has changed. Moving current data to yesterday")
					data.trackedDayStartMarker = todayStart
					data.yesterdayTime = data.todayTime
					data.yesterdayCount = data.todayCount
					data.todayTime = 0
					data.todayCount = 0
					return true
				}
				in 0 until yesterdayStart -> { //current data is too old to keep
					ErrorBox.log("ScreenTrackingReceiver", "Days were skipped. Setting everything to 0")
					data.trackedDayStartMarker = todayStart
					data.yesterdayTime = 0
					data.yesterdayCount = 0
					data.todayTime = 0
					data.todayCount = 0
					return true
				}
			}
			return false
		}
		private fun handleScreenOn(data: ScreenTrackData) {
			if(data.lastScreenOnTime != ScreenTrackData.MISSING_LONG)
				ErrorBox.warn("ScreenTrackingReceiver", "Two ACTION_SCREEN_ON events in succession!")
			else if(data.todayCount != ScreenTrackData.MISSING_INT)
				++data.todayCount
			
			data.lastScreenOnTime = data.now
		}
		private fun handleScreenOff(data: ScreenTrackData) {
			if(data.lastScreenOnTime != ScreenTrackData.MISSING_LONG) {
				val addTime = if(data.lastScreenOnTime < data.todayStart) {
					if(data.lastScreenOnTime < data.yesterdayStart)
						0
					else {
						ErrorBox.warn("ScreenTrackingReceiver", "Screen was turned on before midnight. Splitting time between yesterday and today")
						if(data.yesterdayTime != ScreenTrackData.MISSING_LONG)
							data.yesterdayTime += NativeLink.getMidnightMillis() - data.lastScreenOnTime
						if(data.yesterdayCount != ScreenTrackData.MISSING_INT)
							++data.yesterdayCount
						
						data.now - NativeLink.getMidnightMillis()
					}
				}
				else
					data.now - data.lastScreenOnTime
				
				if(data.todayTime != ScreenTrackData.MISSING_LONG)
					data.todayTime += addTime
			}
			data.lastScreenOnTime = ScreenTrackData.MISSING_LONG
		}
		
		fun markMissings(context: Context) {
			val data = ScreenTrackData(context)
			val prefs = PreferenceManager.getDefaultSharedPreferences(context)
			data.todayTime = ScreenTrackData.MISSING_LONG
			data.todayCount = ScreenTrackData.MISSING_INT
			
			if(data.trackedDayStartMarker in data.yesterdayStart until data.todayStart) {
				ErrorBox.warn("ScreenTrackingReceiver", "Marking today and yesterday as missing")
				data.yesterdayTime = ScreenTrackData.MISSING_LONG
				data.yesterdayCount = ScreenTrackData.MISSING_INT
			}
			else
				ErrorBox.warn("ScreenTrackingReceiver", "Marking today as missing")
			data.trackedDayStartMarker = data.todayStart
			data.save()
		}
		fun newEvent(context: Context, action: String) {
			ErrorBox.log("ScreenTrackingReceiver", "Received event $action")
			val data = ScreenTrackData(context)
			
			checkDay(data)
			
			when(action) {
				Intent.ACTION_SCREEN_ON ->
					handleScreenOn(data)
				Intent.ACTION_SCREEN_OFF, Intent.ACTION_SHUTDOWN ->
					handleScreenOff(data)
			}
			data.save()
		}
		
		fun startReceiver(context: Context): BroadcastReceiver {
			val intentFilter = IntentFilter()
			
			intentFilter.addAction(Intent.ACTION_SCREEN_ON)
			intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
			intentFilter.addAction(Intent.ACTION_SHUTDOWN)
			
			val receiver = ScreenTrackingReceiver()
			context.registerReceiver(receiver, intentFilter)
			return receiver
		}
		fun getData(context: Context): AppUsageView.UsageStatsInfo {
			val data = ScreenTrackData(context)
			
			if(checkDay(data))
				data.save()
			return AppUsageView.UsageStatsInfo(data.yesterdayCount, data.yesterdayTime)
		}
	}
}