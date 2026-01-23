//
// Created by JodliDev on 12.04.23.
//

import Foundation
import sharedCode

class AddStudyState: ObservableObject {
	@Published var serverUrl: String
	@Published var accessKey: String
	@Published var lang: String
	@Published var fallbackUrl: String?
	@Published var studyWebId: Int64
	@Published var qId: Int64
	let serverList = Web.Companion().serverList
	
	init(serverUrl: String = "", accessKey: String = "", lang: String = NativeLink().smartphoneData.lang, fallbackUrl: String? = nil, studyWebId: Int64 = 0, qId: Int64 = 0) {
		self._serverUrl = Published(initialValue: serverUrl.isEmpty ? Web.Companion().serverList[0].second! as String : serverUrl)
		self._accessKey = Published(initialValue: accessKey)
		self._lang = Published(initialValue: lang)
		self._fallbackUrl = Published(initialValue: fallbackUrl)
		self._studyWebId = Published(initialValue: studyWebId)
		self._qId = Published(initialValue: qId)
	}
}
