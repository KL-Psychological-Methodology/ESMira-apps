package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 18.05.2020.
 */
interface SmartphoneDataInterface {
	val phoneType: PhoneType
	val model: String
	val osVersion: String
	val manufacturer: String
	val appVersion: String
	val appType: String
	val lang: String
}