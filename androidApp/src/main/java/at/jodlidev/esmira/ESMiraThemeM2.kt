package at.jodlidev.esmira

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
//import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Created by JodliDev on 05.10.2022.
 */


// Material2:

// This theme is just meant to look close to the current xml design.
// When we migrated everything over to Compose, we will migrate to Material 3
private val LightColorPaletteM2 = androidx.compose.material.lightColors(
	primary = Color(0XFF9FE0F7),
	primaryVariant = Color(0xFF2DBFF3),
	onPrimary = Color.White,
	secondary = Color(0xFFDC4E9D),
	secondaryVariant = Color(0xFFff8fce),
	onSecondary = Color(0xFF757575),
	background = Color.White,
	onBackground = Color(0xFF757575),
	surface = Color.White,
	onSurface = Color(0xFF757575),
	error = Color.Red,
	onError = Color.White,
)

private val DarkColorPaletteM2 = androidx.compose.material.darkColors(
	primary = Color(0xFF8D8D8D),
	primaryVariant = Color(0xFF5E5E5E),
	onPrimary = Color.Black,
	secondary = Color(0xFFDC4E9d),
	secondaryVariant = Color(0xFFff8fce),
	onSecondary = Color.White,
	background = Color.Black,
	onBackground = Color.White,
	surface = Color.Black,
	onSurface = Color.White,
	error = Color.Red,
	onError = Color.Black
)

@Composable
fun ESMiraThemeM2 (
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	androidx.compose.material.MaterialTheme(
		colors  = if (darkTheme) DarkColorPaletteM2 else LightColorPaletteM2,
		typography = androidx.compose.material.MaterialTheme.typography,
		shapes = androidx.compose.material.MaterialTheme.shapes,
		content = content
	)
}

@Composable fun ESMiraSurfaceM2 (
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	ESMiraThemeM2(darkTheme) {
		Surface(color = androidx.compose.material.MaterialTheme.colors.surface, content = content)
	}
}



@Composable
fun DefaultButtonM2(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	OutlinedButton(
		colors = ButtonDefaults.outlinedButtonColors(
			contentColor = MaterialTheme.colors.secondary
		),
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
		modifier = modifier
	) {
		Text(text)
	}
}
@Composable
fun DefaultButtonM2(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable RowScope.() -> Unit
) {
	OutlinedButton(
		colors = ButtonDefaults.outlinedButtonColors(
			contentColor = MaterialTheme.colors.secondary
		),
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
		modifier = modifier,
		content = content
	)
}

@Composable
fun DefaultButtonIconLeftM2(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	textModifier: Modifier = Modifier
) {
	OutlinedButton(
		colors = ButtonDefaults.outlinedButtonColors(
			contentColor = MaterialTheme.colors.secondary
		),
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
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
fun DefaultButtonIconAboveM2(
	text: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	OutlinedButton(
		colors = ButtonDefaults.outlinedButtonColors(
			contentColor = MaterialTheme.colors.secondary
		),
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
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
fun DialogButtonM2(
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
fun TextButtonIconLeftM2(
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
fun TextButtonIconRightM2(
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
fun PreviewPage() {
	ESMiraSurfaceM2 {
		val navController = rememberNavController()
		
		Scaffold(
			topBar = {
				TopAppBar { Text("TopBar") }
			},
			bottomBar = {
				BottomNavigation {
					BottomNavigationItem(
						icon = {Icon(Icons.Filled.Home, contentDescription  = "")},
						label = { Text("test1") },
						selected = true,
						onClick = {}
					)
					BottomNavigationItem(
						icon = {Icon(Icons.Filled.Message, contentDescription  = "")},
						label = { Text("test2") },
						selected = false,
						onClick = {}
					)
				}
			}
		) { innerPadding ->
			NavHost(navController, startDestination = "test1", Modifier.padding(innerPadding)) {
				composable("test1") {
					Text("test1")
				}
				composable("test2") {
					Text("test2")
				}
			}
		}
	}
}

@Preview(name = "Light Mode")
@Preview(
	uiMode = Configuration.UI_MODE_NIGHT_YES,
	showBackground = true,
	name = "Dark Mode"
)
@Composable
fun PreviewButtonsM2() {
	ESMiraSurface {
		Column {
			Text("Text")
			
			DialogButtonM2(onClick = {}, text = "DefaultButton")
			DefaultButtonM2(onClick = {}, text = "DefaultButton")
			DefaultButtonIconLeftM2(onClick = {}, icon = Icons.Default.Home, text = "DefaultButtonIconLeft")
			DefaultButtonIconAboveM2(onClick = {}, icon = Icons.Default.Home, text = "DefaultButtonIconAbove")
			TextButtonIconLeftM2(onClick = {}, icon = Icons.Default.Home, text = "TextButtonIconLeft")
			TextButtonIconRightM2(onClick = {}, icon = Icons.Default.Home, text = "TextButtonIconRight")
		}
	}
}

