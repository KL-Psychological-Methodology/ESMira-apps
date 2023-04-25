//
//  WelcomeView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode


struct WelcomeView: View {
	@Environment(\.presentationMode) var mode: Binding<PresentationMode>
	@EnvironmentObject var addStudyState: AddStudyState
	
	var body: some View {
		VStack {
			HStack {
				Image("roundAppIcon")
				Text("welcome_hello").font(.system(size: 52))
			}
			Text("welcome_first_instructions").padding(.top)
			Spacer()
			Divider()
			HStack {
				Spacer()
				NavigationLink(destination: QrExistsView()) {
					Text("continue_")
					Image(systemName: "chevron.compact.right")
				}
			}
		}
		.padding()
		.navigationBarTitle(Text(""), displayMode: .inline)
	}
}
