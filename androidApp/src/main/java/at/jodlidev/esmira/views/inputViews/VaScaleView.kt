package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import kotlin.math.roundToInt

/**
 * Created by JodliDev on 23.01.2023.
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaScaleView(input: Input, get: () -> String, save: (String) -> Unit) {
	val showThumb = remember { mutableStateOf(get().isNotEmpty()) }
	Column(modifier = Modifier.fillMaxWidth()) {
		Row(modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 5.dp)
		) {
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
		
		if(input.showValue) {
			Text(get(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
		}
		
		Slider(
			value = try { get().toFloat() } catch(_: Throwable) { 50F },
			valueRange = 1F .. (if(input.maxValue > 1) input.maxValue else 100F),
			onValueChange = { value ->
				save(value.roundToInt().toString())
				showThumb.value = true
			},
			colors = SliderDefaults.colors(
				inactiveTrackColor = MaterialTheme.colorScheme.primary,
				activeTrackColor = MaterialTheme.colorScheme.primary,
			),
			thumb = {
				if(showThumb.value) {
					SliderDefaults.Thumb(
						interactionSource = remember { MutableInteractionSource() },
					)
				}
			},
			modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp))
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewVaScaleView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"leftSideLabel": "left", "rightSideLabel": "right"}
	""")
	ESMiraSurface {
		VaScaleView(input, {"70"}) {}
	}
}

@Preview
@Composable
fun PreviewVaScaleWithValueView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"leftSideLabel": "left", "rightSideLabel": "right", "showValue": true}
	""")
	ESMiraSurface {
		VaScaleView(input, {"70"}) {}
	}
}