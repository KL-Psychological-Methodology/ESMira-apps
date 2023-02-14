package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.ESMiraSurfaceM2
import at.jodlidev.esmira.fontSizeSmall
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun LikertView(input: Input, get: () -> String, save: (String) -> Unit) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)) {
			Text(input.leftSideLabel, fontSize = fontSizeSmall)
			Spacer(modifier = Modifier.weight(1F))
			Text(input.rightSideLabel, fontSize = fontSizeSmall)
		}
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
			for(i in 1..input.likertSteps) {
				RadioButton(selected = get() == i.toString(), onClick = {
					save(i.toString())
				})
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