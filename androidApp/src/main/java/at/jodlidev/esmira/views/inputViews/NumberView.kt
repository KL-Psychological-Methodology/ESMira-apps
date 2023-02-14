package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.ESMiraSurfaceM2
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberView(input: Input, get: () -> String, save: (String) -> Unit) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		OutlinedTextField(
			value = get(),
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			onValueChange = { save(it) },
			modifier = Modifier.width(100.dp)
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNumberView() {
	val input = DbLogic.createJsonObj<Input>("""
		{}
	""")
	ESMiraSurface {
		NumberView(input, {"8"}) {}
	}
}