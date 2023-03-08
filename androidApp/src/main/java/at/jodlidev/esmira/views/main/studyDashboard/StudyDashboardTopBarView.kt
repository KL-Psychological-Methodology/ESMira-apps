package at.jodlidev.esmira.views.main.studyDashboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.activities.WelcomeScreenActivity
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.main.DefaultTopBar

/**
 * Created by JodliDev on 16.02.2023.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyEntranceTopBarView(
	studyActions: StudyDashboardActions,
	scrollBehavior: TopAppBarScrollBehavior,
	isDev: () -> Boolean,
	openWelcomeScreen: () -> Unit,
	openErrorReport: () -> Unit,
	openNotificationsDialog: () -> Unit,
	updateStudies: () -> Unit,
	openAbout: () -> Unit,
	openNextNotifications: () -> Unit,
	saveBackup: () -> Unit,
	loadBackup: () -> Unit
) {
	val study = studyActions.getStudy()
	val studyList = studyActions.getStudyList()
	val settingsExpanded = remember { mutableStateOf(false) }
	val studyListExpanded = remember { mutableStateOf(false) }
	
	
	DefaultTopBar(
		title = study.title,
		scrollBehavior = scrollBehavior,
		actions = {
			if(studyList.size >= 2) {
				IconButton(onClick = { studyListExpanded.value = studyListExpanded.value.not() }) {
					Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "change study")
				}
			}
			else {
				val context = LocalContext.current
				IconButton(onClick = { WelcomeScreenActivity.start(context, true) }) {
					Icon(imageVector = Icons.Default.Add, contentDescription = "change study")
				}
			}
			IconButton(onClick = { settingsExpanded.value = settingsExpanded.value.not() }) {
				Icon(imageVector = Icons.Default.Settings, contentDescription = "settings")
			}
			
			DropdownMenu(
				expanded = settingsExpanded.value,
				onDismissRequest = { settingsExpanded.value = false }
			) {
				SettingsDropdownView(
					isDev,
					openErrorReport,
					openNotificationsDialog,
					updateStudies,
					openAbout,
					openNextNotifications,
					saveBackup,
					loadBackup
				)
			}
			DropdownMenu(
				expanded = studyListExpanded.value,
				onDismissRequest = { studyListExpanded.value = false }
			) {
				StudyListDropdownView(studyList, study, openWelcomeScreen) { id ->
					studyListExpanded.value = false
					studyActions.switchStudy(id)
				}
			}
		}
	)
}


@Composable
fun MenuItem(text: String, icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
	DropdownMenuItem(
		leadingIcon = { Icon(icon, contentDescription = "") },
		text = { Text(text) },
		onClick = onClick,
		enabled = enabled
	)
}
@Composable
fun MenuItem(text: String, icon: Painter, onClick: () -> Unit, enabled: Boolean = true) {
	DropdownMenuItem(
		leadingIcon = { Icon(icon, contentDescription = "") },
		text = { Text(text) },
		onClick = onClick,
		enabled = enabled
	)
}


@Composable
fun SettingsDropdownView(
	isDev: () -> Boolean,
	openErrorReport: () -> Unit,
	openNotificationsDialog: () -> Unit,
	updateStudies: () -> Unit,
	openAbout: () -> Unit,
	openNextNotifications: () -> Unit,
	saveBackup: () -> Unit,
	loadBackup: () -> Unit
) {
	MenuItem(stringResource(R.string.send_error_report), Icons.Default.BugReport, openErrorReport)
	MenuItem(stringResource(R.string.notifications_not_working), Icons.Default.NotificationsOff, openNotificationsDialog)
	MenuItem(stringResource(R.string.update_studies), Icons.Default.Refresh, updateStudies)
	MenuItem(stringResource(R.string.about_ESMira), painterResource(id = R.drawable.ic_notification), openAbout)
	
	if(isDev()) {
		Divider(modifier = Modifier.padding(all = 10.dp),
			color = MaterialTheme.colorScheme.secondary)
		MenuItem(stringResource(R.string.next_notifications), Icons.Default.Alarm, openNextNotifications)
		MenuItem(stringResource(R.string.backup), Icons.Default.Upload, saveBackup)
		MenuItem(stringResource(R.string.load_backup), Icons.Default.Download, loadBackup)
	}
}

@Composable
fun StudyListDropdownView(
	studyList: List<Study>,
	currentStudy: Study,
	openWelcomeScreen: () -> Unit,
	switchStudy: (Long) -> Unit
) {
	val context = LocalContext.current
	for(study in studyList) {
		MenuItem(study.title, Icons.Default.ArrowRightAlt, { switchStudy(study.id) }, study.id != currentStudy.id)
	}
	Divider(
		color = MaterialTheme.colorScheme.primary,
		thickness = 1.dp,
		modifier = Modifier.padding(all = 10.dp)
	)
	Spacer(modifier = Modifier.height(5.dp))
	MenuItem(stringResource(R.string.add_a_study), Icons.Default.Add, openWelcomeScreen)
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewSettingsDropdownView() {
	ESMiraSurface {
		DropdownMenu (
			expanded = true,
			onDismissRequest = {}
		) {
			SettingsDropdownView({ true }, {}, {}, {}, {}, {}, {}, {})
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyListDropdownView() {
	ESMiraSurface {
		DropdownMenu (
			expanded = true,
			onDismissRequest = {}
		) {
			StudyListDropdownView(
				studyList = listOf(
					DbLogic.createJsonObj("""{"id": 1, "title": "Title 1"}"""),
					DbLogic.createJsonObj("""{"id": 2, "title": "Title 2"}""")
				),
				currentStudy = DbLogic.createJsonObj("""{"id": 1, "title": "Test Study"}"""),
				{}, {}
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyEntranceTopBarViewSingleStudy() {
	ESMiraSurface {
		StudyEntranceTopBarView(
			studyActions = StudyDashboardActions(
				getStudy = { DbLogic.createJsonObj("""{"id": 1, "title": "Test Study"}""") },
				getStudyList = { listOf(DbLogic.createJsonObj("""{"id": 1, "title": "Title 1"}""")) }
			),
			scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
			isDev = { false },
			openWelcomeScreen = {},
			openErrorReport = {},
			openNotificationsDialog = {},
			updateStudies = {},
			openAbout = {},
			openNextNotifications = {},
			saveBackup = {},
			loadBackup = {}
		)
	}
}
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyEntranceTopBarViewMultipleStudies() {
	ESMiraSurface {
		StudyEntranceTopBarView(
			studyActions = StudyDashboardActions(
				getStudy = { DbLogic.createJsonObj("""{"id": 1, "title": "Test Study"}""") },
				getStudyList = {
					listOf(
						DbLogic.createJsonObj("""{"id": 1, "title": "Title 1"}"""),
						DbLogic.createJsonObj("""{"id": 2, "title": "Title 2"}""")
					)
				}
			),
			scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
			isDev = { false },
			openWelcomeScreen = {},
			openErrorReport = {},
			openNotificationsDialog = {},
			updateStudies = {},
			openAbout = {},
			openNextNotifications = {},
			saveBackup = {},
			loadBackup = {}
		)
	}
}