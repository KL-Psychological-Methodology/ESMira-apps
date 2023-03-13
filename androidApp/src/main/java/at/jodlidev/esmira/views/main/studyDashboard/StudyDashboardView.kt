package at.jodlidev.esmira.views.main.studyDashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.views.ESMiraDialog
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.activities.WelcomeScreenActivity
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.DueDateFormatter
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Alarm
import at.jodlidev.esmira.sharedCode.data_structure.Study
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDashboardView(
	userId: String,
	getMissedNotifications: () -> Int,
	isDev: () -> Boolean,
	actions: StudyDashboardActions,
	openWelcomeScreen: () -> Unit,
	openErrorReport: () -> Unit,
	openNotificationsDialog: () -> Unit,
	updateStudies: () -> Unit,
	openAbout: () -> Unit,
	gotoQuestionnaires: (String) -> Unit,
	gotoMessages: () -> Unit,
	sendEmail: () -> Unit,
	gotoReward: () -> Unit,
	showInformedConsent: () -> Unit,
	gotoStatistics: () -> Unit,
	gotoDataProtocol: () -> Unit,
	openChangeSchedulesDialog: () -> Unit,
	openNextNotifications: () -> Unit,
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
				studyActions = actions,
				scrollBehavior = scrollBehavior,
				isDev = isDev,
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
//					withDismissAction = true,
					duration = SnackbarDuration.Indefinite
				)
				DbLogic.resetMissedInvitations()
			}
		}
		StudyDashboardGrid(
			userId = userId,
			actions = actions,
			gotoQuestionnaires = gotoQuestionnaires,
			gotoMessages = gotoMessages,
			sendEmail = sendEmail,
			gotoReward = gotoReward,
			showInformedConsent = showInformedConsent,
			gotoStatistics = gotoStatistics,
			gotoDataProtocol = gotoDataProtocol,
			openChangeSchedulesDialog = openChangeSchedulesDialog,
			modifier = Modifier.padding(innerPadding)
		)
	}
}

@Composable
fun StudyDashboardGrid(
	userId: String,
	actions: StudyDashboardActions,
	gotoQuestionnaires: (String) -> Unit,
	gotoMessages: () -> Unit,
	sendEmail: () -> Unit,
	gotoReward: () -> Unit,
	showInformedConsent: () -> Unit,
	gotoStatistics: () -> Unit,
	gotoDataProtocol: () -> Unit,
	openChangeSchedulesDialog: () -> Unit,
	modifier: Modifier = Modifier
) {
	val study = actions.getStudy()
	val alarm = actions.getNextAlarm()
	val hasPinnedQuestionnaires = actions.hasPinnedQuestionnaires()
	val hasRepeatingQuestionnaires = actions.hasRepeatingQuestionnaires()
	val hasOneTimeQuestionnaires = actions.hasOneTimeQuestionnaires()
	val hasStatistics = study.hasStatistics()
	val hasMessages = study.hasMessages()
	val hasRewards = study.hasRewards()
	
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
				actions.reloadStudy()
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
				actions.reloadStudy()
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
			val context = LocalContext.current
			StudyDashboardInfoBoxView(null, stringResource(R.string.android_user_id, userId), false, Modifier.clickable {
				val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
				val clipData = ClipData.newPlainText("label", userId)
				clipBoard.setPrimaryClip(clipData)
				Toast.makeText(context, context.getString(R.string.android_info_copied_x_to_clipboard, userId), Toast.LENGTH_SHORT).show()
			})
		}
		item {
			StudyDashboardInfoBoxView(stringResource(R.string.joined_at), NativeLink.formatDate(study.joinedTimestamp))
		}
		if(study.state == Study.STATES.Quit) {
			item {
				StudyDashboardInfoBoxView(stringResource(R.string.quit_at), NativeLink.formatDate(study.quitTimestamp))
			}
		}
		
		val completedQuestionnairesCount = actions.getCompletedQuestionnaireCount()
		if(completedQuestionnairesCount > 0) {
			item {
				StudyDashboardInfoBoxView(stringResource(R.string.completed_questionnaires), completedQuestionnairesCount.toString())
			}
		}
		if(study.hasNotifications()) {
			item {
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
		
		if(hasPinnedQuestionnaires || hasRepeatingQuestionnaires || hasOneTimeQuestionnaires) {
			item(span = { GridItemSpan(maxLineSpan) }) {
				StudyDashboardHeaderView(stringResource(R.string.questionnaires))
			}
			if(hasPinnedQuestionnaires) {
				item {
					StudyDashboardButtonView(
						stringResource(R.string.pinned),
						Icons.Default.PushPin,
						onClick = { gotoQuestionnaires("pinned") },
						badge = actions.countActivePinnedQuestionnaires().let { if(it == 0) null else it.toString() }
					)
				}
			}
			if(hasRepeatingQuestionnaires) {
				item {
					StudyDashboardButtonView(
						stringResource(R.string.repeating),
						Icons.Default.Loop,
						onClick = { gotoQuestionnaires("repeating") },
						badge = actions.countActiveRepeatingQuestionnaires().let { if(it == 0) null else it.toString() }
					)
				}
			}
			if(hasOneTimeQuestionnaires) {
				item {
					StudyDashboardButtonView(
						stringResource(R.string.one_time),
						Icons.Default.LooksOne,
						onClick = { gotoQuestionnaires("oneTime") },
						badge = actions.countActiveOneTimeQuestionnaires().let { if(it == 0) null else it.toString() }
					)
				}
			}
		}
		
		if(hasStatistics || hasMessages || hasRewards) {
			item(span = { GridItemSpan(maxLineSpan) }) {
				StudyDashboardHeaderView(stringResource(R.string.extras))
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
						badge = actions.countUnreadMessages().let { if(it == 0) null else it.toString() }
					)
				}
			}
			if(!study.sendMessagesAllowed && study.contactEmail.isNotEmpty()) {
				item {
					StudyDashboardButtonView(
						stringResource(R.string.send_email),
						Icons.Default.Email,
						onClick = sendEmail
					)
				}
			}
			if(hasRewards) {
				item {
					StudyDashboardButtonView(stringResource(R.string.rewards), Icons.Default.EmojiEvents, onClick = gotoReward)
				}
			}
			
			if(study.informedConsentForm.isNotEmpty()) {
				item {
					StudyDashboardButtonView(stringResource(R.string.informed_consent), Icons.Default.Assignment, onClick = showInformedConsent)
				}
			}
		}
		
		
		item(span = { GridItemSpan(maxLineSpan) }) {
			StudyDashboardHeaderView(stringResource(R.string.settings))
		}
		if(study.hasEditableSchedules()) {
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
					if(actions.hasUnSyncedDataSets())
						Toast.makeText(context, R.string.info_unsynced_datasets, Toast.LENGTH_LONG).show()
					else
						showDeleteStudyDialog.value = true
				}, important = true)
			}
		}
	}
	
}

