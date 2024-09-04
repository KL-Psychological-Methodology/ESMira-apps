package at.jodlidev.esmira.views.inputViews

import android.content.Context
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.R
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationRequest
import android.os.Build
import android.os.CancellationSignal
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.H3
import at.jodlidev.esmira.sharedCode.LatLng
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import kotlinx.coroutines.delay
import java.security.Security
import kotlin.math.max
import kotlin.math.min

const val maxScanSeconds = 60f
const val maxScanMilliS = 60L * 1000L
const val maxLocationAgeNanoS = 5L * 60L * 1000L * 1000L * 1000L
const val minResolution = 0
const val maxResolution = 15

const val h3ViewerBaseURL = "https://wolf-h3-viewer.glitch.me/?h3="

class LocationScanner(val context: Context, private val resolution: Int, private val save: (String) -> Unit): LocationListener {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var isScanning = mutableStateOf(false)
    var result = mutableStateOf(ScanResult.NONE)
    var index = ""
    private var cancellationSignal: CancellationSignal? = null
    fun startScanning(): Boolean {
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            Toast.makeText(context, R.string.error_gps_disabled, Toast.LENGTH_SHORT).show()
            return false
        } else {
            isScanning.value = true

            val cachedLocation = getCachedLocation()
            if(cachedLocation != null) {
                setLocation(cachedLocation)
                return true
            }

            return getCurrentLocation()
        }
    }

    fun stopScanning() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            cancellationSignal?.cancel()
        } else {
            locationManager.removeUpdates(this)
        }
        isScanning.value = false
    }

    private fun getCachedLocation(): Location? {
        val location: Location? = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            } catch (e: SecurityException) {
                null
            }
        } else {
            try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) {
                null
            }
        }
        return location?.let {
            if((location.elapsedRealtimeNanos - SystemClock.elapsedRealtimeNanos()) > maxLocationAgeNanoS) {
                location
            } else {
                null
            }
        }
    }

    private fun getCurrentLocation(): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val provider = LocationManager.FUSED_PROVIDER
            val locationRequest = LocationRequest.Builder(maxScanMilliS).setQuality(LocationRequest.QUALITY_LOW_POWER).build()
            cancellationSignal = CancellationSignal()
            try {
                locationManager.getCurrentLocation(provider, locationRequest, cancellationSignal, context.mainExecutor) { setLocation(it) }
            } catch (e: SecurityException) {
                setLocation(null)
                return false
            }
            return true
        } else {
            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null)
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null)
            } catch (e: SecurityException) {
                setLocation(null)
                return false
            }
            return true
        }
    }

    private fun setLocation(location: Location?) {
        index = ""
        location?.let {
            val geo = LatLng.fromDegrees(location.latitude, location.longitude)
            val effectiveResolution = min(maxResolution, max(minResolution, resolution))
            val indexResult = geo.latLngToCellResult(effectiveResolution)
            indexResult.getOrNull()?.let{
                index = H3.h3toString(it)
            }
        }
        save(index)
        result.value = if(index.isNotEmpty()) ScanResult.SUCCESS else ScanResult.FAIL
        isScanning.value = false
    }

    override fun onLocationChanged(location: Location) {
        setLocation(location)
    }

    override fun onProviderDisabled(provider: String) {
    }

    override fun onProviderEnabled(provider: String) {
    }
    
    enum class ScanResult {
        NONE,
        SUCCESS,
        FAIL
    }
}

@Composable
fun LocationView(input: Input, get: () -> String, save: (String) -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val locationScanner = remember { LocationScanner(context, input.resolution, save) }

    val progress = remember { mutableStateOf(0f) }
    val ellipsisDots = remember { mutableStateOf(0) }

    val permission = Manifest.permission.ACCESS_FINE_LOCATION

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if(it) locationScanner.startScanning()
    }

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        onDispose {
            locationScanner.stopScanning()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if(locationScanner.isScanning.value) {
            LaunchedEffect(key1 = Unit, block = {
                progress.value = 0f
                val step = 1f / maxScanSeconds
                while(progress.value < 1 && locationScanner.isScanning.value) {
                    delay(1000)
                    ellipsisDots.value = (ellipsisDots.value + 1) % 3
                    progress.value += step
                }
                locationScanner.stopScanning()
            })

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.scanning) + ellipsisDots.value.let<Int, String> {
                    val ellipsis = StringBuilder()
                    for(i in 0..it) ellipsis.append(".")
                    ellipsis.toString()
                })
                Spacer(modifier = Modifier.width(10.dp))
            }
        } else if(locationScanner.result.value != LocationScanner.ScanResult.NONE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when(locationScanner.result.value) {
                    LocationScanner.ScanResult.SUCCESS -> DefaultButtonIconLeft(
                        text = stringResource(R.string.found_location, locationScanner.index),
                        icon = Icons.Default.Check,
                        onClick = {
                            val url = StringBuilder().append(h3ViewerBaseURL)
                                .append(locationScanner.index).toString()
                            try { uriHandler.openUri(url) } catch (e: Throwable) {
                                ErrorBox.warn("LocationView", "$url is not a valid URL!")
                            }
                        }
                    )
                    LocationScanner.ScanResult.FAIL -> Text(stringResource(R.string.could_not_determine_location))
                    else -> {}
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
        }

        DefaultButtonIconLeft(
            text = stringResource(R.string.start_scanning),
            icon = Icons.Default.LocationOn,
            enabled = !locationScanner.isScanning.value,
            onClick = {
                if(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                    locationScanner.startScanning()
                else
                    permissionLauncher.launch(permission)
            }
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewLocationView() {
    ESMiraSurface {
        val input = DbLogic.createJsonObj<Input>("""{}""")
        LocationView(input = input, get = { "" }, save = {_ -> })
    }
}