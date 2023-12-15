//
// Created by JodliDev on 12.12.23.
//

import Foundation
import SwiftUI
import sharedCode

struct FaultyAccessKey: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
	let study: Study
	@State var newAccessKey: String = ""
	
	init(study: Study) {
		self.study = study
		self._newAccessKey = State(initialValue: study.accessKey)
	}
	
	func newErrors() -> some View {
		DbLogic().setErrorsAsReviewed()
		return Text("info_error_report_desc").bold()
	}
	
	func createErrorLine(error: ErrorBox) -> some View {
		let color: Color
		switch(error.severity) {
			case ErrorBox.Companion().SEVERITY_ERROR:
				color = Color.red
			case ErrorBox.Companion().SEVERITY_WARN:
				color = Color.orange
			case ErrorBox.Companion().SEVERITY_LOG:
				color = Color.primary
			default:
				color = Color.yellow
		}
		return VStack(alignment: .leading) {
			HStack {
				Text(error.getFormattedDateTime())
				Text(error.title)
					.fontWeight(.bold)
					.foregroundColor(color)
			}
			Spacer()
			Text(error.msg)
		}
	}
	
	var body: some View {
		VStack {
			Text(study.title).bold()
			Text("info_wrong_access_key").padding()
			TextField("", text: self.$newAccessKey)
				.padding() //padding inside border
				.border(Color("Outline"))
				.padding(.horizontal) //padding outside
			
			HStack(alignment: .center) {
				Spacer()
				DefaultButton("cancel", action: { self.navigationState.closeScreenDialog() })
					.padding()
				DefaultButton("ok_", action: {
					self.study.saveFaultyAccessKeyState(faulty: false, newAccessKey: self.newAccessKey)
					Web.Companion().updateStudiesAsync(forceStudyUpdate: false) { updatedCount in
						let faultyStudy = DbLogic().getFirstStudyWithFaultyAccessKey()
						if(faultyStudy != nil) {
							self.navigationState.openFaultyAccessKeyDialog(studyId: self.study.id)
						}
					}
					self.navigationState.closeScreenDialog()
				})
					.padding()
			}
		}
	}
}
