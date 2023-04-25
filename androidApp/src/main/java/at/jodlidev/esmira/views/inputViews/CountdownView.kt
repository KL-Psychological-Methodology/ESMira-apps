package at.jodlidev.esmira.views.inputViews

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.colorGreen
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun CountdownView(input: Input, get: () -> String, save: (String) -> Unit) {
	val timerIsRunning = remember { mutableStateOf(false) }
	
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if(get() == "1") {
			Icon(Icons.Default.CheckCircle, "true", tint = colorGreen, modifier = Modifier.size(30.dp))
		}
		else if(timerIsRunning.value) {
			val ticks = remember { mutableStateOf(input.timeoutSec) }
			LaunchedEffect(Unit) {
				println(ticks.value)
				while(--ticks.value > 0) {
					println(ticks.value)
					delay(1.seconds)
				}
				save("1")
				timerIsRunning.value = false
			}
			Row(modifier = Modifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
				Text(ticks.value.toString(), fontSize = MaterialTheme.typography.headlineSmall.fontSize)
			}
		}
		else {
			DefaultButtonIconLeft(
				text = stringResource(R.string.start_timer),
				icon = Icons.Default.PlayCircle,
				onClick = {
					timerIsRunning.value = true
					save("0")
				}
			)
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewCountdownView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"timeoutSec": 60}
	""")
	ESMiraSurface {
		CountdownView(input, {"70"}) {}
	}
}