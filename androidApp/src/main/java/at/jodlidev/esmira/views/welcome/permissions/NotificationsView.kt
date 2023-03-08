package at.jodlidev.esmira.views.welcome.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Looks3
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.androidNative.Notifications
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.DefaultButtonIconLeft

/**
 * Created by JodliDev on 20.12.2022.
 * Thanks to https://betterprogramming.pub/exploring-animatedcontent-and-crossfade-in-jetpack-compose-45374c6b842
 */

enum class NotificationViewState {
	PERMISSION, SEND_TEST_NOTIFICATION, ASK_TEXT_NOTIFICATION, FAILED, SKIPPED
}
fun intentExists(context: Context, intent: Intent): Boolean {
	var dndIntent = Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
	val list = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		context.packageManager.queryIntentActivities(dndIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
	else
		context.packageManager.queryIntentActivities(dndIntent, PackageManager.MATCH_DEFAULT_ONLY)
	return list.size > 0
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotificationsView(num: Int, currentNum: MutableState<Int>) {
	val success = rememberSaveable { mutableStateOf(true) }
	val state = rememberSaveable { mutableStateOf(NotificationViewState.PERMISSION) }
	
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		PermissionHeaderView(
			num = num,
			currentNum = currentNum,
			success = success,
			header = stringResource(id = R.string.notifications),
			whatFor = stringResource(id = R.string.notification_setup_desc),
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
					NotificationViewState.PERMISSION -> {
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
							NotificationPermissionView(state)
						else
							NotificationSendView(state)
					}
					NotificationViewState.SEND_TEST_NOTIFICATION -> {
						NotificationSendView(state)
					}
					NotificationViewState.ASK_TEXT_NOTIFICATION -> {
						NotificationAskView(state) {
							success.value = true
							++currentNum.value
						}
					
					}
					NotificationViewState.FAILED -> {
						Column {
							NotificationFailedView()
							
							Spacer(modifier = Modifier.height(20.dp))
							
							NotificationTryAgainView(state) {
								state.value = NotificationViewState.SKIPPED
								success.value = false
								++currentNum.value
							}
						}
					}
					NotificationViewState.SKIPPED -> {
						NotificationSkippedView(state)
					}
				}
			}
		}
	}
}

@Composable
fun NotificationPermissionView(state: MutableState<NotificationViewState>) {
	val launcher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		if(isGranted)
			state.value = NotificationViewState.SEND_TEST_NOTIFICATION
		else
			state.value = NotificationViewState.FAILED
	}
	val context = LocalContext.current
	
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.notification_permission_check))
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(stringResource(R.string.enable_notifications),
			onClick = {
				if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
					state.value = NotificationViewState.SEND_TEST_NOTIFICATION
				else
					launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
			}
		)
	}
}

@Composable
fun NotificationSendView(state: MutableState<NotificationViewState>) {
	val context = LocalContext.current
	
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.info_test_notifications))
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(stringResource(R.string.send_test_notification),
			onClick = {
				NativeLink.notifications.remove(Notifications.TEST_ID)
				NativeLink.notifications.fire(
					context.getString(R.string.notification_test_name),
					context.getString(R.string.notification_test_desc),
					Notifications.TEST_ID
				)
				state.value = NotificationViewState.ASK_TEXT_NOTIFICATION
			}
		)
	}
}

@Composable
fun NotificationAskView(state: MutableState<NotificationViewState>, onFinish: () -> Unit) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.ask_notification_posted))
		Spacer(modifier = Modifier.width(10.dp))
		Row (horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
			DefaultButton(stringResource(R.string.no),
				onClick = {
					NativeLink.notifications.remove(Notifications.TEST_ID)
					state.value = NotificationViewState.FAILED
				}
			)
			
			DefaultButton(stringResource(R.string.yes),
				onClick = {
					NativeLink.notifications.remove(Notifications.TEST_ID)
					onFinish()
				}
			)
		}
	}
}

@Composable
fun NotificationTryAgainView(state: MutableState<NotificationViewState>, ignore: () -> Unit) {
	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
		DefaultButton(stringResource(R.string.ignore),
			onClick = ignore
		)
		
		DefaultButton(stringResource(R.string.try_again),
			onClick = {
				state.value = NotificationViewState.PERMISSION
			}
		)
	}
}

@Composable
fun NotificationFailedView() {
	val context = LocalContext.current
	
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.info_notification_additional_settings))
		
		Spacer(modifier = Modifier.height(10.dp))
		DefaultButtonIconLeft(
			text = stringResource(R.string.open_sound_settings),
			icon = Icons.Default.LooksOne,
			onClick = {
				context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
			},
			modifier = Modifier.fillMaxWidth(),
			textModifier = Modifier.weight(1F)
		)
		
		Spacer(modifier = Modifier.height(5.dp))
		DefaultButtonIconLeft(
			text = stringResource(R.string.open_notification_settings),
			icon = Icons.Default.LooksTwo,
			onClick = {
				val notificationIntent = Intent()
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					notificationIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
					notificationIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
					if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
						notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				}
				else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					notificationIntent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
					notificationIntent.putExtra("app_package", context.packageName)
					notificationIntent.putExtra("app_uid", context.applicationInfo.uid)
				}
				else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
					notificationIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
					notificationIntent.addCategory(Intent.CATEGORY_DEFAULT)
					notificationIntent.data = Uri.parse("package:" + context.packageName)
				}
				context.startActivity(notificationIntent)
			},
			modifier = Modifier.fillMaxWidth(),
			textModifier = Modifier.weight(1F)
		)
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Spacer(modifier = Modifier.height(5.dp))
			DefaultButtonIconLeft(
				text = stringResource(R.string.open_doNotDisturb_settings),
				icon = Icons.Default.Looks3,
				onClick = {
					var dndIntent = Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
					
					if(intentExists(context, dndIntent))
						context.startActivity(dndIntent)
					else {
						dndIntent = Intent("android.settings.ZEN_MODE_SETTINGS")
						if(intentExists(context, dndIntent))
							context.startActivity(dndIntent)
					}
				},
				modifier = Modifier.fillMaxWidth(),
				textModifier = Modifier.weight(1F)
			)
		}
	}
}

@Composable
fun NotificationSkippedView(state: MutableState<NotificationViewState>) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.info_notification_setup_failed))
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(stringResource(R.string.try_again),
			onClick = {
				state.value = NotificationViewState.SEND_TEST_NOTIFICATION
			}
		)
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationsView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(1) }
		NotificationsView(1, currentNum)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationPermissionView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(NotificationViewState.PERMISSION) }
		NotificationPermissionView(currentNum)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationSendView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(NotificationViewState.SEND_TEST_NOTIFICATION) }
		NotificationSendView(currentNum)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationAskView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(NotificationViewState.ASK_TEXT_NOTIFICATION) }
		NotificationAskView(currentNum) {}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationFailedView() {
	ESMiraSurface {
		NotificationFailedView()
	}
}
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationTryAgainView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(NotificationViewState.ASK_TEXT_NOTIFICATION) }
		NotificationTryAgainView(currentNum) {}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationSkippedView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(NotificationViewState.FAILED) }
		NotificationSkippedView(currentNum)
	}
}