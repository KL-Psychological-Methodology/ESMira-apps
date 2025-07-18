package at.jodlidev.esmira.views.main.questionnaire

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.androidNative.ChooseInputView
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Page
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinRunner
import at.jodlidev.esmira.views.TextButtonIconLeft
import at.jodlidev.esmira.views.TextButtonIconRight
import at.jodlidev.esmira.views.main.DefaultScaffoldView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Created by JodliDev on 21.02.2023.
 */
@Composable
fun QuestionnaireView(
	questionnaire: Questionnaire,
	pageNumber: Int,
	goBack: () -> Unit,
	goNext: () -> Unit
) {
	val context = LocalContext.current
	val listState = rememberLazyListState()
	val coroutineScope = rememberCoroutineScope()
	val page = questionnaire.getPage(pageNumber)
	val pageIsActive = remember { mutableStateOf(true) }
	val didRemindOfEmptyResponses = remember { mutableStateOf(false) }
	if(page.skipAfterSecs != 0) {
		val currentGoNext = rememberUpdatedState(newValue = goNext)
		LaunchedEffect(Unit) {
			delay(page.skipAfterSecs.seconds)
			if(pageIsActive.value)
				currentGoNext.value()
		}
	}
	
	DefaultScaffoldView(questionnaire.getQuestionnaireTitle(pageNumber), goBack) {
		MainView(
			listState,
			questionnaire,
			page,
			questionnaire.isLastPage(pageNumber),
			questionnaire.questionnairePageHasRequired(pageNumber)
		) {
			val errorIndex = questionnaire.checkQuestionnaire(pageNumber)
			if(errorIndex != -1) {
				Toast.makeText(context, R.string.error_missing_fields, Toast.LENGTH_SHORT).show()
				coroutineScope.launch {
					listState.animateScrollToItem(errorIndex)
				}
				
				return@MainView
			}
			val emptyResponseIndex = questionnaire.checkQuestionnaire(pageNumber, true)
			if (!didRemindOfEmptyResponses.value && emptyResponseIndex != -1) {
				Toast.makeText(context, R.string.hint_missing_fields, Toast.LENGTH_SHORT).show()
				coroutineScope.launch {
					listState.animateScrollToItem(emptyResponseIndex)
				}
				didRemindOfEmptyResponses.value = true
				return@MainView
			}
			pageIsActive.value = false
			goNext()
		}
	}
}

fun getBackgroundColor(i: Int): Color {
	return if(i % 2 == 0) colorLineBackground1 else colorLineBackground2
}

@Composable
fun MainView(
	listState: LazyListState,
	questionnaire: Questionnaire,
	page: Page,
	isLastPage: Boolean,
	hasRequired: Boolean,
	clickBtn: () -> Unit
) {
	val activeInputs = page.activeInputs

	LazyColumn(state = listState) {
		if(page.header.isNotEmpty()) {
			item {
				HtmlHandler.HtmlText(html = page.header, modifier = Modifier
					.fillMaxWidth()
					.padding(all = 5.dp)
				)
			}
		}

		itemsIndexed(activeInputs, { i, _ -> i }) { i, input ->
			ChooseInputView(
				questionnaire,
				input,
				Modifier
					.fillMaxWidth()
					.background(color = getBackgroundColor(i))
					.padding(all = 10.dp)
			)
		}
		
		var colorCount = activeInputs.size
		if(page.footer.isNotEmpty()) {
			item {
				HtmlHandler.HtmlText(html = page.footer, modifier = Modifier
					.fillMaxWidth()
					.background(color = getBackgroundColor(colorCount))
					.padding(all = 5.dp)
				)
			}
			++colorCount
		}
		
		item {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxWidth()
					.background(color = getBackgroundColor(colorCount))
					.padding(vertical = 20.dp)
			) {
				if(hasRequired) {
					Text(stringResource(R.string.info_required), modifier = Modifier
						.padding(all = 10.dp)
						.align(Alignment.Start))
				}
				
				if(isLastPage) {
					TextButtonIconLeft(
						text = stringResource(R.string.save),
						icon = Icons.Default.Save,
						onClick = clickBtn,
					)
				}
				else {
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
fun PreviewQuestionnaireView() {
	ESMiraSurface {
		QuestionnaireView(
			questionnaire = DbLogic.createJsonObj("""{"title": "Questionnaire", "pages": [{},{},{}]}"""),
			pageNumber = 1,
			goBack = {},
			goNext = {}
		)
	}
}