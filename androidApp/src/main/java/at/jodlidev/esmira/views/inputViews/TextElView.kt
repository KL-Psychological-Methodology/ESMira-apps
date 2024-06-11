package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinRunner

/**
 * Created by JodliDev on 23.01.2023.
 */
@Composable
fun TextElView(input: Input, modifier: Modifier = Modifier) {
	if(input.textScript.isNotEmpty()) {
		MerlinRunner.runForString(input.textScript, input.questionnaire).let {
			HtmlHandler.HtmlText(html = it, modifier = modifier)
		}
	}
	else if(input.desc.isNotEmpty()) {
		HtmlHandler.HtmlText(html = input.desc, modifier = modifier)
	}
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