//
// Created by JodliDev on 13.08.20.
//

import Foundation
import sharedCode

class AppState: ObservableObject {
	enum DialogsScreens: Identifiable {
		case errorReport, changeSchedule, questionnaireSavedSuccessfully
		
		var id: Int {
			self.hashValue
		}
	}
	enum Screens: Hashable {
		case main
		case addStudy
	}
	
	@Published var updateLists: Bool = false
	@Published var startScreen: Screens = .main
	@Published var disableLandscape: Bool = false
	
	@Published var addStudyOpened = false
	@Published var questionnaireOpened = false
	@Published var messageOpened = false
	@Published var openScreen: DialogsScreens? = nil
	
	@Published var dialogShown = false
	@Published var toastShown = false
	
	@Published var title = ""
	@Published var msg = ""
	@Published var toastMsg = ""
	@Published var questionnaire: sharedCode.Questionnaire? = nil
	@Published var messageStudy: Study? = nil
	@Published var scheduleStudyId: Int64 = -1
	@Published var connectData: QrInterpreter.ConnectData? = nil
	
	func showDialog(title: String, msg: String) -> Bool {
		if(openScreen != nil) {
			print("Sheet is opened. Dialog wont work. Canceling")
			return false
		}
		self.title = title
		self.msg = msg
		self.dialogShown = true
		return true
	}
	func showToast(_ msg: String) {
		if(openScreen != nil) { //sheet and toast at the same time lead to weird bugs where ?the sheet content is refreshed / out of sync? (and toast is behind it anyway)
			print("Sheet is opened. Toast wont work. Canceling")
			return
		}
		self.toastMsg = msg
		self.toastShown = true
	}
	func showTranslatedToast(_ msg: String) {
		self.showToast(NSLocalizedString(msg, comment: ""))
	}
	
	func openChangeSchedule(_ studyId: Int64 = -1) {
		self.scheduleStudyId = studyId
		self.openScreen = .changeSchedule
	}
	func openQuestionnaire(_ questionnaireId: Int64) {
		self.questionnaire = DbLogic().getQuestionnaire(id: questionnaireId)
		self.questionnaireOpened = true
	}
	func openMessage(_ study: Study) {
		self.messageStudy = study
		self.messageOpened = true
	}
}
