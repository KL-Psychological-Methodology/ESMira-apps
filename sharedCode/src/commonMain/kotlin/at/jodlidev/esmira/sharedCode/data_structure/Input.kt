package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinRunner
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlin.math.pow


/**
 * Created by JodliDev on 16.04.2019.
 * is just an interpreter for creating appropriate Views
 * it is not saved into db
 */
@Serializable
class Input internal constructor( ) {
	enum class TYPES {
		app_usage,
		binary,
		bluetooth_devices,
		compass,
		countdown,
		date,
		datetime,
		duration,
		dynamic_input,
		file_upload,
		image,
		text,
		likert,
		list_single,
		list_multiple,
		location,
		number,
		photo,
		record_audio,
		share,
		text_input,
		time,
		va_scale,
		video,
		ERROR
	}
	var name: String = "input"
	private var text: String = ""
	var required: Boolean = false
	var url: String = ""
	var random: Boolean = false
	var relevance: String = ""
	var defaultValue: String = ""
	var likertSteps: Int = 5
	var leftSideLabel: String = ""
	var rightSideLabel: String = ""
	var numberHasDecimal: Boolean = false
	var asDropDown: Boolean = true
	var packageId: String = "" //for app_usage
	var timeoutSec: Int = 0 //for countdown
	var playSound: Boolean = false //for countdown
	var showValue: Boolean = false //for compass, vas
	var maxValue: Float = 0F //for vas
	var resolution: Int = 0 //for location
	var vertical: Boolean = false //for likert scale
	var textScript: String = ""
	var size: Int = 100 //for image compression
	var other: Boolean = false // for lists
	
	var forceInt: Boolean = false
	
	val desc: String
		get() {
			return if(required && text.isNotEmpty()) "$text*" else text
		}
	
	@SerialName("responseType") private var _type: String = TYPES.text_input.toString()
	val type: TYPES
		get() {
			return try {
				TYPES.valueOf(_type)
			}
			catch(e: Exception) {
				TYPES.ERROR
			}
		}
	
	val listChoices: List<String> = ArrayList()
	val subInputs: List<Input> = ArrayList()
	
	@Transient private lateinit var dynamicInput: Input
	
	
	@Transient lateinit var questionnaire: Questionnaire

	@Transient private lateinit var _displayText: String
	val displayText: String get() {
		if(!this::_displayText.isInitialized) {
			_displayText = if(textScript.isNotEmpty()) {
				MerlinRunner.runForString(
					textScript,
					questionnaire,
					"text script of item $name"
				)
			} else {
				desc
			}
		}
		return _displayText
	}

	@Transient private val additionalValues: HashMap<String, String> = HashMap()
	@Transient private val addedFiles : MutableList<FileUpload> = ArrayList()
	
	@Transient private lateinit var _value: String

	/**
	 * Loads input data from [QuestionnaireCache.loadCacheValue] and returns [_value]
	 */
	fun getValue(): String {
		return if(type == TYPES.dynamic_input && this::dynamicInput.isInitialized)
			dynamicInput.getValue()
		else if(!this::_value.isInitialized) {
			val cache = QuestionnaireCache.loadCacheValue(questionnaire.id, name)
			if(cache != null)
				fromBackupObj(DbLogic.getJsonConfig().decodeFromString(cache))
			else
				_value = if(required) "" else defaultValue
			
			_value
		}
		else
			_value
	}

	fun getAdditional(key: String): String? {
		getValue() //make sure cache has been initialized
		return additionalValues[key]
	}
	
	/**
	 * For saving usual Input data
	 * Overwrites [_value], and [additionalValues] and calls [QuestionnaireCache.saveCacheValue]
	 * Cannot be combined with [setFile]
	 */
	fun setValue(value: String, additionalValues: Map<String, String>? = null) {
		this._value = value
		if(additionalValues != null) {
			this.additionalValues.clear()
			this.additionalValues.putAll(additionalValues)
		}
		
		QuestionnaireCache.saveCacheValue(questionnaire.id, name, getBackupString())
	}
	
	/**
	 * For connecting a File with this input
	 * Saves a [FileUpload], sets [FileUpload.identifier] in [_value] and calls [QuestionnaireCache.saveCacheValue]
	 * Cannot be combined with [setValue]
	 */
	fun setFile(filePath: String, dataType: FileUpload.DataTypes = FileUpload.DataTypes.Image) {
		val currentValue = getValue() // will also set _value
		if(currentValue.isNotEmpty()) {
			DbLogic.getFileUploadByIdentifier(currentValue)?.let { fileUpload ->
				if(addedFiles.find { it.identifier == fileUpload.identifier } == null)
					addedFiles.add(fileUpload)
				return
			}
		}
		
		val study = DbLogic.getStudy(questionnaire.studyId) ?: return
		val fileUpload = FileUpload(study, filePath, dataType)
		fileUpload.save()
		addedFiles.add(fileUpload)
		_value = fileUpload.identifier.toString()
		QuestionnaireCache.saveCacheValue(questionnaire.id, name, getBackupString())
	}
	fun getFileName(): String? {
		val fileUpload = DbLogic.getFileUploadByIdentifier(getValue())
		return fileUpload?.filePath
	}
	
