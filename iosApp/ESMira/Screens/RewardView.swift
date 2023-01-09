//
//  Created by JodliDev on 12.10.22.
//

import SwiftUI
import sharedCode


struct RewardView: View {
	@EnvironmentObject var appState: AppState
	let studyId: Int64
	@State private var study: Study? = nil
	@State private var showShareSheet = false
	@State private var error: String = ""
	@State private var rewardCode: String = ""
	@State private var fulfilledQuestionnaires: [KotlinLong : KotlinBoolean] = [:]
	
	@State private var showAlert: Bool = false
	@State private var alertView: () -> Alert = { Alert(title: Text(""))}
	
	init(studyId: Int64) {
		self.studyId = studyId
		self.study = DbLogic().getStudy(id: studyId)
	}
	
	private func getErrorView(error: String) -> some View {
		VStack(alignment: .leading) {
			Text(error)
			
			if(!self.fulfilledQuestionnaires.isEmpty) {
				Text("error_reward_questionnaires_not_finished").padding(.vertical)
				ForEach(self.study!.questionnaires, id: \.internalId) { (questionnaire: Questionnaire) in
					HStack {
						Text(questionnaire.title)
						Spacer()
						if(self.fulfilledQuestionnaires[KotlinLong(value: questionnaire.internalId)] ?? true) as! Bool {
							Image(systemName: "checkmark.circle.fill").foregroundColor(Color.green)
						}
						else {
							Image(systemName: "xmark.circle.fill").foregroundColor(Color.red)
						}
					}
				}
			}
			Spacer()
		}
		.padding()
	}
	private func getLoadingView() -> some View {
		LoadingSpinner(isAnimating: .constant(true), style: .large)
	}
	private func getCodeView() -> some View {
		VStack {
			HStack {
				Text("colon_reward_code_header")
				Spacer()
				Button("what_for") {
					self.alertView = {
						Alert(title: Text("what_for"), message: Text(NSLocalizedString("reward_code_description", comment: "")), dismissButton: .default(Text("ok_")))
					}
					self.showAlert = true
				}
			}
			HStack {
				Button(action: {
					UIPasteboard.general.string = self.rewardCode
					self.appState.showTranslatedToast(String(format: NSLocalizedString("ios_info_copied_x_to_clipboard", comment: ""), self.rewardCode))
				}) {
					Text(self.rewardCode)
						.font(.system(size: 24))
					Image(systemName: "doc.on.clipboard")
						.foregroundColor(Color("Accent"))
				}
				.padding()
			}
			
			HStack {
				if(!study!.contactEmail.isEmpty) {
					Spacer()
					Button(action: {
						let emailContent = self.study!.rewardEmailContent.isEmpty ? NSLocalizedString("reward_code_content", comment: "") : self.study!.rewardEmailContent
						if let url = URL(string: "mailto:\(self.study!.contactEmail)?body=\(emailContent)") {
							if #available(iOS 10.0, *) {
								UIApplication.shared.open(url)
							} else {
								UIApplication.shared.openURL(url)
							}
						}
					}) {
						VStack {
							Image(systemName: "envelope").frame(height: 30)
							Text("reward_code_send_email")
						}
					}
				}
				
				Spacer()
				Button(action: {
					self.showShareSheet = true
				}) {
					VStack {
						Image(systemName: "square.and.arrow.up").frame(height: 30)
						Text("reward_code_share")
					}
				}
				Spacer()
			}
			Spacer(minLength: 10.0)
			if(!study!.rewardInstructions.isEmpty) {
				Text(study!.rewardInstructions)
				ScrollableHtmlTextView(html: study!.rewardInstructions)
			}
				
		}
		.sheet(isPresented: $showShareSheet) {
			ShareSheet(activityItems: [self.rewardCode])
		}
		.alert(isPresented: self.$showAlert, content: self.alertView)
	}
	
	var body: some View {
		HStack {
			if(self.study != nil) {
				let untilActive = self.study!.daysUntilRewardsAreActive()
				if(!self.study!.enableRewardSystem || untilActive != 0) {
					self.getErrorView(error: String(format: NSLocalizedString("info_reward_is_not_active_yet", comment: ""), untilActive))
				}
				else {
					if(!self.error.isEmpty) {
						self.getErrorView(error: self.error)
					}
					else if(rewardCode.isEmpty) {
						self.getLoadingView()
					}
					else {
						self.getCodeView()
					}
				}
			}
		}
		.padding()
		.onAppear {
			self.study = DbLogic().getStudy(id: self.studyId)
			error = ""
			self.study!.getRewardCode(
				onError: {msg in
						error = msg
					},
				onSuccess: { rewardInfo in
					switch(rewardInfo.errorCode) {
						case Study.Companion().REWARD_SUCCESS:
							rewardCode = rewardInfo.code
						case Study.Companion().REWARD_ERROR_UNFULFILLED_REWARD_CONDITIONS:
							error = NSLocalizedString("error_reward_conditions_not_met", comment: "")
							fulfilledQuestionnaires = rewardInfo.fulfilledQuestionnaires
						case Study.Companion().REWARD_ERROR_ALREADY_GENERATED:
							error = NSLocalizedString("error_already_generated", comment: "")
						default:
							error = rewardInfo.errorMessage
					}
				}
			)
		}
	}
}
