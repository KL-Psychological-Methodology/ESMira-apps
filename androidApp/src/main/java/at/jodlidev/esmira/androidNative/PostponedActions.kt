package at.jodlidev.esmira.androidNative

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import at.jodlidev.esmira.AlarmBox
import at.jodlidev.esmira.sharedCode.PostponedActionsInterface
import at.jodlidev.esmira.WorkerBox
import at.jodlidev.esmira.sharedCode.data_structure.Alarm
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import java.lang.ref.WeakReference

/**
 * Created by JodliDev on 18.05.2020.
 */
object PostponedActions : PostponedActionsInterface {
	const val DATA_ALARM_ID = "alarm_id"
	
	lateinit var context: WeakReference<Context>
	
	fun init(context: Context) {
		this.context = WeakReference(context.applicationContext)
	}
	private fun getPendingIntentFlag(): Int {
		return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		else
			PendingIntent.FLAG_UPDATE_CURRENT
	}
	private fun cancel(alarm: Alarm, intent: Intent, am: AlarmManager) {
		val context = context.get() ?: return
		
		ErrorBox.log("Alarm lifespan", "Canceling alarm: ${alarm.id}")
		val pendingIntent = PendingIntent.getBroadcast(context, alarm.id.toInt(), intent, getPendingIntentFlag())
		if(pendingIntent != null) {
			am.cancel(pendingIntent)
			pendingIntent.cancel()
		}
		else
			ErrorBox.log("AlarmBox", "Could not cancel Alarm (${alarm.id})")
	}
	
	override fun scheduleAlarm(alarm: Alarm): Boolean {
		val context = context.get() ?: return false
		
		val intent = Intent(context, AlarmBox::class.java)
		intent.putExtra(DATA_ALARM_ID, alarm.id)
		
		val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
		val pendingIntent = PendingIntent.getBroadcast(context, alarm.id.toInt(), intent, getPendingIntentFlag())
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
			AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, alarm.timestamp, pendingIntent)
		else {
			ErrorBox.warn("PostponedActions", "ESMira does not have the permission to use setExactAndAllowWhileIdle(). Using setAndAllowWhileIdle instead!")
			AlarmManagerCompat.setAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, alarm.timestamp, pendingIntent)
		}
		return true
	}
	override fun cancel(alarm: Alarm) {
		val context = context.get() ?: return
		
		val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
		val intent = Intent(context, AlarmBox::class.java)
		cancel(alarm, intent, am)
	}
	override fun syncDataSets() {
		context.get()?.let {context ->
			WorkerBox.sync(context, true)
		}
		
	}
	override fun updateStudiesRegularly() {
		context.get()?.let {context ->
			WorkerBox.updateStudiesRegularly(context)
		}
	}
	override fun cancelUpdateStudiesRegularly() {
		context.get()?.let {context ->
			WorkerBox.cancelAllWork(context, WorkerBox.TAG_UPDATE_STUDIES)
		}
	}
}