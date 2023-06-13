package at.jodlidev.esmira.views.main.questionnaire

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.views.DefaultButton

/**
 * Created by JodliDev on 31.03.2023.
 */

@Composable
fun QuestionnaireLine(
	questionnaire: Questionnaire,
	gotoQuestionnaire: (Questionnaire) -> Unit
) {
	DefaultButton(
		onClick = { gotoQuestionnaire(questionnaire) },
		modifier = Modifier
			.padding(all = 5.dp)
			.fillMaxWidth()
	) {
		Column(
			horizontalAlignment = Alignment.End,
			modifier = Modifier.fillMaxWidth()
		) {
			Row(modifier = Modifier.padding(all = 5.dp)) {
				Icon(Icons.Default.Article, "", tint = MaterialTheme.colorScheme.onSurface)
				Spacer(Modifier.size(ButtonDefaults.IconSpacing))
				Text(
					questionnaire.title,
					fontSize = MaterialTheme.typography.bodyLarge.fontSize,
					fontWeight = FontWeight.Normal,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.weight(1f),
				)
				if(questionnaire.showJustFinishedBadge()) {
					Text(
						text = stringResource(R.string.just_finished),
						fontSize = MaterialTheme.typography.labelMedium.fontSize,
						fontWeight = FontWeight.Normal,
						color = MaterialTheme.colorScheme.onTertiary,
						modifier = Modifier
							.background(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(5.dp))
							.padding(horizontal = 3.dp)
					)
				}
			}
			if(questionnaire.showLastCompleted()) {
				Text(
					stringResource(R.string.colon_last_filled_out, NativeLink.formatDateTime(questionnaire.lastCompleted)),
					fontSize = MaterialTheme.typography.labelMedium.fontSize,
					fontWeight = FontWeight.Normal,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.padding(all = 3.dp)
				)
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewQuestionnaireLine() {
	ESMiraSurface {
		val justFilledOutQuestionnaire = DbLogic.createJsonObj<Questionnaire>("""{"title": "Questionnaire 3"}""")
		justFilledOutQuestionnaire.lastCompleted = NativeLink.getNowMillis()
		QuestionnaireLine(justFilledOutQuestionnaire) {}
	}
}