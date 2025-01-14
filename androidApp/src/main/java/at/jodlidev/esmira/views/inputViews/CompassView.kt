package at.jodlidev.esmira.views.inputViews

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.colorGreen
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButton
import kotlin.math.roundToInt

/**
 * Created by JodliDev on 23.01.2023.
 */


private class CompassHelper {
	fun calculateHeading(accelerometerReading: FloatArray, magnetometerReading: FloatArray): Float {
		var Ax = accelerometerReading[0]
		var Ay = accelerometerReading[1]
		var Az = accelerometerReading[2]
		val Ex = magnetometerReading[0]
		val Ey = magnetometerReading[1]
		val Ez = magnetometerReading[2]
		
		//cross product of the magnetic field vector and the gravity vector
		var Hx = Ey * Az - Ez * Ay
		var Hy = Ez * Ax - Ex * Az
		var Hz = Ex * Ay - Ey * Ax
		
		//normalize the values of resulting vector
		val invH = 1.0f / Math.sqrt((Hx * Hx + Hy * Hy + Hz * Hz).toDouble()).toFloat()
		Hx *= invH
		Hy *= invH
		Hz *= invH
		
		//normalize the values of gravity vector
		val invA = 1.0f / Math.sqrt((Ax * Ax + Ay * Ay + Az * Az).toDouble()).toFloat()
		Ax *= invA
		Ay *= invA
		Az *= invA
		
		//cross product of the gravity vector and the new vector H
		val Mx = Ay * Hz - Az * Hy
		val My = Az * Hx - Ax * Hz
		val Mz = Ax * Hy - Ay * Hx
		
		//arctangent to obtain heading in radians
		return Math.atan2(Hy.toDouble(), My.toDouble()).toFloat()
	}
	
	
	fun convertRadtoDeg(rad: Float): Float {
		return (rad / Math.PI).toFloat() * 180
	}
	
	//map angle from [-180,180] range to [0,360] range
	fun map180to360(angle: Float): Float {
		return (angle + 360) % 360
	}
}

@Composable
fun CompassView(input: Input, get: () -> String, save: (String) -> Unit) {
	val alpha = 0.05F
	
	val context = LocalContext.current
	val isScanning = remember { mutableStateOf(false) }
	val azimuth = remember { mutableStateOf(0F) }
	val didReceiveReading = remember { mutableStateOf(false) }
	
	if(isScanning.value) {
		val accelerometerReading = FloatArray(3)
		val magnetometerReading = FloatArray(3)
		val lowPassFilter = { sensorInput: FloatArray, sensorOutput: FloatArray ->
			for((i, entry) in sensorInput.withIndex()) {
				sensorOutput[i] = sensorOutput[i] + alpha * (entry - sensorOutput[i])
			}
		}
		
		val sensorListener = object : SensorEventListener {
			override fun onSensorChanged(event: SensorEvent?) {
				when(event?.sensor?.type) {
					Sensor.TYPE_ACCELEROMETER -> {
						lowPassFilter(event.values, accelerometerReading);
					}
					Sensor.TYPE_MAGNETIC_FIELD -> {
						lowPassFilter(event.values, magnetometerReading);
					}
				}
				
				val r = FloatArray(9)
				val i = FloatArray(9)
				if(SensorManager.getRotationMatrix(r, i, accelerometerReading, magnetometerReading)) {
					val orientation = FloatArray(3)
					SensorManager.getOrientation(r, orientation)
					didReceiveReading.value = true
					azimuth.value = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
				}
			}
			
			override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
		}
		
		val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
		DisposableEffect(lifecycleOwner) {
			val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
			val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
			if(accelerometer != null) {
				sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
			}
			
			val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
			if(magneticField != null) {
				sensorManager.registerListener(sensorListener, magneticField, SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_FASTEST)
			}
			
			onDispose {
				sensorManager.unregisterListener(sensorListener)
			}
		}
		
		
		CompassContentView(
			rotation = { azimuth.value },
			showValue = input.showValue,
			infoLabel = "${azimuth.value.toInt()}°",
			buttonLabel = stringResource(R.string.stop_scanning),
			buttonAction = {
				if(didReceiveReading.value) {
					if (input.numberHasDecimal) {
						save(azimuth.value.toString())
					} else {
						save(azimuth.value.toInt().toString())
					}
				}
				isScanning.value = false
			}
		)
	}
	else if(get() == "") {
		CompassContentView(
			rotation = { 0F },
			showValue = false,
			infoLabel = "",
			buttonLabel = stringResource(R.string.start_scanning),
			buttonAction = {
				isScanning.value = true
			}
		)
	}
	else {
		val value = try { get().toFloat() } catch(e: NumberFormatException) { 0F }
		CompassContentView(
			rotation = { value },
			showValue = input.showValue,
			infoLabel = "${value.toInt()}°",
			buttonLabel = stringResource(R.string.start_scanning),
			buttonAction = {
				isScanning.value = true
			}
		)
	}
}

@Composable
fun CompassContentView(
	rotation: () -> Float,
	showValue: Boolean,
	infoLabel: String,
	buttonLabel: String,
	buttonAction: () -> Unit
) {
	val borderColor = MaterialTheme.colorScheme.outline
	val borderStroke = Stroke(
		width = 10F,
		pathEffect = PathEffect.dashPathEffect(floatArrayOf(20F, 20F), 0F)
	)
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Box(
			modifier = Modifier.size(200.dp)
		) {
			Box(
				modifier = Modifier
					.size(200.dp)
					.clip(CircleShape)
					.rotate(-rotation())
					.drawBehind {
						drawCircle(color = borderColor, style = borderStroke)
					}
					.padding(3.dp)
			) {
				if(showValue) {
					Text(stringResource(R.string.north_abr), modifier = Modifier.align(Alignment.TopCenter))
					Text(stringResource(R.string.east_abr), modifier = Modifier.align(Alignment.CenterEnd).rotate(90F))
					Text(stringResource(R.string.south_abr), modifier = Modifier.align(Alignment.BottomCenter).rotate(180F))
					Text(stringResource(R.string.west_abr), modifier = Modifier.align(Alignment.CenterStart).rotate(270F))
				}
			}
			Icon(
				Icons.Default.ArrowDropUp, "up", modifier = Modifier
					.align(Alignment.TopCenter)
					.alpha(0.3F)
					.size(50.dp)
			)
			Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
				if(showValue) {
					Text(
						infoLabel,
						fontSize = MaterialTheme.typography.headlineSmall.fontSize
					)
				}
				DefaultButton(text = buttonLabel, onClick = buttonAction)
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewCompassView() {
	ESMiraSurface {
		CompassContentView(
			rotation = { 60F },
			showValue = true,
			infoLabel = "60°",
			buttonLabel = "action",
			buttonAction = {}
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewCompassViewWithoutValue() {
	ESMiraSurface {
		CompassContentView(
			rotation = { 60F },
			showValue = false,
			infoLabel = "60°",
			buttonLabel = "action",
			buttonAction = {}
		)
	}
}