package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 12.05.2020.
 */
interface SQLiteCursor {
	fun getBoolean(index: Int): Boolean
	fun getInt(index: Int): Int
	fun getLong(index: Int): Long
    fun getDouble(index: Int): Double
	fun getString(index: Int): String
	fun isNull(index: Int): Boolean
	
	fun moveToNext(): Boolean
	fun moveToFirst(): Boolean
	
	fun close()
}