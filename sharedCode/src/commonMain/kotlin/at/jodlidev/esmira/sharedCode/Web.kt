package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.*
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.ClientEngineClosedException
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlin.jvm.Synchronized

/**
 * Created by JodliDev on 27.05.2020.
 */
class Web {
	var error = false
	private val client = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				encodeDefaults = true
				ignoreUnknownKeys = true
			})
		}
		install(HttpTimeout) {
			requestTimeoutMillis = 300000
			connectTimeoutMillis = 15000
			socketTimeoutMillis = 15000
		}
	}
	private suspend fun get(url: String): String {
		val interpreter: GetStructure = client.get(url).body()
		checkResponse(interpreter)
		return interpreter.dataset
	}
	
	private suspend fun getGenericJson(url: String): HttpResponse {
		return client.get(url)
	}
	private suspend fun postJson(url: String, data: PostStructure): String {
		println("postJson to: $url")
		val interpreter: GetStructure = client.post(url) {
			contentType(ContentType.Application.Json)
			setBody(data)
		}.body()
		checkResponse(interpreter)
		return interpreter.dataset
	}
	private suspend fun postString(url: String, data: String): String {
		println("postString to: $url")
		val interpreter: GetStructure = client.post(url) {
			setBody(data)
		}.body()
		checkResponse(interpreter)
		return interpreter.dataset
	}
	private suspend fun postFile(url: String, fileUpload: FileUpload): String {
		println("postFile to: $url")
		val file = fileUpload.getFile()

		val interpreter: GetStructure = client.submitFormWithBinaryData(
			url = url,
			formData = formData {
				append("userId", DbUser.getUid())
				append("studyId", fileUpload.studyWebId)
				append("dataType", fileUpload.dataType.toString())
				append("appVersion", NativeLink.smartphoneData.appVersion)
				append("appType", DbUser.getAdminAppType())
				append("serverVersion", Updater.EXPECTED_SERVER_VERSION)

				append("upload", file, Headers.build {
					when(fileUpload.dataType) {
						FileUpload.DataTypes.Image ->
							append(HttpHeaders.ContentType, "image/png")
						FileUpload.DataTypes.Audio ->
							append(HttpHeaders.ContentType, "video/mp4")
					}
					append(HttpHeaders.ContentDisposition, "filename=${fileUpload.identifier}")
				})
			}
		).body()
		
		checkResponse(interpreter)
		return interpreter.dataset
	}
	
	private fun checkResponse(interpreter: GetStructure) {
		if(interpreter.serverVersion > Updater.EXPECTED_SERVER_VERSION) {
			NativeLink.dialogOpener.updateNeeded()
			ErrorBox.warn("Web", "This app may be outdated (server version ${interpreter.serverVersion}, local version ${Updater.EXPECTED_SERVER_VERSION})")
		}
		if(!interpreter.success)
			throw SuccessFailedException(interpreter.error, interpreter.errorCode)
	}
	
	fun cancel() {
		client.cancel()
		close()
	}
	private fun close() = client.close()
	
	//internal for testing:
	internal fun getStudyInfoMapForUpdates(forceStudyUpdate: Boolean): Map<String, Map<String, StudyInfo>> {
		ErrorBox.log("Web", "Searching for updated studies...")
		val studies = DbLogic.getJoinedStudies()
		val container: MutableMap<String, MutableMap<String, StudyInfo>> = HashMap()
		
		if(studies.isEmpty()) {
			ErrorBox.log("Web", "No more studies. Canceling")
			NativeLink.postponedActions.cancelUpdateStudiesRegularly()
		}
		
		//sort studies by server:
		for(study in studies) {
			study.leaveAfterCheck() //check if we should leave study
			if(study.state != Study.STATES.Joined)
				continue
			
			val studyInfoList: MutableMap<String, StudyInfo>
			if(!container.containsKey(study.serverUrl)) {
				studyInfoList = HashMap()
				container[study.serverUrl] = studyInfoList
			}
			else
				studyInfoList = container[study.serverUrl]!!
			
			studyInfoList[study.webId.toString()] = StudyInfo(
				version = study.version,
				msgTimestamp = study.msgTimestamp,
				accessKey = study.accessKey,
				forceStudyUpdate = forceStudyUpdate
			)
		}
		
		return container
	}
	internal fun processStudyUpdateResponse(url: String, response: String): Int {
		var updatedCount = 0
		println("Decoding update response: $response")
		val updateInfoList = DbLogic.getJsonConfig().decodeFromString<Map<String, UpdateInfo>>(response)
		
		for((idString, updateInfo) in updateInfoList) {
			ErrorBox.log("Updating studies", "Got reply for $idString")
			
			val study: Study? = DbLogic.getStudy(url, idString.toLong())
			if(study == null) {
				ErrorBox.error("update_studies", "Server ($url) answered with an unknown study id: $idString")
				error = true
				continue
			}
			
			if(updateInfo.errorCode != null) {
				if(updateInfo.errorCode == "wrongAccessKey")
					study.saveFaultyAccessKeyState(true)
				else
					ErrorBox.warn("Study update", "Server ($url) responded with unknown errorCode: ${updateInfo.errorCode}")
			}
			
			val newStudyJson = updateInfo.study
			if(newStudyJson != null) {
				ErrorBox.log("Updating studies", "Found study update")
				val newStudy: Study
				try {
					newStudy = Study.newInstance(study.serverUrl, study.accessKey, newStudyJson)
				}
				catch(e: Throwable) {
					ErrorBox.warn("Study Update", "New JSON is faulty: $newStudyJson", e)
					println(newStudyJson)
					continue
				}
				study.updateWith(newStudy)
				++updatedCount
			}
			
			if(updateInfo.msgs.isNotEmpty()) {
				var latest = -1L
				for(msg in updateInfo.msgs) {
					Message.addMessage(study.id, msg.content, msg.sent, true)
					if(msg.sent > latest)
						latest = msg.sent
				}
				ErrorBox.log("Message", "Found ${updateInfo.msgs.size} messages for study ${study.id}")
				DataSet.createShortDataSet(DataSet.EventTypes.study_message, study)
				if(latest != -1L)
					study.saveMsgTimestamp(latest)
				NativeLink.notifications.fireMessageNotification(study)
			}
		}
		
		return updatedCount
	}
	internal fun processSyncData(url: String, response: String) {
		println("Decoding response: $response")
		try {
			val syncInfo = DbLogic.getJsonConfig().decodeFromString<SyncDataSetResponse>(response)
			
			for(syncState in syncInfo.states) {
				val dataSet = DbLogic.getDataSet(syncState.dataSetId)
				
				if(syncState.success && dataSet != null)
					dataSet.synced = UploadData.States.SYNCED
				else {
					error = true
					dataSet?.synced = UploadData.States.NOT_SYNCED_ERROR_DELETABLE
					ErrorBox.warn("Syncing failed", "Syncing DataSet(server_url:${url}, id:${syncState.dataSetId}) was not successful:\n${syncState.error.ifEmpty { "No server message" }}")
				}
			}
			
			for((studyId, token) in syncInfo.tokens) {
				StudyToken(studyId, token).save()
			}
		}
		catch(e: Throwable) {
			ErrorBox.warn("Syncing failed", "JSON structure is faulty: $response", e)
		}
	}
	
	private suspend fun updateStudies(forceStudyUpdate: Boolean): Int {
		if(NativeLink.isUpdating)
			return 0
		NativeLink.isUpdating = true
		var updatedCount = 0
		try {
			val map = getStudyInfoMapForUpdates(forceStudyUpdate)
			
			//do updates:
			for((url, studyInfo) in map) {
				ErrorBox.log("Updating studies", "Updating $url (${studyInfo.size} studies)")
				val response: String
				try {
					response = postJson(
						"$url${URL_UPDATE_STUDY.replace("%s", NativeLink.smartphoneData.lang)}",
						PostStructure.UpdateStructure(studyInfo)
					)
				}
				catch(e: Throwable) {
					ErrorBox.warn("Updating studies failed", "Could not update studies for $url", e)
					error = true
					continue
				}
				
				updatedCount += processStudyUpdateResponse(url, response)
			}
			
			close()
		}
		catch(e: Throwable) {
			ErrorBox.error("Update Studies", "Unexpected error", e)
			error = true
		}
		finally {
			NativeLink.isUpdating = false
		}
		return if(error) -1 else updatedCount
	}
	private suspend fun syncDataSets(): Boolean {
		if(NativeLink.isSynchronizing)
			return true
		NativeLink.isSynchronizing = true
		delay(200) //in case there are multiple datasets being saved
		try {
			val container = DbLogic.getUnSyncedDataSets()
			for((url, dataSetList) in container) {
				ErrorBox.log("Syncing", "Syncing $url (${dataSetList.size} dataSets)")
				val response: String
				try {
					response = postJson("$url$URL_UPLOAD_DATASET", PostStructure.SyncStructure(dataSetList))
				} catch(e: Throwable) {
					ErrorBox.warn("Syncing failed", "Could not sync ${dataSetList.size} dataSets to $url", e)
					
					for(dataSet in dataSetList) { //mark error on all dataSets that were not synced
						if(dataSet.synced != UploadData.States.SYNCED && (dataSet.serverUrl == url))
							dataSet.synced = UploadData.States.NOT_SYNCED_ERROR
					}
					error = true
					continue
				}
				
				processSyncData(url, response)
			}
			
			error = syncFiles() || error
			close()
		}
		catch(e: Throwable) {
			ErrorBox.error("Sync data", "Unexpected error", e)
			error = true
		}
		finally {
			NativeLink.isSynchronizing = false
		}
		return !error
	}
	private suspend fun syncFiles(): Boolean {
		val fileUploads = DbLogic.getPendingFileUploads()
		ErrorBox.log("FileUpload", "Found ${fileUploads.size} files for uploading")
		for(fileUpload in fileUploads) {
			val url = fileUpload.serverUrl
			ErrorBox.log("FileUpload", "Uploading ${fileUpload.identifier} to $url for study ${fileUpload.studyId}")
			try {
				postFile("$url$URL_UPLOAD_FILE", fileUpload)
			}
			catch(e: Throwable) {
				ErrorBox.warn("Syncing failed", "Could not upload ${fileUpload.identifier} to $url (studyId: ${fileUpload.studyId})", e)
				fileUpload.synced = UploadData.States.NOT_SYNCED_ERROR_DELETABLE // we also make it deletable if the file could not be loaded
				
				error = true
				continue
			}
			
			fileUpload.delete()
		}
		
		return error
	}
	
	private suspend fun sendMessage(content: String, study: Study): String? {
		try {
			postJson(
				"${study.serverUrl}${URL_UPLOAD_MESSAGE.replace("%s", NativeLink.smartphoneData.lang)}",
				PostStructure.MessageStructure(content, study.webId)
			)
		}
		catch(e: Throwable) {
			ErrorBox.warn("Web", "Could not send message", e)
			return e.message ?: "Error"
		}
		close()
		Message.addMessage(study.id, content, NativeLink.getNowMillis())
		return null
	}
	private suspend fun sendErrorReport(comment: String?): String? {
		val output = ErrorBox.getReportHeader(comment)
		
		output.append("\n\n")
		
		//errors:
		for(error in DbLogic.getErrors()) {
			output.append(error.export())
			output.append("\n\n")
		}
		
		try {
			postString(DEV_SERVER + URL_UPLOAD_ERRORBOX, output.toString())
		}
		catch(e: Throwable) {
			ErrorBox.warn("ErrorReport", "Could not upload error report", e)
			return e.message ?: "Error"
		}
		close()
		return null
	}
	
	companion object {
		const val DEV_SERVER = "https://esmira.kl.ac.at"
		//private const val DEBUG_EMULATOR_SERVER = "http://10.0.2.2/smartphones/ESMira/git/ESMira-web/dist"
		private const val DEBUG_EMULATOR_SERVER = "http://10.0.2.2/esmira"

		private const val URL_LIST_STUDIES: String = "/api/studies.php?lang=%s"
		private const val URL_LIST_STUDIES_PASSWORD: String = "/api/studies.php?access_key=%s1&lang=%s2"
		private const val URL_PUBLIC_STATISTICS: String = "/api/statistics.php?id=%d&access_key=%s"
		private const val URL_UPDATE_STUDY: String = "/api/update.php?lang=%s"
		private const val URL_UPLOAD_DATASET: String = "/api/datasets.php"
		private const val URL_UPLOAD_FILE: String = "/api/file_uploads.php"
		private const val URL_UPLOAD_ERRORBOX: String = "/api/save_errors.php"
		private const val URL_UPLOAD_MESSAGE: String = "/api/save_message.php?lang=%s"
		private const val URL_REWARD: String = "/api/reward.php"
		
		private const val DOMAIN_DONT_KILL_MY_APP: String = "https://dontkillmyapp.com"
		private const val URL_DONT_KILL_MY_APP: String = "$DOMAIN_DONT_KILL_MY_APP/api/v2/%s.json"
		
		
		val serverList: List<Pair<String, String>> get() {
			return (if(DbUser.isDev()) {
				at.jodlidev.esmira.sharedCode.serverList + debugServerList
			}
			else
				at.jodlidev.esmira.sharedCode.serverList).asList()
		}
		
		internal class SuccessFailedException(msg: String, val errorCode: Int = 0) : Throwable(msg.ifEmpty { "Failed with empty response from server" })
		
		private val debugServerList = arrayOf(
			Pair("Emulator Server", DEBUG_EMULATOR_SERVER),
			Pair("Test Server", "https://esmira.jodli.dev")
		)
		
		@Serializable
		class StudyInfo(
			val version: Int,
			val msgTimestamp: Long,
			val accessKey: String,
			val forceStudyUpdate: Boolean = false
		)
		
		@Serializable
		private class SyncDataSetResponse (
			val states: List<DataSetResponseStates>,
			val tokens: Map<Long, Long>
		) {
			@Serializable
			class DataSetResponseStates(
				val dataSetId: Long,
				val success: Boolean,
				val error: String = "",
			)
		}
		
		@Serializable
		class RewardInfo(
			val code: String = "",
			val errorMessage: String = "",
			val errorCode: Int = 0,
			val fulfilledQuestionnaires: Map<Long, Boolean> = HashMap()
		)
		
		
		@Serializable
		private class UpdateInfo(
			@Serializable(with = JsonToStringSerializer::class)
			var study: String? = null,
			val msgs: List<MsgInfo> = ArrayList(),
			val errorCode: String? = null,
		)
		
		@Serializable
		private class MsgInfo(
			val content: String,
			val sent: Long
		)
		
		@Serializable
		private class GetStructure(
			val success: Boolean,
			val serverVersion: Int,
			val error: String = "",
			val errorCode: Int = 0,
			@Serializable(with = JsonToStringSerializer::class) val dataset: String = ""
		)
		
		@Serializable
		sealed class PostStructure {
			val userId: String = DbUser.getUid()
			val appVersion: String = NativeLink.smartphoneData.appVersion
			val appType: String = DbUser.getAdminAppType()
			val serverVersion: Int = Updater.EXPECTED_SERVER_VERSION
			
			@Serializable
			class UpdateStructure(
				val dataset: Map<String, StudyInfo>
			) : PostStructure()
			
			
			@Serializable
			class SyncStructure(
				val dataset: List<DataSet>
			) : PostStructure()
			
			@Serializable
			class MessageStructure(
				val content: String,
				val studyId: Long
			) : PostStructure()
			
			@Serializable
			class RewardRequestStructure(
				val studyId: Long
			) : PostStructure()
		}
		
		@Serializable
		public class DontKillMyAppInfo(
			val name: String,
			val url: String,
			val explanation: String,
			val user_solution: String,
			val notFound: Boolean = false,
			val domain: String = DOMAIN_DONT_KILL_MY_APP
		)
		
		
		fun loadStudies(
			serverUrl: String,
			accessKey: String,
			onError: (msg: String, e: Throwable?) -> Unit,
			onSuccess: (studyString: String, urlFormatted: String) -> Unit
		): Web {
			val web = Web()
			var urlFormatted: String
			if(serverUrl.isEmpty())
				urlFormatted = DEV_SERVER
			else if(serverUrl.length <= 2 || !serverUrl.contains('.')) {
				onError("\"$serverUrl\" is not a valid server address.", null)
				return web
			}
			else {
				urlFormatted = serverUrl
				if(urlFormatted.endsWith("/"))
					urlFormatted = urlFormatted.substring(0, urlFormatted.length - 1)
				
				if(urlFormatted.startsWith("http://")) {
					if(!DbUser.isDev())
						urlFormatted = "https" + urlFormatted.substring(4)
				}
				else if(!urlFormatted.startsWith("https://"))
					urlFormatted = "https://$urlFormatted"
			}
			
			val correctedAccessKey = accessKey.trim().lowercase()
			println("Getting studies from: $urlFormatted with accessKey \"$correctedAccessKey\"")
			nativeAsync {
				try {
					val path = urlFormatted + (
						if ((correctedAccessKey.isNotEmpty()))
							URL_LIST_STUDIES_PASSWORD.replace("%s1", correctedAccessKey).replace("%s2", NativeLink.smartphoneData.lang)
						else
							URL_LIST_STUDIES.replace("%s", NativeLink.smartphoneData.lang)
						)
					val response = web.get(path)
					kotlinRunOnUiThread {
						onSuccess(response, urlFormatted)
					}
				}
				catch(e: SuccessFailedException) {
					ErrorBox.warn("Loading Studies", "Failed to load studies", e)
					kotlinRunOnUiThread {
						onError(e.message ?: "Unknown error", e)
					}
				}
				catch(e: ClientEngineClosedException) {
					println("ClientEngineClosedException: Cancelled by user")
				}
				catch(e: Throwable) {
					ErrorBox.warn("Loading Studies", "Failed to load studies", e)
					kotlinRunOnUiThread {
						onError(e.message ?: "Unknown error", e)
					}
				}
				web.close()
			}
			return web
		}
		
		fun updateStudiesBlocking(forceStudyUpdate: Boolean = false): Int {
			var updateCount = -1
			nativeBlocking {
				val web = Web()
				updateCount = web.updateStudies(forceStudyUpdate)
			}
			return updateCount
		}
		@Synchronized
		fun updateStudiesAsync(forceStudyUpdate: Boolean = false, continueWith: ((Int) -> Unit)? = null) {
			nativeAsync {
				val web = Web()
				val r = web.updateStudies(forceStudyUpdate)
				if(continueWith != null) {
					kotlinRunOnUiThread {
						continueWith(r)
					}
				}
			}
		}
		
		fun syncDataSetsBlocking(web: Web = Web()): Boolean {
			var r = false
			nativeBlocking {
				r = web.syncDataSets()
			}
			return r
		}
		@Synchronized
		fun syncDataSetsAsync(continueWith: (Boolean) -> Unit): Web {
			val web = Web()
			nativeAsync {
				val success = web.syncDataSets()
				kotlinRunOnUiThread {
					continueWith(success)
				}
			}
			return web
		}
		
		@Synchronized
		fun sendErrorReportAsync(
			comment: String?,
			onError: (msg: String) -> Unit,
			onSuccess: () -> Unit
		) {
			val web = Web()
			nativeAsync {
				val errorMsg = web.sendErrorReport(comment)
				kotlinRunOnUiThread {
					if(errorMsg == null)
						onSuccess()
					else
						onError(errorMsg)
				}
			}
		}
		
		@Synchronized
		fun sendMessageAsync(
			content: String,
			study: Study,
			onError: (msg: String) -> Unit,
			onSuccess: () -> Unit
		) {
			val web = Web()
			nativeAsync {
				val errorMsg = web.sendMessage(content, study)
				kotlinRunOnUiThread {
					if(errorMsg == null)
						onSuccess()
					else
						onError(errorMsg)
				}
			}
		}
		
		@Synchronized
		fun loadStatistics(
			study: Study,
			onError: (msg: String) -> Unit,
			onSuccess: (id: String) -> Unit
		): Web {
			val web = Web()
			
			nativeAsync {
				try {
					val response = web.get(study.serverUrl + URL_PUBLIC_STATISTICS.replace("%d", study.webId.toString()).replace("%s", study.accessKey))
					kotlinRunOnUiThread {
						onSuccess(response)
					}
				}
				catch (e: Throwable) {
					ErrorBox.warn("Load Statistics", "Could not load statistics", e)
					kotlinRunOnUiThread {
						onError(e.message ?: "No error message")
					}
				}
			}
			
			return web
		}
		
		
		@Synchronized
		fun loadRewardCode(
			study: Study,
			onError: (msg: String) -> Unit,
			onSuccess: (rewardInfo: RewardInfo) -> Unit
		) {
			val web = Web()
			nativeAsync {
				try {
					val response = web.postJson(study.serverUrl + URL_REWARD, PostStructure.RewardRequestStructure(study.webId))
					val obj = DbLogic.getJsonConfig().decodeFromString<RewardInfo>(response)
					ErrorBox.log("Web", "Loaded rewardCode. Response: $response")
					if(obj.errorCode == Study.REWARD_SUCCESS)
						study.saveRewardCode(obj.code)
					kotlinRunOnUiThread {
						onSuccess(obj)
					}
				}
				catch (e: Throwable) {
					kotlinRunOnUiThread {
						onError(e.message ?: "No error message")
					}
				}
			}
		}
		
		@Synchronized
		fun getDonKillMyAppInfo(
			onError: (msg: String) -> Unit,
			onSuccess: (DontKillMyAppInfo) -> Unit
		) {
			val manufacturer = NativeLink.smartphoneData.manufacturer.lowercase().replace(" ", "-")
			val url = URL_DONT_KILL_MY_APP.replace("%s", manufacturer)
			
			val web = Web()
			nativeAsync {
				try {
					web.getGenericJson(url)
					val response = web.getGenericJson(url)
					kotlinRunOnUiThread {
						if(response.status == HttpStatusCode.NotFound)
							onSuccess(
								DontKillMyAppInfo(
									manufacturer,
									"/$manufacturer",
									"",
									"",
									true
								)
							)
						else
							onSuccess(response.body())
					}
				}
				catch (e: Throwable) {
					e.printStackTrace()
					kotlinRunOnUiThread {
						onError(e.message ?: "No error message")
					}
				}
			}
		}
	}
}