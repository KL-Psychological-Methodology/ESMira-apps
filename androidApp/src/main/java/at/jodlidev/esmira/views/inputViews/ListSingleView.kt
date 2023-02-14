package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.DefaultButtonIconLeft
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.ESMiraSurfaceM2
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */

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
fun ListSingleView(input: Input, get: () -> String, save: (String) -> Unit) {
	if(input.asDropDown)
		ListSingleAsDropdownView(input, get, save)
	else
		ListSingleAsListView(input, get, save)
}
@Composable
fun ListSingleAsListView(input: Input, get: () -> String, save: (String) -> Unit) {
	Column {
		for(value in input.listChoices) {
			RadioButtonLine(
				text = value,
				isSelected = { value == get() },
				onSelected = {
					save(value)
				}
			)
		}
	}
}
@Composable
fun ListSingleAsDropdownView(input: Input, get: () -> String, save: (String) -> Unit) {
	val expanded = remember { mutableStateOf(false) }
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Box {
			DefaultButtonIconLeft(
				text = get().ifEmpty { stringResource(R.string.please_select) },
				icon = Icons.Default.ArrowDropDown,
				onClick = {
					expanded.value = true
				},
				modifier = Modifier.padding(horizontal = 20.dp).defaultMinSize(200.dp),
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
				for(value in input.listChoices) {
					DropdownMenuItem(
						text = {
							Text(value)
						},
						onClick = {
							save(value)
							expanded.value = false
						},
						enabled = value != get()
					)
				}
			}
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
		ListSingleAsListView(input, {"bbb"}) {}
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
		ListSingleAsDropdownView(input, {"ccc"}) {}
	}
}