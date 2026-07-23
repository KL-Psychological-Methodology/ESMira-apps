//
//  Created by JodliDev on 12.10.22.
//

import SwiftUI
import sharedCode


struct RewardView: View {
	@EnvironmentObject var appState: AppState
	@State var study: Study
	
	@State private var showShareSheet = false
	@State private var error: String = ""
	@State private var rewardCode: String = ""
	@State private var fulfilledQuestionnaires: [KotlinLong : KotlinBoolean] = [:]
	@State private var getCode: Bool = false
	@State private var showRequestConfirmation: Bool = false
	
	@State private var showAlert: Bool = false
	@State private var alertView: () -> Alert = { Alert(title: Text(""))}
	
	private var canRequest: Bool {
		let untilActive = self.study.daysUntilRewardsAreActive()
		let rewardAvailable = untilActive <= 0
		let allFulfilled = fulfilledQuestionnaires.values.allSatisfy {$0.boolValue}
		return rewardAvailable && allFulfilled
	}
	
	init(study: Study) {
		self.study = study
	}
	
	private func getDefaultView(error: String) -> some View {
		VStack(alignment: .leading) {
			if(!error.isEmpty) {
				Text(error)
			}
			
			if(!self.fulfilledQuestionnaires.isEmpty) {
				Text("error_reward_questionnaires_not_finished").padding(.vertical)
				let availableQuestionnaires = self.study.questionnaires.filter{(questionnaire: Questionnaire) in
					questionnaire.limitToGroup == 0 || questionnaire.limitToGroup == study.group || self.fulfilledQuestionnaires[KotlinLong(value: questionnaire.internalId)] ?? true != true
				}
				ForEach(availableQuestionnaires, id: \.internalId) { (questionnaire: Questionnaire) in
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
			if(study.enableRewardCalculation){
				VStack {
					if(!study.rewardCalculationInfo.isEmpty) {
						ScrollableHtmlTextView(html: study.rewardCalculationInfo)
					}
					Text(String(format: NSLocalizedString("reward_current_amount", comment: ""), study.getRewardAmount()))
				}
			}
			VStack {
				let untilActive = self.study.daysUntilRewardsAreActive()
				if untilActive > 0 {
					Text(String(format: NSLocalizedString("info_reward_not_active_yet", comment: ""), untilActive))
				}
				Spacer()
				Button(action: {
					self.showRequestConfirmation = true
				}) {
					Text("reward_code_request")
				}.disabled(!canRequest)
			}
			
			Spacer()
		}
		.padding()
		.alert(isPresented: $showRequestConfirmation) {
			Alert(
				title: Text("reward_code_request"),
				message: Text(study.enableRewardCalculation ? NSLocalizedString("reward_code_request_info_calculation", comment: "") : NSLocalizedString("reward_code_request_info", comment: "")),
				primaryButton: .default(Text("reward_code_request_action")) {
					self.requestCode()
				},
				secondaryButton: .cancel(Text("cancel"))
			)
		}
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
					Spacer()
					Button(action: {
						var emailContent = self.study.rewardEmailContent.isEmpty ? NSLocalizedString("reward_code_content", comment: "") : self.study.rewardEmailContent
						emailContent = emailContent.replacingOccurrences(of: "[[CODE]]", with: self.rewardCode)
						if let url = URL(string: "mailto:\(self.study.contactEmail)?body=\(emailContent)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)!) {
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
			if(!study.rewardInstructions.isEmpty) {
				ScrollableHtmlTextView(html: study.rewardInstructions)
			}
			
			if(study.enableRewardCalculation) {
				VStack(alignment: .leading) {
					if(!study.rewardCalculationInfo.isEmpty) {
						ScrollableHtmlTextView(html: study.rewardCalculationInfo)
					}
					Text(String(format: NSLocalizedString("reward_final_amount", comment: ""), study.cachedRewardAmount))
				}
			}
				
		}
		.sheet(isPresented: $showShareSheet) {
			ShareSheet(activityItems: [self.rewardCode])
		}
		.alert(isPresented: self.$showAlert, content: self.alertView)
	}
	
	private func requestCode() {
		self.getCode = true
		self.fetchRewardCode()
	}
	
	private func fetchRewardCode() {
		error = ""
		self.study.getRewardCode(
			onError: { msg in
				error = msg
			},
			onSuccess: { rewardInfo in
				switch rewardInfo.errorCode {
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
	
	var body: some View {
		ScrollView{
			HStack {
				let untilActive = self.study.daysUntilRewardsAreActive()
				if(!self.study.enableRewardSystem || untilActive != 0) {
					self.getDefaultView(error: String(format: NSLocalizedString("info_reward_is_not_active_yet", comment: ""), untilActive))
				}
				else if(self.study.enableRewardCalculation) {
					if(!self.error.isEmpty){
						self.getDefaultView(error: self.error)
					} else if(!self.rewardCode.isEmpty) {
						self.getCodeView()
					} else if(self.getCode){
						self.getLoadingView()
					} else {
						self.getDefaultView(error: "")
					}
				}
				else {
					if(!self.error.isEmpty) {
						self.getDefaultView(error: self.error)
					}
					else if(rewardCode.isEmpty) {
						self.getLoadingView()
					}
					else {
						self.getCodeView()
					}
				}
			}.padding()
		}
		.padding()
		.onAppear {
			getCode = study.hasCachedRewardCode()
			if getCode {
				fetchRewardCode()
			} else {
				fulfilledQuestionnaires = (study.getRewardFulfillmentLocal() as? [KotlinLong : KotlinBoolean]) ?? [:]
			}
		}
	}
}
