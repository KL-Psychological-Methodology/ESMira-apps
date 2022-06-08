//
//  ServerQuestion.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct AccessKeyQuesionView: View {
	var serverUrl: String
	@State var accessKey = ""
	@State var askAccessKey = false
	@State var gotoStudyLoader = false
	
	var body: some View {
		VStack {
			HStack {
				Image(systemName: "lock.fill").font(.system(size: 80))
				Text("questionMark").font(.system(size: 64))
			}
			Text("welcome_accessKey_question").padding(.vertical)
			Button("yes") {
				self.askAccessKey = true
			}
				.padding()
			NavigationLink(destination: StudyLoaderView(serverUrl: self.serverUrl, accessKey: self.accessKey), isActive: self.$gotoStudyLoader) {
				Text("welcome_join_public_study")
			}
				.padding()
			Spacer()
		}
			.padding()
			.textFieldAlert(isPresented: self.$askAccessKey, text: self.$accessKey, title: "colon_accessCode") {
				self.gotoStudyLoader = true
			}
	}
}
