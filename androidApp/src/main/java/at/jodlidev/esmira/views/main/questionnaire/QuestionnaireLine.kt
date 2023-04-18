package at.jodlidev.esmira.views.main.questionnaire

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire

/**
 * Created by JodliDev on 31.03.2023.
 */

@Composable
fun QuestionnaireLine(
	questionnaire: Questionnaire,
	gotoQuestionnaire: (Questionnaire) -> Unit,
	active: Boolean = true
) {
	Column(modifier = Modifier.clickable { gotoQuestionnaire(questionnaire) }) {
		Column(
			horizontalAlignment = Alignment.End,
			modifier = Modifier
				.padding(horizontal = 10.dp, vertical = 10.dp)
				.alpha(if(active) 1F else 0.5F)
				.fillMaxWidth()
		) {
			Row {
				Icon(Icons.Default.ArrowForward, "", tint = MaterialTheme.colorScheme.onSurface)
				Spacer(Modifier.size(ButtonDefaults.IconSpacing))
				Text(
					questionnaire.title,
					fontSize = MaterialTheme.typography.bodyLarge.fontSize,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.weight(1f),
				)
				if(questionnaire.showJustFinishedBadge()) {
					Text(
						text = stringResource(R.string.just_finished),
						fontSize = MaterialTheme.typography.labelMedium.fontSize,
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
					color = MaterialTheme.colorScheme.onSurface
				)
			}
			
		}
		Divider(
			color = MaterialTheme.colorScheme.outline,
			modifier = Modifier.padding(horizontal = 10.dp)
		)
	}
}