package at.jodlidev.esmira

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
//import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.views.DialogButton

/**
 * Created by JodliDev on 05.10.2022.
 */


val colorGreen = Color(0xFF009600)
val colorRed = Color(0xFFC80000)

val colorLineBackground1 = Color(0x1A000000)
val colorLineBackground2 = Color(0x3AFFFFFF)

val colorLog = Color(0x34FFFFFF)
val colorWarn = Color(0xFFFFBB00)
val colorError = Color(0xFFFF0000)

private val blue1 = Color(0XFFe6f9ff)
private val blue2 = Color(0xFF9fe0f7)
private val blue3 = Color(0xFF2DBFF3)
private val blue4 = Color(0XFF2B98CA)
private val pink1 = Color(0xFFff8fce)
private val pink2 = Color(0xFFDC4E9d)

private val LightColorPalette = lightColorScheme(
	primary = Color(0XFF2B98CA),
	onPrimary = Color.White,
//	primaryContainer = Color(0XFFe6f9ff),
//	onPrimaryContainer = Color(0XFF2B98CA),
	
	secondary = Color(0xFF2DBFF3),
	onSecondary = Color.White,
	secondaryContainer = Color(0XFF2B98CA),
	onSecondaryContainer = Color(0XFFe6f9ff),
	
	tertiary = Color(0xFFDC4E9d),
	onTertiary = Color.White,
	tertiaryContainer = Color(0xFFff8fce),
	onTertiaryContainer = Color.White,
	
	background = Color.White,
	onBackground = Color.Black,
	
	surfaceTint = Color.White,
	surface = Color(0XFFe6f9ff),
	onSurface = Color(0XFF2B98CA),
//	surfaceVariant =  Color(0xFF9fe0f7),
//	onSurfaceVariant = Color(0XFF2B98CA),
	surfaceVariant =  Color(0XFFe6f9ff), //TextField
	onSurfaceVariant = Color(0XFF2B98CA),
	
	outline = Color(0XFF2B98CA),
	outlineVariant = Color(0xFF9fe0f7),
	
	error = Color.Red,
	onError = Color.White
)
private val DarkColorPalette = darkColorScheme(
	primary = Color(0xFF2DBFF3),
	onPrimary = Color.White,
//	primaryContainer = Color(0xFF9fe0f7),
//	onPrimaryContainer = Color.Black,
	
	secondary = Color(0xFF9fe0f7),
	onSecondary = Color.White,
	secondaryContainer = Color(0xFF757575),
	onSecondaryContainer = Color.White,
	
	surfaceTint = Color.White,
	tertiary = Color(0xFFDC4E9d),
	onTertiary = Color.White,
	tertiaryContainer = Color(0xFFDC4E9d),
	onTertiaryContainer = Color.White,
	
	background = Color.Black,
	onBackground = Color.White,
	
	surface = Color(0XFF2B98CA),
	onSurface = Color.White,
	surfaceVariant =  Color(0xFF3C3C3C),
	onSurfaceVariant = Color.White,
	
	outline = Color(0xFF2DBFF3),
	outlineVariant = Color(0XFF2B98CA),
	
	error = Color.Red,
	onError = Color.White
)
@Composable
fun ESMiraTheme (
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colorScheme = if(darkTheme) DarkColorPalette else LightColorPalette,
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
		Surface(color = MaterialTheme.colorScheme.background, content = content)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light Mode")
@Preview(
	uiMode = Configuration.UI_MODE_NIGHT_YES,
	showBackground = true,
	name = "Dark Mode"
)
@Composable
fun PreviewElements() {
	ESMiraSurface {
		Column {
			OutlinedTextField(value = "OutlinedTextField", onValueChange = {})
			TextField(value = "TextField", onValueChange = {})
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
fun PreviewDialog() {
	ESMiraSurface {
		Column {
			AlertDialog(
				onDismissRequest = { },
				title = { Text("Title") },
				text = { Text("Content") },
				dismissButton = { DialogButton(stringResource(R.string.cancel), onClick = {}) },
				confirmButton = { DialogButton(stringResource(R.string.ok_), onClick = {}) }
			)
		}
	}
}

