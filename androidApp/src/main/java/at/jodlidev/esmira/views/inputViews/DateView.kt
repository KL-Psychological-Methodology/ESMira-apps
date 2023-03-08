package at.jodlidev.esmira.views.inputViews

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
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
import at.jodlidev.esmira.sharedCode.data_structure.Input
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun DateView(input: Input, get: () -> String, save: (String) -> Unit) {
	val context = LocalContext.current
	val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
	val localFormat = DateFormat.getDateFormat(context)
	
	try {
		targetFormat.parse(get())
		localFormat.calendar = targetFormat.calendar
	}
	catch(_: Throwable) {
		targetFormat.calendar = Calendar.getInstance()
	}
	val calendar = targetFormat.calendar
	// DatePickerDialog does not exist in Material3 yet:
	val dialog = DatePickerDialog(
		context,
		R.style.AppTheme_PickerDialog,
		{ _, year, month, day ->
			calendar.set(Calendar.YEAR, year)
			calendar.set(Calendar.MONTH, month)
			calendar.set(Calendar.DAY_OF_MONTH, day)
			
			localFormat.calendar = calendar
			targetFormat.calendar = calendar
			
			save(targetFormat.format(calendar.time))
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
	)
	
	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
		DefaultButtonIconLeft(
			text = if(get().isNotEmpty()) localFormat.format(calendar.time) else stringResource(R.string.no_dateTime_data),
			icon = Icons.Default.Today,
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
fun PreviewDateView() {
	val input = DbLogic.createJsonObj<Input>("""
		{}
	""")
	ESMiraSurface {
		DateView(input, { "1989-02-01" }) {}
	}
}