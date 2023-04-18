package at.jodlidev.esmira.views.main.studyDashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StudyDashboardButtonView(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	badge: String? = null,
	important: Boolean = false
) {
	Box(
		contentAlignment = Alignment.Center,
	) {
		Button(
			onClick = onClick,
			shape = RoundedCornerShape(1.dp),
			colors = if(important) ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.tertiaryContainer,
				contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
			)
			else ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surface,
				contentColor = MaterialTheme.colorScheme.onSurface,
			),
			modifier = Modifier
				.padding(all = 5.dp)
				.heightIn(min = 80.dp)
				.fillMaxSize()
		) {
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Icon(icon, "")
				Text(text, textAlign = TextAlign.Center, fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold)
			}
		}
		if(badge != null) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(bottom = 50.dp)
			) {
				Spacer(modifier = Modifier.weight(1F))
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.size(20.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.error)
				) {
					Text(
						badge,
						color = MaterialTheme.colorScheme.onError,
						fontSize = MaterialTheme.typography.bodySmall.fontSize
					)
				}
				Spacer(modifier = Modifier.weight(0.5F))
			}
		}
	}
}