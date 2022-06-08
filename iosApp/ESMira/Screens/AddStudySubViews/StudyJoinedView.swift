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
					self.appState.openChangeSchedule(self.study.id)
				}) {
					Text("schedules")
				}
				Spacer()
				Button(action: {
					appState.updateLists.toggle()
					self.appState.addStudyOpened = false
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
