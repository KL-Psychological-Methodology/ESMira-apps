package at.jodlidev.esmira.views.elements

import android.app.TimePickerDialog
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import java.util.*
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 22.02.2023.
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeButtonView(
	get: () -> Long,
	save: (Long) -> Unit,
	modifier: Modifier = Modifier
) {
	val text = remember { mutableStateOf(NativeLink.formatTime(get())) }
	val showTimeInputDialog = remember { mutableStateOf(false) }
	val calendar = Calendar.getInstance()
	calendar.timeInMillis = get()
	val timePickerState = rememberTimePickerState(
		initialHour = calendar.get(Calendar.HOUR_OF_DAY),
		initialMinute = calendar.get(Calendar.MINUTE),
		is24Hour = DateFormat.is24HourFormat(LocalContext.current),
	)

	if(showTimeInputDialog.value) {
		ESMiraDialog(
			confirmButtonLabel = stringResource(R.string.ok_),
			dismissButtonLabel = stringResource(R.string.cancel),
			onConfirmRequest = {
				calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
				calendar.set(Calendar.MINUTE, timePickerState.minute)
				save(calendar.timeInMillis)
				text.value = NativeLink.formatTime(get())
				showTimeInputDialog.value = false
			},
			onDismissRequest = {
				showTimeInputDialog.value = false
			}
		) {
			Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
				TimeInput(timePickerState)
			}
		}
	}
	
	DefaultButtonIconLeft(
		text = text.value,
		icon = Icons.Default.AccessTime,
		onClick = {
			timePickerState.hour = calendar.get(Calendar.HOUR_OF_DAY)
			timePickerState.minute = calendar.get(Calendar.MINUTE)
			showTimeInputDialog.value = true
		},
		modifier = modifier.width(100.dp)
	)
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewTimeView() {
	ESMiraSurface {
		TimeButtonView({ NativeLink.getNowMillis() }, {})
	}
}