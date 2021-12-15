//
// Created by JodliDev on 08.06.20.
//

import Foundation
import sharedCode

class SmartphoneData: SmartphoneDataInterface {
	var appType: String = "iOS"
	
	var appVersion: String = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? ""
	
	var manufacturer: String = "Apple"
	
	var model: String {
		get {
			
			var systemInfo = utsname()
			uname(&systemInfo)
			let machineMirror = Mirror(reflecting: systemInfo.machine)
			
			return machineMirror.children.reduce("") { identifier, element in
				guard let value = element.value as? Int8, value != 0 else {return identifier}
				return identifier + String(UnicodeScalar(UInt8(value)))
			}
		}
	}
	
	var osVersion: String {
		get {
			let os = ProcessInfo().operatingSystemVersion
			return "\(os.majorVersion).\(os.minorVersion).\(os.patchVersion)"
		}
	}
	var lang: String {
		get {
			return Locale.current.languageCode ?? "en"
		}
	}
}
