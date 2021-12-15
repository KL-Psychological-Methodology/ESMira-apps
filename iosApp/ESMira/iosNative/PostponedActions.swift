//
// Created by JodliDev on 08.06.20.
// e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"at.jodlidev.esmira.update"]
//

import Foundation
import BackgroundTasks
import sharedCode
import UserNotifications

class PostponedActions: PostponedActionsInterface {
	static let IDENTIFIER_SYNC = "at.jodlidev.esmira.sync"
	static let IDENTIFIER_UPDATE = "at.jodlidev.esmira.update"
	static let IDENTIFIER_NOTIFICATIONS = "at.jodlidev.esmira.notifications"
	static let THREE_HOURS = TimeInterval(60*60*3)
	static let TWELVE_HOURS = TimeInterval(60*60*12)
	
	static func register() {
		BGTaskScheduler.shared.register(forTaskWithIdentifier: IDENTIFIER_UPDATE, using: nil, launchHandler: self.doUpdate)
		BGTaskScheduler.shared.register(forTaskWithIdentifier: IDENTIFIER_SYNC, using: nil, launchHandler: self.doSync)
		BGTaskScheduler.shared.register(forTaskWithIdentifier: IDENTIFIER_NOTIFICATIONS, using: nil, launchHandler: self.doUpdateNotifications)
		
//		BGTaskScheduler.shared.register(forTaskWithIdentifier: IDENTIFIER_UPDATE, using: nil) { task in
//			self.doUpdate(task: task)
//		}
//		BGTaskScheduler.shared.register(forTaskWithIdentifier: IDENTIFIER_SYNC, using: nil) { task in
//			self.doSync(task: task)
//		}
//		BGTaskScheduler.shared.register(forTaskWithIdentifier: IDENTIFIER_NOTIFICATIONS, using: nil) { task in
//			self.doUpdateNotifications(task: task as! BGAppRefreshTask)
//		}
	}
	
	
	private static func doUpdateNotifications(task: BGTask) {
		IosCode.initNativeLink()
		ErrorBox.Companion().log(title: "PostponedActions", msg: "Background notification update started...")
		scheduleNotificationUpdater()
		task.expirationHandler = {
			ErrorBox.Companion().log(title: "PostponedActions", msg: "Expiration time reached!")
		}
		
		Scheduler().scheduleAhead()
		task.setTaskCompleted(success: true)
	}
	
	private static func doSync(task: BGTask) {
		IosCode.initNativeLink()
		ErrorBox.Companion().log(title: "PostponedActions", msg: "Background sync started...")
		let web = Web()
		
		task.expirationHandler = {
			PostponedActions.scheduleSync()
			web.cancel()
		}
		
		Web.Companion().syncDataSetsBlocking(web: web)
		if(web.error) {
			PostponedActions.scheduleSync()
		}
		task.setTaskCompleted(success: web.error)
	}
	private static func doUpdate(task: BGTask) {
		IosCode.initNativeLink()
		ErrorBox.Companion().log(title: "PostponedActions", msg: "Background update started...")
		scheduleStudyUpdate()
//		let web = Web()
//
//		task.expirationHandler = {
//			web.cancel()
//		}
//
//		Web.Companion().updateStudiesBlocking(web: web)
		let web = Web()
		
		task.expirationHandler = {
			
		}
		
		Web.Companion().updateStudiesBlocking(forceStudyUpdate: false)
		task.setTaskCompleted(success: web.error)
	}
	
//	static func submitRequest(request: BGProcessingTaskRequest) {
//
//	}
	
