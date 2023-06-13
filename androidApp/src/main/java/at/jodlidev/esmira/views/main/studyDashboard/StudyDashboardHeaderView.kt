package at.jodlidev.esmira.views.main.studyDashboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface

@Composable
fun StudyDashboardHeaderView(text: String, dropdownContent: @Composable (ColumnScope.() -> Unit)? = null) {
	Surface(
		modifier = Modifier.padding(top = 10.dp, bottom = 5.dp),
		shadowElevation = 5.dp
	) {
		Column {
			Divider()
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 20.dp, top = 5.dp, bottom = 5.dp)
			) {
				Text(text,
					modifier = Modifier.weight(1F),
					fontSize = MaterialTheme.typography.bodyLarge.fontSize,
//					fontWeight = FontWeight.Bold
				)
				if(dropdownContent != null) {
					val dropdownOpened = remember { mutableStateOf(false) }
					IconButton(onClick = { dropdownOpened.value = true }) {
						Icon(Icons.Default.MoreVert, "more")
						DropdownMenu(
							expanded = dropdownOpened.value,
							onDismissRequest = { dropdownOpened.value = false },
							content = dropdownContent
						)
					}
				}
			}
			Divider()
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyDashboardHeaderView() {
	ESMiraSurface {
		Column {
			StudyDashboardHeaderView("Header 1")
			StudyDashboardHeaderView("Header 2") { }
		}
	}
}