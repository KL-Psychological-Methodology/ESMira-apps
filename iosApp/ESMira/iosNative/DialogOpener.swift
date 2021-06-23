//
// Created by JodliDev on 08.06.20.
//

import Foundation
import sharedCode
import SwiftUI

class DialogOpener: DialogOpenerInterface {
	static let KEY_HAS_DIALOG = "has_dialog"
	static let KEY_DIALOG_TITLE = "key_title"
	static let KEY_DIALOG_MSG = "key_msg"

	var appState: AppState?
	
	init(appState: AppState?) {
		self.appState = appState
	}
	func saveDialogForLater(_ title: String, _ msg: String) {
		ErrorBox.Companion().log(title: "DialogOpener", msg: "Saving dialog for later. title: \"\(title)\", msg: \"\(msg)\"")
		UserDefaults.standard.set(true, forKey: DialogOpener.KEY_HAS_DIALOG)
		UserDefaults.standard.set(title, forKey: DialogOpener.KEY_DIALOG_TITLE)
		UserDefaults.standard.set(msg, forKey: DialogOpener.KEY_DIALOG_MSG)
	}
	
	func dialog(title: String, msg: String) {
		if(self.appState == nil) {
			self.saveDialogForLater(title, msg)
		}
		else {
			self.openGuiDialog(title, msg)
		}
		
		NativeLink().notifications.fire(title: title, msg: msg, id: Int32(Date().timeIntervalSince1970))
	}
	func openGuiDialog(_ title: String, _ msg: String) {
		DispatchQueue.main.async { //in case function was started from the background while app was in foreground
			if (!self.appState!.showDialog(title: title, msg: msg)) {
				self.saveDialogForLater(title, msg)
			}
		}
	}
	
	func errorReport() {
		if(self.appState != nil) {
			DispatchQueue.main.async { //in case function was started from the background while app was in foreground
				self.appState!.openScreen = .errorReport
			}
		}
	}
	
	func updateNeeded() {
		self.dialog(title: NSLocalizedString("error_app_update_needed_title", comment: ""), msg: NSLocalizedString("error_app_update_needed_msg", comment: ""))
	}
	
	func openQuestionnaire(questionnaireId: Int64) {
		if(self.appState != nil) {
			DispatchQueue.main.async { //in case function was started from the background while app was in foreground
				self.appState!.openQuestionnaire(questionnaireId)
			}
		}
	}
	
	func openMessage(study: Study) {
		if(self.appState != nil) {
			DispatchQueue.main.async { //in case function was started from the background while app was in foreground
				self.appState!.openMessage(study)
			}
		}
	}
	
	func openChangeSchedule(_ studyId: Int64 = -1) {
		if(self.appState != nil) {
			DispatchQueue.main.async { //in case function was started from the background while app was in foreground
				self.appState!.openChangeSchedule(studyId)
			}
		}
	}
	
	func notificationsBroken() {
		//TODO
		
//		switch(UIApplication.shared.backgroundRefreshStatus) {
//			case .available:
//				break
//			case .denied, .restricted:
//				self.dialogTitle = Text("dialogTitle_ios_backgroundRefresh_disabled")
//				self.dialogMsg = Text("dialogDesc_ios_backgroundRefresh_disabled")
//				self.showDialog = true
//				return
//			default:
//				break
//		}



//		Notifications.authorize { success in
//			if(success) {
//				self.participate()
//			}
//			else {
//				self.dialogTitle = Text("dialogTitle_ios_notifications_disabled")
//				self.dialogMsg = Text("dialogDesc_ios_notifications_disabled")
//				self.showDialog = true
//			}
//		}

//		center.getNotificationSettings { settings in
//			if(settings.authorizationStatus == .authorized) {
//				UNUserNotificationCenter.current().add(request, withCompletionHandler: completionHandler)
//			}
//			else {
//				ErrorBox.Companion().warn(title: "Notifications", msg: "Notifications are disabled by the user!")
//				//TODO: how do we deal with that..?
//			}
//		}
	}
}
