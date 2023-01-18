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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.DefaultButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study

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
fun AppTrackingView(num: Int, currentNum: MutableState<Int>) {
	val success = rememberSaveable { mutableStateOf(true) }
	val context = LocalContext.current
	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		if(checkPermission(context)) {
			success.value = true
			++currentNum.value
		}
		else {
			success.value = false
			++currentNum.value
		}
	}
	
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		PermissionHeaderView(
			num = num,
			currentNum = currentNum,
			success = success,
			header = stringResource(id = R.string.app_usage),
			whatFor = stringResource(id = R.string.app_usage_whatFor),
			modifier = Modifier.fillMaxWidth()
		)
		
		if(currentNum.value == num) {
			Spacer(modifier = Modifier.width(10.dp))
			
			if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
				AppTrackingContentScaffold(R.string.feature_is_not_supported, R.string.continue_) {
					success.value = false
					++currentNum.value
				}
			}
			else {
				AppTrackingContentScaffold(R.string.app_usage_desc, R.string.open_settings) {
					if(checkPermission(context)) {
						success.value = true
						++currentNum.value
					}
					else
						launcher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
				}
			}
		}
		
	}
}

@Composable
fun AppTrackingContentScaffold(desc: Int, btnLabel: Int, onContinue: () -> Unit) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = desc))
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(
			onClick = onContinue
		) {
			Text(stringResource(id = btnLabel))
		}
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppTrackingNotSupported() {
	ESMiraSurface {
		AppTrackingContentScaffold(R.string.feature_is_not_supported, R.string.continue_) {}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppTrackingSupported() {
	ESMiraSurface {
		AppTrackingContentScaffold(R.string.app_usage_desc, R.string.open_settings) {}
	}
}
