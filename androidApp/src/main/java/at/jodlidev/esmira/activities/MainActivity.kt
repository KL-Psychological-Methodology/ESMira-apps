package at.jodlidev.esmira.activities

import android.app.Activity
import at.jodlidev.esmira.views.main.studyDashboard.StudyDashboardView
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import at.jodlidev.esmira.*
import at.jodlidev.esmira.BuildConfig
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.*
import at.jodlidev.esmira.sharedCode.data_structure.QuestionnaireCache
import at.jodlidev.esmira.sharedCode.data_structure.DbUser
import at.jodlidev.esmira.views.ESMiraDialog
import at.jodlidev.esmira.views.NextNotificationsView
import at.jodlidev.esmira.views.main.*
import at.jodlidev.esmira.views.main.studyDashboard.StudyDashboardActions
import at.jodlidev.esmira.views.main.questionnaire.QuestionnaireFinishedView
import at.jodlidev.esmira.views.main.questionnaire.QuestionnaireListView
import at.jodlidev.esmira.views.main.questionnaire.QuestionnaireView
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.delay
import java.io.*


/**
 * Created by JodliDev on 26.04.2019.
 */
class MainActivity: ComponentActivity() {
	private val reloadState = mutableStateOf(false)
	
	private fun reloadPage() {
		reloadState.value = reloadState.value.not()
	}
	
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		DbLogic.startupApp()
		ScreenTrackingService.startService(applicationContext)
		
		if(BuildConfig.DEBUG)
			DbUser.setDev(true, DbLogic.ADMIN_PASSWORD)
		
		var startDestination = ""
		val extras = intent.extras
		
		if(extras != null) {
			if(extras.containsKey(EXTRA_OPEN_MESSAGES)) {
				val studyId = extras.getLong(EXTRA_OPEN_MESSAGES, -1)
				DbUser.setCurrentStudyId(studyId)
				startDestination = "messages"
			}
			else if(extras.containsKey(EXTRA_OPEN_QUESTIONNAIRE)) {
				val questionnaire = DbLogic.getQuestionnaire(extras.getLong(EXTRA_OPEN_QUESTIONNAIRE, -1))
				if(questionnaire != null) {
					DbUser.setCurrentStudyId(questionnaire.studyId)
					
					if(questionnaire.canBeFilledOut())
						startDestination = "questionnaire/${questionnaire.id}/0"
				}
			}
		}
		
