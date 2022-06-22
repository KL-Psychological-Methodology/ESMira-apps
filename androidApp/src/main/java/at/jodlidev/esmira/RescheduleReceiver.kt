package at.jodlidev.esmira

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import at.jodlidev.esmira.sharedCode.Scheduler
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox

/**
 * Created by JodliDev on 21.05.2019.
 */
class RescheduleReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		CrashExceptionHandler.init(context)
		val action: String? = intent.action
		if(action == null) {
			ErrorBox.warn("RescheduleReceiver", "no action")
			return
		}
		val timeChanged: Boolean
		when(action) {
			Intent.ACTION_BOOT_COMPLETED -> {
				timeChanged = false
				ErrorBox.log("RescheduleReceiver", "Detected Boot...")
				ScreenTrackingService.startService(context, true)
			}
			Intent.ACTION_TIMEZONE_CHANGED -> {
				timeChanged = true
				ErrorBox.log("RescheduleReceiver", "Detected changed timezone...")
			}
			Intent.ACTION_TIME_CHANGED -> {
				timeChanged = true
				ErrorBox.log("RescheduleReceiver", "Detected changed time...")
			}
			else -> {
				ErrorBox.warn("RescheduleReceiver", "Unknown action: $action")
				return
			}
		}
		
		Scheduler.reactToBootOrTimeChange(timeChanged)
	}
}