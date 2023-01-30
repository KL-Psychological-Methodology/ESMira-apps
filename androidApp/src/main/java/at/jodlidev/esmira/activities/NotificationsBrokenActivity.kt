package at.jodlidev.esmira.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.views.welcome.permissions.NotificationFailedView

/**
 * Created by JodliDev on 24.04.2019.
 */
class NotificationsBrokenActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			val context = LocalContext.current
			val showDontKillMyAppDialog = rememberSaveable { mutableStateOf(false) }
			val data = produceState<Web.Companion.DontKillMyAppInfo?>(null) {
				Web.getDonKillMyAppInfo(
					onError = {
						runOnUiThread {
							showDontKillMyAppDialog.value = false
							Toast.makeText(context, it, Toast.LENGTH_LONG).show()
						}
					},
					onSuccess = {
						value = it
					}
				)
			}
			
			ESMiraSurface {
				if(showDontKillMyAppDialog.value && data.value != null) {
					DontKillMyAppDialog(
						data.value!!,
						close = { showDontKillMyAppDialog.value = false }
					)
				}
				else {
					MainDialog(
						withHeader = !intent.hasExtra(EXTRA_CALLED_MANUALLY) || intent.extras?.getBoolean(EXTRA_CALLED_MANUALLY) != true,
						getData = { data.value },
						openDontKillMyAppDialog = { showDontKillMyAppDialog.value = true }
					)
				}
			}
		}
		DbLogic.setErrorsAsReviewed()
	}
	
	@Composable
	fun MainDialog(
		withHeader: Boolean,
		getData: () -> Web.Companion.DontKillMyAppInfo?,
		openDontKillMyAppDialog: () -> Unit
	) {
		Column {
			Column(
				modifier = Modifier
					.padding(all = 20.dp)
					.weight(1f)
					.verticalScroll(rememberScrollState())
			) {
				if(withHeader) {
					Text(stringResource(R.string.error_notifications_broken_android_header), fontWeight = FontWeight.Bold)
					Spacer(modifier = Modifier.height(5.dp))
				}
				
				NotificationFailedView()
				
				Spacer(modifier = Modifier.height(20.dp))
				
				Column(
					modifier = Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					val data = getData()
					if(data == null) {
						Text(stringResource(R.string.state_checking_device_model))
						CircularProgressIndicator()
					}
					else if(!data.notFound) {
						Text(stringResource(R.string.error_notifications_broken_android_desc))
						DefaultButton(stringResource(R.string.open_device_instructions),
							modifier = Modifier.fillMaxWidth(),
							onClick = openDontKillMyAppDialog
						)
					}
				}
				
			}
			Row(modifier = Modifier.padding(5.dp)) {
				Spacer(modifier = Modifier.weight(1f))
				DialogButton(stringResource(R.string.close),
					onClick = {
						finish()
					})
			}
		}
	}
	
	@Composable
	fun DontKillMyAppDialog(data: Web.Companion.DontKillMyAppInfo, close: () -> Unit) {
		Dialog(
			onDismissRequest = {
				close()
			},
			content = {
				ESMiraSurface {
					Column {
						val uriHandler = LocalUriHandler.current
						val url = "${data.domain}${data.url}"
						Column(
							modifier = Modifier
								.padding(top = 20.dp, start = 20.dp, end = 20.dp)
								.weight(1F)
								.verticalScroll(rememberScrollState())
						) {
							Text(data.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
							TextButton(onClick = { uriHandler.openUri(url) }) {
								Text(
									stringResource(R.string.from_X, url),
									fontSize = 12.sp,
									modifier = Modifier
										.padding(start = 20.dp)
								)
							}
							Spacer(modifier = Modifier.height(10.dp))
							HtmlHandler.HtmlText(data.explanation)
							Spacer(modifier = Modifier.height(10.dp))
							HtmlHandler.HtmlText(data.user_solution)
						}
						
						Row(modifier = Modifier.padding(5.dp)) {
							Spacer(modifier = Modifier.weight(1f))
							DialogButton(stringResource(R.string.close), onClick = close)
						}
					}
				}
			}
		)
	}
	
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMainDialogWithHeaderAndLoading() {
		ESMiraSurface {
			MainDialog(true, { null }, {})
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMainDialogWithoutHeaderAndNoModelInfo() {
		ESMiraSurface {
			MainDialog(false, { Web.Companion.DontKillMyAppInfo(
				"nothing",
				"/nothing",
				"",
				"",
				true
			) }, {})
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMainDialogWithoutHeaderAndWithModelInfo() {
		ESMiraSurface {
			MainDialog(false, { Web.Companion.DontKillMyAppInfo(
				"something",
				"/something",
				"This is an explanation",
				"This is a proposed solution",
				false
			) }, {})
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewDontKillMyAppDialog() {
		ESMiraSurface {
			DontKillMyAppDialog(
				Web.Companion.DontKillMyAppInfo(
					"something",
					"/something",
					"This is an explanation",
					"This is a proposed solution",
					false
				)
			) {}
		}
	}
	
	companion object {
		private const val EXTRA_CALLED_MANUALLY = "with_error"
		
		fun start(context: Context, calledManually: Boolean = false) {
			val intent = Intent(context, NotificationsBrokenActivity::class.java)
			intent.putExtra(EXTRA_CALLED_MANUALLY, calledManually)
			if(context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			context.startActivity(intent)
		}
	}
}