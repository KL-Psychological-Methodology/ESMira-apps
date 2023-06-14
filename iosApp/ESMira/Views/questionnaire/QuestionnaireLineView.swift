//
//  QuestionnaireLine.swift
//  ESMira
//
//  Created by JodliDev on 04.04.23.
//

import SwiftUI
import sharedCode
import Foundation

struct QuestionnaireLineView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
	let questionnaire: Questionnaire
	
	@State private var questionnaireIsOpened = false
	
	var body: some View {
		Button(action: {
			navigationState.openQuestionnaire(questionnaire)
		}) {
			VStack(alignment: .leading) {
				HStack {
					Image(systemName: "doc.text.fill")
					Text(questionnaire.title)
					Spacer()
					if(questionnaire.showJustFinishedBadge()) {
						ZStack {
							Capsule()
								.foregroundColor(Color("Accent"))
							
							Text("just_finished")
								.foregroundColor(.white)
								.font(Font.system(size: 12))
								.padding(2)
						}
						.aspectRatio(contentMode: .fit)
						.fixedSize(horizontal: true, vertical: false)
						.frame(height: 20)
					}
				}
					.padding(10)
				
				if(questionnaire.showLastCompleted()) {
					HStack {
						Spacer()
						Text(String(format: NSLocalizedString("colon_last_filled_out", comment: ""), NativeLink().formatDateTime(ms: questionnaire.lastCompleted)))
							.font(.caption)
					}
					.padding(.horizontal, 5)
					.padding(.vertical, 2)
				}
			}
		}
		.foregroundColor(Color("PrimaryDark"))
		.background(Color("Surface"))
	}
}
