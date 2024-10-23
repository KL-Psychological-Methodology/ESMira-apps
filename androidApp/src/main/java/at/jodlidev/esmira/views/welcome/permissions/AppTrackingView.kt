package at.jodlidev.esmira.views.welcome.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R

/**
 * Created by JodliDev on 20.12.2022.
 */

fun checkPermission(context: Context) : Boolean {
	if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
		return false
	
	val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
	
	val mode = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
		appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), context.packageName)
	else
		appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
	
	return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun AppTrackingView(num: Int, isActive: () -> Boolean, isCurrent: () -> Boolean, goNext: () -> Unit, buildVersion: Int = Build.VERSION.SDK_INT) {
	val state = rememberSaveable { mutableStateOf(DefaultPermissionState.PERMISSION) }
	val context = LocalContext.current
	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		if(checkPermission(context)) {
			state.value = DefaultPermissionState.SUCCESS
			goNext()
		}
		else {
			state.value = DefaultPermissionState.FAILED
			goNext()
		}
	}

	DefaultPermissionView(
		num = num,
		header = stringResource(id = R.string.app_usage),
		whatFor = stringResource(id = R.string.app_usage_whatFor),
		description = stringResource(id = R.string.app_usage_desc),
		buttonLabel = stringResource(id = R.string.open_settings),
		state = state,
		isActive = isActive,
		isCurrent = isCurrent,
		goNext = goNext,
		onClick = {
			if(checkPermission(context)) {
				state.value = DefaultPermissionState.SUCCESS
				goNext()
			}
			else
				launcher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
		},
		overrideView = {
			if(buildVersion > Build.VERSION_CODES.LOLLIPOP)
				return@DefaultPermissionView false

			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Text(stringResource(R.string.feature_is_not_supported))
				Spacer(modifier = Modifier.width(10.dp))
				DefaultButton(stringResource(R.string.continue_), onClick = {
					state.value = DefaultPermissionState.FAILED
					goNext()
				})
			}
			return@DefaultPermissionView true
		}
	)
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppTrackingView() {
	ESMiraSurface {
		AppTrackingView(1, { true }, { true }, {}, Build.VERSION_CODES.LOLLIPOP+1)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewOutdatedAppTrackingView() {
	ESMiraSurface {
		AppTrackingView(1, { true }, { true }, {}, Build.VERSION_CODES.LOLLIPOP)
	}
}
