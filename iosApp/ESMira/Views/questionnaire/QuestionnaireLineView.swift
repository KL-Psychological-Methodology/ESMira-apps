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
	@Environment(\.colorScheme) private var colorScheme

	var body: some View {
		let dropShadowColor = colorScheme == .dark
			? Color(.sRGBLinear, white: 1, opacity: 0.35)
			: Color(.sRGBLinear, white: 0, opacity: 0.05)
		Button(action: {
			navigationState.openQuestionnaire(questionnaire)
		}) {
			VStack(alignment: .leading) {
				HStack {
					Image(systemName: "doc.text.fill")
						.esTextShadow()
					Text(questionnaire.title)
						.esTextShadow()
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
						Text(String(format: NSLocalizedString("colon_last_filled_out", comment: ""), NativeLink().formatDateTime(ms: questionnaire.metadata.lastCompleted)))
							.font(.caption)
							.esTextShadow()
					}
					.padding(.horizontal, 5)
					.padding(.vertical, 2)
				}
			}
		}
		.foregroundColor(Color("onSurface"))
		.background(Color("Surface"))
		.clipShape(RoundedRectangle(cornerRadius: 16))
		.shadow(color: dropShadowColor, radius: 5, y: 5)
	}
}
