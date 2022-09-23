//
// Created by JodliDev on 21.09.22.
//

import Foundation
import SwiftUI

struct QuestionnaireSavedSuccessfully: View {
	@EnvironmentObject var appState: AppState
	
	
	var body: some View {
		NavigationView {
			VStack {
				Text("info_questionnaire_success")
				Spacer()
				Button(action: {
					self.appState.openScreen = nil
				}) {
					Text("ok_")
				}
			}
			.padding()
		}
	}
}
