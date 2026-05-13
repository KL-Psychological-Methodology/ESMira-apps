//
//  StudyListView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct StudyListView: View {
	var serverUrl: String
	var accessKey: String
	var studiesList: [Study]
	
	
	var body: some View {
		VStack {
			if(studiesList.count == 0) {
				if(self.accessKey.isEmpty) {
					Text("info_no_studies_noAccessKey").padding()
				}
				else {
					Text(String(format: NSLocalizedString("ios_info_no_studies_withAccessKey", comment: ""), self.accessKey)).padding()
				}
			}
			else if(self.studiesList.count == 1) {
				StudyDetailView(study: self.studiesList[0])
			}
			else {
				List(self.studiesList, id: \.webId) { study in
					NavigationLink(destination: study.hasMultipleLanguages() ? AnyView(LangQuestionView(study: study)) : AnyView(StudyDetailView(study: study))) {
						VStack(alignment: .leading) {
							Text(study.title).bold()
							Text(study.contactEmail).offset(x: 10)
						}
					}
				}
			}
		}
			.navigationBarTitle(Text("studies"), displayMode: .inline)
		
	}
}
