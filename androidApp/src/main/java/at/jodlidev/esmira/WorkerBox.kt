package at.jodlidev.esmira

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.work.*
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.Scheduler
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import java.util.concurrent.TimeUnit

/**
 * Created by JodliDev on 15.04.2019.
 */
class WorkerBox constructor(context: Context, params: WorkerParameters) : Worker(context, params) {
	@WorkerThread
	override fun doWork(): Result {
		val data: Data = inputData

		when(data.getInt(DATA_TYPE, -1)) {
			TYPE_SYNC -> {
				if(!Web.syncDataSetsBlocking())
					return Result.failure()
			}
			TYPE_UPDATE_STUDIES -> {
				ErrorBox.log("WorkerBox", "Background update check started...")
				val updatedCount: Int = Web.updateStudiesBlocking()
				if(updatedCount == -1)
					return Result.failure()
				
				Scheduler.checkMissedAlarms()
			}
			TYPE_REMOVE_NOTIFICATION -> {
				val questionnaire: Questionnaire? = DbLogic.getQuestionnaire(data.getLong(DATA_QUESTIONNAIRE_ID, 0))
				if(questionnaire != null) {
					val delay = data.getLong(DATA_DELAY, 0)
					NativeLink.notifications.remove(data.getInt(DATA_ID, 0))
					DbLogic.reportMissedInvitation(questionnaire, NativeLink.getNowMillis() -  delay)
				}
				else
					ErrorBox.error("Removing notification", "Could not remove notification because questionnaire is null!")
			}
			else -> {
				ErrorBox.error("Sync", "Unknown Worker-request. Type: ${data.getInt(DATA_TYPE, -1)}")
				return Result.failure()
			}
		}
		return Result.success()
	}
	
	companion object {
		const val TAG_SYNC: String = "sync"
		const val TAG_UPDATE_STUDIES: String = "update_studies"
		const val TAG_REMOVE_NOTIFICATION: String = "remove_notification"
		const val TYPE_SYNC: Int = 1
		const val TYPE_UPDATE_STUDIES: Int = 4
		const val TYPE_REMOVE_NOTIFICATION: Int = 5
		const val DATA_TYPE: String = "type"
		const val DATA_ID: String = "id"
		const val DATA_DELAY: String = "delay"
		const val DATA_QUESTIONNAIRE_ID: String = "questionnaire_id"

		fun sync(context: Context, waitForNetwork: Boolean) {
			val data: Data.Builder = Data.Builder()
			data.putInt(DATA_TYPE, TYPE_SYNC)
			val work: OneTimeWorkRequest.Builder = OneTimeWorkRequest.Builder(WorkerBox::class.java)
					.setBackoffCriteria(BackoffPolicy.LINEAR, 3, TimeUnit.HOURS)
					.addTag(TAG_SYNC)
					.setInputData(data.build())
			if(waitForNetwork) {
				val constraints: Constraints.Builder = Constraints.Builder()
				constraints.setRequiredNetworkType(NetworkType.CONNECTED)
				work.setConstraints(constraints.build())
			}
			WorkManager.getInstance((context)).beginUniqueWork(TAG_SYNC, ExistingWorkPolicy.REPLACE, work.build()).enqueue()
		}
		
		fun updateStudiesRegularly(context: Context) { //when there are no studies left at the time of execution, updates will stop for good; TODO: not tested
			val data: Data.Builder = Data.Builder()
			data.putInt(DATA_TYPE, TYPE_UPDATE_STUDIES)
			val constraints: Constraints.Builder = Constraints.Builder()
			constraints.setRequiredNetworkType(NetworkType.CONNECTED)
			val work: PeriodicWorkRequest.Builder = PeriodicWorkRequest.Builder(WorkerBox::class.java, 12, TimeUnit.HOURS)
					.setBackoffCriteria(BackoffPolicy.LINEAR, 3, TimeUnit.HOURS)
					.addTag(TAG_UPDATE_STUDIES)
					.setInitialDelay(1, TimeUnit.HOURS)
					.setConstraints(constraints.build())
					.setInputData(data.build())
			WorkManager.getInstance((context)).enqueueUniquePeriodicWork(TAG_UPDATE_STUDIES, ExistingPeriodicWorkPolicy.REPLACE, work.build())
		}
		
		fun updateStudiesOnce(context: Context) {
			val data: Data.Builder = Data.Builder()
			data.putInt(DATA_TYPE, TYPE_UPDATE_STUDIES)
			val work: OneTimeWorkRequest.Builder = OneTimeWorkRequest.Builder(WorkerBox::class.java)
					.setBackoffCriteria(BackoffPolicy.LINEAR, 3, TimeUnit.HOURS)
					.addTag(TAG_UPDATE_STUDIES)
					.setInputData(data.build())
			WorkManager.getInstance(context).beginUniqueWork(TAG_UPDATE_STUDIES, ExistingWorkPolicy.REPLACE, work.build()).enqueue()
		}
		
		fun timeoutNotification(context: Context, id: Int, questionnaireId: Long, timeoutMin: Int) { //needed FOR api < 26 where Notification.Builder.setTimeoutAfter() does not work
			val data: Data.Builder = Data.Builder()
			data.putInt(DATA_TYPE, TYPE_REMOVE_NOTIFICATION)
			data.putInt(DATA_ID, id)
			data.putLong(DATA_DELAY, timeoutMin.toLong() *60*1000)
			data.putLong(DATA_QUESTIONNAIRE_ID, questionnaireId)
			val work: OneTimeWorkRequest.Builder = OneTimeWorkRequest.Builder(WorkerBox::class.java)
					.setInitialDelay(timeoutMin.toLong(), TimeUnit.MINUTES)
					.addTag(TAG_REMOVE_NOTIFICATION + id)
					.setInputData(data.build())

			ErrorBox.log("WorkerBox", "Timeout for notification in $timeoutMin min.")
			WorkManager.getInstance(context).beginUniqueWork(TAG_REMOVE_NOTIFICATION + questionnaireId, ExistingWorkPolicy.REPLACE, work.build()).enqueue()
		}
		
		fun cancelNotificationTimeout(context: Context, questionnaireId: Long) {
			cancelAllWork(context, TAG_REMOVE_NOTIFICATION + questionnaireId)
		}

		fun cancelAllWork(context: Context, tag: String) {
			WorkManager.getInstance(context).cancelUniqueWork(tag)
		}
	}
}