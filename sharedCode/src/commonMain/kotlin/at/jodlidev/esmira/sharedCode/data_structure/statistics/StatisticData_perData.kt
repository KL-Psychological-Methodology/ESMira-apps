package at.jodlidev.esmira.sharedCode.data_structure.statistics

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.data_structure.ObservedVariable
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 22.08.2019.
 */
@Serializable
class StatisticData_perData(
	var index: Int,
	override var sum: Double, // used as value
	
) : StatisticData() {
	var value: Double by this::sum
	override var count: Int = 1 // not used
	
	
	constructor(valueIndex: Int, value: Double, variableName: String, index: Int, studyId: Long): this(valueIndex, value) {
		this.studyId = studyId
		this.observableIndex = index
		this.variableName = variableName
	}
	
	private constructor(observedVariable: ObservedVariable, valueIndex: Int, value: Double): this(valueIndex, value) {
		studyId = observedVariable.studyId
		observedId = observedVariable.id
		observableIndex = observedVariable.index
		variableName = observedVariable.variableName
	}
	
	constructor(c: SQLiteCursor): this(
		c.getInt(3),
		c.getDouble(4),
	) {
		loadCursor(c)
		observableIndex = c.getInt(5)
		variableName = c.getString(6)
	}
	
	constructor(c: SQLiteCursor, observedVariable: ObservedVariable): this(
		c.getInt(3),
		c.getDouble(4),
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
	}
	
	override fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putInt(KEY_INDEX, index)
		values.putDouble(KEY_VALUE, value)
		values.putLong(KEY_OBSERVED_ID, observedId)
		values.putLong(KEY_STUDY_ID, studyId)

		if(exists)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		else
			id = db.insert(TABLE, values)
	}
	
	override fun getType(): Int {
		return ObservedVariable.STORAGE_TYPE_FREQ_DISTR
	}
	
	companion object {
		const val TABLE = "statistics_perData"
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_OBSERVED_ID = "observed_id"
		const val KEY_INDEX = "variable_index"
		const val KEY_VALUE = "variable_value"

		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_STUDY_ID,
			KEY_OBSERVED_ID,
			KEY_INDEX,
			KEY_VALUE
		)

		const val TABLE_CONNECTED = "$TABLE LEFT JOIN ${ObservedVariable.TABLE} ON $TABLE.$KEY_OBSERVED_ID=${ObservedVariable.TABLE}.$KEY_ID"
		val COLUMNS_CONNECTED = arrayOf(
				"$TABLE.$KEY_ID",
				"$TABLE.$KEY_STUDY_ID",
				"$TABLE.$KEY_OBSERVED_ID",
				"$TABLE.$KEY_INDEX",
				"$TABLE.$KEY_VALUE",
				"${ObservedVariable.TABLE}.${ObservedVariable.KEY_INDEX}",
				"${ObservedVariable.TABLE}.${ObservedVariable.KEY_VARIABLE_NAME}"
		)
		
		fun getInstance(observedVariable: ObservedVariable, value: Double): StatisticData_perData {
			val c = NativeLink.sql.select(
				TABLE,
				arrayOf("COUNT(*)"),
				"$KEY_OBSERVED_ID = ?", arrayOf(observedVariable.id.toString()),
				null,
				null,
				null,
				"1"
			)
			return StatisticData_perData(
				observedVariable,
				if(c.moveToFirst()) c.getInt(0) else 0,
				value
			)
		}
	}
}