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
	@State var showAlert: Bool = false
	
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
				}.isDetailLink(false)
			}
		}
		.padding()
		.alert(isPresented: self.$showAlert) {
			Alert(
				title: Text("welcome_exit_questionTitle"),
				message: Text("welcome_exit_questionDesc"),
				primaryButton: .default(Text("ok_")) {
					self.mode.wrappedValue.dismiss()
				},
				secondaryButton: .cancel()
			)
		}
		.navigationBarBackButtonHidden(true)
		.navigationBarItems(leading: Button(action:{
			self.showAlert = true
		}) {
			Image(systemName: "xmark")
			Text("close")
		})
	}
}
