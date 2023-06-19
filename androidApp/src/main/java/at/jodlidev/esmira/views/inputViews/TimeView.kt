package at.jodlidev.esmira.views.inputViews

import android.app.TimePickerDialog
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.Input
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun TimeView(input: Input, get: () -> String, save: (String) -> Unit) {
	val context = LocalContext.current
	val targetFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
	val localFormat = DateFormat.getTimeFormat(context)
	
	val calendar: Calendar
	if(input.forceInt) {
		calendar = Calendar.getInstance()
		val value = get()
		if(value.isNotEmpty()) {
			try {
				val valueInt = value.toInt()
				val hours = valueInt / 60
				val minutes = valueInt % 60
				
				calendar.set(Calendar.HOUR_OF_DAY, hours)
				calendar.set(Calendar.MINUTE, minutes)
			}
			catch(_: Throwable) {
				ErrorBox.warn("TimeView", "Value $value in Item ${input.name} is faulty")
			}
		}
	}
	else {
		try {
			targetFormat.parse(get())
			localFormat.calendar = targetFormat.calendar
		}
		catch(_: Throwable) {
			targetFormat.calendar = Calendar.getInstance()
		}
		calendar = targetFormat.calendar
	}
	
	// TimePickerDialog does not exist in Material3 yet:
	val dialog = TimePickerDialog(
		context,
		R.style.AppTheme_PickerDialog,
		{ _, hour, minute ->
			calendar.set(Calendar.HOUR_OF_DAY, hour)
			calendar.set(Calendar.MINUTE, minute)
			
			localFormat.calendar = calendar
			targetFormat.calendar = calendar
			
			if(input.forceInt) {
				val minutes = hour * 60 + minute
				save(minutes.toString())
			}
			else
				save(targetFormat.format(calendar.time))
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context)
	)
	
	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
		DefaultButtonIconLeft(
			text = if(get().isNotEmpty()) localFormat.format(calendar.time) else stringResource(R.string.no_dateTime_data),
			icon = Icons.Default.AccessTime,
			onClick = {
				dialog.show()
			},
			modifier = Modifier.width(150.dp)
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewTimeView() {
	val input = DbLogic.createJsonObj<Input>("""
		{}
	""")
	ESMiraSurface {
		TimeView(input, { "" }) {}
	}
}