//
// Created by JodliDev on 05.04.22.
//

import Foundation
import sharedCode

class NavigationState: ObservableObject {
	enum DialogsScreens: Identifiable {
		case errorReport, changeSchedule
		
		var id: Int {
			self.hashValue
		}
	}
	@Published var study: Study? = nil
	@Published var questionnaireId: Int64 = -1
	@Published var resetSchedules: Bool = false
	@Published var addStudyConnectData: QrInterpreter.ConnectData? = nil
	
	@Published var questionnaireOpened = false
	@Published var questionnaireSuccessfullOpened = false
	@Published var messagesOpened = false
	@Published var addStudyOpened = false
	@Published var inactiveQuestionnaireOpened = false
	@Published var aboutESMiraOpened = false
	@Published var changeSchedulesOpened: Bool = false { didSet { self.dialogOpened = self.changeSchedulesOpened ? .changeSchedule : nil } }
	
	@Published var dialogOpened: DialogsScreens? = nil
	
	func switchStudy(_ studyId: Int64) {
		DbUser().setCurrentStudyId(studyId: studyId)
		self.study = DbLogic().getStudy(id: studyId)
		self.addStudyOpened = false
	}
	func reloadStudy() {
		if(self.study != nil) {
			self.study = DbLogic().getStudy(id: self.study!.id)
		}
	}
	
	func openMessages(_ studyId: Int64) {
		self.switchStudy(studyId)
		self.messagesOpened = true
	}
	func openAddStudy(_ connectData: QrInterpreter.ConnectData? = nil) {
		self.addStudyConnectData = connectData
		self.addStudyOpened = true
	}
	func openQuestionnaire(_ questionnaire: Questionnaire) {
		switchStudy(questionnaire.studyId)
		self.questionnaireId = questionnaire.id
		self.questionnaireOpened = true
	}
	func openChangeSchedules(_ studyId: Int64, _ resetSchedules: Bool = false) {
		switchStudy(studyId)
		self.dialogOpened = .changeSchedule
		self.resetSchedules = resetSchedules
	}
	func openErrorReport() {
		self.dialogOpened = .errorReport
	}
	func closeScreenDialog() {
		self.dialogOpened = nil
	}
}
