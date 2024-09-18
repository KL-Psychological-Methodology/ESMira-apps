//
// Created by JodliDev on 09.06.20.
//


import SwiftUI
import sharedCode
import Foundation

struct InactiveQuestionnairesView: View {
	@EnvironmentObject var navigationState: NavigationState
	
	let questionnaires: [Questionnaire]
	
	var body: some View {
		if self.questionnaires.count == 0 {
			Text("info_no_questionnaires").padding(20)
		}
		List(self.questionnaires, id: \.id) { questionnaire in
			if questionnaire.showInDisabledList {
				QuestionnaireLineView(questionnaire: questionnaire)
			}
		}
	}
}
