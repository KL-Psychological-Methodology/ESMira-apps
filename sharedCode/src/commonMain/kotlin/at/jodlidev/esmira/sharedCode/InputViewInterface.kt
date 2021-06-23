package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 16.04.2019.
 */
interface InputViewInterface {
	fun getValue(): String?
	fun setValue(s: String?)
	fun isEmpty(): Boolean
}