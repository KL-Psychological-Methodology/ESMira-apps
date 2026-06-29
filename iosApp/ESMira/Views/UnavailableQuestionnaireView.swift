
import Foundation
import SwiftUI
import sharedCode

struct UnavailableQuestionnaireView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	let title: String
	let info: String
	
	init(availabilityStatus: Questionnaire.AvailabilityStatus) {
		// Most of these should never happen, but we cover them so users have feedback if they happen due to some error or bug
		switch availabilityStatus.type {
		case .noNotification:
			self.title = NSLocalizedString("notification_unavailable_title", comment: "")
			self.info = NSLocalizedString("notification_unavailable_info", comment: "")
		case .alreadyFilledOut:
			self.title = NSLocalizedString("notification_unavailable_title_filled_out", comment: "")
			self.info = NSLocalizedString("notification_unavailable_info_already_filled_out", comment: "")
		case .notificationTimeout:
			let maxTimeout = Int(Double(availabilityStatus.dataShouldBe) / (1000.0 * 60.0))
			let reactionTime = Int(ceil(Double(availabilityStatus.dataIs) / (1000.0 * 60.0)))
			self.title = NSLocalizedString("notification_unavailable_title", comment: "")
			self.info = String(format: NSLocalizedString("notification_unavailable_info_notification_timeout", comment: ""), maxTimeout, reactionTime)
		case .specificTime:
			let startTime = NativeLink().formatTime(ms: availabilityStatus.dataIs)
			let endTime = NativeLink().formatTime(ms: availabilityStatus.dataShouldBe)
			self.title = NSLocalizedString("notification_unavailable_title", comment: "")
			self.info = String(format: NSLocalizedString("notification_unavailable_info_specific_time", comment: ""), startTime, endTime)
		case .completionFrequency:
			let lastCompleted = Int(floor(Double(availabilityStatus.dataIs) / (60.0 * 1000.0)))
			let minInterval = Int(ceil(Double(availabilityStatus.dataShouldBe) / (60.0 * 1000.0)))
			self.title = NSLocalizedString("notification_unavailable_title", comment: "")
			self.info = String(format: NSLocalizedString("notification_unavailable_completion_frequency", comment: ""), minInterval, lastCompleted)
		case .scriptFilter:
			self.title = NSLocalizedString("notification_unavailable_title", comment: "")
			self.info = NSLocalizedString("notification_unavailable_info_script_filter", comment: "")
		case .phoneType:
			self.title = NSLocalizedString("notification_unavailable_title_os", comment: "")
			self.info = NSLocalizedString("notification_unavailable_info_phone_type", comment: "")
		case .emptyQuestionnaire:
			self.title = NSLocalizedString("notification_unavailable_title_empty", comment: "")
			self.info = NSLocalizedString("notification_unavailable_info_empty_questionnaire", comment: "")
		case .inactive:
			self.title = NSLocalizedString("notification_unavailable_title_expired", comment: "")
			self.info = NSLocalizedString("notification_unavailable_info_inactive", comment: "")
		default:
			self.title = NSLocalizedString("notification_unavailable_title", comment: "")
			self.info = NSLocalizedString("notification_unavailable_info", comment: "")
		}
	}
	
	var body: some View {
		NavigationView {
			VStack(alignment: .leading, spacing: 20) {
				Text(self.info)
				
				
				HStack(alignment: .center) {
					Spacer()
					DefaultButton("ok_", action: { self.navigationState.closeScreenDialog() }).padding()
				}
				Spacer()
			}
		.padding()
		.navigationBarTitle(Text(self.title), displayMode: .inline)
		}
	}
}
