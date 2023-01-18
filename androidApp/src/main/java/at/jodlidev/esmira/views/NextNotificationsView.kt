package at.jodlidev.esmira.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.DueDateFormatter
import at.jodlidev.esmira.sharedCode.data_structure.Alarm
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.welcome.StudyInfoView

/**
 * Created by JodliDev on 21.12.2022.
 */

@Composable
fun NextNotificationsView(
	alarms: List<Alarm>,
	modifier: Modifier = Modifier,
	exactDate: Boolean = false,
) {
	val dueDateFormatter = if(exactDate) DueDateFormatter()
	else DueDateFormatter(
		soonString = stringResource(id = R.string.soon),
		todayString = stringResource(id = R.string.today),
		tomorrowString = stringResource(id = R.string.tomorrow),
		inXDaysString = stringResource(id = R.string.in_x_days)
	)
	LazyColumn(modifier = modifier) {
		items(alarms) { alarm ->
			Row(modifier = Modifier.fillMaxWidth()) {
				Text(alarm.label, fontWeight = FontWeight.Bold)
				Text(":")
				Spacer(modifier = Modifier.weight(1f))
				Text(dueDateFormatter.get(alarm.timestamp))
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNextNotificationsView() {
	ESMiraSurface {
		val alarms = listOf(
			Alarm(1671605321653, -1, -1, "Alarm 1", 0, 0, -1, -1),
			Alarm(1671605321653, -1, -1, "Alarm 2", 0, 0, -1, -1),
			Alarm(1671605321653, -1, -1, "Alarm 3", 0, 0, -1, -1),
			Alarm(1671605321653, -1, -1, "Alarm 4", 0, 0, -1, -1),
			Alarm(1671605321653, -1, -1, "Alarm 5", 0, 0, -1, -1)
		)
		NextNotificationsView(alarms, Modifier, true)
	}
}