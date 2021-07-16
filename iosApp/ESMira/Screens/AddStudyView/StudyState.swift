//
//  StudyState.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

class StudyState: ObservableObject {
	var appState: AppState? = nil
	
	@Published var serverUrl: String = ""
	@Published var accessKey: String = ""
	@Published var studyWebId: Int64 = 0
	@Published var qId: Int64 = 0
	
	var web: Web? = nil
	@Published var studiesList: [Study] = []
	@Binding var studies: [Study]
	
	init(studies: Binding<[Study]>) {
		self._studies = studies
	}
	func updateStudyList() {
		self.studies = DbLogic().getJoinedStudies()
	}
}
