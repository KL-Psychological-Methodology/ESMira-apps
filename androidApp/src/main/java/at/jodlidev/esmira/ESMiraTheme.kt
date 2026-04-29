package at.jodlidev.esmira

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.views.DialogButton

/**
 * Created by JodliDev on 05.10.2022.
 */


/**
 * Holds the app-level dark mode override.
 * null  = follow system setting (default)
 * true  = force dark
 * false = force light
 */
object ThemeState {
	val isDark: MutableState<Boolean?> = mutableStateOf(null)

	/** Toggle to the opposite of [currentDark], then persist via the caller. */
	fun toggle(currentDark: Boolean): Boolean {
		val newValue = !currentDark
		isDark.value = newValue
		return newValue
	}
}

val colorGreen = Color(0xFF009600)
val colorRed = Color(0xFFC80000)

val colorLineBackground1 = Color(0x1A000000)
val colorLineBackground2 = Color(0x3AFFFFFF)

val colorLog = Color(0x34FFFFFF)
val colorWarn = Color(0xFFFFBB00)
val colorError = Color(0xFFFF0000)


private const val esTextShadowAlpha = 0.18f

/** Returns the appropriate shadow color for elevated surfaces based on the current theme. */
@Composable
fun esShadowColor(): Color {
	val isDark = ThemeState.isDark.value ?: isSystemInDarkTheme()
	return if(isDark) Color.White.copy(alpha = 0.35f) else Color.Black
}

/**
 * An [Icon] with a subtle drop shadow.
 * Achieved by rendering the icon twice: once offset in the shadow color, then normally on top.
 */
@Composable
fun EsIcon(
	imageVector: ImageVector,
	contentDescription: String,
	modifier: Modifier = Modifier,
	tint: Color = LocalContentColor.current
) {
	val isDark = ThemeState.isDark.value ?: isSystemInDarkTheme()
	val shadowColor = if(isDark) Color.White.copy(alpha = esTextShadowAlpha) else Color.Black.copy(alpha = esTextShadowAlpha)
	Box(modifier = modifier) {
		Icon(imageVector, contentDescription = null, tint = shadowColor, modifier = Modifier.offset(x = 1.dp, y = 1.dp))
		Icon(imageVector, contentDescription = contentDescription, tint = tint)
	}
}

val ESMiraShapes = Shapes(
	small = RoundedCornerShape(4.dp),
	medium = RoundedCornerShape(16.dp),
	large = RoundedCornerShape(16.dp),
	extraLarge = RoundedCornerShape(16.dp)
)

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
	
	background = Color(0xFFEBF5FB),
	onBackground = Color.Black,
	
	surfaceTint = Color.White,
	surface = Color(0XFFC2E4F5),
	onSurface = Color(0XFF2B98CA),
//	surfaceVariant =  Color(0xFF9fe0f7),
//	onSurfaceVariant = Color(0XFF2B98CA),
	surfaceVariant =  Color(0XFFC2E4F5), //TextField
	onSurfaceVariant = Color(0XFF2B98CA),
	
	outline = Color(0XFF2B98CA),
	outlineVariant = Color(0xFF9fe0f7),

	error = Color(0xFFD32F2F),
	onError = Color.White
)
private val DarkColorPalette = darkColorScheme(
	primary = Color(0XFF2B98CA),
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
	
	background = Color(0xFF0A1F3A),
	onBackground = Color.White,
	
	surface = Color(0XFF2B98CA),
	onSurface = Color.White,
	surfaceVariant =  Color(0xFF3C3C3C),
	onSurfaceVariant = Color.White,
	
	outline = Color(0XFF2B98CA),
	outlineVariant = Color(0xFF9fe0f7),

	error = Color(0xFFD32F2F),
	onError = Color.White
)
@Composable
fun ESMiraTheme (
	darkTheme: Boolean = ThemeState.isDark.value ?: isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colorScheme = if(darkTheme) DarkColorPalette else LightColorPalette,
		typography = Typography(),
		shapes = ESMiraShapes,
		content = content
	)
}

@Composable fun ESMiraSurface (
	darkTheme: Boolean = ThemeState.isDark.value ?: isSystemInDarkTheme(),
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

