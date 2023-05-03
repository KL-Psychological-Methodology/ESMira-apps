//
// Created by JodliDev on 21.09.22.
//

import Foundation
import SwiftUI

struct QuestionnaireSavedSuccessfully: View {
	@EnvironmentObject var navigationState: NavigationState
	
	var body: some View {
		VStack {
			Text("info_questionnaire_success")
			Spacer()
			Button(action: {
				self.navigationState.questionnaireSuccessfullOpened = false
			}) {
				Text("ok_")
			}
		}
		.padding()
	}
}
