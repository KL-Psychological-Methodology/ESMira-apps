//
// Created by JodliDev on 08.06.20.
//

import Foundation
import sharedCode

class IosCode: IosCodeInterface {
	func currentTimeMillis() -> Int64 {
		Int64((Date().timeIntervalSince1970 * 1000.0).rounded())
	}
	
	func formatDateTime(ms: Int64) -> String {
		let formatter = DateFormatter()
		formatter.dateStyle = .short
		formatter.timeStyle = .short
		return formatter.string(from: Date(timeIntervalSince1970: Double(ms)/1000))
	}
	
	func formatTime(ms: Int64) -> String {
		let formatter = DateFormatter()
		formatter.dateFormat = "HH:mm"
		return formatter.string(from: Date(timeIntervalSince1970: Double(ms)/1000))
	}
	
	func formatDate(ms: Int64) -> String {
		let formatter = DateFormatter()
		formatter.dateStyle = .short
		formatter.timeStyle = .none
		return formatter.string(from: Date(timeIntervalSince1970: Double(ms)/1000))
	}
	
	func getMidnightMillis(timestamp: Int64) -> Int64 {
		Int64((Calendar.current.startOfDay(for: timestamp == -1 ? Date() : Date(timeIntervalSince1970: Double(timestamp/1000))).timeIntervalSince1970 * 1000.0).rounded())
	}
	
	func getTimezone() -> String {
		TimeZone.current.abbreviation() ?? ""
	}
	
	func getTimezoneOffsetMillis() -> Int32 {
		Int32(TimeZone.current.secondsFromGMT() * 1000)
	}
	
	static func initNativeLink(_ appState: AppState, _ navigationState: NavigationState) {
		NativeLink().doInit(
			sql: SQLiteHelper(),
			smartphoneData: SmartphoneData(),
			dialogOpener: DialogOpener(appState: appState, navigationState: navigationState),
			notifications: Notifications(),
			postponedActions: PostponedActions(),
			iosCode: IosCode()
		)
	}
	
	func getDatesDiff(ms1: Int64, ms2: Int64) -> Int64 {
		let formatter = DateFormatter()
		formatter.dateStyle = .short
		formatter.timeStyle = .none
		let date1 = formatter.date(from: formatter.string(from: Date(timeIntervalSince1970: Double(ms1)/1000))) ?? Date(timeIntervalSince1970: 0)
		let date2 = formatter.date(from: formatter.string(from: Date(timeIntervalSince1970: Double(ms2)/1000))) ?? Date(timeIntervalSince1970: 0)
		let diff = Calendar.current.dateComponents([.day], from: date1, to: date2)
		return Int64(diff.day!)
	}
	
	/**
	 * For background service.
	  * Does nothing if already initialized. WIll be overwritten as soon as app is opened by user
	 */
	static func initNativeLink() { //for background service. Either does nothing
		if(!NativeLink().isInitialized) {
			NativeLink().doInit(
				sql: SQLiteHelper(),
				smartphoneData: SmartphoneData(),
				dialogOpener: DialogOpenerForBackground(),
				notifications: Notifications(),
				postponedActions: PostponedActions(),
				iosCode: IosCode()
			)
		}
	}
}
