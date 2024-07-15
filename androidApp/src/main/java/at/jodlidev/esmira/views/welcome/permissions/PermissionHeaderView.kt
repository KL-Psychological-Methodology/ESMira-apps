package at.jodlidev.esmira.views.welcome.permissions

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.views.DialogButton
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 20.12.2022.
 */
@Composable
fun PermissionHeaderView(
	num: Int,
	isActive: () -> Boolean,
	state: MutableState<DefaultPermissionState>,
	header: String,
	modifier: Modifier = Modifier,
	whatFor: String = ""
) {
	val openWhatForDialog = remember { mutableStateOf(false) }
	
	if(openWhatForDialog.value)
		WhatForDialog(openWhatForDialog, whatFor)
	
	Column(modifier = modifier.fillMaxWidth().alpha(if(isActive()) 1f else 0.3f)) {
		Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
			Text("$num.", fontSize = MaterialTheme.typography.titleLarge.fontSize)
			Spacer(modifier = Modifier.width(10.dp))
			Text(header, fontSize = MaterialTheme.typography.titleLarge.fontSize)
			Spacer(modifier = Modifier.width(10.dp))
			
			if(state.value == DefaultPermissionState.FAILED || state.value == DefaultPermissionState.SKIPPED)
				Icon(Icons.Default.Cancel, "Failed", tint = colorRed)
			else if(state.value == DefaultPermissionState.SUCCESS)
				Icon(Icons.Default.CheckCircle, "Finished", tint = colorGreen)
			
			if(whatFor.isNotEmpty()) {
				Spacer(modifier = Modifier.weight(1f))
				DialogButton(stringResource(R.string.what_for), onClick = { openWhatForDialog.value = true })
			}
		}
		Spacer(modifier = Modifier.height(10.dp))
	}
}

@Composable
fun WhatForDialog(openState: MutableState<Boolean>, whatFor: String) {
	ESMiraDialog(
		onDismissRequest = {
			openState.value = false
		},
		title = stringResource(R.string.what_for),
		confirmButtonLabel = stringResource(R.string.ok_),
		onConfirmRequest = { openState.value = false }
	) {
		Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
			Text(whatFor)
		}
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewPermissionHeaderDisabledView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.PERMISSION) }
		PermissionHeaderView(
			num = 2,
			isActive = { false },
			state = state,
			header = "Header",
			modifier = Modifier,
			whatFor = "Explanation"
		)
	}
}
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewPermissionHeaderEnabledView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.PERMISSION) }
		PermissionHeaderView(
			num = 2,
			isActive = { true },
			state = state,
			header = "Header",
			modifier = Modifier,
			whatFor = "Explanation"
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewPermissionFinishedHeaderView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.SUCCESS) }
		PermissionHeaderView(
			num = 1,
			isActive = { true },
			state = state,
			header = "Header",
			modifier = Modifier,
			whatFor = "Explanation"
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewPermissionFailedHeaderView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.FAILED) }
		PermissionHeaderView(
			num = 1,
			isActive = { true },
			state = state,
			header = "Header",
			modifier = Modifier
		)
	}
}