//
//  ServerQuestion.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct StudyLoaderView: View {
	@ObservedObject var addStudyState: AddStudyState
	
	@State private var loadingMessage = ""
	@State private var loadingState: LoadingState = .hidden
	@State private var loadingStarted = false
	
	@State var web: Web? = nil
	@State private var studiesList: Study.StudyList = Study.StudyList(filteredStudies: [], joinedStudies: [])
	@State private var openStudyList: Bool = false
	
	
	func onLoaderShowing() {
		if(loadingStarted) {
			return
		}
		loadingStarted = true
		self.web = Web.Companion().loadStudies(
			serverUrl: self.addStudyState.serverUrl,
			accessKey: self.addStudyState.accessKey,
			fallbackUrl: self.addStudyState.fallbackUrl,
			onError: { msg, e in
				self.loadingMessage = msg
				self.loadingState = .error
			},
			onSuccess: { studyString, urlFormatted in
				self.studiesList = Study.Companion().getFilteredStudyList(
					json: studyString,
					url: urlFormatted,
					accessKey: self.addStudyState.accessKey,
					studyWebId: self.addStudyState.studyWebId,
					qId: self.addStudyState.qId
				)
				self.loadingState = .hidden
				self.openStudyList = true
			}
		)
	}
	func onLoaderCancel() {
		if(!self.openStudyList) {
			self.web?.cancel()
		}
	}
	
	
	var body: some View {
		if(self.openStudyList) {
			VStack {
				if(studiesList.filteredStudies.count == 0) {
					if(self.addStudyState.accessKey.isEmpty) {
						Text("info_no_studies_noAccessKey").padding()
					}
					else {
						Text(String(format: NSLocalizedString("ios_info_no_studies_withAccessKey", comment: ""), self.addStudyState.accessKey)).padding()
					}
				}
				else if(self.studiesList.filteredStudies.count == 1) {
					StudyDetailView(study: self.studiesList.filteredStudies[0])
				}
				else {
					List(self.studiesList.filteredStudies, id: \.webId) { study in
						NavigationLink(destination: StudyDetailView(study: study)) {
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
		else {
			VStack {
				LoadingSpinner(isAnimating: .constant(true), style: .large)
			}
				.onAppear {
					self.onLoaderShowing()
				}
				.onDisappear() {
				   self.onLoaderCancel()
				}
		}
	}
}
