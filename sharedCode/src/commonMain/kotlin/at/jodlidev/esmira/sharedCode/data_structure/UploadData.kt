package at.jodlidev.esmira.sharedCode.data_structure

/**
 * Created by JodliDev on 14.03.2023.
 */
abstract class UploadData {
	enum class States {
		NOT_SYNCED,
		SYNCED,
		NOT_SYNCED_ERROR,
		NOT_SYNCED_ERROR_DELETABLE
	}
	
	abstract var id: Long
	abstract val studyId: Long
	abstract val studyWebId: Long
	abstract var synced: States
	abstract val timestamp: Long
	abstract val questionnaireName: String
	abstract val serverUrl: String
	abstract val serverVersion: Int
	
	abstract val type: String
	
	abstract fun delete()
	
	companion object {
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_STUDY_WEB_ID = "study_webId"
		const val KEY_SYNCED = "is_synced"
		const val KEY_SERVER_URL = "server_url"
		const val KEY_SERVER_VERSION = "server_version"
	}
}