//
//  QrExistsView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct QrExistsView: View {
	var studyState: StudyState
	
	var body: some View {
		VStack {
			HStack {
				Image("QrCode")
				Text("questionMark").font(.system(size: 64))
			}
			Text("welcome_qr_question").padding(.vertical)
			HStack {
				NavigationLink("no", destination: ServerQuestion(studyState: self.studyState).environmentObject(self.studyState))
					.isDetailLink(false)
					.padding()
				Spacer()
				NavigationLink("yes", destination: StudyLoader(studyState: self.studyState, type: .QrScanning))
					.isDetailLink(false)
					.padding()
			}
			Spacer()
		}
		.padding()
	}
}
