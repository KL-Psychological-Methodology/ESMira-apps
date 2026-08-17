package at.jodlidev.esmira.views.inputViews

import android.Manifest
import android.content.BroadcastReceiver
import android.net.wifi.WifiManager
import android.os.Build
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.wifi.ScanResult
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.sharedCode.data_structure.Input
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import at.jodlidev.esmira.views.ESMiraDialog
import kotlinx.coroutines.delay
import org.json.JSONObject

private fun getDevices(input: Input): Map<String, Short> {
    return try {
        DbLogic.createJsonObj(input.getAdditional("devices") ?: "")
    }
    catch(e: Throwable) {
        HashMap()
    }
}

@Composable
fun WlanDevicesView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
    val context = LocalContext.current
    val progress = remember { mutableStateOf(0f) }
    val wlanScanner = remember { WlanScanner(context) }

    DisposableEffect(Unit) {
        onDispose {
            wlanScanner.stopScanning()
        }
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    else arrayOf(
        Manifest.permission.NEARBY_WIFI_DEVICES,
        Manifest.permission.ACCESS_FINE_LOCATION    //necessary for methods startScan() and getScanResults()
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap  ->
        if(permissionsMap.values.reduce { acc, next -> acc && next })
            wlanScanner.startScanning()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if(wlanScanner.isScanning.value) {
            LaunchedEffect(key1 = Unit, block = {
                progress.value = 0f
                val step = 1f / totalScanSeconds
                while (progress.value < 1) {
                    delay(1000)
                    progress.value += step
                }
                wlanScanner.stopScanning()
                save(wlanScanner.deviceCount.toString(), mapOf(Pair("devices", JSONObject(wlanScanner.devices.toMap()).toString())))
            })

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(progress.value * 100).toInt()}%")
                Spacer(modifier = Modifier.width(10.dp))
                LinearProgressIndicator(
                    progress = { progress.value },
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
                                Text(stringResource(at.jodlidev.esmira.R.string.anonymised_device),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                            item {
                                Text(stringResource(at.jodlidev.esmira.R.string.distance_rssi),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }

                            val map = getDevices(input) //??
                            for(entry in map) {
                                item {
                                    Text(entry.key,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    )
                                }
                                item {
                                    Text(stringResource(at.jodlidev.esmira.R.string.distance_rssi_content, Input.rssiToDistance(entry.value.toInt()), entry.value.toInt()),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    },
                    confirmButtonLabel = stringResource(at.jodlidev.esmira.R.string.ok_),
                    onConfirmRequest = { showData.value = false }
                )
            }

            DefaultButton(
                stringResource(at.jodlidev.esmira.R.string.list_devices, getDevices(input).size),
                onClick = {
                    showData.value = true
                }
            )
        }

        DefaultButtonIconLeft(
            icon = Icons.Default.Wifi,
            text = stringResource(R.string.start_scanning),
            enabled = !wlanScanner.isScanning.value,
            onClick = {
                if(permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED } )
                    wlanScanner.startScanning()
                else
                    permissionLauncher.launch(permissions)
            }
        )
    }

}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewWifiDevicesView() {
    ESMiraSurface {
        val input = DbLogic.createJsonObj<Input>("""{}""")
        BluetoothDevicesView(
            input,
            { "" },
            { _, _ -> }
        )
    }
}

class WlanScanner(val context: Context) {

    var devices = HashMap<String, Int>()
    var deviceCount = 0
    var isScanning = mutableStateOf(false)
    private var isRegistered = true
    private val appContext = context.applicationContext




    val wifiManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        appContext.getSystemService(WifiManager::class.java)
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }
    val intentFilter = IntentFilter().apply {
        addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)

    }

    val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(appContext: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if(success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }


    private fun scanSuccess() {
        try {
            if(wifiManager != null) {
                val results = wifiManager.scanResults
                processResults(results)
            }
        } catch (e: SecurityException) {
           //do nothing
        }
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        try {
            if(wifiManager != null) {
                val results = wifiManager.scanResults
                processResults(results)


                //check how od the results are
                val timestampOldData = results.firstOrNull()?.timestamp

                if (timestampOldData == null) {
                    return
                }
                val currentTime = SystemClock.elapsedRealtimeNanos() / 1000
                val wlanDataAgeMinutes = (currentTime - timestampOldData) / 60_000_000

                if (wlanDataAgeMinutes < 10) {
                    processResults(results)
                }
            }
        } catch (e: SecurityException) {
            //do nothing
        }

    }

    private fun processResults(results: List<ScanResult>) {
        for (result in results) {
            val hashed = Input.anonymizeValue(result.BSSID)
            if(!devices.contains(hashed)) {
                ++deviceCount
            }
            devices[hashed] = result.level
        }
    }

   init {
       ContextCompat.registerReceiver(
           appContext,
           wifiScanReceiver,
           intentFilter,
           ContextCompat.RECEIVER_NOT_EXPORTED
       )
   }


    fun startScanning(){
        isScanning.value = true
        devices = HashMap()
        deviceCount = 0

        @Suppress("DEPRECATION")
        val success = wifiManager?.startScan()
    }
    fun stopScanning() {
        isScanning.value = false

        if(isRegistered) {
            appContext.unregisterReceiver(wifiScanReceiver)
            isRegistered = false
        }
    }


}



