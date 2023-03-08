package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun LikertView(input: Input, get: () -> String, save: (String) -> Unit) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Row(modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 5.dp)) {
			Text(input.leftSideLabel,
				fontSize = MaterialTheme.typography.labelMedium.fontSize,
				modifier = Modifier.weight(1F)
			)
			Spacer(modifier = Modifier.widthIn(min = 20.dp))
			Text(input.rightSideLabel,
				fontSize = MaterialTheme.typography.labelMedium.fontSize,
				textAlign = TextAlign.End,
				modifier = Modifier.weight(1F)
			)
		}
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
			val configuration = LocalConfiguration.current
			val width = configuration.screenWidthDp.dp / input.likertSteps
			for(i in 1..input.likertSteps) {
				RadioButton(selected = get() == i.toString(),
					onClick = {
						save(i.toString())
					},
					modifier = Modifier.width(width)
				)
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewLikertView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"leftSideLabel": "left", "rightSideLabel": "right", "likertSteps": "5"}
	""")
	ESMiraSurface {
		LikertView(input, {"1"}) {}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewLikertViewUsesTooMuchSpace() {
	val input = DbLogic.createJsonObj<Input>("""
		{"leftSideLabel": "a very long text that should hopefully break", "rightSideLabel": "right", "likertSteps": "10"}
	""")
	ESMiraSurface {
		Column(modifier = Modifier.width(400.dp)) {
			LikertView(input, { "1" }) {}
		}
	}
}