package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import kotlinx.coroutines.selects.select

/**
 * Created by JodliDev on 23.01.2023.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioButtonLine(text: String, isSelected: () -> Boolean, onSelected: () -> Unit) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.selectable(
				selected = isSelected(),
				onClick = onSelected,
				role = Role.RadioButton
			)
			.padding(8.dp)
	) {
		RadioButton(
			selected = isSelected(),
			onClick = null
		)
		Spacer(modifier = Modifier.width(10.dp))
		Text(text)
	}
}

@Composable
fun ListSingleView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
	if(input.asDropDown)
		ListSingleAsDropdownView(input, get, save)
	else
		ListSingleAsListView(input, get, save)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSingleAsListView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
	val otherText = remember{ mutableStateOf(input.getAdditional("other") ?: "") }
	val otherSelected = remember{ mutableStateOf( get() == if(input.forceInt) (input.listChoices.size + 1).toString() else "other") }

	Column {
		for((i, value) in input.listChoices.withIndex()) {
			val actualValue = if(input.forceInt) (i+1).toString() else value
			
			RadioButtonLine(
				text = value,
				isSelected = { actualValue == get() },
				onSelected = {
					save( actualValue, mapOf("other" to ""))
					otherSelected.value = false
				}
			)
		}
		if(input.other) {
			val actualValue = if(input.forceInt) (input.listChoices.size + 1).toString() else "other"
			RadioButtonLine(
				text = stringResource(id = R.string.option_other),
				isSelected = { actualValue == get() },
				onSelected = {
					save( actualValue, mapOf("other" to otherText.value))
					otherSelected.value = true
				}
			)
			if(otherSelected.value)
				TextField(
					value = otherText.value,
					placeholder = { Text(stringResource(id = R.string.option_other)) },
					onValueChange = {
						otherText.value = it
						save (actualValue, mapOf("other" to otherText.value))
					},
					modifier = Modifier.padding(20.dp).fillMaxWidth()
				)
		}
	}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSingleAsDropdownView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
	val expanded = remember { mutableStateOf(false) }
	val otherText = remember { mutableStateOf(input.getAdditional("other") ?: "")}
	val otherSelected = remember { mutableStateOf(false) }
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Box {
			val shownValue =  if(input.forceInt)
				get().toIntOrNull()?.let { if(it <= input.listChoices.size) input.listChoices[it-1] else ""} ?: ""
			else
				get()
			
			DefaultButtonIconLeft(
				text = shownValue.ifEmpty { stringResource(R.string.please_select) },
				icon = Icons.Default.ArrowDropDown,
				onClick = {
					expanded.value = true
				},
				modifier = Modifier
					.padding(horizontal = 20.dp)
					.defaultMinSize(200.dp),
//				textModifier = Modifier.fillMaxSize()
				textModifier = Modifier.align(Alignment.CenterStart)
			)
			DropdownMenu(
				expanded = expanded.value,
				onDismissRequest = {
					expanded.value = false
				},
				offset = DpOffset(20.dp, 0.dp),
				modifier = Modifier
					.defaultMinSize(200.dp)
			) {
				for((i, value) in input.listChoices.withIndex()) {
					val actualValue = if(input.forceInt) (i+1).toString() else value
					DropdownMenuItem(
						text = {
							Text(value)
						},
						onClick = {
							save(actualValue, mapOf("other" to ""))
							expanded.value = false
							otherSelected.value = false
						},
						enabled = actualValue != get()
					)
				}
				if(input.other) {
					val actualValue = if(input.forceInt) input.listChoices.size.toString() else "other"
					DropdownMenuItem(
						text = { Text(stringResource(id = R.string.option_other)) },
						onClick = {
							save(actualValue, mapOf("other" to otherText.value))
							expanded.value = false
							otherSelected.value = true
						}
						)
				}
			}
		}
		if(otherSelected.value) {
			TextField(
				value = otherText.value,
				placeholder = { Text(stringResource(id = R.string.option_other)) } ,
				onValueChange = {
					otherText.value = it
					save(
						if(input.forceInt) input.listChoices.size.toString() else "other",
						mapOf("other" to otherText.value)
					)
				},
				modifier = Modifier.padding(20.dp).fillMaxWidth()
			)
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewListSingleAsListView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"listChoices": ["aaa", "bbb", "ccc", "ddd"]}
	""")
	ESMiraSurface {
		ListSingleAsListView(input, {"bbb"}) { _: String, _: Map<String, String> -> }
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewListSingleAsDropdownView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"listChoices": ["aaa", "bbb", "ccc", "ddd"]}
	""")
	ESMiraSurface {
		ListSingleAsDropdownView(input, {"ccc"}) { _: String, _: Map<String, String> -> }
	}
}