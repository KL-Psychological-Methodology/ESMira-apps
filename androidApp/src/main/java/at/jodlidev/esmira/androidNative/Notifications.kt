package at.jodlidev.esmira.androidNative

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import at.jodlidev.esmira.*
import at.jodlidev.esmira.sharedCode.NotificationsInterface
import at.jodlidev.esmira.sharedCode.data_structure.*
import java.lang.ref.WeakReference

/**
 * Created by JodliDev on 18.05.2020.
 */
object Notifications: NotificationsInterface {
	private const val CHANNEL_ID_QUESTIONNAIRE_INVITATION = "questionnaire_notification"
	private const val CHANNEL_ID_STUDY_MESSAGES = "study_messages"
	private const val CHANNEL_ID_GENERAL_NOTIFICATION = "general_notification"
	private const val CHANNEL_ID_STUDY_UPDATED = "study_updated"
	
	lateinit var context: WeakReference<Context>
	
	fun init(context: Context) {
		this.context = WeakReference(context.applicationContext)
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val sound: Uri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.notifcation}")
			val attr: AudioAttributes = AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_NOTIFICATION)
				.build()
			val notificationManager: NotificationManager = context.getSystemService(
				NotificationManager::class.java)
			
			//general notifications
			var channel = NotificationChannel(CHANNEL_ID_GENERAL_NOTIFICATION, context.resources.getString(R.string.notificationChannel_generalNotifications_name), NotificationManager.IMPORTANCE_DEFAULT)
			channel.description = context.resources.getString(R.string.notificationChannel_generalNotifications_desc)
			channel.enableVibration(true)
			channel.shouldVibrate()
			channel.enableLights(true)
			channel.lightColor = Color.BLUE
			channel.shouldShowLights()
			channel.setSound(sound, attr)
			notificationManager.createNotificationChannel(channel)
			
			//questionnaire_reminders:
			channel = NotificationChannel(CHANNEL_ID_QUESTIONNAIRE_INVITATION, context.resources.getString(
				R.string.notificationChannel_questionnaire_name), NotificationManager.IMPORTANCE_HIGH)
			channel.description = context.resources.getString(R.string.notificationChannel_questionnaire_desc)
			channel.enableVibration(true)
			channel.shouldVibrate()
			channel.enableLights(true)
			channel.lightColor = Color.BLUE
			channel.shouldShowLights()
			channel.setSound(sound, attr)
			notificationManager.createNotificationChannel(channel)
			
			//study messages:
			channel = NotificationChannel(CHANNEL_ID_STUDY_MESSAGES, context.resources.getString(R.string.notificationChannel_studyMessages_name), NotificationManager.IMPORTANCE_HIGH)
			channel.description = context.resources.getString(R.string.notificationChannel_studyMessages_desc)
			channel.enableVibration(true)
			channel.shouldVibrate()
			channel.enableLights(true)
			channel.lightColor = Color.BLUE
			channel.shouldShowLights()
			channel.setSound(sound, attr)
			notificationManager.createNotificationChannel(channel)
			
			//study_updated:
			channel = NotificationChannel(CHANNEL_ID_STUDY_UPDATED, context.resources.getString(R.string.notificationChannel_studyUpdates_name), NotificationManager.IMPORTANCE_DEFAULT)
			channel.description = context.resources.getString(R.string.notificationChannel_studyUpdates_desc)
			notificationManager.createNotificationChannel(channel)
			
		}
	}
	
	override fun firePostponed(alarm: Alarm, msg: String, subId: Int) {
		ErrorBox.error("Notifications", "firePostponed() was used but is meant for IOS")
	}
	private fun fire(title: String, msg: String, id: Int, channel: String, intent: Intent?) {
		val context = context.get() ?: return
		val realIntent: Intent
		if(intent == null)
			realIntent = Intent()
		else {
			realIntent = intent
			realIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
		val pendingIntent: PendingIntent = PendingIntent.getActivity(context, id, realIntent, PendingIntent.FLAG_UPDATE_CURRENT)
		val builder: NotificationCompat.Builder = createNotification(context, title, msg, pendingIntent, channel)
		NotificationManagerCompat.from(context).notify(id, builder.build())
	}
	
	private fun createNotification(context: Context, title: String, msg: String, pendingIntent: PendingIntent, channel: String): NotificationCompat.Builder {
		val msg = HtmlHandler.fromHtml(msg, context)
		return NotificationCompat.Builder(context, channel)
			.setSmallIcon(R.drawable.ic_notification)
			.setContentTitle(title)
			.setContentText(msg)
			.setStyle(NotificationCompat.BigTextStyle().bigText(msg))
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)
			.setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.notifcation}"))
