//
//  StudyPermissionsView.swift
//  ESMira
//
//  Created by JodliDev on 13.07.21.
//

import SwiftUI
import sharedCode

struct StudyPermissions: View {
	class LineData {
		let header: String
		let desc: String
		let whatFor: String?
		let btn: String
		let btnAction: () -> Void
		
		var errorDesc = ""
		var errorAction: (() -> Void)? = nil
		var errorBtn = ""
		
		init(header: String, desc: String, btn: String, whatFor: String?, btnAction: @escaping () -> Void) {
			self.header = header
			self.desc = desc
			self.whatFor = whatFor
			self.btn = btn
			self.btnAction = btnAction
		}
	}
	
	@EnvironmentObject private var appState: AppState
	var studyState: StudyState
	let study: Study
	
	
	init(studyState: StudyState, study: Study) {
		self.studyState = studyState
		self.study = study
	}
	
	@State private var progress = 0
	@State private var shownLines: [LineData] = []
	@State private var completedLines: [Int] = []
	@State private var failedLines: [Int] = []
	
	@State private var isDone = false
	@State private var openStudyJoined = false
	
	@State private var showAlert: Bool = false
	@State private var alertView: () -> Alert = { Alert(title: Text(""))}
	
	
	var body: some View {
		VStack(alignment: .leading) {
			NavigationLink(destination: StudyJoinedView(study: self.study), isActive: self.$openStudyJoined, label: { EmptyView() }).isDetailLink(false)
			ForEach(self.shownLines.indices, id: \.self) { index in
				self.createLine(index: index, line: self.shownLines[index])
			}
			Spacer()
			if(self.isDone) {
				Text("info_study_permissionSetup_ended")
			}
			Divider()
			HStack {
				Spacer()
				Button("participate") {
					self.study.join()
					
					if(self.study.needsJoinedScreen()) {
						self.openStudyJoined = true
					}
					else {
						self.appState.addStudyOpened = false
					}
					self.studyState.updateStudyList()
				}.disabled(!self.isDone)
			}
		}
		.onAppear {
			var shownLines: [LineData] = []
			if(self.study.hasInformedConsent()) {
				shownLines.append(LineData(header: "informed_consent", desc: "informed_consent_desc", btn: "show_informed_consent", whatFor: nil) {
					self.alertView = {Alert(title: Text("informed_consent"), message: Text(self.study.informedConsentForm), primaryButton: .default(Text("i_agree"), action: {
						self.nextProgress()
					}), secondaryButton: .cancel())}
					self.showAlert = true
				})
			}
			
			if(self.study.hasSchedules() || self.study.hasEvents()) {
				let line = LineData(header: "notifications", desc: "notification_permission_check", btn: "enable_notifications", whatFor: "notification_setup_desc") {
					Notifications.authorize { success in
						self.nextProgress(failed: !success)
					}
				}
				line.errorDesc = "ios_dialogDesc_notifications_disabled"
				line.errorBtn = "open_settings"
				line.errorAction = {
					self.openSettings()
				}
				
				shownLines.append(line)
			}
			
			if(shownLines.count == 0) {
				self.isDone = true
			}
			self.shownLines = shownLines
		}
		.alert(isPresented: self.$showAlert, content: self.alertView)
		.padding()
		.navigationBarTitle(Text("add_a_study"), displayMode: .inline)
	}
	
	private func createLine(index: Int, line: LineData) -> some View {
		let isFailed = self.failedLines.contains(index)
		let isCompleted = self.completedLines.contains(index)
		let isCurrent = self.progress == index
		
		return VStack(alignment: .leading) {
			HStack {
				Text("\(index+1). \(NSLocalizedString(line.header, comment: ""))").font(.system(size: 22))
				if(line.whatFor != nil) {
					Spacer()
					Button("what_for") {
						self.alertView = {Alert(title: Text("what_for"), message: Text(NSLocalizedString(line.whatFor!, comment: "")), dismissButton: .default(Text("ok_")))}
						self.showAlert = true
					}
				}
				Spacer()
				if(isFailed) {
					Image(systemName: "xmark.circle.fill").foregroundColor(Color.red)
				}
				else if(isCompleted) {
					Image(systemName: "checkmark.circle.fill").foregroundColor(Color.green)
				}
			}
			if(isCurrent) {
				Text(NSLocalizedString(line.desc, comment: "")).padding(.vertical)
				Button(NSLocalizedString(line.btn, comment: ""), action: line.btnAction).padding()
			}
			else if(isFailed) {
				Text(NSLocalizedString(line.errorDesc, comment: "")).padding(.vertical)
				if(line.errorAction != nil) {
					Button(NSLocalizedString(line.errorBtn, comment: ""), action: line.errorAction!)
				}
			}
		}
		.opacity(isFailed || isCompleted || isCurrent ? 1 : 0.3)
	}
	
	private func nextProgress(failed: Bool = false) {
		if(failed) {
			self.failedLines.append(self.progress)
		}
		else {
			self.completedLines.append(self.progress)
		}
		withAnimation {
			self.progress += 1
			if(self.progress >= self.shownLines.count) {
				self.isDone = true
			}
		}
		
	}
	
	private func openSettings() {
		guard let settingsUrl = URL(string: UIApplication.openSettingsURLString) else {
			self.appState.showTranslatedToast("error_settings_not_opened")
			return
		}
		
		if(UIApplication.shared.canOpenURL(settingsUrl)) {
			UIApplication.shared.open(settingsUrl) { success in
				if(!success) {
					self.appState.showTranslatedToast("error_settings_not_opened")
				}
			}
		}
	}
}