	static func scheduleSync(_ interval: TimeInterval = THREE_HOURS) {
		let request = BGProcessingTaskRequest(identifier: PostponedActions.IDENTIFIER_SYNC)
		request.requiresNetworkConnectivity = true
		request.earliestBeginDate = Date(timeIntervalSinceNow: interval)
		logScheduleDate("Sync", request.earliestBeginDate)
		
		do {
			try BGTaskScheduler.shared.submit(request)
		}
		catch {
			ErrorBox.Companion().warn(title: "PostponedActions", msg:"Could not reschedule sync!\n\(error.localizedDescription)")
		}
	}
	static func scheduleStudyUpdate() {
		let request = BGProcessingTaskRequest(identifier: PostponedActions.IDENTIFIER_UPDATE)
		request.requiresNetworkConnectivity = true
		request.earliestBeginDate = Date(timeIntervalSinceNow: PostponedActions.TWELVE_HOURS)
//		request.earliestBeginDate = Date(timeIntervalSinceNow: TimeInterval(60*3))
		logScheduleDate("Study Update", request.earliestBeginDate)
		
		do {
			try BGTaskScheduler.shared.submit(request)
		}
		catch {
			ErrorBox.Companion().warn(title: "PostponedActions", msg:"Could not schedule study update!\n\(error.localizedDescription)")
		}
	}
	static func scheduleNotificationUpdater() {
		let refreshTask = BGAppRefreshTaskRequest(identifier: PostponedActions.IDENTIFIER_NOTIFICATIONS)
		refreshTask.earliestBeginDate = Date(timeIntervalSinceNow: PostponedActions.TWELVE_HOURS)
		logScheduleDate("Notification Update", refreshTask.earliestBeginDate)
		
		do {
			try BGTaskScheduler.shared.submit(refreshTask)
		}
		catch {
			ErrorBox.Companion().warn(title: "PostponedActions", msg: "Could not schedule notification updater!\n\(error.localizedDescription)")
		}
		
//		BGTaskScheduler.shared.submitTaskRequest(taskRequest: refreshTask, error: nil)
	}
	
	static func logScheduleDate(_ title: String, _ beginDate: Date?) {
		let now = Date().timeIntervalSince1970
		ErrorBox.Companion().log(title: "PostponedActions", msg: "Scheduled \(title) in \(round(((beginDate?.timeIntervalSince1970 ?? now) - now) / 60)) min")
	}
	
	
	func scheduleAlarm(alarm: Alarm) -> Bool {
		alarm.actionTrigger.execAsPostponedNotifications(alarm: alarm)
		return true
	}
	func cancel(alarm: Alarm) {
        ErrorBox.Companion().log(title: "Alarm lifespan", msg: "Canceling alarm: \(alarm.id)")
		let center = UNUserNotificationCenter.current()
		let sId = String(alarm.id)
		center.removeDeliveredNotifications(withIdentifiers: [sId])
		center.removePendingNotificationRequests(withIdentifiers: [sId])
	}
	
	func updateStudiesRegularly() {
		PostponedActions.scheduleStudyUpdate()
		PostponedActions.scheduleNotificationUpdater()
	}
	func cancelUpdateStudiesRegularly() {
		BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: PostponedActions.IDENTIFIER_UPDATE)
	}
	
	static var isRunning = false
	func syncDataSets() {
//		let request = BGProcessingTaskRequest(identifier: PostponedActions.IDENTIFIER_SYNC)
//		request.requiresNetworkConnectivity = true
//		PostponedActions.logScheduleDate("Sync", request.earliestBeginDate)
//
//		do {
//			try BGTaskScheduler.shared.submit(request)
//		}
//		catch {
//			ErrorBox.Companion().error(title: "PostponedActions", msg:"Could not schedule sync! Falling back to async task\n\(error.localizedDescription)")
//			if(Thread.isMainThread) {
//				print("Syncing in Main Thread")
//				Web.Companion().syncDataSetsAsync { syncCount in
//					if(syncCount == -1) {
//						PostponedActions.scheduleSync()
//					}
//				}
//			}
//			else {
//				print("Syncing in Background Thread")
//				if(Web.Companion().syncDataSetsBlocking(web: Web()) == -1) {
//					PostponedActions.scheduleSync()
//				}
//			}
//		}
		
		
		
//		BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: PostponedActions.IDENTIFIER_SYNC)
		if(Thread.isMainThread) {
			print("Syncing in Main Thread")
			
			if(PostponedActions.isRunning) { //Synchronized in kotlin does not seem to work in ios.
				print("Already running. Postponing sync")
				PostponedActions.scheduleSync(TimeInterval(3*60))
			}
			else {
				PostponedActions.isRunning = true
				Web.Companion().syncDataSetsAsync { syncCount in
					PostponedActions.isRunning = false;
					if(syncCount == -1) {
						print("Failed. We try again later")
						PostponedActions.scheduleSync()
					}
				}
			}
		}
		else {
			print("Syncing in Background Thread")
			if(Web.Companion().syncDataSetsBlocking(web: Web()) == -1) {
				PostponedActions.scheduleSync()
			}
		}
	}
}
