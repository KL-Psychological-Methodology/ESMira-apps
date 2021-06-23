//
// Created by JodliDev on 08.06.20.
//

import Foundation
import sharedCode

class IosCode: IosCodeInterface {
	func currentTimeMillis() -> Int64 {
		Int64(Date().timeIntervalSince1970) * 1000
	}
	
	func formatDateTime(ms: Int64) -> String {
		let formatter = DateFormatter()
		formatter.dateStyle = .short
		formatter.timeStyle = .short
		return formatter.string(from: Date(timeIntervalSince1970: Double(ms)/1000))
	}
	
	func formatTime(ms: Int64) -> String {
		let formatter = DateFormatter()
		formatter.dateFormat = "HH:mm:ss"
		return formatter.string(from: Date(timeIntervalSince1970: Double(ms)/1000))
	}
	
	func formatShortDate(ms: Int64) -> String {
		let formatter = DateFormatter()
		formatter.dateStyle = .short
		formatter.timeStyle = .none
		return formatter.string(from: Date(timeIntervalSince1970: Double(ms)/1000))
	}
	
	func getMidnightMillis(timestamp: Int64) -> Int64 {
		Int64(Calendar.current.startOfDay(for: timestamp == -1 ? Date() : Date(timeIntervalSince1970: Double(timestamp/1000))).timeIntervalSince1970) * 1000
	}
	
	func getTimezone() -> String {
		TimeZone.current.abbreviation() ?? ""
	}
	
	static func initNativeLink(_ appState: AppState? = nil) {
		if(appState != nil || !NativeLink().isInitialized) { // we need to make sure that background service does not override foreground app
			NativeLink().doInit(
				sql: SQLiteHelper(),
				smartphoneData: SmartphoneData(),
				dialogOpener: DialogOpener(appState: appState),
				notifications: Notifications(),
				postponedActions: PostponedActions(),
				iosCode: IosCode()
			)
		}
	}
}
