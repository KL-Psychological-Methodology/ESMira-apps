//
// Created by JodliDev on 25.05.20.
//


import SwiftUI
import sharedCode


struct AddStudyView: View {
	
	@EnvironmentObject var appState: AppState
	
	let connectData: QrInterpreter.ConnectData?
	
	var body: some View {
		Group {
			if(self.connectData != nil) {
				StudyLoaderView(serverUrl: self.connectData!.url, accessKey: self.connectData!.accessKey, studyWebId: self.connectData!.studyId, qId: self.connectData!.qId)
			}
			else {
				if(DbLogic().hasNoStudies()) {
					WelcomeView()
				}
				else {
					QrExistsView()
				}
			}
		}
	}
}






