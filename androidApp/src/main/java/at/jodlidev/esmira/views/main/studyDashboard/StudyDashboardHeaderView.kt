package at.jodlidev.esmira.views.main.studyDashboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface

@Composable
fun StudyDashboardHeaderView(text: String, topSpacing: Boolean = true, dropdownContent: @Composable (ColumnScope.() -> Unit)? = null) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = 12.dp, top = if(topSpacing) 28.dp else 6.dp, end = 4.dp, bottom = 6.dp)
	) {
		// Accent bar
		Box(
			modifier = Modifier
				.width(4.dp)
				.height(22.dp)
				.background(
					color = MaterialTheme.colorScheme.primary,
					shape = MaterialTheme.shapes.small
				)
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = text,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.weight(1f)
		)
		if(dropdownContent != null) {
			val dropdownOpened = remember { mutableStateOf(false) }
			IconButton(onClick = { dropdownOpened.value = true }) {
				Icon(Icons.Default.MoreVert, "more", tint = MaterialTheme.colorScheme.onSurfaceVariant)
				DropdownMenu(
					expanded = dropdownOpened.value,
					onDismissRequest = { dropdownOpened.value = false },
					content = dropdownContent
				)
			}
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
