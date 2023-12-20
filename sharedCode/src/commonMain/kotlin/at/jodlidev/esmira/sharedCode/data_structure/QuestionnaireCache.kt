package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteInterface

/**
 * Created by JodliDev on 09.02.2021.
 */
object QuestionnaireCache {
//	const val RESPONSES_BACKUP_VALID_TIMESPAN_MS = 1000*60L*60L // one hour
	const val RESPONSES_BACKUP_VALID_TIMESPAN_MS = 1000*30L // one hour
	
	const val TABLE = "questionnaireCache"
	const val KEY_ID = "_id"
	const val KEY_QUESTIONNAIRE_ID = "questionnaireId"
	const val KEY_INPUT_NAME = "inputName"
	const val KEY_BACKUP_FROM = "backupFrom"
	const val KEY_CACHE_VALUE = "cacheValue"
	
	const val FORM_STARTED = "~formStarted"
	const val FORM_PAGE = "~formPage"
	const val FORM_PAGE_TIMESTAMPS = "~formPageTimestamp"
	
	internal fun loadCacheValue(questionnaireId: Long, inputName: String, deleteOutdated: Boolean = false): String? {
		val db = NativeLink.sql
		val c = db.select(
			TABLE,
			arrayOf(KEY_BACKUP_FROM, KEY_CACHE_VALUE),
			"$KEY_QUESTIONNAIRE_ID = ? AND $KEY_INPUT_NAME = ?", arrayOf(questionnaireId.toString(), inputName),
			null,
			null,
			null,
			"1"
		)
		
		var r: String? = null
		if(c.moveToFirst()) {
			if(deleteOutdated && c.getLong(0) < NativeLink.getNowMillis() - RESPONSES_BACKUP_VALID_TIMESPAN_MS)
				clearCache(questionnaireId) //FORM_STARTED will be the first one out of date. That makes the rest useless, so we delete all
			else
				r = c.getString(1)
		}
		c.close()
		return r;
	}
	
	/**
	 * Checks if FORM_STARTED exists.
	 */
	private fun cacheAcceptsValues(questionnaireId: Long): Boolean {
		val db = NativeLink.sql
		val c = db.select(
			TABLE,
			arrayOf(KEY_QUESTIONNAIRE_ID),
			"$KEY_QUESTIONNAIRE_ID = ? AND $KEY_INPUT_NAME = ?", arrayOf(questionnaireId.toString(), FORM_STARTED),
			null,
			null,
			null,
			"1"
		)
		val r = c.moveToFirst()
		c.close()
		return r
	}
	
	/**
	 * Only saves values if FORM_STARTED (which is always the first value in cache) exists or is saved right now.
	 * This prevents automatic items from saving values after the questionnaire was already sent on view updates
	 * (at least on Android, when closing the questionnaire, views are animated which leads to the last page being rerendered
	 * and items being reloaded AFTER the questionnaire was saved and the cache should have been emptied)
	 */
	internal fun saveCacheValue(questionnaireId: Long, inputName: String, cache: String) {
		if(inputName != FORM_STARTED && !cacheAcceptsValues(questionnaireId)) {
			println("Cache is disabled. Not saving $inputName for questionnaire $questionnaireId with the value $cache")
			return
		}
		val db = NativeLink.sql
		db.delete(TABLE, "$KEY_QUESTIONNAIRE_ID = ? AND $KEY_INPUT_NAME = ?", arrayOf(questionnaireId.toString(), inputName))
		val values = db.getValueBox()
		values.putLong(KEY_QUESTIONNAIRE_ID, questionnaireId)
		values.putString(KEY_INPUT_NAME, inputName)
		values.putLong(KEY_BACKUP_FROM, NativeLink.getNowMillis())
		values.putString(KEY_CACHE_VALUE, cache)
		db.insert(TABLE, values)
	}
	
	/**
	 * Is used when the questionnaire was opened. So we can use it to invalidate an outdated cache
	 */
	fun saveFormStarted(questionnaireId: Long) {
		if(loadCacheValue(questionnaireId, FORM_STARTED, true) == null)
			saveCacheValue(questionnaireId, FORM_STARTED, NativeLink.getNowMillis().toString())
	}
	fun getFormStarted(questionnaireId: Long): Long {
		return loadCacheValue(questionnaireId, FORM_STARTED)?.toLong() ?: NativeLink.getNowMillis()
	}
	
	fun savePage(questionnaireId: Long, pageNumber: Int) {
		saveCacheValue(questionnaireId, FORM_PAGE, pageNumber.toString())
		val timestamps = getPageTimestamps(questionnaireId).toMutableList()
		if(pageNumber > timestamps.size) {
			for(i in timestamps.size until pageNumber) {
				timestamps.add(NativeLink.getNowMillis())
			}
		}
		else
			timestamps[pageNumber-1] = NativeLink.getNowMillis()
		saveCacheValue(questionnaireId, FORM_PAGE_TIMESTAMPS, timestamps.joinToString(","))
	}
	fun getPage(questionnaireId: Long): Int {
		return loadCacheValue(questionnaireId, FORM_PAGE)?.toInt() ?: 0
	}
	fun getPageTimestamps(questionnaireId: Long): List<Long> {
		val value = loadCacheValue(questionnaireId, FORM_PAGE_TIMESTAMPS) ?: return ArrayList()
		
		return value.split(",").map { it.toLongOrNull() ?: 0L }
	}


	fun clearCache(questionnaireId: Long, db: SQLiteInterface = NativeLink.sql) {
		db.delete(TABLE, "$KEY_QUESTIONNAIRE_ID = ?", arrayOf(questionnaireId.toString()))
	}
}