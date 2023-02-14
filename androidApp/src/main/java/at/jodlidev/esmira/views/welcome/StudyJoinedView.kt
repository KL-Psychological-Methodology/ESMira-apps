package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Alarm
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.NextNotificationsView

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun StudyJoinedView(study: Study, getAlarms: () -> List<Alarm>, gotoNext: () -> Unit) {
	val context = LocalContext.current
	Column(modifier = Modifier
		.fillMaxSize()
		.padding(all = 20.dp)) {
		
		HtmlHandler.HtmlText(study.postInstallInstructions, modifier = Modifier
			.fillMaxWidth()
			.weight(1f))
		
		if(study.hasNotifications()) {
			Text(stringResource(id = R.string.colon_next_expected_notification),
				fontSize = 20.sp,
				fontWeight = FontWeight.Bold
			)
			
			NextNotificationsView(
				alarms = getAlarms(),
				modifier = Modifier.heightIn(0.dp, 300.dp).padding(vertical = 20.dp)
			)
		}
		
		if(study.hasEditableSchedules()) {
			NavigationView(
				gotoPrevious = { Activity_editSchedules.start(context, study.id) },
				gotoNext = gotoNext,
				modifier = Modifier.fillMaxWidth(),
				prevIcon = { Icons.Default.Alarm },
				nextIcon = { Icons.Default.Check },
				prevLabel = stringResource(id = R.string.change_schedules),
				nextLabel = stringResource(id = R.string.complete)
			)
		}
		else {
			NavigationView(
				gotoPrevious = null,
				gotoNext = gotoNext,
				modifier = Modifier.fillMaxWidth(),
				nextLabel = stringResource(id = R.string.complete)
			)
		}
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyJoinedView() {
	ESMiraSurface {
		StudyJoinedView(Study.newInstance("", "",
			"""{"id":1, "title": "Study1", "contactEmail": "contact@email", "studyDescription": "This<br>is<br>a<br>description"}"""),
			{ listOf(
				Alarm(1671605321653, -1, -1, "Alarm 1", 0, 0, -1, -1),
				Alarm(1671605321653, -1, -1, "Alarm 2", 0, 0, -1, -1),
				Alarm(1671605321653, -1, -1, "Alarm 3", 0, 0, -1, -1),
				Alarm(1671605321653, -1, -1, "Alarm 4", 0, 0, -1, -1),
				Alarm(1671605321653, -1, -1, "Alarm 5", 0, 0, -1, -1)
			)},
			{}
		)
	}
}