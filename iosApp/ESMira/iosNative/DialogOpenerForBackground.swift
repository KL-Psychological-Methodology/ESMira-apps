//
// Created by JodliDev on 08.06.20.
//

import Foundation
import sharedCode
import SwiftUI

class DialogOpenerForBackground: DialogOpenerInterface {
	
	static let KEY_HAS_DIALOG = "has_dialog"
	static let KEY_DIALOG_TITLE = "key_title"
	static let KEY_DIALOG_MSG = "key_msg"
	
	func saveDialogForLater(_ title: String, _ msg: String) {
		ErrorBox.Companion().log(title: "DialogOpener", msg: "Saving dialog for later. title: \"\(title)\", msg: \"\(msg)\"")
		UserDefaults.standard.set(true, forKey: DialogOpener.KEY_HAS_DIALOG)
		UserDefaults.standard.set(title, forKey: DialogOpener.KEY_DIALOG_TITLE)
		UserDefaults.standard.set(msg, forKey: DialogOpener.KEY_DIALOG_MSG)
	}
	
	func dialog(title: String, msg: String, triggerNotification: Bool) {
		self.saveDialogForLater(title, msg)
		
		if(triggerNotification) {
			NativeLink().notifications.fire(title: title, msg: msg, id: Int32(Date().timeIntervalSince1970))
		}
	}
	
	func errorReport() {
		//App is not running. Error report will automatically opened next time it is started
	}
	
	func updateNeeded() {
		self.dialog(title: NSLocalizedString("error_app_update_needed_title", comment: ""), msg: NSLocalizedString("error_app_update_needed_msg", comment: ""), triggerNotification: true)
	}
	
	func faultyAccessKey(study: Study) {
		//App is not running. Dialog will automatically opened next time it is started
	}
	
	func notificationsBroken() {
		//TODO
	}
	
	func appTrackingRevoked() {
		//Not relevant on iOS
	}
}