@Composable
fun StudyDashboardHeaderView(text: String) {
	Surface(
		modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)
	) {
		Column {
			Divider(
				color = MaterialTheme.colorScheme.outlineVariant,
				thickness = 1.dp,
			)
			Text(
				text,
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 20.dp, top = 5.dp, bottom = 5.dp)
			)
			Divider(
				color = MaterialTheme.colorScheme.outlineVariant,
				thickness = 1.dp,
			)
		}
	}
}
@Composable
fun StudyDashboardButtonView(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	badge: String? = null,
	important: Boolean = false
) {
	Box(
		contentAlignment = Alignment.Center,
	) {
		Button(
			onClick = onClick,
			shape = RoundedCornerShape(1.dp),
			colors = if(important) ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.tertiaryContainer,
				contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
			)
			else ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surface,
				contentColor = MaterialTheme.colorScheme.onSurface,
			),
			modifier = Modifier
				.padding(all = 5.dp)
				.heightIn(min = 80.dp)
				.fillMaxSize()
		) {
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Icon(icon, "")
				Text(text, textAlign = TextAlign.Center)
			}
		}
		if(badge != null) {
			Row(modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp)) {
				Spacer(modifier = Modifier.weight(1F))
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.size(20.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.error)
				) {
					Text(
						badge,
						color = MaterialTheme.colorScheme.onError,
						fontSize = MaterialTheme.typography.bodySmall.fontSize
					)
				}
				Spacer(modifier = Modifier.weight(0.5F))
			}
		}
	}
}
@Composable
fun StudyDashboardInfoBoxView(
	header: String?,
	content: String,
	important: Boolean = false,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier.padding(all = 5.dp)
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxSize()
				.heightIn(min = if(header == null) 0.dp else 90.dp)
				.border(
					width = 1.dp,
					color = if(important) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant
				)
		) {
			if(header != null) {
				Text(header,
					color = MaterialTheme.colorScheme.onSurface,
					fontSize = MaterialTheme.typography.bodyMedium.fontSize,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(top = 5.dp, start = 5.dp, end = 5.dp).weight(1F)
				)
//				Spacer(modifier = Modifier.weight(1F))
			}
			if(important) {
				Text(
					content,
					fontSize = MaterialTheme.typography.bodyMedium.fontSize,
					color = MaterialTheme.colorScheme.tertiary,
					fontWeight = FontWeight.Bold,
					modifier = Modifier.padding(vertical = 10.dp)
				)
			}
			else {
				Text(
					content,
					fontSize = MaterialTheme.typography.bodyMedium.fontSize,
					modifier = Modifier.padding(vertical = 10.dp)
				)
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
			"""{"id": 1, "enableRewardSystem": true, "publicStatistics": {"charts": [{}]}, "questionnaires": [{"actionTriggers": [{"actions": [{"type": 3}]}]}]}"""
		)
		study.finishJSON("https://jodli.dev", "accessKey")
		StudyDashboardGrid(
			userId = "ABCD-1234-EFGH-5678",
			actions = StudyDashboardActions(
				getStudy = { study },
				getNextAlarm = { Alarm(0L, -1L, -1L, "test", 0, 0) },
				getCompletedQuestionnaireCount = { 132 },
				hasPinnedQuestionnaires = { true },
				countActivePinnedQuestionnaires = { 1 },
				hasRepeatingQuestionnaires = { true },
				countActiveRepeatingQuestionnaires = { 2 },
				hasOneTimeQuestionnaires = { true },
				countActiveOneTimeQuestionnaires = { 3 },
				countUnreadMessages = { 4 },
			),
			gotoQuestionnaires = {},
			gotoMessages = {},
			sendEmail = {},
			gotoReward = {},
			showInformedConsent = {},
			gotoStatistics = {},
			gotoDataProtocol = {},
			openChangeSchedulesDialog = {}
		)
	}
}