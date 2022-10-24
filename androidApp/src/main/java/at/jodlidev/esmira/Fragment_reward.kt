package at.jodlidev.esmira

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.ComposeView
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
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.dialog.MaterialAlertDialogBuilder


/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_reward : Base_fragment() {
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val arguments: Bundle = arguments ?: return null
		
		study = DbLogic.getStudy(arguments.getLong(KEY_STUDY_ID)) ?: throw Exception("Study is null (id: ${arguments.getLong(KEY_STUDY_ID)})!")
		
		return ComposeView(requireContext()).apply {
			setContent {
				ESMiraSurface {
					RewardView()
				}
			}
		}
	}
	
	private lateinit var study: Study
	private var rewardCode by mutableStateOf("")
	private var error by mutableStateOf("")
	private var fulfilledQuestionnaires: Map<Long, Boolean> = HashMap()
	
	@Composable
	fun ErrorView(error: String) {
		Column(
			modifier = Modifier.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(error, textAlign = TextAlign.Center)
		}
		
		if(fulfilledQuestionnaires.isNotEmpty()) {
			Spacer(modifier = Modifier.size(30.dp))
			Text(stringResource(id = R.string.error_reward_questionnaires_not_finished))
			Spacer(modifier = Modifier.size(10.dp))
			LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.Center) {
				study.questionnaires.forEach { questionnaire ->
					item {
						Text("${questionnaire.title}:")
					}
					item {
						if(fulfilledQuestionnaires[questionnaire.internalId] == true)
							Icon(Icons.Default.CheckCircle, "true", tint = colorGreen)
						else
							Icon(Icons.Default.Cancel, "false", tint = colorRed)
					}
				}
			}
		}
	}
	
	@Composable
	fun LoadingView() {
		study.getRewardCode(
			onError = {msg: String ->
				error = msg
			},
			onSuccess = { rewardInfo ->
				when(rewardInfo.errorCode) {
					Study.REWARD_SUCCESS ->
						rewardCode = rewardInfo.code
					Study.REWARD_ERROR_UNFULFILLED_REWARD_CONDITIONS -> {
						error = getString(R.string.error_reward_conditions_not_met)
						println(rewardInfo.fulfilledQuestionnaires)
						fulfilledQuestionnaires = rewardInfo.fulfilledQuestionnaires
					}
					Study.REWARD_ERROR_ALREADY_GENERATED ->
						error = getString(R.string.error_already_generated)
					else -> error = rewardInfo.errorMessage
				}
			}
		)
		Column(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			CircularProgressIndicator()
		}
	}
	
	@Composable
	fun CodeView() {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(stringResource(R.string.colon_reward_code_header))
			Spacer(modifier = Modifier.size(10.dp))
			TextButton(onClick = {
				context?.let {
					MaterialAlertDialogBuilder(it, R.style.AppTheme_ActivityDialog)
						.setMessage(R.string.reward_code_description)
						.setPositiveButton(R.string.close, null)
						.show()
				}
			}) {
				Text(stringResource(id = R.string.what_for))
			}
		}
		
		Spacer(modifier = Modifier.size(10.dp))
		
		Column(
			modifier = Modifier
				.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			TextButton(onClick = {
				val context = requireContext()
				val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
				val clipData = ClipData.newPlainText("label", rewardCode)
				clipBoard.setPrimaryClip(clipData)
				Toast.makeText(context, context.getString(R.string.android_info_copied_x_to_clipboard, rewardCode), Toast.LENGTH_SHORT).show()
			}) {
				Row {
					Text(rewardCode, fontSize = 24.sp, fontWeight = FontWeight.Bold)
					Icon(Icons.Default.ContentCopy, "copy")
				}
			}
			
			Spacer(modifier = Modifier.size(30.dp))
			
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly
			) {
				if(study.contactEmail.isNotEmpty()) {
					DefaultButton(
						modifier = Modifier.weight(0.45F),
						onClick = {
							val context = requireContext()
							val intent = Intent(Intent.ACTION_SEND)
							val emailContent = study.rewardEmailContent.ifEmpty { context.getString(R.string.reward_code_content) }
							intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(study.contactEmail))
							intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.android_email_study_subject, study.title))
							intent.putExtra(Intent.EXTRA_TEXT, emailContent.replace("[[CODE]]", rewardCode))
							intent.type = "plain/text"
							try {
								context.startActivity(intent)
							} catch(e: Exception) {
								Toast.makeText(context, R.string.error_no_email_client, Toast.LENGTH_LONG).show()
							}
						}
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							Icon(Icons.Default.Email, "email")
							Text(stringResource(R.string.reward_code_send_email))
						}
						
					}
					
					Spacer(modifier = Modifier.weight(0.1F))
				}
				DefaultButton(
					modifier = Modifier.weight(0.45F),
					onClick = {
						val context = requireContext()
						val intent = Intent(Intent.ACTION_SEND)
						intent.putExtra(Intent.EXTRA_TEXT, rewardCode)
						intent.type = "plain/text"
						try {
							context.startActivity(intent)
						} catch(e: Exception) {
							Toast.makeText(context, R.string.error_no_email_client, Toast.LENGTH_LONG).show()
						}
					}
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Icon(Icons.Default.Share, "share")
						Text(stringResource(R.string.reward_code_share))
					}
				}
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
	fun RewardView() {
		Column(
			modifier = Modifier
				.padding(all = 10.dp)
				.fillMaxWidth()
		) {
			val untilActive = study.daysUntilRewardsAreActive()
			if(!study.enableRewardSystem || untilActive != 0) {
				val resources = LocalContext.current.resources
				ErrorView(resources.getQuantityString(R.plurals.info_reward_is_not_active_yet, untilActive, untilActive))
			}
			else {
				if(error.isNotEmpty())
					ErrorView(error)
				else if(rewardCode.isEmpty())
					LoadingView()
				else
					CodeView()
			}
		}
	}
	
	@Preview
	@Composable
	fun PreviewNotActiveYet() {
		ESMiraSurface {
			study = Study.newInstance("", "", """{"id":1, "enableRewardSystem": true, "rewardVisibleAfterDays": 1}""")
			study.joined = NativeLink.getNowMillis()
			RewardView()
		}
	}
	
	@Preview
	@Composable
	fun ErrorWithFulfilledQuestionnairesPreview() {
		ESMiraSurface {
			error = "Preview error message"
			study = Study.newInstance(
				"",
				"",
				"""{"id":1, "enableRewardSystem": true, "questionnaires": [{"title": "questionnaire1", "internalId": 1}, {"title": "questionnaire2", "internalId": 2}]}"""
			)
			fulfilledQuestionnaires = mapOf(Pair(1L, true), Pair(2L, false))
			RewardView()
		}
	}
	
	@Preview
	@Composable
	fun PreviewCode() {
		ESMiraSurface {
			study = Study.newInstance("", "", """{"id":1, "enableRewardSystem": true, "contactEmail": "test@test", "rewardInstructions": "<div>Some <b>instructions</b><br/><br/>new<br/>line</div>"}""")
			rewardCode = "Test code"
			RewardView()
		}
	}
	
	companion object {
		const val KEY_STUDY_ID: String = "study_id"
	}
}


