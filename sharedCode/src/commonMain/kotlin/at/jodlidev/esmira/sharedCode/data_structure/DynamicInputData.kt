package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor

/**
 * Created by JodliDev on 17.02.2020.
 */
class DynamicInputData {
	var exists = false
	var questionnaireId: Long
	var variable: String
	var index: Int
	var createdTime: Long = 0
	
	constructor(c: SQLiteCursor) {
		questionnaireId = c.getLong(0)
		variable = c.getString(1)
		index = c.getInt(2)
		createdTime = c.getLong(3)
		exists = true
	}
	
	constructor(questionnaireId: Long, variable: String, index: Int) {
		this.questionnaireId = questionnaireId
		this.variable = variable
		this.index = index
		renew()
	}
	
	fun renew() {
		createdTime = NativeLink.getNowMillis()
	}
	
	fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_QUESTIONNAIRE_ID, questionnaireId)
		values.putString(KEY_VARIABLE, variable)
		values.putInt(KEY_ITEM_INDEX, index)
		values.putLong(KEY_CREATED_TIME, createdTime)
		
		if(exists)
			db.update(
				TABLE,
				values,
				"$KEY_QUESTIONNAIRE_ID=? AND $KEY_VARIABLE=?",
				arrayOf(questionnaireId.toString(), variable)
			)
		else {
			db.insert(TABLE, values)
			exists = true
		}
	}
	
	
	companion object {
		const val TABLE = "dynamicInput_store"
		const val KEY_QUESTIONNAIRE_ID = "group_id"
		const val KEY_VARIABLE = "variable_name"
		const val KEY_ITEM_INDEX = "item_index"
		const val KEY_CREATED_TIME = "created_time"
		
		val COLUMNS = arrayOf(
			KEY_QUESTIONNAIRE_ID,
			KEY_VARIABLE,
			KEY_ITEM_INDEX,
			KEY_CREATED_TIME
		)
	}
}