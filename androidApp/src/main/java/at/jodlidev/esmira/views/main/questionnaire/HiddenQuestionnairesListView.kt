package at.jodlidev.esmira.views.main.questionnaire

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.views.main.DefaultScaffoldView

/**
 * Created by JodliDev on 21.02.2023.
 */

@Composable
fun HiddenQuestionnairesListView(
	questionnaires: List<Questionnaire>,
	goBack: () -> Unit,
	gotoQuestionnaire: (Questionnaire) -> Unit,
) {
	DefaultScaffoldView(stringResource(R.string.disabled_questionnaires), goBack) {
		LazyColumn(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxWidth()
		) {
			if(questionnaires.isNotEmpty()) {
				items(questionnaires) { questionnaire ->
					QuestionnaireLine(
						questionnaire = questionnaire,
						gotoQuestionnaire = gotoQuestionnaire,
					)
				}
			}
			else {
				item {
					Text(stringResource(R.string.info_no_questionnaires))
				}
			}
		}
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewDisabledQuestionnairesListView() {
	ESMiraSurface {
		val justFilledOutQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire Aang"}""")
		justFilledOutQuestionnaire.lastCompleted = NativeLink.getNowMillis()
		val completedQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire Katara"}""")
		completedQuestionnaire.lastCompleted = NativeLink.getNowMillis() - 1000 * 60 * 60 * 24
		HiddenQuestionnairesListView(
			questionnaires = listOf(
				completedQuestionnaire,
				DbLogic.createJsonObj("""{"title": "Questionnaire Zuko"}"""),
				justFilledOutQuestionnaire,
				DbLogic.createJsonObj("""{"title": "Questionnaire Toph"}""")
			),
			{}, {})
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewDisabledQuestionnairesListViewWithNoQuestionnaires() {
	ESMiraSurface {
		val justFilledOutQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire 3"}""")
		justFilledOutQuestionnaire.lastCompleted = NativeLink.getNowMillis()
		HiddenQuestionnairesListView(ArrayList(), {}, {})
	}
}