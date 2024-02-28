package at.jodlidev.esmira.androidNative

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.views.inputViews.*

/**
 * Created by JodliDev on 19.05.2020.
 */
@Composable
fun ChooseInputView(questionnaire: Questionnaire, input: Input, modifier: Modifier) {
	val response = remember { mutableStateOf(input.getValue()) }
	
	val get = {
		response.value
	}
	val set = { inputValue: String, additionalValues: Map<String, String>? ->
		println("${input.name} = $inputValue; $additionalValues")
		response.value = inputValue
		input.setValue(inputValue, additionalValues)
	}
	val setValue = { inputValue: String ->
		set(inputValue, null)
	}
	val setAdditionalValue = { inputValue: String, additionalValues: Map<String, String>? ->
		set(inputValue, additionalValues)
	}
	val setFilePath = { filePath: String ->
		println("File: ${input.name} = $filePath")
		input.setFile(filePath)
	}
	
	Column(modifier = modifier) {
		TextElView(input)
		Spacer(modifier = Modifier.height(10.dp))
		when(input.type) {
			Input.TYPES.app_usage -> AppUsageView(input, get, setAdditionalValue)
			Input.TYPES.binary -> BinaryView(input, get, setValue)
			Input.TYPES.bluetooth_devices -> BluetoothDevicesView(input, get, setAdditionalValue)
			Input.TYPES.compass -> CompassView(input, get, setValue)
			Input.TYPES.countdown -> CountdownView(input, get, setValue)
			Input.TYPES.date -> DateView(input, get, setValue)
			Input.TYPES.dynamic_input -> DynamicView(input)
			Input.TYPES.image -> ImageView(input, get, setValue)
			Input.TYPES.likert -> LikertView(input, get, setValue)
			Input.TYPES.list_multiple -> ListMultipleView(input, get, setAdditionalValue)
			Input.TYPES.list_single -> ListSingleView(input, get, setValue)
			Input.TYPES.location -> LocationView(input, get, setValue)
			Input.TYPES.number -> NumberView(input, get, setValue)
			Input.TYPES.photo -> PhotoView(input, get, setFilePath)
			Input.TYPES.file_upload -> FileUploadView(input, get, setFilePath)
			Input.TYPES.record_audio -> RecordAudioView(input)
			Input.TYPES.share -> ShareView(input, get, setValue)
			Input.TYPES.text -> Unit
			Input.TYPES.text_input -> TextInputView(input, get, setValue)
			Input.TYPES.time -> TimeView(input, get, setValue)
			Input.TYPES.va_scale -> VaScaleView(input, get, setValue)
			Input.TYPES.video -> VideoView(input, get, setValue)
			else -> ErrorView(input)
		}
	}
	
}