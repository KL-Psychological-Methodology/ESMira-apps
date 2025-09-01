package at.jodlidev.esmira.views.main.studyDashboard

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.views.ESMiraDialog
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.activities.WelcomeScreenActivity
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.main.questionnaire.QuestionnaireLine
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDashboardView(
	getStudy: () -> Study,
	getStudyList: () -> List<Study>,
	getQuestionnaireList: () -> List<Questionnaire>,
	hasEditableSchedules: () -> Boolean,
	hasUnSyncedDataSets: () -> Boolean,
	countUnreadMessages: () -> Int,
	getMissedNotifications: () -> Int,
	isDev: () -> Boolean,
	reloadStudy: () -> Unit,
	switchStudy: (Long) -> Unit,
	updateStudies: () -> Unit,
	sendEmail: () -> Unit,
	openWelcomeScreen: () -> Unit,
	openErrorReport: () -> Unit,
	openNotificationsDialog: () -> Unit,
	openAbout: () -> Unit,
	openChangeSchedulesDialog: () -> Unit,
	openNextNotifications: () -> Unit,
	gotoQuestionnaire: (Questionnaire) -> Unit,
	gotoDisabledQuestionnaires: (() -> Unit)?,
	gotoMessages: () -> Unit,
	gotoReward: () -> Unit,
	gotoStatistics: () -> Unit,
	gotoDataProtocol: () -> Unit,
	gotoStudyInformation: () -> Unit,
	gotoFaq: () -> Unit,
	saveBackup: () -> Unit,
	loadBackup: () -> Unit
) {
	val context = LocalContext.current
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()
	
	Scaffold(
		snackbarHost = { SnackbarHost(snackbarHostState) },
		topBar = {
			StudyEntranceTopBarView(
				scrollBehavior = scrollBehavior,
				getStudy = getStudy,
				getStudyList = getStudyList,
				isDev = isDev,
				switchStudy = switchStudy,
				openWelcomeScreen = openWelcomeScreen,
				openErrorReport = openErrorReport,
				openNotificationsDialog = openNotificationsDialog,
				updateStudies = updateStudies,
				openAbout = openAbout,
				openNextNotifications = openNextNotifications,
				saveBackup = saveBackup,
				loadBackup = loadBackup
			)
		},
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
	) { innerPadding ->
		LaunchedEffect(getMissedNotifications()) {
			val missedNotifications = getMissedNotifications()
			if(missedNotifications == 0)
				return@LaunchedEffect
			scope.launch {
				snackbarHostState.showSnackbar(
					message = context.resources.getQuantityString(R.plurals.android_info_missed_notifications, missedNotifications, missedNotifications),
					actionLabel = context.getString(R.string.understood),
					duration = SnackbarDuration.Indefinite
				)
				DbLogic.resetMissedInvitations()
			}
		}
		StudyDashboardGrid(
			getStudy = getStudy,
			reloadStudy = reloadStudy,
			getQuestionnaireList = getQuestionnaireList,
			hasEditableSchedules = hasEditableSchedules,
			hasUnSyncedDataSets = hasUnSyncedDataSets,
			countUnreadMessages = countUnreadMessages,
			openChangeSchedulesDialog = openChangeSchedulesDialog,
			gotoQuestionnaire = gotoQuestionnaire,
			gotoDisabledQuestionnaires = gotoDisabledQuestionnaires,
			gotoMessages = gotoMessages,
			gotoReward = gotoReward,
			gotoStatistics = gotoStatistics,
			gotoDataProtocol = gotoDataProtocol,
			gotoStudyInformation = gotoStudyInformation,
			gotoFaq = gotoFaq,
			sendEmail = sendEmail,
			modifier = Modifier.padding(innerPadding)
		)
	}
}

