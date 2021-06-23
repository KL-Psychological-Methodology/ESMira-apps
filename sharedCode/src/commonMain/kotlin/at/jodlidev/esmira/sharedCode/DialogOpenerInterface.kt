package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 18.05.2020.
 */
interface DialogOpenerInterface {
	fun errorReport()
	fun updateNeeded()
	fun notificationsBroken()
	fun dialog(title: String, msg: String)
}