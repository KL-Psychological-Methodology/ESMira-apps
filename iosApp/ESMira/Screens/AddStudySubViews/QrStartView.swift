//
//  QrExistsView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode
import CodeScanner

struct QrStartView: View {
	
	@EnvironmentObject var appState: AppState
	
	@State var serverUrl = ""
	@State var accessKey = ""
	@State var studyWebId: Int64 = 0
	@State var qId: Int64 = 0
	@State var openQrScanner = false
	@State var gotoStudyLoader = false
	let interpreter = QrInterpreter()
	
	var body: some View {
		VStack {
			NavigationLink(
				destination: StudyLoaderView(serverUrl: self.serverUrl, accessKey: self.accessKey, studyWebId: self.studyWebId, qId: self.qId),
				isActive: self.$gotoStudyLoader
			) {
				EmptyView()
			}
			Image(systemName: "camera.fill").font(.system(size: 80))
			Text("welcome_qr_instructions").padding(.vertical)
			Button("start") {
				self.openQrScanner = true
			}
			Spacer()
		}
		.padding()
		.sheet(isPresented: self.$openQrScanner) {
			CodeScannerView(codeTypes: [.qr]) { result in
				switch result {
				case .success(let code):
					print("Found code: \(code)")
					let r = self.interpreter.check(s: code.string)
					if(r != nil) {
						self.serverUrl = r!.url
						self.accessKey = r!.accessKey
						self.studyWebId = r!.studyId
						self.qId = r!.qId
						self.gotoStudyLoader = true
					}
					else {
						self.appState.showTranslatedToast("error_qr_faulty")
					}
					self.openQrScanner = false
					
				case .failure(let error):
					self.appState.showToast(error.localizedDescription)
					self.openQrScanner = false
				}
			}
		}
	}
}
