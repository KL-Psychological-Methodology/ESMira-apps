//
//  AppDelegate.swift
//  ESMira
//
//  Created by JodliDev on 30.04.20.
//

import UIKit
import sharedCode

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {
	let appState = AppState()
	let navigationState = NavigationState()
	
	func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
		PostponedActions.register()
		UNUserNotificationCenter.current().delegate = self
		IosCode.initNativeLink(self.appState, self.navigationState)
		self.navigationState.switchStudy(DbUser().getCurrentStudyId())
		
		return true
	}
	
	func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
		// Called when a new scene session is being created.
		// Use this method to select a configuration to create the new scene with.
		return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
	}
 
	func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
		// Called when the user discards a scene session.
		// If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
		// Use this method to release any resources that were specific to the discarded scenes, as they will not return.
	}
	
	
	
	func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
		let identifier = response.notification.request.identifier
		let id = identifier.split(separator: "_")
		
		ErrorBox.Companion().log(title: "Notification", msg: "Executing notification \"\(identifier)\"")
		
		switch(id[0]) {
			case "normal":
				(NativeLink().dialogOpener as! DialogOpener).openGuiDialog(response.notification.request.content.title, response.notification.request.content.body)
				break
			case "schedule":
				let study = DbLogic().getStudy(id: Int64(id[1]) ?? -1)
			   if(study != nil) {
				   navigationState.openChangeSchedules(study!.id)
			   }
			case "invitation":
				let questionnaire = DbLogic().getQuestionnaire(id: Int64(id[1]) ?? -1)
				if(questionnaire != nil) {
					navigationState.openQuestionnaire(questionnaire!)
				}
				else {
					ErrorBox.Companion().error(title: "Alarm", msg: "Questionnaire (id=\(id[1])) is null")
				}
			case "postponed":
				print("Getting alarm with id: \(id[1])")
				let alarm = DbLogic().getAlarm(id: Int64(id[1]) ?? -1)
				if(alarm != nil) {
					alarm?.exec(fireNotifications: false)
					if(alarm!.actionTrigger.hasInvitation(nothingElse: false)) {
						let questionnaire = DbLogic().getQuestionnaire(id: alarm?.questionnaireId ?? 0)
						if(questionnaire != nil) {
							navigationState.openQuestionnaire(questionnaire!)
						}
						else {
							ErrorBox.Companion().error(title: "Alarm", msg: "Questionnaire (id=\(alarm?.questionnaireId ?? -1) for Alarm (id=\(alarm?.id ?? -1)) is null")
						}
					}
				}
				else {
					ErrorBox.Companion().error(title: "Alarm", msg: "Alarm is null! Ios-identifier: \(response.notification.request.identifier)")
				}
			case "message":
				print("Opening message for study id: \(id[1])")
				navigationState.openMessages(Int64(id[1]) ?? -1)
			default:
				break
		}
		completionHandler()
	}
	func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
		completionHandler([.alert, .sound])
		Scheduler().checkMissedAlarms(missedAlarmsAsBroken: false)
		print("notification!")
		self.navigationState.reloadStudy()	}
}
