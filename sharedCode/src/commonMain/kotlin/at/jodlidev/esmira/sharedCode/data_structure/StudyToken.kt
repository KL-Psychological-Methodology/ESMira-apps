package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink

/**
 * Created by JodliDev on 09.02.2021.
 */
class StudyToken(
	private val studyId: Long,
	private val token: Long
) {
	
	fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_TOKEN, token)
		values.putLong(KEY_STUDY_ID, studyId)

		if(hasToken(studyId))
			db.update(TABLE, values, "$KEY_STUDY_ID = ?", arrayOf(studyId.toString()))
		else
			db.insert(TABLE, values)
	}
	
	companion object {
		const val TABLE = "studyTokens"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_TOKEN = "server_token"

		val COLUMNS = arrayOf(
			KEY_STUDY_ID,
			KEY_TOKEN
		)
		
		fun hasToken(studyId: Long): Boolean {
			val c = NativeLink.sql.select(
				TABLE,
				arrayOf(KEY_TOKEN),
				"$KEY_STUDY_ID = ?", arrayOf(studyId.toString()),
				null,
				null,
				null,
				null
			)
			
			val r = c.moveToFirst()
			c.close()
			return r;
		}
	}
}