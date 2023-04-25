//
//  StudyJoinedView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct StudyJoinedView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState

	let study: Study
	var body: some View {
		VStack {
			ScrollableHtmlTextView(html: self.study.postInstallInstructions)
			Spacer()
			if(study.hasNotifications()) {
				Divider()
				Text("colon_next_expected_notification").bold().padding()
				NextNotificationsView(studyId: self.study.id)
			}
			Divider()
			HStack {
				Button(action: {
					self.navigationState.openChangeSchedules(self.study.id, true)
				}) {
					Text("schedules")
				}
				Spacer()
				Button(action: {
					self.navigationState.switchStudy(self.study.id)
				}) {
					Text("complete")
				}
			}
		}
		.padding()
		.navigationBarBackButtonHidden(true)
		.navigationBarTitle(Text("complete"), displayMode: .inline)
	}
}
