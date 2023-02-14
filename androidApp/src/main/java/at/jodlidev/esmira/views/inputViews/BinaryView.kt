package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun BinaryView(input: Input, get: () -> String, save: (String) -> Unit) {
	Row(modifier = Modifier.fillMaxWidth()) {
		DefaultButtonIconLeft(
			text = input.leftSideLabel,
			icon = if(get() == "1") Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
			onClick = {
				save("0")
			},
			modifier = Modifier.weight(0.45f)
		)
		
		Spacer(modifier = Modifier.width(10.dp))
		
		DefaultButtonIconRight(
			text = input.rightSideLabel,
			icon = if(get() == "2") Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
			onClick = {
				save("1")
			},
			modifier = Modifier.weight(0.45f)
		)
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