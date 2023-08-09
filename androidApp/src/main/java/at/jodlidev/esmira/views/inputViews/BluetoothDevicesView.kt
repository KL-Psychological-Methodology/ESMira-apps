package at.jodlidev.esmira.views.inputViews

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import at.jodlidev.esmira.views.ESMiraDialog
import kotlinx.coroutines.delay
import org.json.JSONObject

/**
 * Created by JodliDev on 23.01.2023.
 */

const val totalScanSeconds = 60f

private fun getDevices(input: Input): Map<String, Short> {
	return try {
		DbLogic.createJsonObj(input.getAdditional("devices") ?: "")
	}
	catch(e: Throwable) {
		HashMap()
	}
}

class BluetoothScanner(val context: Context) {
	private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
	var devices = HashMap<String, Int>()
	var deviceCount = 0
	var isScanning = mutableStateOf(false)
	
	private var callback = object:ScanCallback() {
		override fun onScanResult(callbackType: Int, result: ScanResult?) {
			super.onScanResult(callbackType, result)
			
			val device = result?.device ?: return
			
			print("Found device ${device.address ?: "Error"}")
			device.address?.let {
				val hashed = Input.anonymizeValue(it)
				if(!devices.contains(hashed)) {
					++deviceCount
				}
				devices[hashed] = result.rssi
			}
		}
	}
	
	fun finishScanning() {
		try {
			if(bluetoothManager.adapter.bluetoothLeScanner != null) { //is null when bluetooth is disabled
				bluetoothManager.adapter.bluetoothLeScanner.flushPendingScanResults(callback)
				bluetoothManager.adapter.bluetoothLeScanner.stopScan(callback)
			}
		}
		catch(e: SecurityException) {
			// do nothing
		}
		
		isScanning.value = false
	}
	
	fun startScanning(): Boolean {
		if(bluetoothManager.adapter == null || !bluetoothManager.adapter.isEnabled || !context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
			Toast.makeText(context, R.string.error_bluetooth_disabled, Toast.LENGTH_SHORT).show()
			return false
		}
		else {
			isScanning.value = true
			devices = HashMap()
			deviceCount = 0
			try {
				bluetoothManager.adapter.bluetoothLeScanner.startScan(callback)
			}
			catch(e: SecurityException) { return false }
			return true
		}
	}
}

@Composable
fun BluetoothDevicesView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
	val context = LocalContext.current
	val bluetoothScanner = remember { BluetoothScanner(context) }
	
//	val isScanning = remember { mutableStateOf(false) }
	val progress = remember { mutableStateOf(0f) }
//	val startScanning = {
//		if(bluetoothScanner.startScanning())
//			isScanning.value = true
//	}
	
//	val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//	val isScanning = remember { mutableStateOf(false) }
//	val deviceCount = remember { mutableStateOf(get().toIntOrNull() ?: 0) }
//	val progress = remember { mutableStateOf(0f) }
//
//	var receiver: BroadcastReceiver? = null
//	var devices = HashMap<String, Short>()
//
//	val finishScanning = { saveValue: Boolean ->
//		bluetoothManager.adapter.cancelDiscovery()
//		if(receiver != null) {
//			context.unregisterReceiver(receiver)
//			receiver = null
//		}
//		if(saveValue) {
//			save(deviceCount.value.toString(), mapOf(Pair("devices", JSONObject(devices.toMap()).toString())))
//		}
//		isScanning.value = false
//	}
//
//	val startScanning = {
//		if(bluetoothManager.adapter == null || !bluetoothManager.adapter.isEnabled || !context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
//			Toast.makeText(context, R.string.error_bluetooth_disabled, Toast.LENGTH_SHORT).show()
//		else {
//			isScanning.value = true
//			deviceCount.value = 0
//			devices = HashMap()
//			receiver = object : BroadcastReceiver() {
//				override fun onReceive(context: Context, intent: Intent) {
//					when(intent.action) {
//						BluetoothDevice.ACTION_FOUND -> {
//							val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//							device?.address?.let {
//								val hashed = Input.anonymizeValue(it)
//								if(!devices.contains(hashed)) {
//									++deviceCount.value
//								}
//								devices[hashed] = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, -1)
//							}
//						}
//						BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
//							try {
//								bluetoothManager.adapter.startDiscovery();
//							}
//							catch(e: SecurityException) {
//								finishScanning(true)
//							}
//						}
//					}
//				}
//			}
//
//			val filter = IntentFilter().apply {
//				addAction(BluetoothDevice.ACTION_FOUND)
//				addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//			}
//			context.registerReceiver(receiver, filter)
//			bluetoothManager.adapter.startDiscovery()
//		}
//	}
	val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		arrayOf(
			Manifest.permission.BLUETOOTH,
			Manifest.permission.BLUETOOTH_SCAN,
			Manifest.permission.BLUETOOTH_ADMIN,
			Manifest.permission.ACCESS_FINE_LOCATION
		)
	else
		arrayOf(
			Manifest.permission.BLUETOOTH,
			Manifest.permission.BLUETOOTH_ADMIN,
			Manifest.permission.ACCESS_FINE_LOCATION
		)
	
	val permissionLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { permissionsMap  ->
		if(permissionsMap.values.reduce { acc, next -> acc && next })
			bluetoothScanner.startScanning()
	}
	
	val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
	DisposableEffect(lifecycleOwner) {
		onDispose {
			bluetoothScanner.finishScanning()
//			finishScanning(false)
		}
	}
	
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth()
	) {
		if(bluetoothScanner.isScanning.value) {
			LaunchedEffect(key1 = Unit, block = {
				progress.value = 0f
				val step = 1f / totalScanSeconds
				while (progress.value < 1) {
					delay(1000)
					progress.value += step
				}
				bluetoothScanner.finishScanning()
				save(bluetoothScanner.deviceCount.toString(), mapOf(Pair("devices", JSONObject(bluetoothScanner.devices.toMap()).toString())))
//				finishScanning(true)
			})
			
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
//				Text(deviceCount.value.toString())
				Text("${(progress.value * 100).toInt()}%")
				Spacer(modifier = Modifier.width(10.dp))
				LinearProgressIndicator(
					progress = progress.value,
					modifier = Modifier.padding(all = 10.dp)
				)
				
			}
		}
		else if(get().isNotEmpty()) {
			val showData = remember { mutableStateOf(false) }
			
			if(showData.value) {
				ESMiraDialog(
					content = {
						LazyVerticalGrid(
							columns = GridCells.Fixed(2),
							modifier = Modifier.fillMaxWidth(),
						) {
							item {
								Text(stringResource(R.string.anonymised_device),
									fontWeight = FontWeight.Bold,
									textAlign = TextAlign.Center,
									modifier = Modifier.padding(vertical = 5.dp)
								)
							}
							item {
								Text(stringResource(R.string.distance_rssi),
									fontWeight = FontWeight.Bold,
									textAlign = TextAlign.Center,
									modifier = Modifier.padding(vertical = 5.dp)
								)
							}
							
							val map = getDevices(input)
							for(entry in map) {
								item {
									Text(entry.key,
										textAlign = TextAlign.Center,
										modifier = Modifier.padding(vertical = 5.dp)
									)
								}
								item {
									Text(stringResource(R.string.distance_rssi_content, Input.rssiToDistance(entry.value.toInt()), entry.value.toInt()),
										textAlign = TextAlign.Center,
										modifier = Modifier.padding(vertical = 5.dp)
									)
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
			enabled = !bluetoothScanner.isScanning.value,
			onClick = {
				if(permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED } )
					bluetoothScanner.startScanning()
				else
					permissionLauncher.launch(permissions)
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