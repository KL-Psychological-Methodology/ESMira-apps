package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinRunner

/**
 * Created by JodliDev on 23.01.2023.
 */
@Composable
fun TextElView(input: Input, modifier: Modifier = Modifier) {
	HtmlHandler.HtmlText(
		html = input.displayText.takeUnless { it == MerlinRunner.ERROR_MARKER } ?: stringResource(R.string.text_script_error),
		modifier = modifier
	)
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewTextElView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"text": "A <b>meaningful</b> description"}
	""")
	ESMiraSurface {
		TextElView(input, Modifier)
	}
}