		setContent {
			ESMiraSurface {
				val navController = rememberAnimatedNavController()
				MainView(
					startDestination = startDestination,
					navController = navController,
				)
			}
		}
	}
	
	public override fun onResume() {
		super.onResume()
		DbLogic.checkLeaveStudies()
		reloadPage()
	}
	
	@OptIn(ExperimentalAnimationApi::class)
	@Composable
	fun MainView(
		startDestination: String,
		navController: NavHostController = rememberAnimatedNavController()
	) {
		val studyId = remember(reloadState.value) { mutableStateOf(DbUser.getCurrentStudyId()) }
		if(studyId.value == 0L) {
			WelcomeScreenActivity.start(LocalContext.current)
			return
		}
		AnimatedNavHost(
			navController,
			startDestination = "entrance",
			enterTransition = {
				slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(300))
			},
			exitTransition = {
				slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(300))
			},
			popEnterTransition = {
				slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(300))
			},
			popExitTransition = {
				slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(300))
			}
		) {
			composable("entrance") {
				val navigationComplete = rememberSaveable { mutableStateOf(false) }
				LaunchedEffect(startDestination) {
					if(!navigationComplete.value && startDestination.isNotEmpty()) {
						delay(500)
						navController.navigate(startDestination)
					}
					navigationComplete.value = true
				}
				PageEntrance(
					studyId = studyId.value,
					reloadStudy = { reloadPage() },
					switchStudy = { newStudyId ->
						studyId.value = newStudyId
					},
					navController = navController
				)
			}
			composable("questionnaireList/{type}/{enabledView}",
				arguments = listOf(
					navArgument("type") {
						type = NavType.StringType
					},
					navArgument("enabledView") {
						type = NavType.BoolType
					}
				)
			) { backStackEntry ->
				PageQuestionnaireList(
					studyId.value,
					backStackEntry.arguments?.getString("type") ?: "",
					backStackEntry.arguments?.getBoolean("enabledView") ?: true,
					navController
				)
			}
			composable("questionnaire/{qId}/{pageNumber}",
				arguments = listOf(
					navArgument("qId") {
						type = NavType.LongType
					},
					navArgument("pageNumber") {
						type = NavType.IntType
					}
				)
			) { backStackEntry ->
				val qId = backStackEntry.arguments?.getLong("qId") ?: return@composable
				val pageNumber = backStackEntry.arguments?.getInt("pageNumber") ?: return@composable
				PageQuestionnaire(qId, pageNumber, navController)
			}
			
			composable("finishedQuestionnaire") {
				QuestionnaireFinishedView {
					navController.popBackStack("entrance", false)
				}
			}
			
			composable("messages") {
				PageMessages(studyId.value)
			}
			
			composable("reward") {
				PageRewards(studyId.value)
			}
			
			composable("statistics") {
				PageStatistics(studyId.value)
			}
			
			composable("uploadProtocol") {
				PageUploadProtocol(studyId.value)
			}
			
			composable("about") {
				PageAbout()
			}
		}
	}
	
	@Composable
	fun PageEntrance(
		studyId: Long,
		reloadStudy: () -> Unit,
		switchStudy: (Long) -> Unit,
		navController: NavHostController
	) {
		val context = LocalContext.current
		Crossfade(studyId) { currentStudyId ->
			
			val study = DbLogic.getStudy(currentStudyId) ?: return@Crossfade //can be null when study was deleted
			
			val studyList = remember(reloadState.value) { mutableStateOf(DbLogic.getAllStudies()) }
			val questionnaireDataSetCount = remember(reloadState.value) { mutableStateOf(DbLogic.getQuestionnaireDataSetCount(currentStudyId)) }
			val hasPinnedQuestionnaires = remember(reloadState.value) { mutableStateOf(DbLogic.hasPinnedQuestionnaires(currentStudyId)) }
			val hasRepeatingQuestionnaires = remember(reloadState.value) { mutableStateOf(DbLogic.hasRepeatingQuestionnaires(currentStudyId)) }
			val hasOneTimeQuestionnaires = remember(reloadState.value) { mutableStateOf(DbLogic.hasOneTimeQuestionnaires(currentStudyId)) }
			val countActivePinnedQuestionnaires = remember(reloadState.value) { mutableStateOf(DbLogic.getPinnedQuestionnairesSplitByState(currentStudyId).first.size) }
			val countActiveRepeatingQuestionnaires = remember(reloadState.value) { mutableStateOf(DbLogic.getRepeatingQuestionnairesSplitByState(currentStudyId).first.size) }
			val countActiveOneTimeQuestionnaires = remember(reloadState.value) { mutableStateOf(DbLogic.getOneTimeQuestionnairesSplitByState(currentStudyId).first.size) }
			val hasUnSyncedDataSets = remember(reloadState.value) { mutableStateOf(DbLogic.hasUnSyncedDataSets(currentStudyId)) }
			val countUnreadMessages = remember(reloadState.value) { mutableStateOf(DbLogic.countUnreadMessages(currentStudyId)) }
			val nextAlarmWithNotifications = remember(reloadState.value) { mutableStateOf(DbLogic.getNextAlarmWithNotifications(currentStudyId)) }
			val missedInvitations = remember(reloadState.value) { mutableStateOf(DbLogic.getMissedInvitations()) }
			
			val studyData = StudyDashboardActions(
				getStudy = { study },
				getStudyList = { studyList.value },
				reloadStudy = reloadStudy,
				switchStudy = { newStudyId ->
					DbUser.setCurrentStudyId(newStudyId)
					switchStudy(newStudyId)
				},
				getCompletedQuestionnaireCount = { questionnaireDataSetCount.value },
				hasPinnedQuestionnaires = { hasPinnedQuestionnaires.value },
				countActivePinnedQuestionnaires = { countActivePinnedQuestionnaires.value },
				hasRepeatingQuestionnaires = { hasRepeatingQuestionnaires.value },
				countActiveRepeatingQuestionnaires = { countActiveRepeatingQuestionnaires.value },
				hasOneTimeQuestionnaires = { hasOneTimeQuestionnaires.value },
				countActiveOneTimeQuestionnaires = { countActiveOneTimeQuestionnaires.value },
				hasUnSyncedDataSets = { hasUnSyncedDataSets.value },
				countUnreadMessages = { countUnreadMessages.value },
				getNextAlarm = { nextAlarmWithNotifications.value }
			)
			
			
			val showNextNotifications = remember { mutableStateOf(false) }
			if(showNextNotifications.value) {
				ESMiraDialog(
					title = stringResource(R.string.next_notifications),
					confirmButtonLabel = stringResource(R.string.ok_),
					onConfirmRequest = { showNextNotifications.value = false },
				) {
					NextNotificationsView(
						alarms = DbLogic.getAlarms(),
						modifier = Modifier
							.heightIn(0.dp, 300.dp)
							.padding(vertical = 20.dp),
						exactDate = true
					)
				}
			}
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
			
			val saveBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/x-sqlite3")) { uri ->
				if(uri != null) {
					try {
						nativeAsync {
							val input: InputStream = FileInputStream(context.getDatabasePath(DbLogic.DATABASE_NAME))
							val output: OutputStream = context.contentResolver.openOutputStream(uri)
								?: throw IOException("OutputStream was null")
							
							input.copyTo(output, 1024)
							
							input.close()
							output.flush()
							output.close()
						}
					} catch(e: IOException) {
						Toast.makeText(context, getString(R.string.android_error_general, e.message), Toast.LENGTH_LONG).show()
					}
				}
			}
			val loadBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
				if(uri != null) {
					try {
						val input: InputStream = context.contentResolver.openInputStream(uri)
							?: throw IOException("InputStream was null")
						val output: OutputStream = FileOutputStream(context.getDatabasePath(DbLogic.DATABASE_NAME))
						
						input.copyTo(output, 1024)
						
						input.close()
						output.flush()
						output.close()
						NativeLink.resetSql(SQLite(context))
						start(context)
						finish()
					} catch(e: IOException) {
						Toast.makeText(context, getString(R.string.android_error_general, e.message), Toast.LENGTH_LONG).show()
					}
				}
			}
			
			StudyDashboardView(
				userId = DbUser.getUid(),
				actions = studyData,
				isDev = { DbUser.isDev() },
				getMissedNotifications = { missedInvitations.value },
				gotoQuestionnaires = { type -> navController.navigate("questionnaireList/$type/true") },
				gotoMessages = { navController.navigate("messages") },
				sendEmail = {
					val intent = Intent(Intent.ACTION_SEND)
					intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(study.contactEmail))
					intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.android_email_study_subject, study.title))
					intent.type = "plain/text"
					try {
						context.startActivity(intent)
					}
					catch(e: Exception) {
						Toast.makeText(context, R.string.error_no_email_client, Toast.LENGTH_LONG).show()
					}
				},
				gotoReward = { navController.navigate("reward") },
				showInformedConsent = { showInformedConsent.value = true },
				gotoStatistics = { navController.navigate("statistics") },
				gotoDataProtocol = { navController.navigate("uploadProtocol") },
				openChangeSchedulesDialog = { ChangeSchedulesDialogActivity.start(context, studyId) },
				openWelcomeScreen = { WelcomeScreenActivity.start(context, true) },
				openErrorReport = { ErrorReportDialogActivity.start(context, true) },
				openNotificationsDialog = { NotificationsBrokenDialogActivity.start(context, true) },
				updateStudies = {
					Web.updateStudiesAsync {updatedCount ->
						if(updatedCount != -1)
							Toast.makeText(context, context.getString(R.string.info_update_complete, updatedCount), Toast.LENGTH_SHORT).show()
						else
							Toast.makeText(context, context.getString(R.string.info_update_failed), Toast.LENGTH_SHORT).show()
					}
				},
				openAbout = { navController.navigate("about") },
				openNextNotifications = {
					showNextNotifications.value = true
				},
				saveBackup = { saveBackupLauncher.launch("backup.db") },
				loadBackup = { loadBackupLauncher.launch("*/*") },
			)
		}
	}
	
	@Composable
	fun PageQuestionnaireList(studyId: Long, type: String, enabledView: Boolean, navController: NavHostController) {
		val questionnaires = when(type) {
			"pinned" -> DbLogic.getPinnedQuestionnairesSplitByState(studyId)
			"repeating" -> DbLogic.getRepeatingQuestionnairesSplitByState(studyId)
			"oneTime" -> DbLogic.getOneTimeQuestionnairesSplitByState(studyId)
			else -> Pair(ArrayList(), ArrayList())
		}
		QuestionnaireListView(
			title = if(enabledView) stringResource(R.string.questionnaires) else stringResource(R.string.disabled_questionnaires),
			questionnaires = if(enabledView) questionnaires.first else questionnaires.second,
			goBack = { onBackPressedDispatcher.onBackPressed() },
			gotoQuestionnaire = { questionnaire ->
				navController.navigate("questionnaire/${questionnaire.id}/0")
			},
			gotoDisabledQuestionnaires = if(enabledView && questionnaires.second.isNotEmpty()) {
				{ navController.navigate("questionnaireList/$type/false") }
			} else null
		)
	}
	
	@Composable
	fun PageQuestionnaire(qId: Long, pageNumber: Int, navController: NavHostController) {
		val questionnaire = DbLogic.getQuestionnaire(qId) ?: return
		val formStarted = QuestionnaireCache.getFormStarted(questionnaire.id)
		
		QuestionnaireView(
			questionnaire = questionnaire,
			pageNumber = pageNumber,
			goBack = {
				onBackPressedDispatcher.onBackPressed()
			},
			goNext = {
				if(pageNumber == questionnaire.pages.size - 1) {
					questionnaire.saveQuestionnaire(formStarted)
					navController.popBackStack("entrance", false)
					navController.navigate("finishedQuestionnaire") {
						popUpTo("entrance") { inclusive = false }
					}
				}
				else {
					navController.navigate("questionnaire/${questionnaire.id}/${pageNumber + 1}")
				}
			}
		)
	}
	
	@Composable
	fun PageMessages(studyId: Long) {
		MessagesView(getStudy = { DbLogic.getStudy(studyId)!! }) {
			onBackPressedDispatcher.onBackPressed()
		}
	}
	
	@Composable
	fun PageStatistics(studyId: Long) {
		StatisticsView(getStudy = { DbLogic.getStudy(studyId)!! }) {
			onBackPressedDispatcher.onBackPressed()
		}
	}
	
	@Composable
	fun PageRewards(studyId: Long) {
		RewardView(getStudy = { DbLogic.getStudy(studyId)!! }) {
			onBackPressedDispatcher.onBackPressed()
		}
	}
	
	@Composable
	fun PageUploadProtocol(studyId: Long) {
		val context = LocalContext.current
		UploadProtocolView(
			getDataSets = { DbLogic.getDataSets(studyId) },
			reSyncDataSets = { onFinish -> Web.syncDataSetsAsync { success ->
				onFinish()
				if(success)
					Toast.makeText(context, context.getString(R.string.info_sync_complete), Toast.LENGTH_SHORT).show()
				else
					Toast.makeText(context, context.getString(R.string.info_sync_failed), Toast.LENGTH_SHORT).show()
				
			} },
			goBack = { onBackPressedDispatcher.onBackPressed() }
		)
	}
	
	@Composable
	fun PageAbout() {
		AboutView {
			onBackPressedDispatcher.onBackPressed()
		}
	}
	
	
	companion object {
		const val EXTRA_OPEN_MESSAGES = "extra_open_messages"
		const val EXTRA_OPEN_QUESTIONNAIRE = "extra_open_questionnaire"
		
		fun start(context: Context) {
			val intent = Intent(context, MainActivity::class.java)
			context.startActivity(intent)
		}
	}
}