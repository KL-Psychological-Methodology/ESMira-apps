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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import at.jodlidev.esmira.views.ESMiraDialog
import at.jodlidev.esmira.views.main.studyDashboard.StudyDashboardButtonView

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
	goBack: () -> Unit
) {
	val study = getStudy()
	
	val showInformedConsent = remember { mutableStateOf(false) }
	if(showInformedConsent.value) {
		ESMiraDialog(
			title = stringResource(R.string.informed_consent),
			confirmButtonLabel = stringResource(R.string.close),
			onConfirmRequest = { showInformedConsent.value = false },
		) {
			Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
				Text(study.informedConsentForm)
			}
		}
	}
	
	val showStudyDescription = remember { mutableStateOf(false) }
	if(showStudyDescription.value) {
		ESMiraDialog(
			title = stringResource(R.string.study_description),
			confirmButtonLabel = stringResource(R.string.close),
			onConfirmRequest = { showStudyDescription.value = false },
		) {
			Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
				HtmlHandler.HtmlText(study.studyDescription)
			}
		}
	}
	
	DefaultScaffoldView(
		title = stringResource(R.string.study_information),
		goBack = goBack,
	) {
		LazyVerticalGrid(
			columns = GridCells.Fixed(2),
			modifier = Modifier.fillMaxWidth()
		) {
			item {
				Header(stringResource(R.string.user_id))
			}
			item {
				val context = LocalContext.current
				Row(
					modifier = Modifier
						.padding(all = 5.dp)
						.clickable {
							val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
							val clipData = ClipData.newPlainText("label", userId)
							clipBoard.setPrimaryClip(clipData)
							Toast
								.makeText(context, context.getString(R.string.android_info_copied_x_to_clipboard, userId), Toast.LENGTH_SHORT)
								.show()
						},
				) {
					Text(userId)
					Spacer(Modifier.size(ButtonDefaults.IconSpacing))
					Icon(Icons.Default.ContentCopy,
						contentDescription = "copy",
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.size(ButtonDefaults.IconSize)
					)
				}
			}

			item {
				Header(stringResource(R.string.joined_at))
			}
			item {
				Content(NativeLink.formatDate(study.joinedTimestamp))
			}
			
			if(study.state == Study.STATES.Quit) {
				item {
					Header(stringResource(R.string.quit_at))
				}
				item {
					Content(NativeLink.formatDate(study.quitTimestamp))
				}
			}
			
			item {
				Header(stringResource(R.string.completed_questionnaires))
			}
			item {
				Content(getCompletedQuestionnaireCount().toString())
			}
			
			if(hasNotifications(study)) {
				
				item {
					Header(stringResource(R.string.next_notification))
				}
				item {
					val alarm = getNextAlarm()
					if(alarm == null) {
						Content(stringResource(R.string.none))
					}
					else {
						val formatter = DueDateFormatter(
							soonString = stringResource(id = R.string.soon),
							todayString = stringResource(id = R.string.today),
							tomorrowString = stringResource(id = R.string.tomorrow),
							inXDaysString = stringResource(id = R.string.in_x_days)
						)
						Content(formatter.get(alarm.timestamp))
					}
				}
			}
			
			if(study.studyDescription.isNotEmpty()) {
				item {
					StudyDashboardButtonView(stringResource(R.string.study_description), Icons.Default.Info, onClick = { showStudyDescription.value = true; })
				}
			}
			if(study.informedConsentForm.isNotEmpty()) {
				item {
					StudyDashboardButtonView(stringResource(R.string.informed_consent), Icons.Default.Assignment, onClick = { showInformedConsent.value = true; })
				}
			}
		}
	}
}

@Composable
private fun Header(text: String) {
	Text(
		text,
		color = MaterialTheme.colorScheme.primary,
		fontSize = MaterialTheme.typography.bodyMedium.fontSize,
		modifier = Modifier.padding(all = 5.dp)
	)
}
@Composable
private fun Content(text: String) {
	Text(
		text,
		fontSize = MaterialTheme.typography.bodyMedium.fontSize,
		modifier = Modifier.padding(all = 5.dp)
	)
}

@Composable
fun StudyInformationLine(
	header: String?,
	content: String,
	important: Boolean = false
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(all = 5.dp)
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxSize()
				.heightIn(min = if(header == null) 0.dp else 90.dp)
				.border(
					width = 1.dp,
					color = if(important) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
				)
		) {
			if(header != null) {
				Text(
					header,
					color = if(important) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
					fontSize = MaterialTheme.typography.bodyMedium.fontSize,
					fontWeight = if(important) FontWeight.Bold else FontWeight.Normal,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.padding(top = 5.dp, start = 5.dp, end = 5.dp)
				)
			}
			Text(
				content,
				color = if(important) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground,
				fontSize = MaterialTheme.typography.bodyMedium.fontSize,
				modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp)
			)
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
			goBack = {}
		)
	}
}