//
//  ServerQuestion.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct ServerQuestionView: View {
	@State var serverUrl = ""
	@State var manualUrl = ""
	@State var askManualUrl = false
	let serverList = Web.Companion().serverList
	
	var body: some View {
		VStack {
			HStack {
				Image(systemName: "globe").font(.system(size: 80))
				Text("questionMark").font(.system(size: 64))
			}
			Text("welcome_server_question").padding(.vertical)
			ScrollView {
				VStack(alignment: .leading) {
					ForEach(self.serverList, id: \.self) { (pair: KotlinPair) in
						HStack {
							RadioButtonView(state: self.$serverUrl, value: pair.second! as String, labelEl: VStack(alignment: .leading) {
								Text(pair.first! as String).bold()
								Text(pair.second! as String).padding(.leading).font(.system(size: 14)).opacity(0.8)
							})
						}
					}
					HStack {
						RadioButtonView(state: self.$serverUrl, value: self.manualUrl, labelEl: VStack(alignment: .leading) {
							Text("enter_manually").bold()
							Text(self.manualUrl).padding(.leading).font(.system(size: 14)).opacity(0.8)
						}) { _ in
							self.askManualUrl = true
						}
					}
				}
			}.foregroundColor(.primary)
			Spacer()
			
			Divider()
			HStack {
				Spacer()
				NavigationLink(destination: AccessKeyQuesionView(serverUrl: self.serverUrl)) {
					Text("continue_")
					Image(systemName: "chevron.compact.right")
				}.isDetailLink(false)
			}
		}
		.padding()
		.onAppear {
			self.serverUrl = self.serverList[0].second! as String
		}
		.textFieldAlert(isPresented: self.$askManualUrl, text: self.$manualUrl, title: "colon_enter_manually") {
			self.serverUrl = self.manualUrl
		}
	}
}
