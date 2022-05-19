package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.JsonToStringSerializer
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.data_structure.statistics.Condition
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_timed
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 22.08.2019.
 */
@Serializable
class ObservedVariable internal constructor() {
	@SerialName("conditions") @Serializable(with = JsonToStringSerializer::class) var conditionsJson: String = ""
	var conditionType: Int = Condition.TYPE_ALL
	var storageType: Int = STORAGE_TYPE_TIMED
	var timeInterval: Long = StatisticData_timed.ONE_DAY
	
	@Transient var id: Long = -1
	@Transient var index: Int = -1
	@Transient var studyId: Long = -1
	@Transient lateinit var variableName: String
	@Transient var exists = false
	
	internal constructor(c: SQLiteCursor): this() {
		id = c.getLong(0)
		studyId = c.getLong(1)
		index = c.getInt(2)
		variableName = c.getString(3)
		conditionsJson = c.getString(4)
		conditionType = c.getInt(5)
		storageType = c.getInt(6)
		timeInterval = c.getLong(7)
		exists = true
	}
	
	internal fun finishJSON(study: Study, index: Int, variableName: String) {
		this.studyId = study.id
		this.index = index
		this.variableName = variableName
		
		val c = NativeLink.sql.select(
			TABLE, arrayOf(KEY_ID),
			"$KEY_STUDY_ID=? AND $KEY_VARIABLE_NAME=? AND $KEY_INDEX=?",
			arrayOf(studyId.toString(), variableName, index.toString()),
			null,
			null,
			null,
			"1"
		)
		if(c.moveToFirst()) {
			this.id = c.getLong(0)
			this.exists = true
		}
	}
	
	fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_STUDY_ID, studyId)
		values.putInt(KEY_INDEX, index)
		values.putString(KEY_VARIABLE_NAME, variableName)
		values.putString(KEY_CONDITIONS_JSON, conditionsJson)
		values.putInt(KEY_CONDITION_TYPE, conditionType)
		values.putInt(KEY_STORAGE_TYPE, storageType)
		values.putLong(KEY_TIME_INTERVAL, timeInterval)
		if(exists)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		else {
			id = db.insert(TABLE, values)
			exists = true
		}
	}
	
	private fun stringToDouble(s: String): Double {
		return try {
			s.toDouble()
		}
		catch(e: Exception) {
			0.0
		}
	}
	
	//internal for testing
	internal fun checkCondition(responses: Map<String, JsonElement>): Boolean {
		//the logic of this function is pretty slow and the same condition-JSON is interpreted every time - but it should not matter
		if(conditionType == Condition.TYPE_ALL)
			return true
		
		val conditions = DbLogic.getJsonConfig().decodeFromString<List<Condition>>(conditionsJson)
		val conditionTypeIsOr = conditionType == Condition.TYPE_OR
		val conditionTypeIsAnd = conditionType == Condition.TYPE_AND
		val conditionIsMet = !conditionTypeIsOr
		
		
		for(condition in conditions) {
			val rawResponse = responses[condition.key]!!
			//Note: JsonPrimitive.toString() adds quotes around value. So we get the value directly
			val response = if(rawResponse.jsonPrimitive.doubleOrNull != null) rawResponse.jsonPrimitive.double.toString() else rawResponse.jsonPrimitive.content
			val conditionValue = if(condition.value.toDoubleOrNull() != null) condition.value.toDouble().toString() else condition.value
			
			val isTrue = when(condition.operator) {
				Condition.OPERATOR_EQUAL -> response == conditionValue
				Condition.OPERATOR_UNEQUAL -> response != conditionValue
				Condition.OPERATOR_GREATER -> stringToDouble(response) >= stringToDouble(conditionValue)
				Condition.OPERATOR_LESS -> stringToDouble(response) <= stringToDouble(conditionValue)
				else -> true
			}
			if(isTrue) {
				if(conditionTypeIsOr) {
					return true
				}
			}
			else if(conditionTypeIsAnd) {
				return false
			}
		}
		
		return conditionIsMet
	}
	
	fun createStatistic(responses: Map<String, JsonElement>) {
		if(checkCondition(responses)) {
			when(storageType) {
				STORAGE_TYPE_TIMED -> {
					val content = responses[variableName]?.jsonPrimitive?.content ?: ""
					val num = try {if(content.isEmpty()) 0.0 else content.toDouble()} catch(e: Exception) {0.0}
					StatisticData_timed.getInstance(this, num).save()
				}
				STORAGE_TYPE_FREQ_DISTR ->
					//Note: JsonPrimitive.toString() adds quotes around value. So we get the value directly
					StatisticData_perValue.getInstance(this, responses[variableName]?.jsonPrimitive?.content ?: "").save()
			}
		}
	}
	
	companion object {
		const val TABLE = "observed_variables"
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_INDEX = "position_index"
		const val KEY_VARIABLE_NAME = "variable_name"
		const val KEY_CONDITIONS_JSON = "conditions_json"
		const val KEY_CONDITION_TYPE = "conditionType"
		const val KEY_STORAGE_TYPE = "storageType"
		const val KEY_TIME_INTERVAL = "timeInterval"

		const val STORAGE_TYPE_TIMED = 0
		const val STORAGE_TYPE_FREQ_DISTR = 1

		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_STUDY_ID,
			KEY_INDEX,
			KEY_VARIABLE_NAME,
			KEY_CONDITIONS_JSON,
			KEY_CONDITION_TYPE,
			KEY_STORAGE_TYPE,
			KEY_TIME_INTERVAL
		)
	}
}