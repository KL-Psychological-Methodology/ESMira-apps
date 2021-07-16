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
	var studyState: StudyState
	
	public let study: Study
	@State private var openStudyJoined = false
	
	init(studyState: StudyState, study: Study) {
		self.studyState = studyState
		self.study = study
	}
	
	var body: some View {
		VStack(alignment: .leading) {
			NavigationLink(destination: StudyJoinedView(study: self.study), isActive: self.$openStudyJoined, label: { EmptyView() }).isDetailLink(false)
			Text(study.title).bold()
			Text(study.contactEmail).offset(x: 10)
			ScrollableHtmlTextView(html: self.study.studyDescription)
			Spacer()
			Divider()
			HStack {
				Spacer()
				if(self.study.needsPermissionScreen()) {
					NavigationLink("consent", destination: StudyPermissions(studyState: self.studyState, study: study)).isDetailLink(false)
				}
				else {
					Button("participate") {
						self.study.join()
						
						if(self.study.needsJoinedScreen()) {
							self.openStudyJoined = true
						}
						else {
							self.appState.addStudyOpened = false
						}
					}
				}
			}
		}
		.padding()
		.navigationBarTitle(Text("add_a_study"), displayMode: .inline)
	}
}
