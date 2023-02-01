package at.jodlidev.esmira.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox

/**
 * Created by JodliDev on 24.04.2019.
 */
class ErrorReportDialogActivity : ComponentActivity() {
	private val error = mutableStateOf("")
	private var comment = mutableStateOf("")
	private val openWhatIsSentDialog = mutableStateOf(false)
	private val openToWhomDialog = mutableStateOf(false)
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			ESMiraSurface {
				MainDialog(!intent.hasExtra(EXTRA_CALLED_MANUALLY) || intent.extras?.getBoolean(EXTRA_CALLED_MANUALLY) != true)
				if(openWhatIsSentDialog.value)
					WhatIsSentDialog(DbLogic.getErrors(), ErrorBox.getReportHeader(comment.value).toString())
				if(openToWhomDialog.value)
					ToWhomDialog()
			}
		}
		DbLogic.setErrorsAsReviewed()
	}
	
	private fun sendErrorReport() {
		val testLabSetting = Settings.System.getString(applicationContext.contentResolver, "firebase.test.lab")
		if("true" != testLabSetting) {//we don't want to have google bots trigger the error report
			Web.sendErrorReportAsync(
				comment = comment.value.ifEmpty { null },
				onError = { msg -> error.value = msg },
				onSuccess = {
					runOnUiThread {
						Toast.makeText(applicationContext, R.string.info_thank_you, Toast.LENGTH_SHORT).show()
					}
					finish()
				}
			)
		}
	}
	
	@Composable
	fun ToWhomDialog() {
		AlertDialog(
			onDismissRequest = {
				openToWhomDialog.value = false
			},
			title = {
				Text(stringResource(R.string.sent_to_whom))
			},
			text = {
				Text(stringResource(R.string.data_will_be_sent_to_app_developer, Web.DEV_SERVER))
			},
			confirmButton = {
				DialogButton(stringResource(R.string.close),
					onClick = {
						openToWhomDialog.value = false
					})
			},
		)
	}
	
	@Composable
	fun WhatIsSentDialog(errors: List<ErrorBox>, errorReportHeader: String) {
		Dialog(
			onDismissRequest = {
				openWhatIsSentDialog.value = false
			},
			content = {
				ESMiraSurface {
					Column {
						Text(stringResource(R.string.what_is_sent),
							fontSize = 18.sp,
							modifier = Modifier.padding(all = 20.dp)
						)
						LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
							item {
								Text(errorReportHeader,
									fontSize = 12.sp,
									modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
								)
							}
							
							itemsIndexed(errors, { i, _ -> i }) { i, error ->
								Column(
									modifier = Modifier
										.background(color = if(i % 2 == 0) colorLineBackground1 else colorLineBackground2)
										.padding(all = 20.dp)
								) {
									Row {
										Text(
											error.title,
											modifier = Modifier.weight(1f),
											fontWeight = FontWeight.Bold,
											color = when(error.severity) {
												ErrorBox.SEVERITY_ERROR -> colorError
												ErrorBox.SEVERITY_WARN -> colorWarn
												else -> MaterialTheme.colors.onSurface
											}
										)
										Text(error.getFormattedDateTime(), fontSize = 10.sp)
									}
									Text(error.msg, modifier = Modifier.padding(all = 5.dp))
								}
							}
						}
						Row {
							Spacer(modifier = Modifier.weight(1f))
							DialogButton(stringResource(R.string.close),
								onClick = {
									openWhatIsSentDialog.value = false
								},
								modifier = Modifier.padding(all = 10.dp)
							)
						}
					}
				}
			}
		)
	}
	
	@Composable
	fun MainDialog(withHeader: Boolean) {
		Column {
			Column(
				modifier = Modifier
					.padding(all = 20.dp)
					.verticalScroll(rememberScrollState())
			) {
				Text(stringResource(R.string.send_error_report), fontSize = 18.sp)
				Spacer(modifier = Modifier.size(20.dp))
				if(withHeader) {
					Text(stringResource(R.string.info_error_report_header), fontWeight = FontWeight.Bold)
					Text(stringResource(R.string.info_error_report_desc))
					Spacer(modifier = Modifier.size(30.dp))
				}
				TextField(
					modifier = Modifier
						.fillMaxWidth()
						.height(150.dp),
					value = comment.value,
					onValueChange = {
						comment.value = it
					},
					label = { Text(stringResource(R.string.hint_error_comment)) }
				)
				Row {
					DialogButton(stringResource(R.string.what_is_sent),
						onClick = {
							openWhatIsSentDialog.value = true
						})
					Spacer(modifier = Modifier.weight(1f))
					DialogButton(stringResource(R.string.sent_to_whom),
						onClick = {
							openToWhomDialog.value = true
						})
				}
				
				Spacer(modifier = Modifier.size(20.dp))
				if(error.value.isNotEmpty()) {
					Text(error.value, fontSize = 18.sp, color = colorRed, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
				}
			}
			Row(modifier = Modifier.padding(5.dp)) {
				Spacer(modifier = Modifier.weight(1f))
				DialogButton(stringResource(R.string.cancel),
					onClick = {
						finish()
					})
				DialogButton(stringResource(R.string.send),
					onClick = {
						sendErrorReport()
					})
			}
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewWhatIsSent() {
		val errors = listOf(
			ErrorBox("Error title", ErrorBox.SEVERITY_ERROR, "Content of message"),
			ErrorBox("Log title1", ErrorBox.SEVERITY_LOG, "Content of message"),
			ErrorBox("Log title2", ErrorBox.SEVERITY_LOG, "Content of message"),
			ErrorBox("Warn title", ErrorBox.SEVERITY_WARN, "Content of message"),
			ErrorBox("Log title3", ErrorBox.SEVERITY_LOG, "Content of message")
		)
		ESMiraSurface {
			WhatIsSentDialog(errors, "Error report header")
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewWithHeader() {
		ESMiraSurface {
			MainDialog(true)
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewWithoutHeader() {
		ESMiraSurface {
			MainDialog(false)
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewWithError() {
		ESMiraSurface {
			error.value = "Test error"
			MainDialog(true)
		}
	}
	
	companion object {
		private const val EXTRA_CALLED_MANUALLY = "with_error"
		
		fun start(context: Context, calledManually: Boolean = false) {
			val intent = Intent(context, ErrorReportDialogActivity::class.java)
			intent.putExtra(EXTRA_CALLED_MANUALLY, calledManually)
			if(context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			context.startActivity(intent)
		}
	}
}