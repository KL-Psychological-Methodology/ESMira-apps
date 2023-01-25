package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun BinaryView(input: Input, get: () -> String, set: (String) -> Unit) {
	Row(modifier = Modifier.fillMaxSize()) {
		OutlinedButton(
			modifier = Modifier.weight(0.45f),
			onClick = {
				set("1")
			}
		) {
			Icon(
				if(get() == "1") Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
				contentDescription = "",
				modifier = Modifier.size(ButtonDefaults.IconSize)
			)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(input.leftSideLabel)
		}
		
		Spacer(modifier = Modifier.width(10.dp))
		
		OutlinedButton(
			modifier = Modifier.weight(0.45f),
			onClick = {
				set("2")
			}
		) {
			Text(input.rightSideLabel)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Icon(
				if(get() == "2") Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
				contentDescription = "",
				modifier = Modifier.size(ButtonDefaults.IconSize)
			)
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewBinaryView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"leftSideLabel": "left", "rightSideLabel": "right", "text": "A meaningful description"}
	""")
	ESMiraSurface {
		BinaryView(input, {"1"}) {}
	}
}