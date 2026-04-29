package at.jodlidev.esmira

import android.app.Application
import android.content.Context
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.androidNative.*
import at.jodlidev.esmira.sharedCode.SQLite


/**
 * Created by JodliDev on 24.04.2019.
 */
class Application : Application() {
	override fun onCreate() {
		NativeLink.init(SQLite(applicationContext), SmartphoneData, DialogOpener, Notifications, PostponedActions)
		CrashExceptionHandler.init(applicationContext)
		DialogOpener.init(applicationContext)
		Notifications.init(applicationContext)
		PostponedActions.init(applicationContext)
		
		super.onCreate()
	}
}