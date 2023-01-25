package at.jodlidev.esmira.androidNative

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
	val backup = rememberSaveable { mutableStateOf(input.getBackupString()) }
	val response = rememberSaveable { mutableStateOf(input.value) }
	
	LaunchedEffect(backup.value, input) {
		input.fromBackupString(backup.value)
	}
	val get = {
		response.value
	}
	val set = { inputValue: String ->
		response.value = inputValue
		input.value = inputValue
		backup.value = input.getBackupString()
	}
	
	Column(modifier = modifier) {
		TextElView(input)
		Spacer(modifier = Modifier.height(10.dp))
		when(input.type) {
			Input.TYPES.text -> Unit
			Input.TYPES.binary -> BinaryView(input, get, set)
			Input.TYPES.app_usage -> AppUsageView(input, get, set)
			Input.TYPES.dynamic_input -> DynamicView(input, questionnaire, get, set)
			else -> ErrorView(input)
		}
	}
	
}