//				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
			.setLights(Color.BLUE, 1000, 1000)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
	}
	
	private fun notificationWasPosted(notification_id: Int): Boolean {
		val context = context.get() ?: return false
		when {
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
				val notificationManager: NotificationManager = context.getSystemService(
					NotificationManager::class.java)
				val notifications: Array<StatusBarNotification> = notificationManager.activeNotifications
				for(n: StatusBarNotification in notifications) {
					if(n.id == notification_id) {
						return true
					}
				}
				return false
			}
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ->
				return NotificationManagerCompat.from(context).areNotificationsEnabled()
			else ->
				return true
		}
	}
	
	override fun fire(title: String, msg: String, id: Int) {
		fire(title, msg, id, CHANNEL_ID_GENERAL_NOTIFICATION, null)
	}
	override fun fireSchedulesChanged(study: Study) {
		val context = context.get() ?: return
		ErrorBox.log("update_studies", "schedules have been reset")
		val intent = Intent(context, Activity_editSchedules::class.java)
		fire(context.getString(R.string.android_info_study_updated, study.title), context.getString(R.string.info_study_updated_desc), study.id.toInt(), CHANNEL_ID_STUDY_UPDATED, intent)
	}
	
	override fun fireQuestionnaireBing(title: String, msg: String, questionnaire: Questionnaire, timeoutMin: Int, type: String, scheduledToTimestamp: Long) {
		val context = context.get() ?: return
		val notificationId: Int = questionnaire.id.toInt() //even if questionnaireId is too big, it will just overflow and still be unique. So we still can use questionnaireId to find its notification
		val intent = Intent(context, Activity_main::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		intent.putExtra(Activity_main.EXTRA_OPEN_QUESTIONNAIRE, questionnaire.id)
		
		val pendingIntent: PendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
		val builder: NotificationCompat.Builder = createNotification(context, title, msg, pendingIntent, CHANNEL_ID_QUESTIONNAIRE_INVITATION)
		if(timeoutMin != 0) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				builder.setTimeoutAfter(timeoutMin * 1000 * 60.toLong()) //we don't really need this because WorkerBox (below) will remove the notification anyway
			WorkerBox.timeoutNotification(context, notificationId, questionnaire.id, timeoutMin)
		}
		NotificationManagerCompat.from(context).notify(notificationId, builder.build())
		
		//TODO: it is possible that this check happens too soon and will not always catch the freshly posted notification
		DataSet.createActionSentDataSet(type, questionnaire, scheduledToTimestamp)
	}
	override fun fireStudyNotification(title: String, msg: String, questionnaire: Questionnaire, scheduledToTimestamp: Long) {
		val notificationId = questionnaire.id.toInt()
		fire(title, msg, notificationId)
		
		//TODO: it is possible that this check happens too soon and will not always catch the freshly posted notification
		DataSet.createActionSentDataSet(DataSet.TYPE_NOTIFICATION, questionnaire, scheduledToTimestamp)
	}
	
	override fun fireMessageNotification(study: Study) {
		val context = context.get() ?: return
		val notificationId = study.id.toInt()
		val intent = Intent(context, Activity_main::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		intent.putExtra(Activity_main.EXTRA_OPEN_STUDY_MESSAGES, study.id)
		
		val pendingIntent: PendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
		val builder: NotificationCompat.Builder = createNotification(context, study.title, context.getString(R.string.info_new_message), pendingIntent, CHANNEL_ID_STUDY_MESSAGES)
		NotificationManagerCompat.from(context).notify(notificationId, builder.build())
	}
	
	override fun removeQuestionnaireBing(questionnaire: Questionnaire) {
		remove(questionnaire.id.toInt())
		val context = context.get() ?: return
		WorkerBox.cancelNotificationTimeout(context, questionnaire.id)
	}
	
	override fun remove(id: Int) {
		val context = context.get() ?: return
		NotificationManagerCompat.from(context).cancel(id)
	}
	
}