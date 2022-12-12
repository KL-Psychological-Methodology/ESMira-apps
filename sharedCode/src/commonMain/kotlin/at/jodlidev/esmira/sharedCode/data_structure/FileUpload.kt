package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.FileOpener
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import kotlinx.serialization.*
import kotlinx.serialization.json.*


/**
 * Created by JodliDev on 17.04.2019.
 */
class FileUpload {
	enum class TYPES {
		Image
	}
	internal var id: Long = 0
	internal var webId: Long = 0
	internal val identifier: Int
	internal val serverUrl: String
	private var isTemporary = true
	private val filePath: String
	internal val type: TYPES
	
	internal val studyId: Long
	
	internal constructor(study: Study, filePath: String, type: TYPES) {
		this.studyId = study.id
		this.webId = study.webId
		this.serverUrl = study.serverUrl
		this.filePath = filePath
		this.identifier = (0 .. Int.MAX_VALUE).random()
		this.type = type
	}
	internal constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		studyId = c.getLong(1)
		webId = c.getLong(2)
		serverUrl = c.getString(3)
		isTemporary = c.getBoolean(4)
		filePath = c.getString(5)
		identifier = c.getInt(6)
		type = TYPES.values()[c.getInt(7)]
	}
	
	internal fun save() {
		if(id != 0L)
			throw RuntimeException("Trying to save an already created FileUpload")
		
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_STUDY_ID, studyId)
		values.putLong(KEY_STUDY_WEB_ID, webId)
		values.putString(KEY_SERVER_URL, serverUrl)
		values.putBoolean(KEY_IS_TEMPORARY, isTemporary)
		values.putString(KEY_FILE_PATH, filePath)
		values.putInt(KEY_IDENTIFIER, identifier)
		values.putInt(KEY_TYPE, type.ordinal)
		id = db.insert(TABLE, values)
	}
	
	internal fun setReadyForUpload() {
		if(id != 0L) {
			isTemporary = false
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putBoolean(KEY_IS_TEMPORARY, false)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		}
	}
	internal fun setTemporary() {
		if(id != 0L) {
			val db = NativeLink.sql
			val values = db.getValueBox()
			values.putBoolean(KEY_IS_TEMPORARY, true)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		}
	}
	internal fun delete() {
		if(!FileOpener.deleteFile(filePath)) {
			setTemporary()
			ErrorBox.error("FileOpener", "Could not delete temporary file $filePath ($identifier)")
			return
		}
		val db = NativeLink.sql
		db.delete(TABLE, "$KEY_ID = ?", arrayOf(id.toString()))
	}
	
	fun getFile(): ByteArray {
		return FileOpener.getFile(filePath)
	}
	fun getFileSize(): Long {
		return FileOpener.getFileSize(filePath)
	}
	
	companion object {
		const val TABLE = "fileUploads"
		
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_STUDY_WEB_ID = "study_webId"
		const val KEY_SERVER_URL = "server_url"
		const val KEY_IS_TEMPORARY = "isTemporary"
		const val KEY_FILE_PATH = "filePath"
		const val KEY_IDENTIFIER = "identifier"
		const val KEY_TYPE = "uploadType"

		val COLUMNS = arrayOf(
			"$TABLE.$KEY_ID",
			"$TABLE.$KEY_STUDY_ID",
			"$TABLE.$KEY_STUDY_WEB_ID",
			"$TABLE.$KEY_SERVER_URL",
			"$TABLE.$KEY_IS_TEMPORARY",
			"$TABLE.$KEY_FILE_PATH",
			"$TABLE.$KEY_IDENTIFIER",
			"$TABLE.$KEY_TYPE"
		)
	}
}