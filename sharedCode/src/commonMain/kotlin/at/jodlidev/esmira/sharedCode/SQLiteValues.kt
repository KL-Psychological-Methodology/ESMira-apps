package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 10.08.20.
 */
interface SQLiteValues {
	fun putBoolean(key: String, value: Boolean)
	fun putInt(key: String, value: Int)
	fun putLong(key: String, value: Long?)
	fun putDouble(key: String, value: Double)
	fun putString(key: String, value: String)
	
	fun getContent(): Any
}