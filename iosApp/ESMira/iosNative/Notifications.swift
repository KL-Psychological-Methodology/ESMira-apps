//
// Created by JodliDev on 08.06.20.
//

import Foundation
import sharedCode
import UserNotifications

class Notifications: NotificationsInterface {
	
	static private var copyId: Int = 1
	static private let KEY_MISSED_NOTIFICATIONS: String = "missed_notifications"
	static func authorize(completionHandler: @escaping (Bool) -> Void) {
		UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { (didAllow, error) in
			DispatchQueue.main.async { //we need to go back to the main thread
				completionHandler(didAllow && error == nil)
			}
		}
	}
	
	func createNotification(id: String, title: String, msg: String, trigger: UNTimeIntervalNotificationTrigger? = nil, completionHandler: ((Error?) -> Void)? = nil) {
		let notification = UNMutableNotificationContent()
		
		notification.title = title
		notification.body = msg.stripOutHtml()
		notification.sound = UNNotificationSound(named: UNNotificationSoundName(rawValue: "notification.wav"))
		notification.badge = 1
		
		let request = UNNotificationRequest(identifier: id, content: notification, trigger: trigger)
		
		let center = UNUserNotificationCenter.current()
		center.getNotificationSettings { settings in
			if(settings.authorizationStatus == .authorized) {
				UNUserNotificationCenter.current().add(request, withCompletionHandler: completionHandler)
			}
			else {
				ErrorBox.Companion().warn(title: "Notifications", msg: "Notifications are disabled by the user!")
				NativeLink().dialogOpener.notificationsBroken()
			}
		}
	}
	
	func firePostponed(alarm: Alarm, msg: String, subId: Int32) {
		let id: String
		if(subId != -1) {
			id = String("postponed_\(alarm.id)_\(subId)")
		}
		else {
			id = String("postponed_\(alarm.id)")
		}
		let trigger = UNTimeIntervalNotificationTrigger(timeInterval: Double(alarm.timestamp/1000) - Date().timeIntervalSince1970, repeats: false)
		
		let questionnaire = DbLogic().getQuestionnaire(id: alarm.questionnaireId)
		createNotification(id: id, title: questionnaire?.title ?? "Error", msg: msg, trigger: trigger)
	}
	
	func fire(title: String, msg: String, id: Int32) {
		createNotification(id: "normal_\(id)", title: title, msg: msg) { (error) in
			if error != nil {
				ErrorBox.Companion().warn(title: "Notification", msg: "Notification (title=\(title); msg=\(msg)) was not sent!\n\(String(describing: error))")
			}
		}
	}
	
	//is probably never used in iOS (because usually bings will be scheduled by firePostponed() unless they need to be fired right now).
	//timeout_min (automatic removal of notification after x mins) is not supported on iOs
	func fireQuestionnaireBing(title: String, msg: String, questionnaire: Questionnaire, timeoutMin: Int32, type: DataSet.EventTypes, scheduledToTimestamp: Int64) {
		createNotification(id: String("invitation_\(questionnaire.id)"), title: title, msg: msg) { (error) in
			DispatchQueue.main.async {
				DataSet.Companion().createActionSentDataSet(type: type, questionnaire: questionnaire, scheduledToTimestamp: scheduledToTimestamp)
			}
		}
	}
	
	//is probably never used in iOS (because usually bings will be scheduled by firePostponed() unless they need to be fired right now).
	func fireStudyNotification(title: String, msg: String, questionnaire: Questionnaire, scheduledToTimestamp: Int64) {
		createNotification(id: String("notification_\(questionnaire.id)"), title: title, msg: msg) { (error) in
			DispatchQueue.main.async {
				DataSet.Companion().createActionSentDataSet(type: DataSet.EventTypes.notification, questionnaire: questionnaire, scheduledToTimestamp: scheduledToTimestamp)
			}
		}
	}
	
	func fireMessageNotification(study: Study) {
		createNotification(
			id: String("message_\(study.id)"),
			title: study.title,
			msg: NSLocalizedString("info_new_message", comment: "")
		)
	}
	
	func fireSchedulesChanged(study: Study) {
		createNotification(
			id: String("schedule_\(study.id)"),
			title: String(format: NSLocalizedString("ios_info_study_updated", comment: ""), String(study.title)),
			msg: NSLocalizedString("info_study_updated_desc", comment: "")
		)
	}
	
	func remove(id: Int32) {
		let center = UNUserNotificationCenter.current()

		let variants = getNotificationVariants(id)
		ErrorBox.Companion().log(title: "Notification", msg: "Removing notification id=\(id)")
		center.removeDeliveredNotifications(withIdentifiers: variants)
		center.removePendingNotificationRequests(withIdentifiers: variants)
	}
	
	func removeQuestionnaireBing(questionnaire: Questionnaire) {
	    //not needed in iOS because we have no notification timeout to remove
	}

	private func getNotificationVariants(_ id: Int32) -> [String] {
		return ["normal_\(id)", "postponed_\(id)", "invitation_\(id)", "notification_\(id)"]
	}
	
	func exists(id: Int32) {
//		let sId = String(id)
//		UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
//			for notification in notifications {
//				if(notification.ide)
//			}
//		}
	}
	
}

extension String {
	func stripOutHtml() -> String {
		do {
			guard let data = self.data(using: .unicode) else {
				return self
			}
			let attributed = try NSAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding: String.Encoding.utf8.rawValue], documentAttributes: nil)
			return attributed.string
		} catch {
			return self
		}
	}
}
