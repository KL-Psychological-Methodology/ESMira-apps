package at.jodlidev.esmira.views.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.DefaultButtonIconAbove
import at.jodlidev.esmira.views.DialogButton
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 28.02.2023.
 */
@Composable
fun RewardView(
	getStudy: () ->Study,
	goBack: () -> Unit
) {
	val context = LocalContext.current
	val study = getStudy()
	val error = remember { mutableStateOf("") }
	val fulfilledQuestionnaires = remember {
		mutableMapOf<Long, Boolean>()
	}
	val rewardCode = produceState(initialValue = "") {
		study.getRewardCode(
			onError = { error.value = it },
			onSuccess = { rewardInfo ->
				ErrorBox.log("Reward", "Received reward response (errorMessage: ${rewardInfo.errorMessage}, errorCode: ${rewardInfo.errorCode}, fulfilledQuestionnaires: ${rewardInfo.fulfilledQuestionnaires})")
				when(rewardInfo.errorCode) {
					Study.REWARD_SUCCESS ->
						value = rewardInfo.code
					Study.REWARD_ERROR_UNFULFILLED_REWARD_CONDITIONS -> {
						error.value = context.getString(R.string.error_reward_conditions_not_met)
						fulfilledQuestionnaires.putAll(rewardInfo.fulfilledQuestionnaires)
					}
					Study.REWARD_ERROR_ALREADY_GENERATED ->
						error.value = context.getString(R.string.error_already_generated)
					else -> error.value = rewardInfo.errorMessage
				}
			}
		)
	}
	DefaultScaffoldView(title = stringResource(R.string.rewards), goBack = goBack) {
		Column(
			modifier = Modifier
				.padding(all = 20.dp)
				.fillMaxWidth()
		) {
			val untilActive = study.daysUntilRewardsAreActive()
			if(!study.enableRewardSystem || untilActive != 0) {
				val resources = LocalContext.current.resources
				RewardErrorView(
					study,
					resources.getQuantityString(R.plurals.info_reward_is_not_active_yet, untilActive, untilActive),
					HashMap()
				)
			}
			else {
				if(error.value.isNotEmpty())
					RewardErrorView(study, error.value, fulfilledQuestionnaires)
				else if(rewardCode.value.isEmpty())
					RewardLoadingView()
				else
					RewardCodeView(study, rewardCode.value)
			}
		}
	}
}

@Composable
fun RewardCodeView(study: Study, rewardCode: String) {
	val showRewardCodeExplanation = remember { mutableStateOf(false) }
	val context = LocalContext.current
	if(showRewardCodeExplanation.value) {
		ESMiraDialog(
			onDismissRequest = { showRewardCodeExplanation.value = false },
			title = null,
			confirmButtonLabel = stringResource(R.string.close),
			onConfirmRequest = { showRewardCodeExplanation.value = false },
		) {
			Text(stringResource(R.string.reward_code_description))
		}
	}
	Row(
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(stringResource(R.string.colon_reward_code_header))
		Spacer(modifier = Modifier.size(10.dp))
		DialogButton(stringResource(id = R.string.what_for), onClick = { showRewardCodeExplanation.value = true })
	}
	
	Spacer(modifier = Modifier.size(10.dp))
	
	Column(
		modifier = Modifier
			.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		TextButton(
			onClick = {
			val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
				val clipData = ClipData.newPlainText("label", rewardCode)
				clipBoard.setPrimaryClip(clipData)
				Toast.makeText(context, context.getString(R.string.android_info_copied_x_to_clipboard, rewardCode), Toast.LENGTH_SHORT).show()
			},
			colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
		) {
			Text(rewardCode, fontSize = MaterialTheme.typography.headlineLarge.fontSize, fontWeight = FontWeight.Bold)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Icon(Icons.Default.ContentCopy,
				contentDescription = "copy",
				modifier = Modifier.size(ButtonDefaults.IconSize)
			)
		}
		
		Spacer(modifier = Modifier.size(30.dp))
		
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			if(study.contactEmail.isNotEmpty()) {
				DefaultButtonIconAbove(
					icon = Icons.Default.Email,
					text = stringResource(R.string.reward_code_send_email),
					modifier = Modifier.weight(0.45F),
					onClick = {
						val intent = Intent(Intent.ACTION_SEND)
						val emailContent = study.rewardEmailContent.ifEmpty { context.getString(R.string.reward_code_content) }
						intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(study.contactEmail))
						intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.android_email_study_subject, study.title))
						intent.putExtra(Intent.EXTRA_TEXT, emailContent.replace("[[CODE]]", rewardCode))
						intent.type = "text/plain"
						try {
							context.startActivity(intent)
						} catch(e: Exception) {
							Toast.makeText(context, R.string.error_no_email_client, Toast.LENGTH_LONG).show()
						}
					}
				)
				
				Spacer(modifier = Modifier.weight(0.1F))
			}
			DefaultButtonIconAbove(
				icon = Icons.Default.Share,
				text = stringResource(R.string.reward_code_share),
				modifier = Modifier.weight(0.45F),
				onClick = {
					val intent = Intent(Intent.ACTION_SEND)
					intent.putExtra(Intent.EXTRA_TEXT, rewardCode)
					intent.type = "text/plain"
					try {
						context.startActivity(intent)
					} catch(e: Exception) {
						Toast.makeText(context, R.string.error_no_email_client, Toast.LENGTH_LONG).show()
					}
				}
			)
		}
	}
	if(study.rewardInstructions.isNotEmpty()) {
		Spacer(modifier = Modifier.size(20.dp))
		Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
			HtmlHandler.HtmlText(study.rewardInstructions, modifier = Modifier.fillMaxWidth())
		}
	}
}


