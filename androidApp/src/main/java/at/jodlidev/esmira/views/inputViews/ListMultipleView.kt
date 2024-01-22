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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */

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


@Composable
fun ListMultipleView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
	val choices = remember {
		val list = ArrayList<Pair<String,Boolean>>()
		val inputValue = get()
		for(value in input.listChoices) {
			list.add(Pair(value, inputValue.contains(value)))
		}
		list
	}
	
	Column {
		for((i, pair) in choices.withIndex()) {
			CheckBoxLine(
				text = pair.first,
				isChecked = { pair.second },
				onChecked = {
					choices[i] = pair.copy(second = it)
					val map = HashMap<String, String>()
					val s = StringBuilder()
					if(getStudyServerVersion(input) <= 11) { // This is necessary for backwards compatibility with older server
						for (pair_ in choices) {
							map[pair_.first] = if (pair_.second) "1" else "0"
							if (pair_.second) {
								s.append(pair_.first)
								s.append(',')
							}
						}
					} else {
						for(i in choices.indices) {
							map[i.toString()] = if (choices[i].second) "1" else "0"
						}
						s.append(choices.filter( { pair_ -> pair.second} ).map( { pair_ -> pair_.first } ).joinToString())
					}
					save(s.toString(), map)
				}
			)
		}
	}
}

internal fun getStudyServerVersion(input: Input): Int {
	val questionnaire = input.questionnaire
	val studyId = questionnaire.studyId
	val study = DbLogic.getStudy(studyId)
	return study?.serverVersion ?: return -1
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