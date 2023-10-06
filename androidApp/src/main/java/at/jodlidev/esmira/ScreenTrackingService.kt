package at.jodlidev.esmira

import android.app.Service
import android.content.*
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.androidNative.Notifications
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox


/**
 * Created by JodliDev on 14.06.2022.
 */
class ScreenTrackingService : Service() {
	private var receiver: BroadcastReceiver? = null
	override fun onBind(intent: Intent?): IBinder? {
		return null
	}
	
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return START_STICKY
	}
	override fun onCreate() {
		isStarted = true
		CrashExceptionHandler.init(applicationContext)
		ErrorBox.log("ScreenTrackingService", "ScreenTrackingService was started")
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			startForeground(Notifications.SCREEN_TRACKING_ID, Notifications.createScreenTrackingNotification(applicationContext))
		
		receiver = ScreenTrackingReceiver.startReceiver(applicationContext)
	}
	
	override fun onDestroy() {
		if(receiver != null) {
			try {
				applicationContext.unregisterReceiver(receiver)
			}
			catch(e: Throwable) {
				ErrorBox.error("ScreenTrackingService", "Could not unregister ScreenTrackingService", e)
			}
			receiver = null
		}
	}
	companion object {
		//this is a static variable. So it wont reset as long as the service stays alive. The moment ESMira gets killed, it will initialize to false again.
		private var isStarted = false
		
		fun startService(context: Context, noPenalty: Boolean = false) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
				return
				
			var screenTracking = false
			for(study in DbLogic.getJoinedStudies()) {
				if(study.hasScreenTracking()) {
					screenTracking = true
					break
				}
			}
			if(!screenTracking)
				return
			
			val intent = Intent(context, ScreenTrackingService::class.java)
			if(!isStarted) {
				if(!noPenalty) {
					ErrorBox.warn("ScreenTrackingService", "ScreenTrackingService seems to have died.")
					ScreenTrackingReceiver.markMissings(context)
				}
				else {
					val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
					val screenIsOn = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) powerManager.isInteractive else powerManager.isScreenOn
					ScreenTrackingReceiver.newEvent(context, if(screenIsOn) Intent.ACTION_SCREEN_ON else Intent.ACTION_SCREEN_OFF)
				}
			}
			ErrorBox.log("ScreenTrackingService", "Sending Intent to start ScreenTrackingService...")
			ContextCompat.startForegroundService(context, intent)
		}
	}
}