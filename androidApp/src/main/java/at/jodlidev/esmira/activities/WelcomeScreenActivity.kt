package at.jodlidev.esmira.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.preference.PreferenceManager
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.QrInterpreter
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.ESMiraDialog
import at.jodlidev.esmira.views.welcome.*
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.parcelize.Parcelize


/**
 * Created by JodliDev on 26.04.2019.
 */
class WelcomeScreenActivity: ComponentActivity() {
	
	@Parcelize
	data class StudyLoadingData(
		var serverUrl: String, // needs to be changed when formatting is different. A change will not trigger a reload - which is what we want
		val accessKey: String,
		val fallbackUrl: String?,
		val studyId: Long,
		val qId: Long,
		val loadedTimestamp: Long = NativeLink.getNowMillis() // just exists to force reloads of the same data
	) : Parcelable
	
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val extras = intent.extras
		val data = intent?.data
		val serverUrl = extras?.getString(KEY_SERVER_URL, "") ?: ""
		val accessKey = extras?.getString(KEY_ACCESS_KEY, "") ?: ""
		val studyWebId = extras?.getLong(KEY_STUDY_WEB_ID, -1) ?: -1
		val isOpenedFromLink = data != null
		val hasStudyData = serverUrl.isNotEmpty() && studyWebId != -1L
		val openStudyDirectly = isOpenedFromLink || hasStudyData
		val skipEntrance = (extras != null && extras.getBoolean(KEY_SKIP_ENTRANCE)) || openStudyDirectly
		
		setContent {
			ESMiraSurface {
				val context = LocalContext.current
				val navController = rememberNavController()
				
				val studyLoadingData = rememberSaveable {
					if(isOpenedFromLink) {
						val urlData = QrInterpreter().check(data.toString())
						if(urlData == null) {
							finish()
							mutableStateOf<StudyLoadingData?>(null)
						}
						else
							mutableStateOf<StudyLoadingData?>(StudyLoadingData(urlData.url, urlData.accessKey, urlData.fallbackUrl, urlData.studyId, urlData.qId))
					}
					else if(hasStudyData)
						mutableStateOf<StudyLoadingData?>(StudyLoadingData(serverUrl, accessKey, null, studyWebId, 0))
					else
						mutableStateOf<StudyLoadingData?>(null)
				}
				
				val showStudyLoader = remember { mutableStateOf(openStudyDirectly) }
				
				val getStudyList = {
					val loadData = studyLoadingData.value
					val studiesJson = getStudyJsonList(context)
					if(loadData != null && studiesJson.isNotEmpty())
						Study.getFilteredStudyList(studiesJson, loadData.serverUrl, loadData.accessKey, loadData.studyId, loadData.qId)
					else
						ArrayList()
				}
				val studyList = remember { mutableStateOf(getStudyList()) }
				
				val gotoStudies = {
					if(studyList.value.size == 1) {
						if(openStudyDirectly)
							navController.popBackStack()
						navController.navigate("studyInfo/0")
					}
					else if(navController.currentDestination?.route != "studyList")
						navController.navigate("studyList")
				}
				
				DisposableEffect(studyLoadingData.value) {
					if(!showStudyLoader.value) // block will be called again, when screen was rotated because studyLoadingData was set in loadStudies()
						return@DisposableEffect onDispose {}
					
					val loadData = studyLoadingData.value ?: return@DisposableEffect onDispose {}
					
					val web = Web.loadStudies(loadData.serverUrl, loadData.accessKey, loadData.fallbackUrl, onError = { msg, e ->
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
						studyList.value = getStudyList()
						runOnUiThread {
							showStudyLoader.value = false

							gotoStudies()
						}
					})
					onDispose {
						web.cancel()
					}
				}
				
				if(showStudyLoader.value) {
					LoadingView {
						showStudyLoader.value = false
						studyLoadingData.value = null
					}
				}
				
				MainView(
					startDestination = if(openStudyDirectly) "studyList" else if(skipEntrance) "qrQuestion" else "entrance",
					getServerList = {
						Web.serverList
					},
					studyList = { studyList.value },
					loadStudies = { serverUrl: String, accessKey: String, studyId: Long, qId: Long, fallbackUrl: String? ->
						showStudyLoader.value = true
						studyLoadingData.value = StudyLoadingData(serverUrl, accessKey, fallbackUrl, studyId, qId)
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
		studyList: () -> List<Study>,
		loadStudies: (
			serverUrl: String,
			accessKey: String,
			studyId: Long,
			qId: Long,
			fallbackUrl: String?
		) -> Unit,
		navController: NavHostController = rememberNavController()
	) {
		val serverUrl = rememberSaveable { mutableStateOf("") }
		val accessKey = rememberSaveable { mutableStateOf("") }
		
		
		NavHost(
			navController,
			startDestination = startDestination,
			enterTransition = {
				slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
			},
			exitTransition = {
				slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
			},
			popEnterTransition = {
				slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
			},
			popExitTransition  = {
				slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
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
					gotoNext = { _serverUrl: String, _accessKey: String, studyId: Long, qId: Long, fallbackUrl: String? ->
						serverUrl.value = _serverUrl
						accessKey.value = _accessKey
						loadStudies(serverUrl.value, _accessKey, studyId, qId, fallbackUrl)
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
						loadStudies(serverUrl.value, _accessKey, 0, 0, null)
					},
				)
			}
			composable("studyList") {
				if(studyList().isEmpty()) {
					StudyEmptyListView(accessKey.value) {
						onBackPressedDispatcher.onBackPressed()
					}
				}
				else {
					StudyListView(
						studies = studyList(),
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
				val study = studyList()[studyIndex]
				
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
				val study = studyList()[studyIndex]
				
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
						DbLogic.getQuestionnaireAlarmsWithNotifications(study.id)
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
		ESMiraDialog(
			onDismissRequest = {
				onCancel()
			},
			confirmButtonLabel = stringResource(R.string.cancel),
			onConfirmRequest = onCancel
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.fillMaxWidth()
			) {
				CircularProgressIndicator()
			}
		}
	}
	
	companion object {
		private const val KEY_SKIP_ENTRANCE = "skipEntrance"
		private const val KEY_SERVER_URL = "serverUrl"
		private const val KEY_ACCESS_KEY = "accessKey"
		private const val KEY_STUDY_WEB_ID = "studyWebId"
		
		private const val KEY_STUDY_LIST_JSON: String = "study_list_json"
		
		fun start(context: Context, notFirstTime: Boolean = false) {
			val intent = Intent(context, WelcomeScreenActivity::class.java)
			if(notFirstTime)
				intent.putExtra(KEY_SKIP_ENTRANCE, true)
			context.startActivity(intent)
		}
		
		fun start(context: Context, serverUrl: String, accessKey: String, studyWebId: Long) {
			val intent = Intent(context, WelcomeScreenActivity::class.java)
			intent.putExtra(KEY_SERVER_URL, serverUrl)
			intent.putExtra(KEY_ACCESS_KEY, accessKey)
			intent.putExtra(KEY_STUDY_WEB_ID, studyWebId)
			context.startActivity(intent)
		}
	}
}