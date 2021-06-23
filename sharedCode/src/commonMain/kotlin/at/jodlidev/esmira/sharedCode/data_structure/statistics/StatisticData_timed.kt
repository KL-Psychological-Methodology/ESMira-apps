package at.jodlidev.esmira.sharedCode.data_structure.statistics

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.ObservedVariable
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 22.08.2019.
 */
@Serializable
class StatisticData_timed internal constructor() : StatisticData() {
	@Transient var dayTimestampSec: Long = 0
	override var sum: Double = 0.0
	override var count: Int = 0
	
	constructor(dayTimestamp: Long, variableName: String, index: Int): this() {
		this.dayTimestampSec = dayTimestamp
		this.observableIndex = index
		this.variableName = variableName
	}
	
	private constructor(observedVariable: ObservedVariable, dayTimestamp: Long): this() {
		studyId = observedVariable.studyId
		observedId = observedVariable.id
		dayTimestampSec = dayTimestamp
		observableIndex = observedVariable.index
		variableName = observedVariable.variableName
	}
	
	constructor(c: SQLiteCursor): this() {
		loadCursor(c)
		observableIndex = c.getInt(6)
		variableName = c.getString(7)
	}
	
	constructor(c: SQLiteCursor, observedVariable: ObservedVariable): this() {
		loadCursor(c)
		observableIndex = observedVariable.index
		variableName = observedVariable.variableName
	}
	
	fun addData(dayTimestamp: Long, variableName: String, index: Int, studyId: Long) {
		this.studyId = studyId
		dayTimestampSec = dayTimestamp
		observableIndex = index
		this.variableName = variableName
	}
	
	private fun loadCursor(c: SQLiteCursor) {
		exists = true
		id = c.getLong(0)
		studyId = c.getLong(1)
		observedId = c.getLong(2)
		dayTimestampSec = c.getLong(3)
		sum = c.getDouble(4)
		count = c.getInt(5)
	}
	
	override fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_TIMESTAMP, dayTimestampSec)
		values.putLong(KEY_OBSERVED_ID, observedId)
		values.putLong(KEY_STUDY_ID, studyId)
		values.putDouble(KEY_SUM, sum)
		values.putInt(KEY_COUNT, count)

		if(exists)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		else
			id = db.insert(TABLE, values)
	}
	
	override fun getType(): Int {
		return ObservedVariable.STORAGE_TYPE_TIMED
	}
	
	companion object {
		const val TABLE = "statistics_timed"
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_OBSERVED_ID = "observed_id"
		const val KEY_TIMESTAMP = "reduced_timestamp"
		const val KEY_SUM = "variable_sum"
		const val KEY_COUNT = "variable_count"

		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_STUDY_ID,
			KEY_OBSERVED_ID,
			KEY_TIMESTAMP,
			KEY_SUM,
			KEY_COUNT
		)
		const val TABLE_CONNECTED = "$TABLE LEFT JOIN ${ObservedVariable.TABLE} ON $TABLE.$KEY_OBSERVED_ID=${ObservedVariable.TABLE}.$KEY_ID"
		val COLUMNS_CONNECTED = arrayOf(
				"$TABLE.$KEY_ID",
				"$TABLE.$KEY_STUDY_ID",
				"$TABLE.$KEY_OBSERVED_ID",
				"$TABLE.$KEY_TIMESTAMP",
				"$TABLE.$KEY_SUM",
				"$TABLE.$KEY_COUNT",
				"${ObservedVariable.TABLE}.${ObservedVariable.KEY_INDEX}",
				"${ObservedVariable.TABLE}.${ObservedVariable.KEY_VARIABLE_NAME}"
		)
		
		const val ONE_DAY: Long = 86400 //60*60*24
		fun getInstance(observedVariable: ObservedVariable, data: Double): StatisticData_timed {
			val today = NativeLink.getNowMillis() / 1000 / observedVariable.timeInterval * observedVariable.timeInterval
			ErrorBox.log("StatisticsTimed", "Loading instance for $today")
			val c = NativeLink.sql.select(
				TABLE,
				COLUMNS,
				"$KEY_OBSERVED_ID = ? AND $KEY_TIMESTAMP = ?", arrayOf(observedVariable.id.toString(), today.toString()),
				null,
				null,
				null,
				"1"
			)
			val statistic = if(c.moveToFirst()) StatisticData_timed(
				c,
				observedVariable
			)
			else StatisticData_timed(
				observedVariable,
				today
			)
			statistic.sum += data
			++statistic.count
			return statistic
		}
		
		//make sure all dataSets start with the same day
		fun correctEntriesToFirstDay(dataListContainer: Map<String, MutableList<StatisticData>>, firstDay: Long) { //make sure all dataSets start with the same day
			for((_, list) in dataListContainer) {
				if(list.size == 0)
					continue
				
				val currentFirstEntry = list[0] as? StatisticData_timed
					?: continue
				
				val index = currentFirstEntry.observableIndex
				val key = currentFirstEntry.variableName
				var currentDayTimestampSec = currentFirstEntry.dayTimestampSec
				
				while(currentDayTimestampSec > firstDay) {
					currentDayTimestampSec -= ONE_DAY
					list.add(0,
						StatisticData_timed(
							currentDayTimestampSec,
							key,
							index
						)
					)
				}
			}
		}
	}
}