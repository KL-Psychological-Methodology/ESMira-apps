package at.jodlidev.esmira.views.inputViews

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun DateView(input: Input, get: () -> String, set: (String) -> Unit) {
	val format = SimpleDateFormat("yyyy-MM-dd")
	val context = LocalContext.current
	val date = try { format.parse(get()) ?: Date() } catch(e: Throwable) { Date() }
	val dialog = DatePickerDialog(
		context,
		{ _, year, month, day ->
			set("$year-${if(month <= 9) "0$month" else month}-${if(day <= 9) "0$day" else day}")
		}, date.year, date.month, date.day
	)
	Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
		OutlinedButton(
			modifier = Modifier.width(150.dp),
			onClick = {
				dialog.show()
			}
		) {
			Icon(
				Icons.Default.Today,
				contentDescription = "",
				modifier = Modifier.size(ButtonDefaults.IconSize)
			)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(get().ifEmpty { stringResource(R.string.no_dateTime_data) })
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewDateView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"leftSideLabel": "left", "rightSideLabel": "right", "text": "A meaningful description"}
	""")
	ESMiraSurface {
		DateView(input, { "" }) {}
	}
}