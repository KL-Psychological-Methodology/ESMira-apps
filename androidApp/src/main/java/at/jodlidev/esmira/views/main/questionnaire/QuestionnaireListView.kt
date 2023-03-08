package at.jodlidev.esmira.views.main.questionnaire

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.colorLineBackground1
import at.jodlidev.esmira.colorLineBackground2
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.views.main.DefaultScaffoldView

/**
 * Created by JodliDev on 21.02.2023.
 */

@Composable
fun QuestionnaireListView(
	title: String,
	questionnaires: List<Questionnaire>,
	goBack: () -> Unit,
	gotoQuestionnaire: (Questionnaire) -> Unit,
	gotoDisabledQuestionnaires: (() -> Unit)?
) {
	DefaultScaffoldView(title, goBack) {
		LazyColumn(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxWidth()
		) {
			if(questionnaires.isNotEmpty()) {
				itemsIndexed(questionnaires) { i, questionnaire ->
					QuestionnaireLine(
						questionnaire = questionnaire,
						gotoQuestionnaire = gotoQuestionnaire,
						modifier = Modifier.background(color = if(i % 2 != 0) colorLineBackground1 else colorLineBackground2)
					)
				}
			}
			else {
				item {
					Text(stringResource(R.string.info_no_questionnaires))
				}
			}
			
			if(gotoDisabledQuestionnaires != null) {
				item {
					Spacer(modifier = Modifier.height(40.dp))
					TextButton(onClick = gotoDisabledQuestionnaires) {
						Text(stringResource(R.string.show_disabled_questionnaires))
					}
				}
			}
		}
	}
}

@Composable
fun QuestionnaireLine(
	questionnaire: Questionnaire,
	gotoQuestionnaire: (Questionnaire) -> Unit,
	modifier: Modifier = Modifier,
	active: Boolean = true
) {
	Column(
		horizontalAlignment = Alignment.End,
		modifier = modifier
			.alpha(if(active) 1F else 0.5F)
			.clickable { gotoQuestionnaire(questionnaire) }
			.padding(horizontal = 10.dp, vertical = 10.dp)
			.fillMaxWidth()
	) {
		Row {
			Text(questionnaire.title,
				fontSize = 18.sp,
				modifier = Modifier.weight(1f),
			)
			if(questionnaire.showJustFinishedBadge()) {
				Text(
					text = stringResource(R.string.just_finished),
					color = MaterialTheme.colorScheme.onTertiary,
					modifier = Modifier
						.background(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(5.dp))
						.padding(horizontal = 3.dp)
				)
			}
		}
		if(questionnaire.showLastCompleted()) {
			Text(stringResource(R.string.colon_last_filled_out, NativeLink.formatDateTime(questionnaire.lastCompleted)),
				fontSize = MaterialTheme.typography.labelSmall.fontSize,
			)
		}
		else
			Spacer(modifier = Modifier.height(15.dp))
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewQuestionnaireListView() {
	ESMiraSurface {
		val justFilledOutQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire Aang"}""")
		justFilledOutQuestionnaire.lastCompleted = NativeLink.getNowMillis()
		val completedQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire Katara"}""")
		completedQuestionnaire.lastCompleted = NativeLink.getNowMillis() - 1000 * 60 * 60 * 24
		QuestionnaireListView(
			title = stringResource(R.string.questionnaires),
			questionnaires = listOf(
				completedQuestionnaire,
				DbLogic.createJsonObj("""{"title": "Questionnaire Zuko"}"""),
				justFilledOutQuestionnaire,
				DbLogic.createJsonObj("""{"title": "Questionnaire Toph"}""")
			),
			{}, {}, {})
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewQuestionnaireListViewWithNoDisabled() {
	ESMiraSurface {
		val justFilledOutQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire Azula"}""")
		justFilledOutQuestionnaire.lastCompleted = NativeLink.getNowMillis()
		val completedQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire Sokka"}""")
		completedQuestionnaire.lastCompleted = NativeLink.getNowMillis() - 1000 * 60 * 60 * 24
		QuestionnaireListView(
			title = stringResource(R.string.disabled_questionnaires),
			questionnaires = listOf(
				completedQuestionnaire,
				DbLogic.createJsonObj("""{"title": "Questionnaire Suki"}"""),
				justFilledOutQuestionnaire,
				DbLogic.createJsonObj("""{"title": "Questionnaire Iroh"}""")
			),
			{}, {}, null)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewQuestionnaireListViewWithNoQuestionnaires() {
	ESMiraSurface {
		val justFilledOutQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire 3"}""")
		justFilledOutQuestionnaire.lastCompleted = NativeLink.getNowMillis()
		QuestionnaireListView(stringResource(R.string.questionnaires), ArrayList(), {}, {}, {})
	}
}