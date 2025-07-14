package mock

import at.jodlidev.esmira.sharedCode.DialogOpenerInterface
import at.jodlidev.esmira.sharedCode.data_structure.Study

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockDialogOpener: DialogOpenerInterface {
	var errorReportCount = 0
	var updateNeededCount = 0
	var notificationsBrokenCount = 0
	var dialogCount = 0
	var faultyAccessKeyCount = 0
	var appTrackingRevokedCount = 0

	override fun errorReport() {
		++errorReportCount
	}
	
	override fun updateNeeded() {
		++updateNeededCount
	}
	
	override fun notificationsBroken() {
		++notificationsBrokenCount
	}

	override fun faultyAccessKey(study: Study) {
		++faultyAccessKeyCount
	}

	override fun appTrackingRevoked() {
		++appTrackingRevokedCount
	}

	override fun dialog(title: String, msg: String) {
		++dialogCount
	}
	
	fun reset() {
		errorReportCount = 0
		updateNeededCount = 0
		notificationsBrokenCount = 0
		dialogCount = 0
		faultyAccessKeyCount = 0
		appTrackingRevokedCount = 0
	}
}