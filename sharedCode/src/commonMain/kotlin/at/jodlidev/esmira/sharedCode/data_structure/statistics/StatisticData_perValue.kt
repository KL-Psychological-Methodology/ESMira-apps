package at.jodlidev.esmira.sharedCode.data_structure.statistics

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.data_structure.ObservedVariable
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 22.08.2019.
 */
@Serializable
class StatisticData_perValue internal constructor(
	var value: String
) : StatisticData() {
	override var sum: Double = 0.0
	override var count: Int = 0
	
	constructor(value: String, variableName: String, index: Int, count: Int, studyId: Long): this(
		value
	) {
		this.studyId = studyId
		this.observableIndex = index
		this.count = count
		this.variableName = variableName
	}
	
	private constructor(observedVariable: ObservedVariable, value: String): this(
		value
	) {
		studyId = observedVariable.studyId
		observedId = observedVariable.id
		observableIndex = observedVariable.index
		variableName = observedVariable.variableName
	}
	
	constructor(c: SQLiteCursor): this(
		c.getString(3)
	) {
		loadCursor(c)
		observableIndex = c.getInt(5)
		variableName = c.getString(6)
	}
	
	constructor(c: SQLiteCursor, observedVariable: ObservedVariable): this(
		c.getString(3)
	) {
		loadCursor(c)
		observableIndex = observedVariable.index
		variableName = observedVariable.variableName
	}
	
	private fun loadCursor(c: SQLiteCursor) {
		exists = true
		id = c.getLong(0)
		studyId = c.getLong(1)
		observedId = c.getLong(2)
//		value = c.getString(3)
		count = c.getInt(4)
	}
	
	override fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putString(KEY_VALUE, value)
		values.putLong(KEY_OBSERVED_ID, observedId)
		values.putLong(KEY_STUDY_ID, studyId)
		values.putInt(KEY_COUNT, count)

		if(exists)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		else
			id = db.insert(TABLE, values)
	}
	
	override fun getType(): Int {
		return ObservedVariable.STORAGE_TYPE_FREQ_DISTR
	}
	
	companion object {
		const val TABLE = "statistics_perValue"
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_OBSERVED_ID = "observed_id"
		const val KEY_VALUE = "variable_value"
		const val KEY_COUNT = "variable_count"

		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_STUDY_ID,
			KEY_OBSERVED_ID,
			KEY_VALUE,
			KEY_COUNT
		)

		const val TABLE_CONNECTED = "$TABLE LEFT JOIN ${ObservedVariable.TABLE} ON $TABLE.$KEY_OBSERVED_ID=${ObservedVariable.TABLE}.$KEY_ID"
		val COLUMNS_CONNECTED = arrayOf(
				"$TABLE.$KEY_ID",
				"$TABLE.$KEY_STUDY_ID",
				"$TABLE.$KEY_OBSERVED_ID",
				"$TABLE.$KEY_VALUE",
				"$TABLE.$KEY_COUNT",
				"${ObservedVariable.TABLE}.${ObservedVariable.KEY_INDEX}",
				"${ObservedVariable.TABLE}.${ObservedVariable.KEY_VARIABLE_NAME}"
		)
		
		fun getInstance(observedVariable: ObservedVariable, value: String): StatisticData_perValue {
			val c = NativeLink.sql.select(
				TABLE,
				COLUMNS,
				"$KEY_OBSERVED_ID = ? AND $KEY_VALUE = ?", arrayOf(observedVariable.id.toString(), value),
				null,
				null,
				null,
				"1"
			)
			val statistic = if(c.moveToFirst()) StatisticData_perValue(
				c,
				observedVariable
			)
			else StatisticData_perValue(
				observedVariable,
				value
			)
			++statistic.count
			return statistic
		}
	}
}