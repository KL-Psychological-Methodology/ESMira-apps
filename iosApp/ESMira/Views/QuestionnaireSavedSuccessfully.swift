//
// Created by JodliDev on 21.09.22.
//

import Foundation
import SwiftUI

struct QuestionnaireSavedSuccessfully: View {
	@EnvironmentObject var navigationState: NavigationState
	
	var body: some View {
		VStack {
			Spacer()
			Text("info_questionnaire_success")
			Spacer()
			DefaultButton("ok_") {
				self.navigationState.questionnaireSuccessfullOpened = false
			}
		}
		.navigationBarTitle(Text(""), displayMode: .inline)
		.padding()
	}
}
