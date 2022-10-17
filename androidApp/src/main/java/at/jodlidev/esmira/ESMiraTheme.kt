package at.jodlidev.esmira

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Created by JodliDev on 05.10.2022.
 */


//Material 3:
//private val LightColorPalette = lightColorScheme(
//	primary = Color(0XFF2B98CA),
//	onPrimary = Color.White,
//	primaryContainer = Color(0XFFe6f9ff),
//	onPrimaryContainer = Color(0XFF2B98CA),
//
//	secondary = Color(0xFF2DBFF3),
//	onSecondary = Color.White,
//	secondaryContainer = Color(0xFF9fe0f7),
//	onSecondaryContainer = Color(0xFF2DBFF3),
//
//	tertiary = Color(0xFFDC4E9d),
//	onTertiary = Color.White,
//	tertiaryContainer = Color(0xFFff8fce),
//	onTertiaryContainer = Color.White,
//
//	background = Color.White,
//	onBackground = Color(0xFF757575),
//
////	surface = Color.White,
////	onSurface = Color(0xFF757575),
////	surfaceVariant =  Color(0xFFEEEEEE),
////	onSurfaceVariant = Color(0xFF757575),
//
//	outline = Color(0xFF9fe0f7),
//	outlineVariant = Color(0xFF2DBFF3),
//
//	error = Color.Red,
//	onError = Color.White
//)


val colorGreen = Color(0xFF009600)
val colorRed = Color(0xFFC80000)

// This theme is just meant to look close to the current xml design.
// When we migrated everything over to Compose, we will migrate to Material 3
private val LightColorPalette = lightColors(
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

private val DarkColorPalette = darkColors(
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
fun ESMiraTheme (
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colors  = if (darkTheme) DarkColorPalette else LightColorPalette,
		typography = typography,
		shapes = shapes,
		content = content
	)
}

@Composable fun ESMiraSurface (
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	ESMiraTheme(darkTheme) {
		Surface(color = MaterialTheme.colors.surface, content = content)
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
	ESMiraSurface {
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

@Composable
fun DefaultButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable RowScope.() -> Unit
) {
	OutlinedButton(
		colors = ButtonDefaults.outlinedButtonColors(
			contentColor = MaterialTheme.colors.secondary
		),
		onClick = onClick,
		modifier = modifier,
		content = content
	)
}
@Composable
fun UnimportantButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable RowScope.() -> Unit
) {
	TextButton(
		colors = ButtonDefaults.textButtonColors(
			contentColor = MaterialTheme.colors.secondary
		),
		onClick = onClick,
		modifier = modifier,
		content = content
	)
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
			DefaultButton(onClick = {}) {
				Icon(Icons.Default.Home, "home")
				Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
				Text("DefaultButton")
			}
			UnimportantButton(onClick = {}) {
				Icon(Icons.Default.Home, "home")
				Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
				Text("UnimportantButton")
			}
			Button(onClick = {}) {
				Icon(Icons.Default.Home, "home")
				Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
				Text("Button")
			}
			OutlinedButton(onClick = {}) {
				Icon(Icons.Default.Home, "home")
				Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
				Text("OutlinedButton")
			}
			TextButton(onClick = {}) {
				Icon(Icons.Default.Home, "home")
				Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
				Text("TextButton")
			}
		}
	}
}

