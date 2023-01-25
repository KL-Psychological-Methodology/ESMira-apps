package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Page

/**
 * Created by JodliDev on 23.01.2023.
 */

@Composable
fun ErrorView(input: Input) {
	Text("${stringResource(R.string.error_input)}: ${input.type}", fontWeight = FontWeight.Bold, color = Color.Red)
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewErrorView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"responseType": "something", "text": "A meaningful description"}
	""")
	ESMiraSurface {
		ErrorView(input)
	}
}