@Composable
fun RewardLoadingView() {
	Column(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		CircularProgressIndicator()
	}
}

@Composable
fun RewardErrorView(study: Study, error: String, fulfilledQuestionnaires: Map<Long, Boolean>) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(error, textAlign = TextAlign.Center)
	}
	
	if(fulfilledQuestionnaires.isNotEmpty() && study.questionnaires.isNotEmpty()) {
		Spacer(modifier = Modifier.size(30.dp))
		Text(stringResource(id = R.string.error_reward_questionnaires_not_finished))
		Spacer(modifier = Modifier.size(10.dp))
		
		
		study.questionnaires.forEach { questionnaire ->
			Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
				Text("${questionnaire.title}:", modifier = Modifier.weight(1F))
				val modifier = Modifier.width(50.dp)
				if(fulfilledQuestionnaires[questionnaire.internalId] == true)
					Icon(Icons.Default.CheckCircle, "true", tint = colorGreen, modifier = modifier)
				else
					Icon(Icons.Default.Cancel, "false", tint = colorRed, modifier = modifier)
			}
		}
//		LazyVerticalGrid(
//			columns = GridCells.Fixed(2),
//			horizontalArrangement = Arrangement.Center,
//			verticalArrangement = Arrangement.Center
//		) {
//			study.questionnaires.forEach { questionnaire ->
//				item {
//					Text("${questionnaire.title}:")
//				}
//				item {
//					Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
//						if(fulfilledQuestionnaires[questionnaire.internalId] == true)
//							Icon(Icons.Default.CheckCircle, "true", tint = colorGreen)
//						else
//							Icon(Icons.Default.Cancel, "false", tint = colorRed)
//					}
//				}
//			}
//		}
	}
}

@Preview
@Composable
fun PreviewNotActiveYet() {
	ESMiraSurface {
		val study = Study.newInstance("", "", """{"id":1, "enableRewardSystem": true, "rewardVisibleAfterDays": 1}""")
		study.joinedTimestamp = NativeLink.getNowMillis()
		RewardErrorView(study, "Preview error message", HashMap())
	}
}

@Preview
@Composable
fun ErrorWithFulfilledQuestionnairesPreview() {
	ESMiraSurface {
		val study = Study.newInstance(
			"",
			"",
			"""{"id":1, "enableRewardSystem": true, "questionnaires": [{"title": "This is a very long title for a questionnaire that hopefully has a break", "internalId": 1}, {"title": "questionnaire 2", "internalId": 2}]}"""
		)
		Column(modifier = Modifier.width(400.dp)) {
			RewardErrorView(study, "Preview error message", mapOf(Pair(1L, true), Pair(2L, false)))
		}
	}
}