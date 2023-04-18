//
// Created by JodliDev on 13.08.20.
//

import Foundation
import sharedCode

class AppState: ObservableObject {
	@Published var disableLandscape: Bool = false
	
	@Published var dialogShown = false
	@Published var toastShown = false
	
	@Published var title = ""
	@Published var msg = ""
	@Published var toastMsg = ""
	
	func showDialog(title: String, msg: String) {
		self.title = title
		self.msg = msg
		self.dialogShown = true
	}
	func showToast(_ msg: String) {
		self.toastMsg = msg
		self.toastShown = true
	}
	func showTranslatedToast(_ msg: String) {
		self.showToast(NSLocalizedString(msg, comment: ""))
	}
}
