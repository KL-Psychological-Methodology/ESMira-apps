package at.jodlidev.esmira.androidNative

import android.content.Context
import at.jodlidev.esmira.activities.AppTrackingRevokedDialogActivity
import at.jodlidev.esmira.activities.SimpleDialogActivity
import at.jodlidev.esmira.activities.ErrorReportDialogActivity
import at.jodlidev.esmira.activities.FaultyAccessKeyDialogActivity
import at.jodlidev.esmira.activities.NotificationsBrokenDialogActivity
import at.jodlidev.esmira.sharedCode.DialogOpenerInterface
import at.jodlidev.esmira.sharedCode.data_structure.Study
import java.lang.ref.WeakReference

/**
 * Created by JodliDev on 18.05.2020.
 */
object DialogOpener : DialogOpenerInterface {
	lateinit var context: WeakReference<Context>
	fun init(context: Context) {
		this.context = WeakReference(context.applicationContext)
	}
	override fun errorReport() {
		context.get()?.let {
			ErrorReportDialogActivity.start(it)
		}
	}
	override fun updateNeeded() {
		context.get()?.let {
			SimpleDialogActivity.updateMsg(it)
		}
	}
	override fun notificationsBroken() {
		context.get()?.let {
			NotificationsBrokenDialogActivity.start(it)
		}
	}
	
	override fun faultyAccessKey(study: Study) {
		context.get()?.let {
			FaultyAccessKeyDialogActivity.start(it, study)
		}
	}

	override fun appTrackingRevoked() {
		context.get()?.let {
			AppTrackingRevokedDialogActivity.start(it)
		}
	}

	
	override fun dialog(title: String, msg: String) {
		context.get()?.let {
			SimpleDialogActivity.start(it, title, msg)
		}
	}
}