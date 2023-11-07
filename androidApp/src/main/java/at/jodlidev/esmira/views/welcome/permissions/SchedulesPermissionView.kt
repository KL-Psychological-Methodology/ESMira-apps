package at.jodlidev.esmira.views.welcome.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.views.DefaultButton


enum class SchedulesPermissionViewState {
	PERMISSION, FAILED, SKIPPED
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SchedulesPermissionView(num: Int, currentNum: MutableState<Int>) {
	val success = rememberSaveable { mutableStateOf(true) }
	val state = rememberSaveable { mutableStateOf(SchedulesPermissionViewState.PERMISSION) }
	
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		PermissionHeaderView(
			num = num,
			currentNum = currentNum,
			success = success,
			header = stringResource(id = R.string.schedules),
			whatFor = stringResource(id = R.string.schedule_permission_setup_desc),
			modifier = Modifier.fillMaxWidth()
		)
		if(currentNum.value == num || !success.value) {
			Spacer(modifier = Modifier.width(10.dp))
			
			AnimatedContent(
				targetState = state.value,
				transitionSpec = {
					if(targetState > initialState){
						ContentTransform(
							targetContentEnter = slideInHorizontally { width -> width } + fadeIn(),
							initialContentExit = slideOutHorizontally { width -> -width } + fadeOut()
						)
					}
					else {
						ContentTransform(
							targetContentEnter = slideInHorizontally { width -> -width } + fadeIn(),
							initialContentExit = slideOutHorizontally { width -> width } + fadeOut()
						)
					}
				}
			) { currentState ->
				when(currentState) {
					SchedulesPermissionViewState.PERMISSION -> {
						SchedulesPermissionQuestionView({
							success.value = true
							++currentNum.value
						})
					}
					SchedulesPermissionViewState.FAILED -> {
						Column {
							SchedulesPermissionSkippedView(state)
							SchedulesPermissionTryAgainView(state) {
								state.value = SchedulesPermissionViewState.SKIPPED
								success.value = false
								++currentNum.value
							}
						}
					}
					SchedulesPermissionViewState.SKIPPED -> {
						SchedulesPermissionSkippedView(state)
					}
				}
			}
		}
	}
}

@Composable
fun SchedulesPermissionQuestionView(onFinish: () -> Unit, lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
	val context = LocalContext.current
	val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
	
	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
		DisposableEffect(lifecycleOwner) {
			val observer = LifecycleEventObserver { _, event ->
				if(event == Lifecycle.Event.ON_RESUME && alarmManager.canScheduleExactAlarms())
					onFinish()
			}
			lifecycleOwner.lifecycle.addObserver(observer)
			
			onDispose {
				lifecycleOwner.lifecycle.removeObserver(observer)
			}
		}
	}
	
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.schedule_permission_check))
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(stringResource(R.string.enable_schedules),
			onClick = {
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
					onFinish()
				else {
					context.startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
				}
			}
		)
	}
}

@Composable
fun SchedulesPermissionTryAgainView(state: MutableState<SchedulesPermissionViewState>, ignore: () -> Unit) {
	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
		DefaultButton(stringResource(R.string.ignore),
			onClick = ignore
		)
		
		DefaultButton(stringResource(R.string.try_again),
			onClick = {
				state.value = SchedulesPermissionViewState.PERMISSION
			}
		)
	}
}

@Composable
fun SchedulesPermissionSkippedView(state: MutableState<SchedulesPermissionViewState>) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.info_schedule_setup_skipped))
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(stringResource(R.string.try_again),
			onClick = {
				state.value = SchedulesPermissionViewState.PERMISSION
			}
		)
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewSchedulesPermissionView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(1) }
		SchedulesPermissionView(1, currentNum)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewSchedulesPermissionQuestionView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(SchedulesPermissionViewState.PERMISSION) }
		SchedulesPermissionQuestionView({})
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewSchedulesPermissionTryAgainView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(SchedulesPermissionViewState.FAILED) }
		SchedulesPermissionTryAgainView(currentNum) {}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewSchedulesPermissionSkippedView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(SchedulesPermissionViewState.SKIPPED) }
		SchedulesPermissionSkippedView(currentNum)
	}
}