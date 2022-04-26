package mock.mockSql

import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.SQLiteValues

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockCursor(
		private val keys: Array<String>,
		private val list: List<SQLiteValues>
	): SQLiteCursor {
	private var pos = -1
	
	private fun getCurrentLine(): HashMap<String, Any?> {
		return list[pos].getContent() as HashMap<String, Any?>
	}
	private fun getEntry(index: Int): Any? {
		return getCurrentLine()[getKey(index)]
	}
	private fun getKey(index: Int): String {
		val key = keys[index]
		val split = key.split('.')
		return split.last()
//		return keys[index]
	}
	
	override fun getBoolean(index: Int): Boolean {
		return getEntry(index) as Boolean
	}
	
	override fun getInt(index: Int): Int {
		return getEntry(index) as Int
	}
	
	override fun getLong(index: Int): Long {
		val value = getEntry(index)
		return if(value == null) -1L else value as Long
	}
	
	override fun getDouble(index: Int): Double {
		return getEntry(index) as Double
	}
	
	override fun getString(index: Int): String {
		return getEntry(index) as String
	}
	
	override fun isNull(index: Int): Boolean {
		return getEntry(index) == null
	}
	
	override fun moveToNext(): Boolean {
		return ++pos < list.size
	}
	
	override fun moveToFirst(): Boolean {
		pos = 0
		return list.isNotEmpty()
	}
	
	override fun close() {
		// do nothing
	}
	
}