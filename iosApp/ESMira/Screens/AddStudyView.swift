//
// Created by JodliDev on 25.05.20.
//


import SwiftUI
import sharedCode


struct AddStudyView: View {
	
	@EnvironmentObject var appState: AppState
	
	@ObservedObject var studyState: StudyState
	
	let loadImmediately: Bool
	
	init(connectData: QrInterpreter.ConnectData?, studies: Binding<[Study]>) {
		self._studyState = ObservedObject(initialValue: StudyState(studies: studies))
		if(connectData == nil) {
			self.loadImmediately = false
		}
		else {
			self.loadImmediately = true
			self.studyState.serverUrl = connectData!.url
			self.studyState.accessKey = connectData!.accessKey
			self.studyState.studyWebId = connectData!.studyId
			self.studyState.qId = connectData!.qId
		}
	}
	
	var body: some View {
		Group {
			if(self.loadImmediately) {
				StudyLoader(studyState: self.studyState, type: .Immediately)
			}
			else {
				if(DbLogic().hasNoStudies()) {
					WelcomeView(studyState: self.studyState)
				}
				else {
					QrExistsView(studyState: self.studyState)
				}
			}
		}
		.onAppear { //@EnvironmentObject seems to fill its variable after init, so we improvise
			
			self.studyState.appState = self.appState
		}
	}
}






