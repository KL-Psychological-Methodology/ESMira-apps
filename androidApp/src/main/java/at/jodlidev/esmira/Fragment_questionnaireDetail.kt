package at.jodlidev.esmira

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Page
import at.jodlidev.esmira.sharedCode.DbLogic
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.androidNative.ChooseInputView
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import java.io.File


/**
 * Created by JodliDev on 11.04.2019.
 */
class Fragment_questionnaireDetail : Base_fragment() {
	private lateinit var questionnaire: Questionnaire
	private var pageNumber: Int = 0
	private var formStarted: Long = 0
	private var originalOrientation: Int = 0
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		originalOrientation = requireActivity().requestedOrientation
		activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
		
		val state = savedInstanceState ?: arguments ?: return null
		val questionnaireId: Long = state.getLong(KEY_QUESTIONNAIRE, -1)
		
		questionnaire = DbLogic.getQuestionnaire(questionnaireId) ?: throw Exception()
		if(!questionnaire.canBeFilledOut())
			return null
		
		pageNumber = state.getInt(KEY_PAGE, 0)
		val isLastPage = pageNumber == questionnaire.pages.size - 1
		
		setTitle(questionnaire.getQuestionnaireTitle(pageNumber), false)
		
		for(i in 0 .. pageNumber) {
			for((j, value) in (state.getStringArrayList("$STATE_INPUT_DATA$i") ?: ArrayList()).withIndex()) {
				questionnaire.pages[i].orderedInputs[j].fromBackupString(value)
			}
		}
		
		return ComposeView(requireContext()).apply {
			setContent {
				val formStarted = rememberSaveable { state.getLong(KEY_FORM_STARTED, System.currentTimeMillis()) }
				val listState = rememberLazyListState()
				val coroutineScope = rememberCoroutineScope()
				
				ESMiraSurface {
					MainView(listState, questionnaire, questionnaire.pages[pageNumber], isLastPage) {
						
						val errorIndex = questionnaire.checkQuestionnaire(pageNumber)
						if(errorIndex != -1) {
							message(R.string.error_missing_fields)
							coroutineScope.launch {
								listState.animateScrollToItem(errorIndex)
							}

							return@MainView
						}

						if(isLastPage) {
							questionnaire.saveQuestionnaire(formStarted)
							goToAsRoot(Activity_main.SITE_QUESTIONNAIRE_SUCCESS, null)
						}
						else {
							val currentState = createState(pageNumber)
							arguments = currentState

							val gotoState = currentState.clone() as Bundle
							gotoState.putInt(KEY_PAGE, pageNumber + 1)

							goToAsSub(Activity_main.SITE_QUESTIONNAIRE_DETAIL, gotoState)
						}
					}
				}
			}
		}
	}
	
	@Composable
	fun MainView(listState: LazyListState, questionnaire: Questionnaire, page: Page, isLastPage: Boolean, clickBtn: () -> Unit) {
		LazyColumn(state = listState) {
			if(page.header.isNotEmpty()) {
				item {
					HtmlHandler.HtmlText(html = page.header, modifier = Modifier
						.fillMaxWidth()
						.padding(all = 5.dp)
					)
				}
			}
			
			itemsIndexed(page.inputs, { i, _ -> i }) { i, input ->
				ChooseInputView(
					questionnaire,
					input,
					Modifier
						.fillMaxWidth()
						.background(color = if(i % 2 == 0) colorLineBackground1 else colorLineBackground2)
						.padding(all = 5.dp)
				)
			}
			
			
			if(page.footer.isNotEmpty()) {
				item {
					HtmlHandler.HtmlText(html = page.footer, modifier = Modifier
						.fillMaxWidth()
						.background(color = if(page.inputs.size % 2 == 0) colorLineBackground1 else colorLineBackground2)
						.padding(all = 5.dp)
					)
				}
			}
			
			item {
				Row(
					horizontalArrangement = Arrangement.Center,
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 10.dp)
				) {
					if(isLastPage) {
						Spacer(modifier = Modifier.weight(1f))
						TextButtonIconLeft(
							text = stringResource(R.string.save),
							icon = Icons.Default.Save,
							onClick = clickBtn,
						)
					}
					else {
						Spacer(modifier = Modifier.weight(1f))
						TextButtonIconRight(
							text = stringResource(R.string.continue_),
							icon = Icons.Default.KeyboardArrowRight,
							onClick = clickBtn,
						)
					}
				}
			}
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMainView() {
		val questionnaire = DbLogic.createJsonObj<Questionnaire>("""{}""")
		val page = DbLogic.createJsonObj<Page>("""{
			"header": "This is the header",
			"footer": "This is the footer",
			"inputs":[
				{"responseType": "text", "text": "A meaningful description"},
				{"responseType": "va_scale", "text": "A meaningful description"}
			]
		}""")
		
		ESMiraSurface {
			MainView(listState = rememberLazyListState(), questionnaire = questionnaire, page = page, isLastPage = false) {}
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMainViewLastPage() {
		val questionnaire = DbLogic.createJsonObj<Questionnaire>("""{}""")
		val page = DbLogic.createJsonObj<Page>("""{
			"header": "This is the header",
			"footer": "This is the footer",
			"inputs":[
				{"responseType": "text", "text": "A meaningful description"},
				{"responseType": "va_scale", "text": "A meaningful description"}
			]
		}""")

		ESMiraSurface {
			MainView(listState = rememberLazyListState(), questionnaire = questionnaire, page = page, isLastPage = true) {}
		}
	}
	
	
	private fun createState(pageNumber: Int, bundle: Bundle = Bundle()): Bundle {
		bundle.putInt(KEY_PAGE, pageNumber)
		bundle.putLong(KEY_FORM_STARTED, formStarted)
		if(!this::questionnaire.isInitialized)
			return bundle
		
		for((i, page) in questionnaire.pages.withIndex()) {
			val pageCache = ArrayList<String>()
			for(input in page.orderedInputs) {
				pageCache.add(input.getBackupString())
			}
			bundle.putStringArrayList("$STATE_INPUT_DATA$i", pageCache)
		}
		bundle.putLong(KEY_QUESTIONNAIRE, questionnaire.id)
		
		return bundle
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		createState(pageNumber, outState)

		super.onSaveInstanceState(outState)
	}
	
	override fun onDestroy() {
		activity?.requestedOrientation = originalOrientation
		super.onDestroy()
	}
	
	
	companion object {
		const val KEY_QUESTIONNAIRE: String = "questionnaire_id"
		private const val KEY_PAGE: String = "pageIndex"
		private const val KEY_FORM_STARTED: String = "form_started"
		private const val STATE_INPUT_DATA: String = "input_data"
	}
}