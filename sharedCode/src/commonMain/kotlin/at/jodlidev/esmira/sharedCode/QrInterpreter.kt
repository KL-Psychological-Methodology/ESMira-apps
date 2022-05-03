package at.jodlidev.esmira.sharedCode

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable
/**
 * Created by JodliDev on 27.08.2020.
 */
class QrInterpreter {
	class ConnectData (
		var url: String,
		val accessKey: String = "",
		val studyId: Long = 0,
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
	private val patternStudy = "^(.+/)(app-)?(\\d*)-?([a-zA-Z][a-zA-Z0-9]+)?$".toRegex() //url, ("app-"), (studyId), (accessKey)
	
	
	fun check(s: String): ConnectData? {
		val matchStudy = patternStudy.find(s)
		return if(matchStudy != null) {
			val (url, _, studyId, key) = matchStudy.destructured
			ConnectData(url = url, accessKey = key, studyId = if(studyId.isNotEmpty()) studyId.toLong() else 0)
		}
		else {
			val matchQuestionnaire = patternQuestionnaire.find(s)
			if(matchQuestionnaire != null) {
				val (url, qId, key) = matchQuestionnaire.destructured
				ConnectData(url = url, accessKey = key, qId = if(qId.isNotEmpty()) qId.toLong() else 0)
			}
			else {
				println("Not valid: $s")
				null
			}
		}
	}
}