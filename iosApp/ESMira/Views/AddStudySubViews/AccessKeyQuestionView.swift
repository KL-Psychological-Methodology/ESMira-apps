//
//  ServerQuestion.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct AccessKeyQuesionView: View {
	@ObservedObject var addStudyState: AddStudyState
	
	@State var askAccessKey = false
	@State var gotoStudyLoader = false
	
	var body: some View {
		VStack {
			NavigationLink(destination: StudyLoaderView(addStudyState: self.addStudyState), isActive: self.$gotoStudyLoader, label: { EmptyView() })
			HStack {
				Image(systemName: "lock.fill").font(.system(size: 80))
				Text("questionMark").font(.system(size: 64))
			}
			Text("welcome_accessKey_question").padding(.vertical)
			Button("yes") {
				self.askAccessKey = true
			}
				.padding()
			
			Button("welcome_join_public_study") {
				self.addStudyState.accessKey = ""
				self.gotoStudyLoader = true
			}
				.padding()
			Spacer()
		}
			.padding()
			.textFieldAlert(isPresented: self.$askAccessKey, text: self.$addStudyState.accessKey, title: "colon_accessCode") {
				self.gotoStudyLoader = true
			}
	}
}
