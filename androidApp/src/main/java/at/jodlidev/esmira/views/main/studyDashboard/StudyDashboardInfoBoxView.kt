package at.jodlidev.esmira.views.main.studyDashboard

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.main.StudyInformationView

@Composable
fun StudyDashboardInfoBoxView(
	header: String?,
	content: String,
	important: Boolean = false
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(all = 5.dp)
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxSize()
				.heightIn(min = if(header == null) 0.dp else 90.dp)
				.border(
					width = 1.dp,
					color = if(important) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
				)
		) {
			if(header != null) {
				Text(
					header,
					color = if(important) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
					fontSize = MaterialTheme.typography.bodyMedium.fontSize,
					fontWeight = if(important) FontWeight.Bold else FontWeight.Normal,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.padding(top = 5.dp, start = 5.dp, end = 5.dp)
				)
			}
			Text(
				content,
				color = if(important) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground,
				fontSize = MaterialTheme.typography.bodyMedium.fontSize,
				modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp)
			)
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyDashboardInfoBoxView() {
	ESMiraSurface {
		LazyVerticalGrid(
			columns = GridCells.Fixed(2),
			modifier = Modifier.fillMaxWidth()
		) {
			//line 1
			item {
				StudyDashboardInfoBoxView(header = "Header", content = "Content")
			}
			item {
				StudyDashboardInfoBoxView(header = "Header", content = "Content", true)
			}
			
			//line 2
			item {
				StudyDashboardInfoBoxView(header = "Header", content = "Content")
			}
			item {
				StudyDashboardInfoBoxView (header = "Header that is long and will break", content = "Content")
			}
			
			//line 3
			item {
				StudyDashboardInfoBoxView (header = "Header that is long and will break", content = "Content")
			}
			item {
				StudyDashboardInfoBoxView (header = "Header that is long and will break", content = "Content")
			}
			
			//line 4
			item {
				StudyDashboardInfoBoxView(header = "Header", content = "Content")
			}
			item {
				StudyDashboardInfoBoxView (header = "Header", content = "Content that is long and will break")
			}
			
			//line 5
			item {
				StudyDashboardInfoBoxView (header = "Header", content = "Content that is long and will break")
			}
			item {
				StudyDashboardInfoBoxView (header = "Header", content = "Content that is long and will break")
			}
			
			//line 6
			item {
				StudyDashboardInfoBoxView(header = "Header", content = "Content")
			}
		}
	}
}