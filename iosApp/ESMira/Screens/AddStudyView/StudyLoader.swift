//
//  StudyLoader.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode
import CodeScanner

struct StudyLoader: View {
	enum ViewType {
		case QrScanning, AccessKey, Immediately
	}
	@EnvironmentObject var appState: AppState
	
	var studyState: StudyState
	
	@State private var loadingState: LoadingState = .hidden
	@State private var loadingMessage = ""
	@State private var openStudyList = false
	@State var type: ViewType
	
	@State var askAccessKey = false
	@State var openQrScanner = false
	@State var accessKey = ""
	let interpreter = QrInterpreter()
	
	func gotoStudyList(serverUrl: String, accessKey: String = "", studyWebId: Int64 = 0, qId: Int64 = 0) {
		print(accessKey)
		self.studyState.serverUrl = serverUrl
		self.studyState.accessKey = accessKey
		self.studyState.studyWebId = studyWebId
		self.studyState.qId = qId
		self.loadingState = .loading
	}
	func onLoaderShowing() {
		print(">"+self.studyState.serverUrl+"<")
		print(">"+self.studyState.accessKey+"<")
		self.studyState.web = Web.Companion().loadStudies(
			serverUrl: self.studyState.serverUrl,
			accessKey: self.studyState.accessKey,
			onError: { msg, e in
				self.loadingMessage = msg
				self.loadingState = .error
				
				
//					self.appState.showToast(msg)
			},
			onSuccess: { studyString, urlFormatted in
				self.studyState.studiesList = Study.Companion().getFilteredStudyList(
					json: studyString,
					url: urlFormatted,
					accessKey: self.studyState.accessKey,
					studyWebId: self.studyState.studyWebId,
					qId: self.studyState.qId
				)
				self.loadingState = .hidden
				self.openStudyList = true
			}
		)
	}
	func onLoaderCancel() {
		self.studyState.web?.cancel()
	}
	
	
	func getContent() -> some View {
		return Group {
			if(self.type == .AccessKey) {
				VStack {
					HStack {
						Image(systemName: "lock.fill").font(.system(size: 80))
						Text("questionMark").font(.system(size: 64))
					}
					Text("welcome_accessKey_question").padding(.vertical)
					Button("yes") {
						self.askAccessKey = true
					}.padding()
					Button("welcome_join_public_study") {
						self.gotoStudyList(serverUrl: self.studyState.serverUrl)
					}.padding()
					Spacer()
				}
				.padding()
				.textFieldAlert(isPresented: self.$askAccessKey, text: self.$accessKey, title: "colon_accessCode") {
					self.studyState.accessKey = self.accessKey
					self.gotoStudyList(serverUrl: self.studyState.serverUrl, accessKey: self.studyState.accessKey)
				}
			}
			else if(self.type == .QrScanning) {
				VStack {
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
							let r = self.interpreter.check(s: code)
							if(r != nil) {
								self.gotoStudyList(
									serverUrl: r!.url,
									accessKey: r!.accessKey,
									studyWebId: r!.studyId,
									qId: r!.qId
								)
								self.openQrScanner = false
							}
							
							print("Found code: \(code)")
						case .failure(let error):
							self.appState.showToast(error.localizedDescription)
						}
					}
				}
			}
			else {
				LoadingSpinner(isAnimating: .constant(true), style: .large)
					.onAppear {
						self.onLoaderShowing()
					}
					.onDisappear() {
						self.onLoaderCancel()
					}
			}
		}
	}
	
	var body: some View {
		Group {
			if(self.type == .Immediately) {
				if(self.openStudyList) {
					StudyListView(studyState: self.studyState)
				}
				else {
					LoadingSpinner(isAnimating: .constant(true), style: .large)
						.onAppear {
							self.onLoaderShowing()
						}
						.onDisappear() {
							self.onLoaderCancel()
						}
				}
			}
			else {
				VStack {
					NavigationLink(destination: StudyListView(studyState: self.studyState), isActive: self.$openStudyList) { EmptyView() }.isDetailLink(false)
					self.getContent()
				}
				.customLoader(isShowing: self.$loadingState, onShowing: self.onLoaderShowing, onCancel: self.onLoaderCancel, message: self.loadingMessage)
			}
		}
	}
}
