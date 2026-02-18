package at.jodlidev.esmira.androidNative

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.*
import at.jodlidev.esmira.activities.ChangeSchedulesDialogActivity
import at.jodlidev.esmira.activities.MainActivity
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
	private const val CHANNEL_ID_SCREEN_TRACKING = "screen_tracking"
	
	private const val ID_RANGE_SIZE = 1000
	private const val SCHEDULE_CHANGED_ID_RANGE = 1000
	private const val QUESTIONNAIRE_BING_ID_RANGE = 3000
	private const val MESSAGE_ID_RANGE = 5000
	const val TEST_ID = 10
	const val SCREEN_TRACKING_ID = 20
	
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
			
			//screen_tracking:
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
				channel = NotificationChannel(
					CHANNEL_ID_SCREEN_TRACKING,
					context.resources.getString(R.string.notificationChannel_screen_tracking),
					NotificationManager.IMPORTANCE_LOW
				)
				channel.description = context.resources.getString(R.string.notificationChannel_screen_tracking_desc)
				notificationManager.createNotificationChannel(channel)
			}
		}
	}
	
	private fun getPendingIntentFlag(): Int {
		return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		else
			PendingIntent.FLAG_UPDATE_CURRENT
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
		val pendingIntent: PendingIntent = PendingIntent.getActivity(context, id, realIntent, getPendingIntentFlag())
		val builder: NotificationCompat.Builder = createNotification(context, title, msg, pendingIntent, channel)
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
			NotificationManagerCompat.from(context).notify(id, builder.build())
		}
		else {
			ErrorBox.warn("Notifications", "User has revoked permissions for notifications")
		}
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
	private fun createId(value: Long, range: Int): Int {
		return (value%ID_RANGE_SIZE + range).toInt()
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
		val intent = Intent(context, ChangeSchedulesDialogActivity::class.java)
		fire(context.getString(R.string.android_info_study_updated, study.title), context.getString(R.string.info_study_updated_desc), createId(study.id, SCHEDULE_CHANGED_ID_RANGE), CHANNEL_ID_STUDY_UPDATED, intent)
	}
	
	override fun fireQuestionnaireBing(title: String, msg: String, questionnaire: Questionnaire, timeoutMin: Int, type: DataSet.EventTypes, scheduledToTimestamp: Long) {
		val context = context.get() ?: return
		val notificationId: Int = createId(questionnaire.id, QUESTIONNAIRE_BING_ID_RANGE)
		val intent = Intent(context, MainActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		intent.putExtra(MainActivity.EXTRA_OPEN_QUESTIONNAIRE, questionnaire.id)
		
		val pendingIntent: PendingIntent = PendingIntent.getActivity(context, notificationId, intent, getPendingIntentFlag())
		val builder: NotificationCompat.Builder = createNotification(context, title, msg, pendingIntent, CHANNEL_ID_QUESTIONNAIRE_INVITATION)
		if(timeoutMin != 0) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				builder.setTimeoutAfter(timeoutMin * 1000 * 60.toLong()) //we don't really need this because WorkerBox (below) will remove the notification anyway
			WorkerBox.timeoutNotification(context, notificationId, questionnaire.id, timeoutMin)
		}
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
			NotificationManagerCompat.from(context).notify(notificationId, builder.build())
		}
		else {
			ErrorBox.warn("Notifications", "User has revoked permissions for notifications")
		}
		
		//TODO: it is possible that this check happens too soon and will not always catch the freshly posted notification
		DataSet.createActionSentDataSet(type, questionnaire, scheduledToTimestamp)
	}
	override fun fireStudyNotification(title: String, msg: String, questionnaire: Questionnaire, scheduledToTimestamp: Long) {
		val notificationId = questionnaire.id.toInt()
		fire(title, msg, notificationId)
		
		//TODO: it is possible that this check happens too soon and will not always catch the freshly posted notification
		DataSet.createActionSentDataSet(DataSet.EventTypes.notification, questionnaire, scheduledToTimestamp)
	}
	
	override fun fireMessageNotification(study: Study) {
		val context = context.get() ?: return
		val notificationId = createId(study.id, MESSAGE_ID_RANGE)
		val intent = Intent(context, MainActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		intent.putExtra(MainActivity.EXTRA_OPEN_MESSAGES, study.id)
		
		val pendingIntent: PendingIntent = PendingIntent.getActivity(context, notificationId, intent, getPendingIntentFlag())
		val builder: NotificationCompat.Builder = createNotification(context, study.title, context.getString(R.string.info_new_message), pendingIntent, CHANNEL_ID_STUDY_MESSAGES)
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
			NotificationManagerCompat.from(context).notify(notificationId, builder.build())
		}
		else {
			ErrorBox.warn("Notifications", "User has revoked permissions for notifications")
		}
	}
	
	override fun removeQuestionnaireBing(questionnaire: Questionnaire) {
		val notificationId = createId(questionnaire.id, QUESTIONNAIRE_BING_ID_RANGE)
		remove(notificationId)
		val context = context.get() ?: return
		WorkerBox.cancelNotificationTimeout(context, questionnaire.id)
	}
	
	override fun remove(id: Int) {
		val context = context.get() ?: return
		NotificationManagerCompat.from(context).cancel(id)
	}
	
	
	fun createScreenTrackingNotification(context: Context): Notification {
		return NotificationCompat.Builder(context, CHANNEL_ID_SCREEN_TRACKING)
			.setSmallIcon(R.drawable.ic_notification)
			.setContentTitle(context.getString(R.string.app_name))
			.setContentText(context.getString(R.string.info_screen_tracking_notification_content))
			.setAutoCancel(false)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.build()
	}
}