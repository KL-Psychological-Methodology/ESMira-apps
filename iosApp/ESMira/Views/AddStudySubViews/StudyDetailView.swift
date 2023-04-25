//
//  StudyDetailsView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct StudyDetailView: View {
	@EnvironmentObject private var appState: AppState
	@EnvironmentObject private var navigationState: NavigationState
	var study: Study
	
	@State private var openStudyJoined = false
	
	var body: some View {
		VStack(alignment: .leading) {
			NavigationLink(destination: StudyJoinedView(study: self.study), isActive: self.$openStudyJoined, label: { EmptyView() })
			Text(study.title).bold()
			Text(study.contactEmail).offset(x: 10)
			ScrollableHtmlTextView(html: self.study.studyDescription)
			Spacer()
			Divider()
			HStack {
				Spacer()
				if(self.study.needsPermissionScreen()) {
					NavigationLink("consent", destination: StudyPermissionsView(study: study))
				}
				else {
					Button("participate") {
						self.study.join()
						
						if(self.study.needsJoinedScreen()) {
							self.openStudyJoined = true
						}
						else {
							self.navigationState.switchStudy(self.study.id)
						}
					}
				}
			}
		}
		.padding()
		.navigationBarTitle(Text("add_a_study"), displayMode: .inline)
	}
}
