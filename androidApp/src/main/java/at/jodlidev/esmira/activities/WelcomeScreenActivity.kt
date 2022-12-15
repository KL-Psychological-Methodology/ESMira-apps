package at.jodlidev.esmira.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.jodlidev.esmira.*
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.views.welcome.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

/**
 * Created by JodliDev on 26.04.2019.
 */
class WelcomeScreenActivity: ComponentActivity() {
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val extras = intent.extras
		val skipEntrance = extras != null && extras.getBoolean(KEY_SKIP_ENTRANCE)
		
		setContent {
			ESMiraSurface {
				val navController = rememberAnimatedNavController()
				val showCancelWarning = remember { mutableStateOf(false) }
				
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
				
				if(showCancelWarning.value)
					CancelWarningDialog(showCancelWarning) {
						finish()
					}
				
				MainView(
					startDestination = if(skipEntrance) "qrQuestion" else "entrance",
					getServerList = {
						Web.serverList
					},
					openAddStudyActivity = { serverTitle: String, serverUrl: String, accessKey: String, studyId: Long ->
						Activity_addStudy.start(this, serverTitle, serverUrl, accessKey, studyId)
					},
					navController = navController
				)
			}
		}
	}
	
	
	@OptIn(ExperimentalAnimationApi::class)
	@Composable
	fun MainView(
		startDestination: String,
		getServerList: () -> List<Pair<String, String>>,
		openAddStudyActivity: (serverTitle: String, serverUrl: String, accessKey: String, studyId: Long) -> Unit,
		navController: NavHostController = rememberAnimatedNavController()
	) {
		val serverTitle = remember { mutableStateOf("") }
		val serverUrl = remember { mutableStateOf("") }
		val accessKey = remember { mutableStateOf("") }
		
		
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
					gotoNext = { serverTitle: String, serverUrl: String, accessKey: String, studyId: Long ->
						openAddStudyActivity(serverTitle, serverUrl, accessKey, studyId)
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
					gotoNext = { _serverTitle: String, _serverUrl: String ->
						serverTitle.value = _serverTitle
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
						openAddStudyActivity(serverTitle.value, serverUrl.value, _accessKey, 0)
					},
				)
			}
		}
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
				TextButton(
					onClick = {
						openState.value = false
					}
				) {
					Text(stringResource(R.string.cancel))
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						openState.value = false
						onConfirm()
					}
				) {
					Text(stringResource(R.string.ok_))
				}
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
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMainView() {
		ESMiraSurface {
			MainView(
				startDestination = "entrance",
				getServerList = { ArrayList() },
				openAddStudyActivity = { _, _, _, _ ->}
			)
		}
	}
	
	companion object {
		private const val KEY_SKIP_ENTRANCE = "skip_entrance"
		fun start(context: Context, notFirstTime: Boolean = false) {
			val intent = Intent(context, WelcomeScreenActivity::class.java)
			if(notFirstTime)
				intent.putExtra(KEY_SKIP_ENTRANCE, true)
			context.startActivity(intent)
		}
	}
}