package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.FileOpener
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor


/**
 * Created by JodliDev on 17.04.2019.
 */
class FileUpload: UploadData {
	enum class DataTypes {
		Image,
		Audio
	}
	override var id: Long = 0
	override var studyWebId: Long = 0
	override val studyId: Long
	override val timestamp: Long
		get() = creationTime
	override val questionnaireName: String = "" //not used except for UploadProtocolView
	override val serverUrl: String
	override val serverVersion: Int
	
	internal val dataType: DataTypes
	override val type: String
		get() = dataType.toString()
	
	internal val identifier: Int
	private var isTemporary = true
	internal val filePath: String
	private val creationTime: Long
	
	private var _synced = States.NOT_SYNCED
	override var synced: States //this value will be updated in db immediately
		get() {
			return _synced
		}
		set(v) {
			_synced = v
			if(id != 0L) {
				val db = NativeLink.sql
				val values = db.getValueBox()
				values.putInt(KEY_SYNCED, _synced.ordinal)
				db.update(TABLE, values, "${KEY_ID} = ?", arrayOf(id.toString()))
			}
		}
	
	internal constructor(study: Study, filePath: String, type: DataTypes) {
		this.studyId = study.id
		this.studyWebId = study.webId
		this.serverUrl = study.serverUrl
		this.serverVersion = study.serverVersion
		this.filePath = filePath
		this.identifier = (0 .. Int.MAX_VALUE).random()
		this.dataType = type
		this.creationTime = NativeLink.getNowMillis()
	}
	internal constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		studyId = c.getLong(1)
		studyWebId = c.getLong(2)
		serverUrl = c.getString(3)
		serverVersion = c.getInt(4)
		isTemporary = c.getBoolean(5)
		filePath = c.getString(6)
		identifier = c.getInt(7)
		dataType = DataTypes.values()[c.getInt(8)]
		creationTime = c.getLong(9)
		_synced = States.values()[c.getInt(10)]
	}
	
	internal fun isTooOld(): Boolean {
		return creationTime + MAX_TEMPORARY_AGE < NativeLink.getNowMillis()
	}
	
	internal fun save() {
		if(id != 0L)
			throw RuntimeException("Trying to save an already created FileUpload")
		
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_STUDY_ID, studyId)
		values.putLong(KEY_STUDY_WEB_ID, studyWebId)
		values.putString(KEY_SERVER_URL, serverUrl)
		values.putInt(KEY_SERVER_VERSION, serverVersion)
		values.putBoolean(KEY_IS_TEMPORARY, isTemporary)
		values.putString(KEY_FILE_PATH, filePath)
		values.putInt(KEY_IDENTIFIER, identifier)
		values.putInt(KEY_TYPE, dataType.ordinal)
		values.putLong(KEY_CREATION_TIME, creationTime)
		values.putInt(KEY_SYNCED, _synced.ordinal)
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
	override fun delete() {
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
		const val MAX_TEMPORARY_AGE = 86400000
		const val TABLE = "fileUploads"
		
		const val KEY_ID = UploadData.KEY_ID
		const val KEY_STUDY_ID = UploadData.KEY_STUDY_ID
		const val KEY_STUDY_WEB_ID = UploadData.KEY_STUDY_WEB_ID
		const val KEY_SERVER_URL = UploadData.KEY_SERVER_URL
		const val KEY_SERVER_VERSION = UploadData.KEY_SERVER_VERSION
		const val KEY_IS_TEMPORARY = "isTemporary"
		const val KEY_FILE_PATH = "filePath"
		const val KEY_IDENTIFIER = "identifier"
		const val KEY_TYPE = "uploadType"
		const val KEY_CREATION_TIME = "creationTimestamp"
		const val KEY_SYNCED = UploadData.KEY_SYNCED

		val COLUMNS = arrayOf(
			"$TABLE.$KEY_ID",
			"$TABLE.$KEY_STUDY_ID",
			"$TABLE.$KEY_STUDY_WEB_ID",
			"$TABLE.$KEY_SERVER_URL",
			"$TABLE.${KEY_SERVER_VERSION}",
			"$TABLE.$KEY_IS_TEMPORARY",
			"$TABLE.$KEY_FILE_PATH",
			"$TABLE.$KEY_IDENTIFIER",
			"$TABLE.$KEY_TYPE",
			"$TABLE.$KEY_CREATION_TIME",
			"$TABLE.$KEY_SYNCED"
		)
	}
}