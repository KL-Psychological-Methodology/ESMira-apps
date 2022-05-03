package mock.mockSql

import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.SQLiteValues

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockCursor(
		private val keys: Array<String>,
		private val list: List<MockDatabase.LineData>
	): SQLiteCursor {
	private var pos = -1
	
	private fun getEntry(index: Int): Any? {
		val split = keys[index].split('.')
		return if(split.size == 1) {
			if(split[0].toUpperCase() == "COUNT(*)") {
				return list.size
			}
			list[pos].getValue(split[0])
		}
		else
			list[pos].getValue(split[0], split[1])
	}
	
	override fun getBoolean(index: Int): Boolean {
		return ((getEntry(index) ?: 0) as Int) == 1
	}
	
	override fun getInt(index: Int): Int {
		return (getEntry(index) ?: 0) as Int
	}
	
	override fun getLong(index: Int): Long {
		return (getEntry(index) ?: -1L) as Long
	}
	
	override fun getDouble(index: Int): Double {
		return getEntry(index) as Double
	}
	
	override fun getString(index: Int): String {
		return (getEntry(index) ?: "") as String
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