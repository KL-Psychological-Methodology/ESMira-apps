package at.jodlidev.esmira.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.preference.PreferenceManager
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.QrInterpreter
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Alarm
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.welcome.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.parcelize.Parcelize


/**
 * Created by JodliDev on 26.04.2019.
 */
class WelcomeScreenActivity: ComponentActivity() {
	
	@Parcelize
	data class StudyLoadingData(
		var serverUrl: String, // needs to be changed when formatting is different. A change will not trigger a reload - which is what we want
		val accessKey: String,
		val studyId: Long,
		val qId: Long
	) : Parcelable
	
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val extras = intent.extras
		val data = intent?.data
		val isOpenedFromLink = data != null
		val skipEntrance = (extras != null && extras.getBoolean(KEY_SKIP_ENTRANCE)) || isOpenedFromLink
		
		setContent {
			ESMiraSurface {
				val context = LocalContext.current
				val navController = rememberAnimatedNavController()
				
				val studyLoadingData = rememberSaveable {
					if(isOpenedFromLink) {
						val urlData = QrInterpreter().check(data.toString())
						if(urlData == null) {
							finish()
							mutableStateOf<StudyLoadingData?>(null)
						}
						else
							mutableStateOf<StudyLoadingData?>(StudyLoadingData(urlData.url, urlData.accessKey, urlData.studyId, urlData.qId))
					}
					else
						mutableStateOf<StudyLoadingData?>(null)
				}
				
				val showCancelWarning = remember { mutableStateOf(false) }
				val showStudyLoader = remember { mutableStateOf(isOpenedFromLink) }
				val studyList = remember { derivedStateOf {
					val loadData = studyLoadingData.value
					val studiesJson = getStudyJsonList(context)
					if(loadData != null && studiesJson.isNotEmpty())
						Study.getFilteredStudyList(studiesJson, loadData.serverUrl, loadData.accessKey, loadData.studyId, loadData.qId)
					else
						ArrayList()
				} }
				
				DisposableEffect(studyLoadingData.value) {
					if(!showStudyLoader.value) // block will be called again, when screen was rotated because studyLoadingData was set in loadStudies()
						return@DisposableEffect onDispose {}
					
					val loadData = studyLoadingData.value ?: return@DisposableEffect onDispose {}
					
					val web = Web.loadStudies(loadData.serverUrl, loadData.accessKey, onError = { msg, e ->
						runOnUiThread {
							showStudyLoader.value = false
							studyLoadingData.value = null
							clearStudyJsonList(context)
							Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
						}
						e?.printStackTrace()
					}, onSuccess = { studyString, urlFormatted ->
						loadData.serverUrl = urlFormatted
						saveStudyJsonList(context, studyString)
						runOnUiThread {
							showStudyLoader.value = false
							
							if(studyList.value.size == 1) {
								if(isOpenedFromLink)
									navController.popBackStack()
								navController.navigate("studyInfo/0")
							}
							else if(navController.currentDestination?.route != "studyList")
								navController.navigate("studyList")
						}
					})
					onDispose {
						web.cancel()
					}
				}
				
				if(!skipEntrance) {
					onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if(navController.backQueue.size == 2)
								showCancelWarning.value = true
							else
								navController.navigateUp()
						}
					})
				}
				
				if(showStudyLoader.value) {
					LoadingView {
						showStudyLoader.value = false
						studyLoadingData.value = null
					}
				}
				
				if(showCancelWarning.value) {
					CancelWarningDialog(showCancelWarning) { finish() }
				}
				
				MainView(
					startDestination = if(isOpenedFromLink) "studyList" else if(skipEntrance) "qrQuestion" else "entrance",
					getServerList = {
						Web.serverList
					},
					studyList = studyList,
					loadStudies = { serverUrl: String, accessKey: String, studyId: Long, qId: Long ->
						showStudyLoader.value = true
						studyLoadingData.value = StudyLoadingData(serverUrl, accessKey, studyId, qId)
					},
					navController = navController
				)
			}
		}
	}
	
	private fun getStudyJsonList(context: Context): String {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_STUDY_LIST_JSON, "") ?: ""
	}
	private fun saveStudyJsonList(context: Context, listJson: String) {
		val edit = PreferenceManager.getDefaultSharedPreferences(context).edit()
		edit.putString(KEY_STUDY_LIST_JSON, listJson)
		edit.commit() //needs to happen immediately
	}
	private fun clearStudyJsonList(context: Context) {
		val edit = PreferenceManager.getDefaultSharedPreferences(context).edit()
		edit.putString(KEY_STUDY_LIST_JSON, "")
		edit.apply()
	}
	
	
	private fun joinStudy(study: Study, navController: NavHostController) {
		study.join()
		if(study.needsJoinedScreen())
			navController.navigate("studyJoined/${study.id}")
		else
			finish()
	}
	
	@OptIn(ExperimentalAnimationApi::class)
	@Composable
	fun MainView(
		startDestination: String,
		getServerList: () -> List<Pair<String, String>>,
		studyList: State<List<Study>>,
		loadStudies: (
			serverUrl: String,
			accessKey: String,
			studyId: Long,
			qId: Long
		) -> Unit,
		navController: NavHostController = rememberAnimatedNavController()
	) {
		val serverUrl = rememberSaveable { mutableStateOf("") }
		val accessKey = rememberSaveable { mutableStateOf("") }
		
		
		AnimatedNavHost(
			navController,
			startDestination = startDestination,
			enterTransition = {
				slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(300))
			},
			exitTransition = {
				slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(300))
			},
			popEnterTransition = {
				slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(300))
			},
			popExitTransition  = {
				slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(300))
			}
		) {
			composable("entrance") {
				EntranceView {
					navController.navigate("qrQuestion")
				}
			}
			composable("qrQuestion") {
				QrQuestionView(
					gotoNo = {
						navController.navigate("serverQuestion")
					},
					gotoYes = {
						navController.navigate("qrScanning")
					},
					gotoPrevious = {
						onBackPressedDispatcher.onBackPressed()
					}
				)
			}
			composable("qrScanning") {
				QrScanningView(
					gotoPrevious = {
						onBackPressedDispatcher.onBackPressed()
					},
					gotoNext = { _serverUrl: String, _accessKey: String, studyId: Long, qId: Long ->
						serverUrl.value = _serverUrl
						accessKey.value = _accessKey
						loadStudies(serverUrl.value, _accessKey, studyId, qId)
					}
				)
			}
			composable("serverQuestion") {
				ServerQuestionView(
					initialServerUrl = serverUrl.value,
					getServerList = getServerList,
					gotoPrevious = {
						onBackPressedDispatcher.onBackPressed()
					},
					gotoNext = { _serverUrl: String ->
						serverUrl.value = _serverUrl
						navController.navigate("accessKeyQuestion")
					}
				)
			}
			composable("accessKeyQuestion") {
				AccessKeyQuestionView(
					accessKey = accessKey.value,
					gotoPrevious = {
						onBackPressedDispatcher.onBackPressed()
					},
					gotoNext = { _accessKey: String ->
						accessKey.value = _accessKey
						loadStudies(serverUrl.value, _accessKey, 0, 0)
					},
				)
			}
			composable("studyList") {
				if(studyList.value.isEmpty()) {
					StudyEmptyListView(accessKey.value) {
						onBackPressedDispatcher.onBackPressed()
					}
				}
				else {
					StudyListView(
						studies = studyList.value,
						gotoPrevious = {
							onBackPressedDispatcher.onBackPressed()
						},
						gotoNext = { index ->
							navController.navigate("studyInfo/$index")
						}
					)
				}
			}
			composable("studyInfo/{studyIndex}",
				arguments = listOf(
					navArgument("studyIndex") {
						type = NavType.IntType
					}
				)
			) { backStackEntry ->
				val studyIndex = backStackEntry.arguments?.getInt("studyIndex") ?: 0
				val study = studyList.value[studyIndex]
				
				StudyInfoView(
					study = study,
					gotoPrevious = {
						onBackPressedDispatcher.onBackPressed()
					},
					gotoNext = {
						if(study.needsPermissionScreen())
							navController.navigate("studyPermissions/$studyIndex")
						else
							joinStudy(study, navController)
					}
				)
			}
			composable("studyPermissions/{studyIndex}",
				arguments = listOf(
					navArgument("studyIndex") {
						type = NavType.IntType
					}
				)) { backStackEntry ->
				val studyIndex = backStackEntry.arguments?.getInt("studyIndex") ?: 0
				val study = studyList.value[studyIndex]
				
				StudyPermissionsView(
					study = study,
					gotoPrevious = {
						onBackPressedDispatcher.onBackPressed()
					},
					gotoNext = {
						joinStudy(study, navController)
					}
				)
			}
			composable("studyJoined/{studyId}",
				arguments = listOf(
					navArgument("studyId") {
						type = NavType.LongType
					}
				)) { backStackEntry ->
				val studyId = backStackEntry.arguments?.getLong("studyId") ?: 0L
				val study = DbLogic.getStudy(studyId) ?: return@composable
				StudyJoinedView(
					study = study,
					getAlarms = {
						val alarms = DbLogic.getNextAlarms(study.id)
						val suitableAlarms = ArrayList<Alarm>()
						for(alarm in alarms) {
							if(alarm.actionTrigger.hasNotifications())
								suitableAlarms.add(alarm)
						}
						suitableAlarms
					},
					gotoNext = {
						finish()
					}
				)
			}
		}
	}
	
	
	@Composable
	fun LoadingView(onCancel: () -> Unit) {
		AlertDialog(
			onDismissRequest = {
				onCancel()
			},
			text = {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier.fillMaxWidth()
				) {
					CircularProgressIndicator()
				}
			},
			confirmButton = {
				DialogButton(stringResource(R.string.cancel),
					onClick = {
						onCancel()
					}
				)
			},
		)
	}
	
	@Composable
	fun CancelWarningDialog(openState: MutableState<Boolean>, onConfirm: () -> Unit) {
		AlertDialog(
			onDismissRequest = {
				openState.value = false
			},
			title = {
				Text(stringResource(R.string.welcome_exit_questionTitle))
			},
			text = {
				Text(stringResource(R.string.welcome_exit_questionDesc))
			},
			dismissButton = {
				DialogButton(stringResource(R.string.cancel),
					onClick = {
						openState.value = false
					}
				)
			},
			confirmButton = {
				DialogButton(stringResource(R.string.ok_),
					onClick = {
						openState.value = false
						onConfirm()
					}
				)
			},
		)
	}
	
	
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewCancelWarningDialog() {
		val open = remember { mutableStateOf(false) }
		
		ESMiraSurface {
			CancelWarningDialog(open) {}
		}
	}
	
	companion object {
		private const val KEY_SKIP_ENTRANCE = "skip_entrance"
		
		private const val KEY_STUDY_LIST_JSON: String = "study_list_json"
		
		fun start(context: Context, notFirstTime: Boolean = false) {
			val intent = Intent(context, WelcomeScreenActivity::class.java)
			if(notFirstTime)
				intent.putExtra(KEY_SKIP_ENTRANCE, true)
			context.startActivity(intent)
		}
	}
}