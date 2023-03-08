package at.jodlidev.esmira.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface

/**
 * Created by JodliDev on 02.03.2023.
 */

val defaultButtonPadding = PaddingValues(
	start = 4.dp,
	top = 1.dp,
	end = 4.dp,
	bottom = 1.dp
)

@Composable
fun DefaultButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	OutlinedButton(
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
		contentPadding = defaultButtonPadding,
		modifier = modifier
	) {
		Text(text)
	}
}
@Composable
fun DefaultButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable RowScope.() -> Unit
) {
	OutlinedButton(
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
		contentPadding = defaultButtonPadding,
		modifier = modifier,
		content = content
	)
}
@Composable
fun DefaultButtonIconLeft(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	textModifier: Modifier = Modifier
) {
	OutlinedButton(
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
		contentPadding = defaultButtonPadding,
		modifier = modifier
	) {
		Icon(
			icon,
			contentDescription = "",
			modifier = Modifier.size(ButtonDefaults.IconSize)
		)
		Spacer(Modifier.size(ButtonDefaults.IconSpacing))
		Text(text, modifier = textModifier)
	}
}
@Composable
fun DefaultButtonIconRight(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	textModifier: Modifier = Modifier
) {
	OutlinedButton(
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
		contentPadding = defaultButtonPadding,
		modifier = modifier
	) {
		Text(text, modifier = textModifier)
		Spacer(Modifier.size(ButtonDefaults.IconSpacing))
		Icon(
			icon,
			contentDescription = "",
			modifier = Modifier.size(ButtonDefaults.IconSize)
		)
	}
}
@Composable
fun DefaultButtonIconAbove(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	OutlinedButton(
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
		contentPadding = defaultButtonPadding,
		modifier = modifier
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.padding(vertical = 10.dp)
		) {
			Icon(icon, "")
			Text(text)
		}
	}
}

@Composable
fun DialogButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	TextButton(
		onClick = onClick,
		modifier = modifier
	) {
		Text(text)
	}
}

@Composable
fun TextButtonIconLeft(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	TextButton(
		onClick = onClick,
		modifier = modifier
	) {
		Icon(
			icon,
			contentDescription = "",
			modifier = Modifier.size(ButtonDefaults.IconSize)
		)
		Spacer(Modifier.size(ButtonDefaults.IconSpacing))
		Text(text)
	}
}
@Composable
fun TextButtonIconRight(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true
) {
	TextButton(
		onClick = onClick,
		modifier = modifier,
		enabled = enabled
	) {
		Text(text)
		Spacer(Modifier.size(ButtonDefaults.IconSpacing))
		Icon(
			icon,
			contentDescription = "",
			modifier = Modifier.size(ButtonDefaults.IconSize)
		)
	}
}


@Preview(name = "Light Mode")
@Preview(
	uiMode = Configuration.UI_MODE_NIGHT_YES,
	showBackground = true,
	name = "Dark Mode"
)
@Composable
fun PreviewButtons() {
	ESMiraSurface {
		Column {
			Text("Text")
			
			DialogButton(onClick = {}, text = "DefaultButton")
			DefaultButton(onClick = {}, text = "DefaultButton")
			DefaultButtonIconLeft(onClick = {}, icon = Icons.Default.Home, text = "DefaultButtonIconLeft")
			DefaultButtonIconAbove(onClick = {}, icon = Icons.Default.Home, text = "DefaultButtonIconAbove")
			TextButtonIconLeft(onClick = {}, icon = Icons.Default.Home, text = "TextButtonIconLeft")
			TextButtonIconRight(onClick = {}, icon = Icons.Default.Home, text = "TextButtonIconRight")
			IconButton(onClick = {}) {
				Icon(Icons.Default.Home, "")
			}
		}
	}
}