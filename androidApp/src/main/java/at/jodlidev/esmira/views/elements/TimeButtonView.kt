package at.jodlidev.esmira.views.elements

import android.app.TimePickerDialog
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import java.util.*
import at.jodlidev.esmira.sharedCode.NativeLink

/**
 * Created by JodliDev on 22.02.2023.
 */


@Composable
fun TimeButtonView(
	get: () -> Long,
	save: (Long) -> Unit,
	modifier: Modifier = Modifier
) {
	val text = remember { mutableStateOf(NativeLink.formatTime(get())) }
	val context = LocalContext.current
	val calendar = Calendar.getInstance()
	calendar.timeInMillis = get()
	// TimePickerDialog does not exist in Material3 yet:
	val dialog = TimePickerDialog(
		context,
		R.style.AppTheme_PickerDialog,
		{ _, hour, minute ->
			calendar.set(Calendar.HOUR_OF_DAY, hour)
			calendar.set(Calendar.MINUTE, minute)
			save(calendar.timeInMillis)
			text.value = NativeLink.formatTime(get())
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context)
	)
	
	DefaultButtonIconLeft(
		text = text.value,
		icon = Icons.Default.AccessTime,
		onClick = {
			dialog.show()
		},
		modifier = modifier.width(90.dp)
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