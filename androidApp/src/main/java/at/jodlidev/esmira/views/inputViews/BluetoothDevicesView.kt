package at.jodlidev.esmira.views.inputViews

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import at.jodlidev.esmira.AlarmBox.Companion.registerReceiver
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.colorLineBackground1
import at.jodlidev.esmira.colorLineBackground2
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import at.jodlidev.esmira.views.DialogButton
import at.jodlidev.esmira.views.ESMiraDialog
import kotlinx.coroutines.*
import org.json.JSONObject

/**
 * Created by JodliDev on 23.01.2023.
 */

private fun getDevices(input: Input): Map<String, Short> {
	try {
		return DbLogic.createJsonObj(input.getAdditional("devices") ?: "")
	}
	catch(e: Throwable) {
		return HashMap()
	}
}

@Composable
fun BluetoothDevicesView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
	val context = LocalContext.current
	val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
	val isScanning = remember { mutableStateOf(false) }
	val deviceCount = remember { mutableStateOf(get().toIntOrNull() ?: 0) }
	
	var receiver: BroadcastReceiver? = null
	var devices = HashMap<String, Short>()
	
	val finishScanning = { saveValue: Boolean ->
		bluetoothManager.adapter.cancelDiscovery()
		if(receiver != null) {
			context.unregisterReceiver(receiver)
			receiver = null
		}
		if(saveValue) {
			save(deviceCount.value.toString(), mapOf(Pair("devices", JSONObject(devices.toMap()).toString())))
		}
		isScanning.value = false
	}
	
	val startScanning = {
		if(bluetoothManager.adapter == null || !bluetoothManager.adapter.isEnabled || !context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
			Toast.makeText(context, R.string.error_bluetooth_disabled, Toast.LENGTH_SHORT).show()
		else {
			isScanning.value = true
			deviceCount.value = 0
			devices = HashMap()
			receiver = object : BroadcastReceiver() {
				override fun onReceive(context: Context, intent: Intent) {
					when(intent.action) {
						BluetoothDevice.ACTION_FOUND -> {
							val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
							device?.address?.let {
								val hashed = it.hashCode().toUInt().toString()
								if(!devices.contains(hashed)) {
									devices[hashed] = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, -1)
									++deviceCount.value
								}
							}
						}
						BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
							finishScanning(true)
						}
					}
				}
			}
			
			val filter = IntentFilter().apply {
				addAction(BluetoothDevice.ACTION_FOUND)
				addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
			}
			context.registerReceiver(receiver, filter)
			bluetoothManager.adapter.startDiscovery()
		}
	}
	
	val permissionLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		if(isGranted)
			startScanning()
	}
	
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
	DisposableEffect(lifecycleOwner) {
		onDispose {
			finishScanning(false)
		}
	}
	
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth()
	) {
		if(isScanning.value) {
			Box(contentAlignment = Alignment.Center) {
				CircularProgressIndicator(
					modifier = Modifier
						.padding(all = 10.dp)
				)
				
				Spacer(modifier = Modifier.width(10.dp))
				Text(deviceCount.value.toString())
			}
		}
		else if(get().isNotEmpty()) {
			val showData = remember { mutableStateOf(false) }
			
			if(showData.value) {
				ESMiraDialog(
					content = {
						LazyColumn(
							modifier = Modifier.fillMaxWidth()
						) {
							
							val map = getDevices(input)
							var i = 0
							for(entry in map) {
								item {
									Text(
										"${entry.key}: ${entry.value}", modifier = Modifier
											.background(color = if(i % 2 != 0) colorLineBackground1 else colorLineBackground2)
											.fillMaxWidth()
											.padding(all = 5.dp),
										textAlign = TextAlign.Center
									)
									++i
								}
							}
						}
					},
					confirmButtonLabel = stringResource(R.string.ok_),
					onConfirmRequest = { showData.value = false }
				)
			}
			
			DefaultButton(
				stringResource(R.string.show_data),
				onClick = {
					showData.value = true
				}
			)
		}
		
		DefaultButtonIconLeft(
			icon = Icons.Default.Bluetooth,
			text = stringResource(R.string.start_scanning),
			enabled = !isScanning.value,
			onClick = {
				if(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED)
					startScanning()
				else
					permissionLauncher.launch(Manifest.permission.BLUETOOTH)
			}
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewBluetoothDevicesView() {
	ESMiraSurface {
		val input = DbLogic.createJsonObj<Input>("""{}""")
		BluetoothDevicesView(
			input,
			{ "" },
			{ _, _ -> }
		)
	}
}