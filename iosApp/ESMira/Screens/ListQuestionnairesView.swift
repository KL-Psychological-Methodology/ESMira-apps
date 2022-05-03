//
// Created by JodliDev on 09.06.20.
//


import SwiftUI
import sharedCode
import Foundation

struct ListQuestionnairesView: View {
	@EnvironmentObject var appState: AppState
	@Binding var studies: [Study]
	
	@State private var showInformedConsent = false
	@State private var showStudyOptions = false
	@State private var showLeaveAlert = false
	@State private var currentStudy: Study? = nil
	
	let updateTimer = Timer.publish(every: 10, on: .main, in: .common).autoconnect()
	
	private func reloadStudies() {
		self.studies = DbLogic().getJoinedStudies()
	}
	
	var body: some View {
		VStack(alignment: .leading) {
			if(studies.count == 0) {
				Text("info_no_studies_joined").padding()
			}
			else {
				//Workaround for https://stackoverflow.com/questions/56690310/swiftui-dynamic-list-with-sections-does-not-layout-correctly
				List() {
					ForEach(studies, id: \.webId) { study in
						Section(header:
						HStack {
							Text(study.title).bold()
							Spacer()
							Button(action: {
								self.currentStudy = study
								self.showStudyOptions = true
							}) {
								Image(systemName: "ellipsis.circle")
							}
						}
						) {
							if study.availableQuestionnaires.count == 0 {
								Text("info_no_questionnaires")
							}
							else {
								ForEach(study.availableQuestionnaires, id: \.internalId) { questionnaire in
									Button(action: {
										self.appState.openQuestionnaire(questionnaire.id)
									}) {
										Text(questionnaire.title)
									}
								}
							}
						}
					}
				}
			}
			Spacer()
			Button(action: {
				self.appState.addStudyOpened = true
			}) {
				HStack {
					Image(systemName: "plus")
					Text("add_a_study")
				}.padding()
			}
		}
			.onAppear {
				print("onAppear")
				reloadStudies()
			}
			.onReceive(appState.$updateLists) { _ in
				print("$updateLists")
				reloadStudies()
			}
			.onReceive(updateTimer) { _ in
				print("updateTimer")
				reloadStudies()
			}
			.actionSheet(isPresented: self.$showStudyOptions) {
				ActionSheet(title: Text(self.currentStudy!.title), buttons: [
					.default(Text(String(format: NSLocalizedString("ios_contact_email", comment: ""), self.currentStudy!.contactEmail))) {
						if let url = URL(string: "mailto:\(self.currentStudy!.contactEmail)") {
							if #available(iOS 10.0, *) {
								UIApplication.shared.open(url)
							} else {
								UIApplication.shared.openURL(url)
							}
						}
					},
					.default(Text("informed_consent")) {
						self.showInformedConsent = true
					},
					.default(Text("leave_study")) {
						self.showLeaveAlert = true
					},
					.cancel()
				])
			}
			.alert(isPresented: self.$showLeaveAlert) {
				Alert(title: Text("dialogTitle_leave_study"), message: Text("dialogDesc_leave_study"),
					primaryButton: .destructive(Text("leave")) {
						self.currentStudy!.leave()
						self.studies = DbLogic().getJoinedStudies()
					}, secondaryButton: .cancel())
			}
			.sheet(isPresented: self.$showInformedConsent) {
				VStack {
					ScrollView {
						Text(self.currentStudy!.informedConsentForm)
					}
					Spacer()
					HStack {
						Spacer()
						Button(action: {
							self.showInformedConsent = false
						}) {
							Text("close")
						}
					}
				}.padding()
			}
	}
}
