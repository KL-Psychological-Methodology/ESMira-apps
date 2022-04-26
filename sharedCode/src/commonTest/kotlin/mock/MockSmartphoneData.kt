package mock

import at.jodlidev.esmira.sharedCode.PhoneType
import at.jodlidev.esmira.sharedCode.SmartphoneDataInterface

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockSmartphoneData(
	override val model: String = "Test",
	override val osVersion: String = "1.0",
	override val manufacturer: String = "JodliDev",
	override val appVersion: String = "0.0",
	override val appType: String = "Test",
	override val lang: String = "en"
) : SmartphoneDataInterface {
	internal var currentPhoneType: PhoneType = PhoneType.Android
	override val phoneType: PhoneType
	get() {
		return currentPhoneType
	}
}