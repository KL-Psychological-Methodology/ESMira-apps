//
//  Created by JodliDev on 12.10.22.
//

import SwiftUI
import sharedCode


struct RewardView: View {
	@EnvironmentObject var appState: AppState
	let study: Study
	@State private var showShareSheet = false
	@State private var error: String = ""
	@State private var rewardCode: String = ""
	@State private var fulfilledQuestionnaires: [KotlinLong : KotlinBoolean] = [:]
	
	@State private var showAlert: Bool = false
	@State private var alertView: () -> Alert = { Alert(title: Text(""))}
	
	private func getErrorView(error: String) -> some View {
		VStack(alignment: .leading) {
			Text(error)
			
			if(!self.fulfilledQuestionnaires.isEmpty) {
				Spacer()
				Text("error_reward_questionnaires_not_finished")
				Spacer(minLength: 20)
				ForEach(self.study.questionnaires, id: \.internalId) { (questionnaire: Questionnaire) in
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
				if(!study.contactEmail.isEmpty) {
					Button(action: {
						let emailContent = study.rewardEmailContent.isEmpty ? NSLocalizedString("reward_code_content", comment: "") : study.rewardEmailContent
						if let url = URL(string: "mailto:\(self.study.contactEmail)?body=\(emailContent)") {
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
					Spacer()
				}
				Button(action: {
					self.showShareSheet = true
				}) {
					VStack {
						Image(systemName: "square.and.arrow.up").frame(height: 30)
						Text("reward_code_share")
					}
				}
			}
			Spacer()
			if(!study.rewardInstructions.isEmpty) {
				ScrollableHtmlTextView(html: study.rewardInstructions)
			}
				
		}
		.sheet(isPresented: $showShareSheet) {
			ShareSheet(activityItems: [self.rewardCode])
		}
		.alert(isPresented: self.$showAlert, content: self.alertView)
	}
	
	var body: some View {
		HStack {
			let untilActive = self.study.daysUntilRewardsAreActive()
			if(!study.enableRewardSystem || untilActive != 0) {
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
		.padding()
		.onAppear {
			error = ""
			study.getRewardCode(
				onError: {msg in
						error = msg
					},
				onSuccess: { rewardInfo in
					switch(rewardInfo.errorCode) {
						case Study.Companion().REWARD_SUCCESS:
							rewardCode = rewardInfo.code
						case Study.Companion().REWARD_ERROR_UNFULFILLED_REWARD_CONDITIONS:
							error = NSLocalizedString("error_reward_conditions_not_met", comment: "")
							fulfilledQuestionnaires = rewardInfo.fulFilledQuestionnaires
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