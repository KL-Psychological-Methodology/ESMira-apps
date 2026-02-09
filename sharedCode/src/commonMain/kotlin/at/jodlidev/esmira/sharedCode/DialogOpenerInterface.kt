package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.Study

/**
 * Created by JodliDev on 18.05.2020.
 */
interface DialogOpenerInterface {
	fun errorReport()
	fun updateNeeded()
	fun notificationsBroken()
	fun faultyAccessKey(study: Study)
	fun appTrackingRevoked()
	fun dialog(title: String, msg: String, triggerNotification: Boolean = true)
}