package at.jodlidev.esmira.views.main.questionnaire

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.views.main.DefaultScaffoldView

/**
 * Created by JodliDev on 21.02.2023.
 */
@Composable
fun QuestionnaireFinishedView(
	close: () -> Unit
) {
	DefaultScaffoldView(title = "", null) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier.fillMaxSize().clickable(onClick = close)
		) {
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Text(stringResource(R.string.info_questionnaire_success), modifier = Modifier.padding(all = 20.dp))
				TextButton(onClick = close) {
					Text(stringResource(R.string.ok_))
				}
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewQuestionnaireFinishedView() {
	ESMiraSurface {
		QuestionnaireFinishedView {}
	}
}