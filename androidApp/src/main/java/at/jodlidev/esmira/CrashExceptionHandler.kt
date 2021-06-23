package at.jodlidev.esmira

import android.content.Context
import android.util.Log
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox

/**
 * Created by JodliDev on 24.04.2019.
 */
class CrashExceptionHandler private constructor(context: Context) : Thread.UncaughtExceptionHandler {
	private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
	override fun uncaughtException(t: Thread, e: Throwable) {
		try {
			ErrorBox.error("Crash", "App crashed", e)
		}
		catch(ee: Exception) {
			Log.e("CrashExceptionHandler", "CrashExceptionHandler crashed while crashing. Winter is coming!")
			e.printStackTrace()
			ee.printStackTrace()
		}
		defaultHandler?.uncaughtException(t, e)
	}
	
	companion object {
		fun init(context: Context) {
			if(Thread.getDefaultUncaughtExceptionHandler() !is CrashExceptionHandler) {
				Thread.setDefaultUncaughtExceptionHandler(CrashExceptionHandler(context))
			}
		}
	}
	
}