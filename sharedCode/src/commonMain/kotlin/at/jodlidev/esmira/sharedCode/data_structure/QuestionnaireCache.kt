package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteInterface
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

/**
 * Created by JodliDev on 09.02.2021.
 */
object QuestionnaireCache {
	const val RESPONSES_BACKUP_VALID_TIMESPAN_MS = 1000*60L*60L // one hour
	
	const val TABLE = "questionnaireCache"
	const val KEY_ID = "_id"
	const val KEY_QUESTIONNAIRE_ID = "questionnaireId"
	const val KEY_INPUT_NAME = "inputName"
	const val KEY_BACKUP_FROM = "backupFrom"
	const val KEY_CACHE_VALUE = "cacheValue"
	
	const val FORM_STARTED_NAME = "~formStarted"
	
	internal fun loadCacheValue(questionnaireId: Long, inputName: String): String? {
		val db = NativeLink.sql
		val c = db.select(
			TABLE,
			arrayOf(KEY_BACKUP_FROM, KEY_CACHE_VALUE),
			"$KEY_QUESTIONNAIRE_ID = ? AND $KEY_INPUT_NAME = ?", arrayOf(questionnaireId.toString(), inputName),
			null,
			null,
			null,
			null
		)
		
		var r: String? = null
		if(c.moveToFirst()) {
			if(c.getLong(0) < NativeLink.getNowMillis() - RESPONSES_BACKUP_VALID_TIMESPAN_MS)
				clearCache(questionnaireId) //if one is out of date, we dont want any of them
			else
				r = c.getString(1)
		}
		c.close()
		return r;
	}
	internal fun saveCacheValue(questionnaireId: Long, inputName: String, cache: String) {
		val db = NativeLink.sql
		db.delete(TABLE, "$KEY_QUESTIONNAIRE_ID = ? AND $KEY_INPUT_NAME = ?", arrayOf(questionnaireId.toString(), inputName))
		val values = db.getValueBox()
		values.putLong(KEY_QUESTIONNAIRE_ID, questionnaireId)
		values.putString(KEY_INPUT_NAME, inputName)
		values.putLong(KEY_BACKUP_FROM, NativeLink.getNowMillis())
		values.putString(KEY_CACHE_VALUE, cache)
		db.insert(TABLE, values)
	}
	
	fun getFormStarted(questionnaireId: Long): Long {
		val value = loadCacheValue(questionnaireId, FORM_STARTED_NAME)
		return if(value != null)
			value.toLong()
		else {
			// it is possible that some cache entries are out of date while others arent yet.
			// But we can assume, that formStarted is always checked first. So it is always the oldest.
			// And loadCacheValue() calls clearCache(), so it deletes ALL entries if it is out of date
			
			val formStarted = NativeLink.getNowMillis()
			saveCacheValue(questionnaireId, FORM_STARTED_NAME, formStarted.toString())
			formStarted
		}
	}


	fun clearCache(questionnaireId: Long, db: SQLiteInterface = NativeLink.sql) {
		db.delete(TABLE, "$KEY_QUESTIONNAIRE_ID = ?", arrayOf(questionnaireId.toString()))
	}
	
	
	
//	private fun cacheExists(questionnaire: Questionnaire): Boolean {
//		val c = NativeLink.sql.select(
//			TABLE,
//			arrayOf(KEY_QUESTIONNAIRE_ID),
//			"$KEY_QUESTIONNAIRE_ID = ?", arrayOf(questionnaire.id.toString()),
//			null,
//			null,
//			null,
//			null
//		)
//
//		val r = c.moveToFirst()
//		c.close()
//		return r;
//	}
//
//	fun loadIntoQuestionnaire(questionnaire: Questionnaire): Long {
//		val db = NativeLink.sql
//		val c = db.select(
//			TABLE,
//			arrayOf(KEY_CACHE_VALUE),
//			"$KEY_QUESTIONNAIRE_ID = ?", arrayOf(questionnaire.toString()),
//			null,
//			null,
//			null,
//			null
//		)
//
//		if(c.moveToFirst()) {
//			val cache = c.getString(0)
//			c.close()
//
//
//
//			if(cache.isEmpty())
//				return NativeLink.getNowMillis()
//			val obj = DbLogic.getJsonConfig().decodeFromString<JsonObject>(cache)
//
//			val backupFrom = obj.getValue("backupFrom").jsonPrimitive.long
//
//			if(backupFrom < NativeLink.getNowMillis() - RESPONSES_BACKUP_VALID_TIMESPAN_MS) {
//				clearCache(questionnaire, db)
//				return NativeLink.getNowMillis()
//			}
//
//			val jsonPages = obj.getValue("pages").jsonArray
//			for((iPage, jsonPage) in jsonPages.withIndex()) {
//				val orderedInputs = questionnaire.pages[iPage].orderedInputs
//				for((iInput, jsonInput) in jsonPage.jsonArray.withIndex()) {
//					orderedInputs[iInput].fromBackupObj(jsonInput.jsonObject)
//				}
//			}
//			return obj.getValue("formStarted").jsonPrimitive.long
//		}
//		c.close()
//		return NativeLink.getNowMillis();
//	}
//	fun saveFromQuestionnaire(questionnaire: Questionnaire, formStarted: Long) {
//		val db = NativeLink.sql
//		val json = buildJsonObject {
//			put("backupFrom", NativeLink.getNowMillis())
//			put("formStarted", formStarted)
//			put("pages", buildJsonArray {
//				for(page in questionnaire.pages) {
//					add(buildJsonArray {
//						for(input in page.orderedInputs) {
//							add(input.getBackupJsonObj())
//						}
//					})
//				}
//			})
//		}
//
//		val responsesBackup = json.toString()
//		val values = db.getValueBox()
//		values.putString(KEY_CACHE_VALUE, responsesBackup)
//		if(cacheExists(questionnaire))
//			db.update(TABLE, values, "$KEY_QUESTIONNAIRE_ID = ?", arrayOf(questionnaire.id.toString()))
//		else
//			db.insert(TABLE, values)
//	}
}