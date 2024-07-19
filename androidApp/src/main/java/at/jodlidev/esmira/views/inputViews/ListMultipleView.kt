package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckBoxLine(text: String, isChecked: () -> Boolean, onChecked: (Boolean) -> Unit) {
	val checkedState = remember { mutableStateOf(isChecked()) }

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.selectable(
				selected = checkedState.value,
				onClick = {
					checkedState.value = checkedState.value.not()
					onChecked(checkedState.value)
				},
				role = Role.Checkbox
			)
			.padding(8.dp)
	) {
		Checkbox(
			checked = checkedState.value,
			onCheckedChange = null
		)
		Spacer(modifier = Modifier.width(10.dp))
		Text(text)
	}
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListMultipleView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
	val otherString = stringResource(id = R.string.option_other)
	val choices = remember {
		val list = ArrayList<Pair<String,Boolean>>()
		val inputValue = get()
		for(value in input.listChoices) {
			list.add(Pair(value, inputValue.contains(value)))
		}
		list
	}
	val otherText = remember { mutableStateOf(input.getAdditional("other_text") ?: "") }
	val otherSelected = remember { mutableStateOf((input.getAdditional("other") ?: "0") == "1") }

	val saveChoices = {
		val map = HashMap<String, String>()

		// This is the old save format (variable~choice)
		for(pair in choices) {
			map[pair.first] = if (pair.second) "1" else "0"
		}

		// This is the new save format (variable~index)
		for(i in choices.indices) {
			map[(i+1).toString()] = if (choices[i].second) "1" else "0"
		}

		val selectedList = choices.filter { it.second }.map { it.first }.toMutableList()

		if(input.other) {
			map["other"] = if (otherSelected.value) "1" else "0"
			map["other_text"] = if (otherSelected.value) otherText.value else ""
			if(otherSelected.value) {
				selectedList.add(otherString)
			}
		}

		save(selectedList.joinToString(), map)
	}

	Column {
		for((i, pair) in choices.withIndex()) {
			CheckBoxLine(
				text = pair.first,
				isChecked = { pair.second }
			) {
				choices[i] = pair.copy(second = it)
				saveChoices()
			}
		}
		if(input.other) {
			CheckBoxLine(
				text = otherString,
				isChecked = { otherSelected.value }
			) {
				otherSelected.value = it
				saveChoices()
			}

		}
		if(otherSelected.value) {
			TextField(
				value = otherText.value,
				placeholder = { Text(stringResource(id = R.string.option_other)) },
				onValueChange = {
					otherText.value = it
					saveChoices()
				},
				modifier = Modifier.padding(20.dp).fillMaxWidth()
			)
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewListMultipleView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"listChoices": ["aaa", "bbb", "ccc", "ddd"]}
	""")
	ESMiraSurface {
		ListMultipleView(input, {"ccc"}) { _, _ -> }
	}
}