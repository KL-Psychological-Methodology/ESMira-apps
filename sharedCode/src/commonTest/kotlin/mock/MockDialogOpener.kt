package mock

import at.jodlidev.esmira.sharedCode.DialogOpenerInterface

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockDialogOpener: DialogOpenerInterface {
	var errorReportCount = 0
	var updateNeededCount = 0
	var notificationsBrokenCount = 0
	var dialogCount = 0
	
	override fun errorReport() {
		++errorReportCount
	}
	
	override fun updateNeeded() {
		++updateNeededCount
	}
	
	override fun notificationsBroken() {
		++notificationsBrokenCount
	}
	
	override fun dialog(title: String, msg: String) {
		++dialogCount
	}
}