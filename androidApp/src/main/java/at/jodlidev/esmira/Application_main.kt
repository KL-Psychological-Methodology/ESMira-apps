package at.jodlidev.esmira

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.androidNative.*


/**
 * Created by JodliDev on 24.04.2019.
 */
class Application_main : Application() {
	override fun onCreate() {
//		NativeLink.init(SQLite(applicationContext), SmartphoneData, DialogOpener, Notifications, PostponedActions, FileOpener)
		NativeLink.init(SQLite(applicationContext), SmartphoneData, DialogOpener, Notifications, PostponedActions)
		CrashExceptionHandler.init(applicationContext)
		DialogOpener.init(applicationContext)
		Notifications.init(applicationContext)
		PostponedActions.init(applicationContext)
		
		super.onCreate()
	}
	
	// Fix for Multidex combined with WorkerManager in Android API < 21
	// See: https://stackoverflow.com/questions/58595909/androidx-work-impl-workmanagerinitializer-java-lang-classnotfoundexception-an
	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)
		MultiDex.install(this)
	}
}