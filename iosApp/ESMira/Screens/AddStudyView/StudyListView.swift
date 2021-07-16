//
//  StudyListView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct StudyListView: View {
	@EnvironmentObject var appState: AppState
	var studyState: StudyState
	@State var loadingState: LoadingState = .hidden
	@State var openStudyList = false
	
	
	var body: some View {
		VStack {
			if(self.studyState.studiesList.count == 0) {
				if(self.studyState.accessKey.isEmpty) {
					Text("info_no_studies_noAccessKey").padding()
				}
				else {
					Text(String(format: NSLocalizedString("info_no_studies_withAccessKey", comment: ""), self.studyState.accessKey)).padding()
				}
			}
			else if(self.studyState.studiesList.count == 1) {
				StudyDetailView(studyState: self.studyState, study: self.studyState.studiesList[0])
			}
			else {
				List(self.studyState.studiesList, id: \.webId) { study in
					NavigationLink(destination: StudyDetailView(studyState: self.studyState, study: study)) {
						VStack(alignment: .leading) {
							Text(study.title).bold()
							Text(study.contactEmail).offset(x: 10)
						}
					}.isDetailLink(false)
				}
			}
		}
		.navigationBarTitle(Text("studies"), displayMode: .inline)
	}
}
