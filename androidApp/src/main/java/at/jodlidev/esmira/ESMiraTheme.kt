package at.jodlidev.esmira

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
//import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Created by JodliDev on 05.10.2022.
 */


val fontSizeSmall = 12.sp

val colorGreen = Color(0xFF009600)
val colorRed = Color(0xFFC80000)

//val colorLineBackground1 = Color(0x1A000000)
//val colorLineBackground2 = Color(0x1AFFFFFF)
val colorLineBackground1 = Color(0x0D000000)
val colorLineBackground2 = Color(0x0DFFFFFF)

val colorLog = Color(0x34FFFFFF)
val colorWarn = Color(0xFFFFBB00)
val colorError = Color(0xFFFF0000)


//Material 3:
private val blue1 = Color(0XFFe6f9ff)
private val blue2 = Color(0xFF9fe0f7)
private val blue3 = Color(0xFF2DBFF3)
private val blue4 = Color(0XFF2B98CA)
private val pink1 = Color(0xFFff8fce)
private val pink2 = Color(0xFFDC4E9d)

private val LightColorPalette = lightColorScheme(
	primary = Color(0XFF2B98CA),
	onPrimary = Color.White,
	primaryContainer = Color(0xFF9fe0f7),
	onPrimaryContainer = Color.Black,
	
	secondary = Color(0xFF2DBFF3),
	onSecondary = Color.White,
	secondaryContainer = Color(0XFFe6f9ff),
	onSecondaryContainer = Color.Black,
	
	tertiary = Color(0xFFDC4E9d),
	onTertiary = Color.White,
	tertiaryContainer = Color(0xFFff8fce),
	onTertiaryContainer = Color.Black,
	
	background = Color.White,
	onBackground = Color.Black,
	
	surfaceTint = Color(0XFFe6f9ff),
	surface = Color.White,
	onSurface = Color.Black,
	surfaceVariant =  Color(0x1A000000),
	onSurfaceVariant = Color(0x80000000),
	
	outline = Color(0x1A000000),
	outlineVariant = Color(0x80000000),
	
	error = Color.Red,
	onError = Color.White
)
private val DarkColorPalette = darkColorScheme(
	primary = Color(0XFF2B98CA),
	onPrimary = Color.White,
	primaryContainer = Color(0xFF9fe0f7),
	onPrimaryContainer = Color.Black,
	
	secondary = Color(0xFF2DBFF3),
	onSecondary = Color.White,
	secondaryContainer = Color(0XFFe6f9ff),
	onSecondaryContainer = Color.Black,
	
	tertiary = Color(0xFFDC4E9d),
	onTertiary = Color.White,
	tertiaryContainer = Color(0xFFff8fce),
	onTertiaryContainer = Color.Black,
	
	background = Color.Black,
	onBackground = Color.White,
	
	surfaceTint = Color(0XFFe6f9ff),
	surface = Color.Black,
	onSurface = Color.White,
	surfaceVariant =  Color(0x4DFFFFFF),
	onSurfaceVariant = Color(0xCCFFFFFF),
	
	outline = Color(0x4DFFFFFF),
	outlineVariant = Color(0xCCFFFFFF),
	
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
		Surface(color = MaterialTheme.colorScheme.surface, content = content)
	}
}



@Composable
fun DefaultButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	OutlinedButton(
		shape = RoundedCornerShape(5.dp),
		onClick = onClick,
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
		}
	}
}

