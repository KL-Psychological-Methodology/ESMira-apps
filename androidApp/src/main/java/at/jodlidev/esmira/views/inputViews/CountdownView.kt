package at.jodlidev.esmira.views.inputViews

import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
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
	val context = LocalContext.current
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
				do {
					delay(1.seconds)
				} while(--ticks.value > 0)
				
				save("1")
				timerIsRunning.value = false
				
				if(input.playSound) {
					//play beep:
					val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
					tone.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 500)
					
					//vibrate:
					val vibrator = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
						val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
						vibratorManager.defaultVibrator
					}
					else
						context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
					
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
						vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
					else
						vibrator.vibrate(500)
				}
			}
			Row(modifier = Modifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
				if(input.showValue) {
					Text(
						ticks.value.toString(),
						fontSize = MaterialTheme.typography.headlineSmall.fontSize
					)
				} else {
					Text(
						stringResource(R.string.countdown_running)
					)
				}
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