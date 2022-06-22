package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable


/**
 * Created by JodliDev on 16.04.2019.
 * is just an interpreter for creating appropriate Views
 * it is not saved into db
 */
@Serializable
class Input internal constructor( ) {
	enum class TYPES {
		text, binary, va_scale, likert, list_single, list_multiple, number, text_input, time,
		time_old,  //TODO: can be removed when Selinas study is done
		date,
		date_old,  //TODO: can be removed when Selinas study is done
		datetime, dynamic_input, app_usage, video, image, photo, ERROR
	}
	var name: String = "input"
	private var text: String = ""
	var required: Boolean = false
	var url: String = ""
	var random: Boolean = false
	var defaultValue: String = ""
	var likertSteps: Int = 5
	var leftSideLabel: String = ""
	var rightSideLabel: String = ""
	var numberHasDecimal: Boolean = false
	var asDropDown: Boolean = true
	var packageId: String = "" //for app_usage
	
	var forceInt: Boolean = false
	
	val additionalValues: HashMap<String, String> = HashMap()
	
	@Transient private var _value: String = if(required) "" else defaultValue
	var value: String
		get() {
			return if(type == TYPES.dynamic_input && this::dynamicInput.isInitialized)
//				"$_value/${dynamicInput.value}"
				dynamicInput.value
			else
				_value
		}
		set(value) {
			_value = value
		}
	
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
	@Transient val addedFiles : MutableList<FileUpload> = ArrayList()
	
	@Transient private lateinit var dynamicInput: Input
	
	
	fun addImage(filePath: String, studyId: Long) {
		val study = DbLogic.getStudy(studyId) ?: return
		val fileUpload = FileUpload(study, filePath, FileUpload.TYPES.Image)
		fileUpload.save()
		addedFiles.add(fileUpload)
		value = fileUpload.identifier.toString()
	}
	
	fun getDynamicInput(questionnaire: Questionnaire): Input {
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
		additionalValues["index"] = dynamicIndex.toString();
		dynamicInput = subInputs[dynamicIndex]
		
		required = dynamicInput.required
		if(dynamicInput.required && desc.length > 1)
			dynamicInput.required = false // it is only used for the description. And we dont want to show a * if parent input already shows one
		
		return dynamicInput
	}
	
	fun needsValue(): Boolean {
		return if(required)
			value.isEmpty()
		else
			false
	}
	
	fun getBackupString(): String {
		var r = "$value~"
		
		if(additionalValues.isNotEmpty()) {
			for(additionalValue in additionalValues) {
				r += "${additionalValue.key}=${additionalValue.value},"
			}
			return r.substring(0, r.length-1)
		}
		
		return r
	}
	fun fromBackupString(backup: String) {
		val parts = backup.split("~")
		value = parts[0]
		if(parts[1].isNotEmpty()) {
			parts[1].split(",").associateTo(additionalValues) {
				val (key, value) = it.split("=")
				key to value
			}
		}
	}
	
	internal fun hasScreenTracking(): Boolean {
		return type == TYPES.app_usage && packageId.isEmpty()
	}
	internal fun hasScreenOrAppTracking(): Boolean {
		return type == TYPES.app_usage
	}
	
	internal fun fillIntoDataSet(dataSet: DataSet) {
		dataSet.addResponseData(name, value)
		val additionalName = "${name}~"
		for(additionalValue in additionalValues) {
			dataSet.addResponseData(additionalName + additionalValue.key, additionalValue.value)
		}
		for(fileUpload in addedFiles) {
			fileUpload.setReadyForUpload()
		}
	}
}