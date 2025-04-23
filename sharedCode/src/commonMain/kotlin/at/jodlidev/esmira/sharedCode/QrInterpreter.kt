package at.jodlidev.esmira.sharedCode

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Created by JodliDev on 27.08.2020.
 */
class QrInterpreter {
	class ConnectData (
		var url: String,
		val accessKey: String = "",
		val studyId: Long = 0,
		val fallbackUrl: String? = null,
		val qId: Long = 0
	) {
		init {
			if(url.startsWith("esmira:"))
				url = "https${url.substring(6)}"
		}
	}
	
	
	// example.com/survey-12345		example.com/survey-12345-KEY
	private val patternQuestionnaire = "^(.+/)survey-(\\d+)-?([a-zA-Z][a-zA-Z0-9]+)?$".toRegex() //url, qId, (accessKey)
	
	// example.com/KEY				example.com/1234				example.com/1234-KEY
	// example.com/app-KEY			example.com/app-1234			example.com/app-1234-KEY
	private val patternStudy = "^(.+/)(app-)?(\\d*)-?([a-zA-Z][a-zA-Z0-9]+)?(\\?fallback=?([a-zA-Z0-9/+=]+))?$".toRegex() //url, ("app-"), (studyId), (accessKey) ("?fallback=") (fallback_url)
	
	@OptIn(ExperimentalEncodingApi::class)
	fun check(s: String): ConnectData? {
		val matchStudy = patternStudy.find(s)
		return if(matchStudy != null) {
			val (url, _, studyId, key, _, fallbackUrl) = matchStudy.destructured
			var decodedFallbackUrl: String? = null
			if(fallbackUrl.isNotEmpty()) {
				try {
					decodedFallbackUrl = Base64.decode(fallbackUrl).decodeToString()
				} catch (_: Throwable) {}
			}
			ConnectData(url = url, accessKey = key, studyId = if(studyId.isNotEmpty()) studyId.toLong() else 0,
				fallbackUrl = decodedFallbackUrl)
		}
		else {
			val matchQuestionnaire = patternQuestionnaire.find(s)
			if(matchQuestionnaire != null) {
				val (url, qId, key) = matchQuestionnaire.destructured
				ConnectData(
					url = url,
					accessKey = key,
					qId = if(qId.isNotEmpty()) qId.toLong() else 0,
					fallbackUrl = null
				)
			}
			else {
				println("Not valid: $s")
				null
			}
		}
	}
}