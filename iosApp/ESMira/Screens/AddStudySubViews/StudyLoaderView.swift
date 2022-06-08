//
//  ServerQuestion.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct StudyLoaderView: View {
	
	@State private var loadingMessage = ""
	@State private var loadingState: LoadingState = .hidden
	
	private var serverUrl: String
	private var accessKey: String
	private var studyWebId: Int64
	private var qId: Int64
	@State var web: Web? = nil
	@State private var studiesList: [Study] = []
	@State private var openStudyList: Bool = false
	
	
	init(serverUrl: String, accessKey: String, studyWebId: Int64 = 0, qId: Int64 = 0) {
		self.serverUrl = serverUrl
		self.accessKey = accessKey
		self.studyWebId = studyWebId
		self.qId = qId
	}
	
	func onLoaderShowing() {
		self.web = Web.Companion().loadStudies(
			serverUrl: self.serverUrl,
			accessKey: self.accessKey,
			onError: { msg, e in
				self.loadingMessage = msg
				self.loadingState = .error
			},
			onSuccess: { studyString, urlFormatted in
				self.studiesList = Study.Companion().getFilteredStudyList(
					json: studyString,
					url: urlFormatted,
					accessKey: self.accessKey,
					studyWebId: self.studyWebId,
					qId: self.qId
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
						NavigationLink(destination: StudyDetailView(study: study)) {
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
		else {
			VStack {
				NavigationLink(
					destination: StudyListView(serverUrl: self.serverUrl, accessKey: self.accessKey, studiesList: self.studiesList),
					isActive: self.$openStudyList
				) { EmptyView() }.isDetailLink(false)
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
