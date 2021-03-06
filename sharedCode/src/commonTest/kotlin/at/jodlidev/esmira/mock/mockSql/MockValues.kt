package mock.mockSql

import at.jodlidev.esmira.sharedCode.SQLiteValues

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockValues(): SQLiteValues {
	private val data = HashMap<String, Comparable<*>?>()
	constructor(oldValues: MockValues): this() {
		for((key, value) in oldValues.getValues()) {
			data[key] = value
		}
	}
	
	override fun putBoolean(key: String, value: Boolean) {
		data[key] = if(value) 1 else 0
	}
	
	override fun putInt(key: String, value: Int) {
		data[key] = value
	}
	
	override fun putLong(key: String, value: Long?) {
		data[key] = value
	}
	
	override fun putDouble(key: String, value: Double) {
		data[key] = value
	}
	
	override fun putString(key: String, value: String) {
		data[key] = value
	}
	
	override fun getContent(): Any {
		return data
	}
	
	fun getValues(): HashMap<String, Comparable<*>?> {
		return data
	}
}