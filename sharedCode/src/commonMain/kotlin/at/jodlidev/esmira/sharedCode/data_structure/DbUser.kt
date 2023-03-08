package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink

object DbUser {
	internal const val TABLE = "user"
	internal const val KEY_ID = "_id"
	internal const val KEY_UID = "uid"
	internal const val KEY_NOTIFICATIONS_SETUP = "notifications_setup"
	internal const val KEY_IS_DEV = "is_dev"
	internal const val KEY_WAS_DEV = "was_dev"
	internal const val KEY_NOTIFICATIONS_MISSED = "notifications_missed"
	internal const val KEY_APP_LANG = "app_lang"
	internal const val KEY_CURRENT_STUDY = "current_study"
	
	
	
	fun getUid(): String {
		val c = NativeLink.sql.select(
			DbUser.TABLE,
			arrayOf(DbUser.KEY_UID),
			null, null,
			null,
			null,
			null,
			"1"
		)
		val uid: String
		if(c.moveToFirst())
			uid = c.getString(0)
		else {
			val chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz" //for readability, there is no I,l,oO,0
			var len = 12
			val charsRange = chars.indices
			
			val sb = StringBuilder(len)
			while(len > 0) {
				sb.append(chars[charsRange.random()])
				if(--len != 0 && len % 4 == 0)
					sb.append('-')
			}
			uid = sb.toString()
			
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putString(KEY_UID, uid)
			db.insert(TABLE, values)
			
			ErrorBox.log("UID", "Created new UID: $uid")
		}
		
		c.close()
		return uid
	}
	
	fun getCurrentStudyId(): Long { // should be used with the assumption that return value always has a valid id unless it is 0L
		if(DbLogic.hasNoStudies())
			return 0L
		val c = NativeLink.sql.select(
			TABLE,
			arrayOf(KEY_CURRENT_STUDY),
			null, null,
			null,
			null,
			null,
			"1"
		)
		val studyId = if(c.moveToFirst())
			c.getLong(0)
		else
			0L
		c.close()
		return if(studyId != 0L) studyId else DbLogic.getFirstStudy()?.id ?: 0L
	}
	
	fun setCurrentStudyId(studyId: Long) {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_CURRENT_STUDY, studyId)
		db.update(TABLE, values, null, null)
	}
	
	internal fun getAdminAppType(): String {
		val c = NativeLink.sql.select(
			TABLE,
			arrayOf(KEY_IS_DEV, KEY_WAS_DEV),
			null, null,
			null,
			null,
			null,
			"1"
		)
		
		val r = if(!c.moveToFirst())
			NativeLink.smartphoneData.appType
		else if(c.getBoolean(0))
			"${NativeLink.smartphoneData.appType}_dev"
		else if(c.getBoolean(1))
			"${NativeLink.smartphoneData.appType}_wasDev"
		else
			NativeLink.smartphoneData.appType
		
		c.close()
		return r
	}
	
	fun isDev(): Boolean {
		val c = NativeLink.sql.select(
			TABLE,
			arrayOf(KEY_IS_DEV),
			null, null,
			null,
			null,
			null,
			"1"
		)
		
		val r = c.moveToFirst() && c.getBoolean(0)
		c.close()
		return r
	}
	
	fun setDev(enabled: Boolean, pass: String = ""): Boolean {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putBoolean(KEY_IS_DEV, enabled)
		if(enabled) {
			if(pass != DbLogic.ADMIN_PASSWORD)
				return false
		}
		else
			values.putBoolean(KEY_WAS_DEV, true)
		db.update(TABLE, values, null, null)
		
		return true
	}
	
	fun getLang(): String {
		val c = NativeLink.sql.select(
			DbUser.TABLE,
			arrayOf(DbUser.KEY_APP_LANG),
			null, null,
			null,
			null,
			null,
			"1"
		)
		
		val r = if(c.moveToFirst()) c.getString(0) else ""
		c.close()
		return r
	}
	fun setLang(lang: String) {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putString(DbUser.KEY_APP_LANG, lang)
		db.update(DbUser.TABLE, values, null, null)
	}
}