@Composable
fun StudyDashboardGrid(
	getStudy: () -> Study,
	reloadStudy: () -> Unit,
	getQuestionnaireList: () -> List<Questionnaire>,
	hasEditableSchedules: () -> Boolean,
	hasUnSyncedDataSets: () -> Boolean,
	countUnreadMessages: () -> Int,
	openChangeSchedulesDialog: () -> Unit,
	gotoQuestionnaire: (Questionnaire) -> Unit,
	gotoDisabledQuestionnaires: (() -> Unit)?,
	gotoMessages: () -> Unit,
	gotoReward: () -> Unit,
	gotoStatistics: () -> Unit,
	gotoDataProtocol: () -> Unit,
	gotoStudyInformation: () -> Unit,
	gotoFaq: () -> Unit,
	sendEmail: () -> Unit,
	modifier: Modifier = Modifier
) {
	val study = getStudy()
	val hasStatistics = study.hasStatistics()
	val hasMessages = study.hasMessages()
	val hasRewards = study.hasRewards()
	val hasFAQs = study.faq.isNotEmpty()
	
	val showLeaveStudyDialog = remember { mutableStateOf(false) }
	
	if(showLeaveStudyDialog.value) {
		ESMiraDialog(
			onDismissRequest = { showLeaveStudyDialog.value = false },
			title = stringResource(R.string.dialogTitle_leave_study),
			dismissButtonLabel = stringResource(R.string.cancel),
			confirmButtonLabel = stringResource(R.string.leave),
			onConfirmRequest = {
				showLeaveStudyDialog.value = false
				study.leave()
				reloadStudy()
			}
		) {
			Text(stringResource(R.string.dialogDesc_leave_study))
		}
	}
	
	val showDeleteStudyDialog = remember { mutableStateOf(false) }
	
	if(showDeleteStudyDialog.value) {
		ESMiraDialog(
			onDismissRequest = { showDeleteStudyDialog.value = false },
			title = stringResource(R.string.delete_study),
			dismissButtonLabel = stringResource(R.string.cancel),
			confirmButtonLabel = stringResource(R.string.delete_),
			onConfirmRequest = {
				study.delete()
				reloadStudy()
			}
		) {
			Text(stringResource(R.string.confirm_delete_study))
		}
	}
	
	LazyVerticalGrid(
		columns = GridCells.Fixed(2),
		modifier = modifier.fillMaxWidth()
	) {
		if(study.state == Study.STATES.Quit) {
			item(span = { GridItemSpan(maxLineSpan) }) {
				StudyDashboardInfoBoxView(null, stringResource(R.string.info_study_not_active_anymore), true)
			}
		}
		
		
		
		item(span = { GridItemSpan(maxLineSpan) }) {
			StudyDashboardHeaderView(
				stringResource(R.string.questionnaires),
				if(gotoDisabledQuestionnaires != null) {
					{
						MenuItem(
							text = stringResource(R.string.show_inactive_questionnaires),
							icon = Icons.Default.VisibilityOff,
							onClick = gotoDisabledQuestionnaires
						)
					}
				} else null
			)
		}
		val questionnaireList = getQuestionnaireList()
		if(questionnaireList.isNotEmpty()) {
			item(span = { GridItemSpan(maxLineSpan) }) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
				) {
					for(questionnaire in questionnaireList) {
						QuestionnaireLine(
							questionnaire = questionnaire,
							gotoQuestionnaire = gotoQuestionnaire,
						)
					}
				}
			}
		} else {
			item(span = { GridItemSpan(maxLineSpan) }) {
				Text(
					text = stringResource(R.string.no_active_questionnaires),
					color = MaterialTheme.colorScheme.onSurface,
					textAlign = TextAlign.Center
				)
			}
		}
		
		item(span = { GridItemSpan(maxLineSpan) }) {
			StudyDashboardHeaderView(stringResource(R.string.extras))
		}
		item {
			StudyDashboardButtonView(stringResource(R.string.study_information), Icons.Default.Info, onClick = gotoStudyInformation)
		}

		if(hasFAQs) {
			item {
				StudyDashboardButtonView(
					stringResource(R.string.faqs),
					Icons.AutoMirrored.Filled.Help,
					onClick = gotoFaq
					)
			}
		}
		if(hasStatistics) {
			item {
				StudyDashboardButtonView(stringResource(R.string.statistics), Icons.Default.BarChart, onClick = gotoStatistics)
			}
		}
		if(hasMessages) {
			item {
				StudyDashboardButtonView(
					stringResource(R.string.messages),
					Icons.Default.Message,
					onClick = gotoMessages,
					badge = countUnreadMessages().let { if(it == 0) null else it.toString() }
				)
			}
		}
		if(hasRewards) {
			item {
				StudyDashboardButtonView(stringResource(R.string.rewards), Icons.Default.EmojiEvents, onClick = gotoReward)
			}
		}
		
		
		item(span = { GridItemSpan(maxLineSpan) }) {
			StudyDashboardHeaderView(stringResource(R.string.settings))
		}
		if(hasEditableSchedules()) {
			item {
				StudyDashboardButtonView(stringResource(R.string.change_schedules), Icons.Default.AccessTime, onClick = openChangeSchedulesDialog)
			}
		}
		item {
			StudyDashboardButtonView(stringResource(R.string.upload_protocol), Icons.Default.MenuBook, onClick = gotoDataProtocol)
		}
		if(study.state == Study.STATES.Joined) {
			item {
				StudyDashboardButtonView(stringResource(R.string.leave_study), Icons.Default.Logout, onClick = {
					showLeaveStudyDialog.value = true
				}, important = true)
			}
		}
		else {
			item {
				val context = LocalContext.current
				StudyDashboardButtonView(stringResource(R.string.rejoin_study), Icons.Default.Restore, onClick = {
					WelcomeScreenActivity.start(context, study.serverUrl, study.accessKey, study.webId)
				})
			}
			
			item {
				val context = LocalContext.current
				StudyDashboardButtonView(stringResource(R.string.delete_study), Icons.Default.Delete, onClick = {
					Web.syncDataSetsAsync {  }
					if(hasUnSyncedDataSets())
						Toast.makeText(context, R.string.info_unsynced_datasets, Toast.LENGTH_LONG).show()
					else
						showDeleteStudyDialog.value = true
				}, important = true)
			}
		}
	}
	
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyDashboardView() {
	ESMiraSurface {
		val study = DbLogic.createJsonObj<Study>(
			"""{"id": 1, "faq": "A", "enableRewardSystem": true, "publicStatistics": {"charts": [{}]}, "questionnaires": [{"actionTriggers": [{"actions": [{"type": 3}]}]}]}"""
		)
		study.finishJSON("https://jodli.dev", "accessKey")
		
		val justFilledOutQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire 1"}""")
		justFilledOutQuestionnaire.metadata.lastCompleted = NativeLink.getNowMillis()
		
		val finishedQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire 2"}""")
		finishedQuestionnaire.metadata.lastCompleted = NativeLink.getNowMillis() - 1000 * 60 * 60 * 24
		
		StudyDashboardGrid(
			getStudy = { study },
			reloadStudy = {},
			getQuestionnaireList = { listOf(
				justFilledOutQuestionnaire,
				finishedQuestionnaire,
				DbLogic.createJsonObj("""{"title": "Questionnaire 3"}""")
			) },
			hasUnSyncedDataSets = { true },
			hasEditableSchedules = { true },
			countUnreadMessages = { 4 },
			gotoQuestionnaire = {},
			gotoDisabledQuestionnaires = {},
			gotoMessages = {},
			sendEmail = {},
			gotoReward = {},
			gotoStatistics = {},
			gotoDataProtocol = {},
			gotoStudyInformation = {},
			gotoFaq = {},
			openChangeSchedulesDialog = {}
		)
	}
}

// Without active questionnaires
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyDashboardNoQuestionnairesView() {
	ESMiraSurface {
		val study = DbLogic.createJsonObj<Study>(
			"""{"id": 1, "faq": "A", "enableRewardSystem": true, "publicStatistics": {"charts": [{}]}, "questionnaires": [{"actionTriggers": [{"actions": [{"type": 3}]}]}]}"""
		)
		study.finishJSON("https://jodli.dev", "accessKey")

		StudyDashboardGrid(
			getStudy = { study },
			reloadStudy = {},
			getQuestionnaireList = { listOf() },
			hasUnSyncedDataSets = { true },
			hasEditableSchedules = { true },
			countUnreadMessages = { 4 },
			gotoQuestionnaire = {},
			gotoDisabledQuestionnaires = {},
			gotoMessages = {},
			sendEmail = {},
			gotoReward = {},
			gotoStatistics = {},
			gotoDataProtocol = {},
			gotoStudyInformation = {},
			gotoFaq = {},
			openChangeSchedulesDialog = {}
		)
	}
}