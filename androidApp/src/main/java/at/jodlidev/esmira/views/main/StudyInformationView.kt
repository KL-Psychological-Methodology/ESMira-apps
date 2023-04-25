package at.jodlidev.esmira.views.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.DueDateFormatter
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Alarm
import at.jodlidev.esmira.sharedCode.data_structure.DbUser
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.ESMiraDialog
import at.jodlidev.esmira.views.main.studyDashboard.StudyDashboardButtonView
import at.jodlidev.esmira.views.main.studyDashboard.StudyDashboardInfoBoxView

/**
 * Created by JodliDev on 27.02.2023.
 */

@Composable
fun StudyInformationView(
	userId: String,
	getStudy: () -> Study,
	getCompletedQuestionnaireCount: () -> Int,
	hasNotifications: (Study) -> Boolean,
	getNextAlarm: () -> Alarm?,
	showInformedConsent: () -> Unit,
	showStudyDescription: () -> Unit,
	goBack: () -> Unit
) {
	
	val study = getStudy()
	DefaultScaffoldView(
		title = stringResource(R.string.study_information),
		goBack = goBack,
	) {
		LazyVerticalGrid(
			columns = GridCells.Fixed(2),
			modifier = Modifier.fillMaxWidth()
		) {
			item(span = { GridItemSpan(maxLineSpan) }) {
				val context = LocalContext.current
				Box(modifier = Modifier.clickable {
					val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
					val clipData = ClipData.newPlainText("label", userId)
					clipBoard.setPrimaryClip(clipData)
					Toast.makeText(context, context.getString(R.string.android_info_copied_x_to_clipboard, userId), Toast.LENGTH_SHORT).show()
				}) {
					StudyDashboardInfoBoxView(null, stringResource(R.string.android_user_id, userId), false)
				}
			}

			item {
				StudyDashboardInfoBoxView(stringResource(R.string.joined_at), NativeLink.formatDate(study.joinedTimestamp))
			}

			if(study.state == Study.STATES.Quit) {
				item {
					StudyDashboardInfoBoxView(stringResource(R.string.quit_at), NativeLink.formatDate(study.quitTimestamp))
				}
			}

			val completedQuestionnairesCount = getCompletedQuestionnaireCount()
			item {
				StudyDashboardInfoBoxView(stringResource(R.string.completed_questionnaires), completedQuestionnairesCount.toString())
			}
			
			if(hasNotifications(study)) {
				item {
					val alarm = getNextAlarm()
					if(alarm == null) {
						StudyDashboardInfoBoxView(stringResource(R.string.next_notification), stringResource(R.string.none))
					}
					else {
						val formatter = DueDateFormatter(
							soonString = stringResource(id = R.string.soon),
							todayString = stringResource(id = R.string.today),
							tomorrowString = stringResource(id = R.string.tomorrow),
							inXDaysString = stringResource(id = R.string.in_x_days)
						)
						StudyDashboardInfoBoxView(stringResource(R.string.next_notification), formatter.get(alarm.timestamp))
					}
				}
			}
			
			item(span = { GridItemSpan(maxLineSpan) }) {
				Divider(modifier = Modifier.padding(all = 10.dp))
			}
			
			if(study.studyDescription.isNotEmpty()) {
				item {
					StudyDashboardButtonView(stringResource(R.string.study_description), Icons.Default.Info, onClick = showStudyDescription)
				}
			}
			if(study.informedConsentForm.isNotEmpty()) {
				item {
					StudyDashboardButtonView(stringResource(R.string.informed_consent), Icons.Default.Assignment, onClick = showInformedConsent)
				}
			}
		}
	}
}



@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyInformationView() {
	ESMiraSurface {
		
		val study = DbLogic.createJsonObj<Study>("""{"id":-1, "studyDescription": "Description", "informedConsentForm": "informed consent"}""")
		StudyInformationView (
			userId = "this-is00-a000-uId0",
			getStudy = { study },
			getCompletedQuestionnaireCount = { 5 },
			hasNotifications = { true },
			getNextAlarm = { null },
			showInformedConsent = {},
			showStudyDescription = {},
			goBack = {}
		)
	}
}