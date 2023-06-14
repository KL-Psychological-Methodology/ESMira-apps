//
//  QrExistsView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct QrExistsView: View {
	var body: some View {
		VStack {
			HStack {
				Image("QrCode")
				Text("questionMark").font(.system(size: 64))
			}
			Text("welcome_qr_question").padding(.vertical)
			HStack {
				NavigationLink("no", destination: ServerQuestionView())
					.defaultDesign()
				Spacer()
				NavigationLink("yes", destination: QrStartView())
					.defaultDesign()
			}
			Spacer()
		}
		.padding()
	}
}
