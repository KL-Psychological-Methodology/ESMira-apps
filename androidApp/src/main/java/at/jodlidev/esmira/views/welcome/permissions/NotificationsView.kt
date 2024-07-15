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
import androidx.annotation.RequiresApi
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
	NONE, SEND_TEST_NOTIFICATION, ASK_TEXT_NOTIFICATION
}

fun intentExists(context: Context, dndIntent: Intent): Boolean {
	val list = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		context.packageManager.queryIntentActivities(
			dndIntent,
			PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
		)
	else
		context.packageManager.queryIntentActivities(dndIntent, PackageManager.MATCH_DEFAULT_ONLY)
	return list.size > 0
}


@Composable
fun NotificationsView(
	num: Int,
	isActive: () -> Boolean,
	isCurrent: () -> Boolean,
	goNext: () -> Unit
) {
	val state = rememberSaveable { mutableStateOf(DefaultPermissionState.PERMISSION) }
	val additionalState = rememberSaveable {
		mutableStateOf(
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
				NotificationViewState.NONE
			else
				NotificationViewState.SEND_TEST_NOTIFICATION
		)
	}
	
	val launcher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		if(isGranted)
			additionalState.value = NotificationViewState.SEND_TEST_NOTIFICATION
		else
			state.value = DefaultPermissionState.FAILED
	}
	
	val context = LocalContext.current
	
	DefaultPermissionView(
		num = num,
		header = stringResource(id = R.string.notifications),
		whatFor = stringResource(id = R.string.notification_setup_desc),
		description = stringResource(id = R.string.notification_permission_check),
		buttonLabel = stringResource(id = R.string.enable_notifications),
		state = state,
		isActive = isActive,
		isCurrent = isCurrent,
		goNext = goNext,
		onClick = {
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
				|| ContextCompat.checkSelfPermission(
					context,
					Manifest.permission.POST_NOTIFICATIONS
				) == PackageManager.PERMISSION_GRANTED
			)
				additionalState.value = NotificationViewState.SEND_TEST_NOTIFICATION
			else
				launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
		},
		overrideView = {
			when(additionalState.value) {
				NotificationViewState.NONE -> {
					if(state.value == DefaultPermissionState.FAILED) {
						Column(
							modifier = Modifier.fillMaxWidth(),
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							NotificationFailedView()
							
							NotificationTryAgainView(
								onIgnore = {
									state.value = DefaultPermissionState.SKIPPED
									goNext()
								},
								onTryAgain = {
									state.value = DefaultPermissionState.PERMISSION
								}
							)
						}
						return@DefaultPermissionView true
					} else
						return@DefaultPermissionView false
				}
				
				NotificationViewState.SEND_TEST_NOTIFICATION -> {
					NotificationSendView {
						NativeLink.notifications.remove(Notifications.TEST_ID)
						NativeLink.notifications.fire(
							context.getString(R.string.notification_test_name),
							context.getString(R.string.notification_test_desc),
							Notifications.TEST_ID
						)
						additionalState.value = NotificationViewState.ASK_TEXT_NOTIFICATION
					}
					return@DefaultPermissionView true
				}
				
				NotificationViewState.ASK_TEXT_NOTIFICATION -> {
					NotificationAskView({
						NativeLink.notifications.remove(Notifications.TEST_ID)
						additionalState.value = NotificationViewState.NONE
						state.value = DefaultPermissionState.FAILED
					}) {
						NativeLink.notifications.remove(Notifications.TEST_ID)
						additionalState.value = NotificationViewState.NONE
						state.value = DefaultPermissionState.SUCCESS
						goNext()
					}
					return@DefaultPermissionView true
				}
			}
		},
	)
}

@Composable
fun NotificationSendView(onClick: () -> Unit) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.info_test_notifications))
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(
			stringResource(R.string.send_test_notification),
			onClick = onClick
		)
	}
}

@Composable
fun NotificationAskView(onFail: () -> Unit, onFinish: () -> Unit) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.ask_notification_posted))
		Spacer(modifier = Modifier.width(10.dp))
		Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
			DefaultButton(
				stringResource(R.string.no),
				onClick = onFail
			)
			
			DefaultButton(
				stringResource(R.string.yes),
				onClick = onFinish
			)
		}
	}
}


@Composable
fun NotificationTryAgainView(onIgnore: () -> Unit, onTryAgain: () -> Unit) {
	Spacer(modifier = Modifier.height(20.dp))
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceAround
	) {
		DefaultButton(
			stringResource(R.string.ignore),
			onClick = onIgnore
		)
		
		DefaultButton(
			stringResource(R.string.try_again),
			onClick = onTryAgain
		)
	}
}

/**
 * Is also used in NotificationsBrokenDialogActivity. So we need to add the retry button outside of this method
 */
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
				} else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					notificationIntent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
					notificationIntent.putExtra("app_package", context.packageName)
					notificationIntent.putExtra("app_uid", context.applicationInfo.uid)
				} else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
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


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationsView() {
	ESMiraSurface {
		NotificationsView(1, { true }, { true }, {})
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationSendView() {
	ESMiraSurface {
		NotificationSendView {}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNotificationAskView() {
	ESMiraSurface {
		NotificationAskView({ }, { })
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