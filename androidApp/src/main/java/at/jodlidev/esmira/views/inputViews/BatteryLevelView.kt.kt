package at.jodlidev.esmira.views.inputViews

import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.BatteryManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButton

@Composable
fun BatteryLevelView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
    val context = LocalContext.current
    val value = remember { mutableStateOf(get()) }
    val charging = remember { mutableStateOf(input.getAdditional("charging") ?: "") }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        var text = (value.value.ifEmpty{ "-" }) + " %"
        if(charging.value == "1") { text += " (" + stringResource(R.string.charging) + ")" }
        Text(text)
        DefaultButton(text = stringResource(R.string.measure), onClick = {
            val batteryStatus: Intent? =
                IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                    context.registerReceiver(null, ifilter)
                }
            batteryStatus?.let { status ->
                val scale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val level = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val chargeStatus = status.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = chargeStatus == BatteryManager.BATTERY_STATUS_CHARGING || chargeStatus == BatteryManager.BATTERY_STATUS_FULL
                if (scale != -1 && level != -1) {
                    val percentage = level * 100 / scale
                    value.value = percentage.toString()
                    charging.value = if(isCharging) { "1" } else { "0" }
                    save(value.value, mapOf("charging" to charging.value))

                }
            }
        })
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewBatteryLevelView() {
    val input = DbLogic.createJsonObj<Input>( """
        {}
    """)
    ESMiraSurface {
        BatteryLevelView(input, { "73" }) {_, _ -> }
    }
}