	fun getDynamicInput(): Input {
		if(this::dynamicInput.isInitialized)
			return dynamicInput
		
		val dynamicIndex: Int
		val variable = name
		val length = subInputs.size
		var lastIndexBox = DbLogic.getLastDynamicTextIndex(questionnaire.id, variable) //get current input
		if(lastIndexBox == null || lastIndexBox.createdTime < questionnaire.lastCompleted || lastIndexBox.index >= length) { //lastIndexBox.index can be bigger than length when the study was updated
			//either there is no current input, or we need a new one
			if(random) {
				val checkedChoices = DbLogic.getAvailableListForDynamicText(questionnaire.id, variable, length)
				val index = checkedChoices.indices.random()
				dynamicIndex = checkedChoices[index]
				DynamicInputData(questionnaire.id, variable, dynamicIndex).save()
			}
			else {
				if(lastIndexBox == null)
					lastIndexBox = DynamicInputData(questionnaire.id, variable, 0)
				
				else if(++lastIndexBox.index >= length)
					lastIndexBox.index = 0
				
				lastIndexBox.renew()
				lastIndexBox.save()
				dynamicIndex = lastIndexBox.index
			}
		}
		else
			dynamicIndex = lastIndexBox.index
		
		
//		value = dynamicIndex.toString()
		additionalValues["index"] = dynamicIndex.toString()
		dynamicInput = subInputs[dynamicIndex]
		dynamicInput.questionnaire = questionnaire
		dynamicInput.name = name
		
		required = dynamicInput.required
		if(dynamicInput.required && desc.length > 1)
			dynamicInput.required = false // it is only used for the description. And we dont want to show a * if parent input already shows one
		
		return dynamicInput
	}
	
	fun needsValue(): Boolean {
		return if(required)
			getValue().isEmpty()
		else
			false
	}
	
	fun getFilledUrl(): String {
		return url.replace("[[USER_ID]]", DbUser.getUid())
	}
	
	private fun getBackupJsonObj(): JsonObject {
		return buildJsonObject {
			put("value", _value)
			
			if(additionalValues.isNotEmpty()) {
				put("additionalValues", buildJsonObject {
					for(additionalValue in additionalValues) {
						put(additionalValue.key, additionalValue.value)
					}
				})
			}
			
			if(addedFiles.isNotEmpty()) {
				put("addedFiles", buildJsonArray {
					for(addedFile in addedFiles) {
						add(addedFile.id)
					}
				})
			}
		}
	}
	fun getBackupString(): String { //public for testing
		return getBackupJsonObj().toString()
	}
	private fun fromBackupObj(obj: JsonObject) {
		_value = obj.getValue("value").jsonPrimitive.content
		
		if(obj.contains("additionalValues")) {
			val jsonAdditionalValues = obj.getValue("additionalValues").jsonObject
			for(additionalValue in jsonAdditionalValues) {
				additionalValues[additionalValue.key] = additionalValue.value.jsonPrimitive.content
			}
		}
		
		if(obj.contains("addedFiles")) {
			val jsonAddedFiles = obj.getValue("addedFiles").jsonArray
			for(fileUploadId in jsonAddedFiles) {
				val fileUpload = DbLogic.getFileUpload(fileUploadId.jsonPrimitive.long) ?: continue
				addedFiles.add(fileUpload)
			}
		}
	}
	fun fromBackupString(backup: String) {
		fromBackupObj(DbLogic.getJsonConfig().decodeFromString(backup))
	}
	
	internal fun hasScreenTracking(): Boolean {
		return type == TYPES.app_usage && packageId.isEmpty()
	}
	internal fun hasScreenOrAppTracking(): Boolean {
		return type == TYPES.app_usage
	}
	
	internal fun fillIntoDataSet(dataSet: DataSet) {
		dataSet.addResponseData(name, getValue())
		val additionalName = "${name}~"
		for(additionalValue in additionalValues) {
			dataSet.addResponseData(additionalName + additionalValue.key, additionalValue.value)
		}
		for(fileUpload in addedFiles) {
			fileUpload.setReadyForUpload()
		}
	}
	
	companion object {
		fun anonymizeValue(s: String): String {
			return s.hashCode().toUInt().toString()
		}
		fun rssiToDistance(rssi: Int): Double {
			return 10.0.pow((-69 -rssi)/(10 * 2))
		}
	}
}