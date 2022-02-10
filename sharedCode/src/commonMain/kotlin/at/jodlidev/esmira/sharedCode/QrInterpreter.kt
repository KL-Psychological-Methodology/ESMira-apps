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
	//TODO: can be removed when Selinas study is done: -->
	@Serializable
	class OldConnectData (
		val url: String,
		val accessKey: String = "",
		val study_id: Long
	)
	
	// example.com/KEY+1234				example.com/KEY+1234.0
	// example.com/KEY+a1234			example.com/KEY+a1234.0
	private val patternWithPass = "^([\\w/.:-]+/)([\\w-_]+)\\+a?(\\d+)\\.?(\\d*)$".toRegex() //url, key, studyId, (qIndex)
	
	// example.com/1234				example.com/1234.0
	// example.com/a1234			example.com/a1234.0
	private val patternWithoutPass = "^([\\w/.:-]+/)a?(\\d+)\\.?(\\d*)$".toRegex() //url, studyId, (qIndex)
	//TODO: <--
	
	
	
	// example.com/survey-12345		example.com/survey-12345-KEY
	private val patternQuestionnaire = "^([\\w/.:-]+/)survey-(\\d+)-?([a-zA-Z][a-zA-Z0-9]+)?$".toRegex() //url, qId, (accessKey)
	
	// example.com/KEY				example.com/1234				example.com/1234-KEY
	// example.com/app-KEY			example.com/app-1234			example.com/app-1234-KEY
	private val patternStudy = "^([\\w/.:-]+/)(app-)?(\\d*)-?([a-zA-Z][a-zA-Z0-9]+)?$".toRegex() //url, ("app-"), (studyId), (accessKey)
	
	
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
			else { //TODO: can be removed when Selinas study is done:
				val matchWithPass = patternWithPass.find(s)
				
				return if(matchWithPass != null) {
					val (url, pass, studyId) = matchWithPass.destructured
					ConnectData(url = url, accessKey = pass, studyId = studyId.toLong())
				}
				else {
					val matchWithoutPass = patternWithoutPass.find(s)
					
					if(matchWithoutPass != null) {
						val (url, studyId) = matchWithoutPass.destructured
						ConnectData(url = url, accessKey = "", studyId = studyId.toLong())
					}
					else {
						try {
							val oldData = DbLogic.getJsonConfig().decodeFromString<OldConnectData>(s)
							ConnectData(url = oldData.url, accessKey = oldData.accessKey, studyId = oldData.study_id)
						}
						catch(e: Exception) {
							println("Not a json: $s")
							null
						}
					}
				}
			}
		}
	}
}