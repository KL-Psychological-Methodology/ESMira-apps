package at.jodlidev.esmira.views.welcome.permissions

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R


@Composable
fun SchedulesPermissionView(
	num: Int,
	isActive: () -> Boolean,
	isCurrent: () -> Boolean,
	goNext: () -> Unit,
	hasPermission: (context: Context) -> Boolean = { context ->
		val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
		Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
	}
) {
	val state = rememberSaveable { mutableStateOf(DefaultPermissionState.PERMISSION) }

	val onFinish = {
		state.value = DefaultPermissionState.SUCCESS
		goNext()
	}
	
	val context = LocalContext.current
	
	if(!hasPermission(context)) {
		val lifecycleOwner = LocalLifecycleOwner.current
		DisposableEffect(lifecycleOwner) {
			val observer = LifecycleEventObserver { _, event ->
				if(event == Lifecycle.Event.ON_RESUME && hasPermission(context))
					onFinish()
			}
			lifecycleOwner.lifecycle.addObserver(observer)

			onDispose {
				lifecycleOwner.lifecycle.removeObserver(observer)
			}
		}
	}
	
	DefaultPermissionView(
		num = num,
		header = stringResource(id = R.string.schedules),
		whatFor = stringResource(id = R.string.schedule_permission_setup_desc),
		description = stringResource(id = R.string.schedule_permission_check),
		buttonLabel = stringResource(id = R.string.enable_schedules),
		state = state,
		isActive = isActive,
		isCurrent = isCurrent,
		goNext = goNext,
		onClick = {
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S || hasPermission(context))
				onFinish()
			else {
				context.startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
			}
		},
	)
}



@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewSchedulesPermissionView() {
	ESMiraSurface {
		SchedulesPermissionView(
			num = 1,
			isActive = { true },
			isCurrent = { true },
			goNext = {},
			hasPermission = { false }
		)
	}
}