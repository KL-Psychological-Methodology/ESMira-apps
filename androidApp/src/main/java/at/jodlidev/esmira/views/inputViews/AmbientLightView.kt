package at.jodlidev.esmira.views.inputViews

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButton

@Composable
fun AmbientLightView(input: Input, get: () -> String, save: (String) -> Unit) {
    val context = LocalContext.current
    val isScanning = remember { mutableStateOf(false) }

    if(isScanning.value) {
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    save(
                        if (input.numberHasDecimal) {
                            it.values.first().toString()
                        } else {
                            it.values.first().toInt().toString()
                        }
                    )
                    // Only need to get one value, so we stop scanning after the first update
                    isScanning.value = false
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val ambientLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            ambientLight?.let{sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_FASTEST)}

            onDispose {
                sensorManager.unregisterListener(sensorListener)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val value = (get().ifEmpty { "-" }) + " lx"
        Text(value)
        DefaultButton(text = stringResource(R.string.measure), onClick =  { isScanning.value = true })

    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNoValueAmbientLightView() {
    val input = DbLogic.createJsonObj<Input>("""
        {}
    """.trimIndent())
    ESMiraSurface {
        AmbientLightView(input, {""}) { }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAmbientLightView() {
    val input = DbLogic.createJsonObj<Input>("""
        {}
    """)
    ESMiraSurface {
        AmbientLightView(input, {"3141"}) { }
    }
}