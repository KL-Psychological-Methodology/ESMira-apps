package at.jodlidev.esmira.androidNative

import android.content.Context
import at.jodlidev.esmira.*
import at.jodlidev.esmira.sharedCode.DialogOpenerInterface
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
			Activity_errorReport.start(it)
		}
	}
	override fun updateNeeded() {
		context.get()?.let {
			Activity_dialog.updateMsg(it)
		}
	}
	override fun notificationsBroken() {
		context.get()?.let {
			Activity_notificationsBroken.start(it)
		}
	}
	override fun dialog(title: String, msg: String) {
		context.get()?.let {
			Activity_dialog.start(it, title, msg)
		}
	}
}