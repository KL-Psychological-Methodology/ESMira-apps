package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.colorGreen
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.DbUser
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun ShareView(input: Input, get: () -> String, save: (String) -> Unit) {
	val uriHandler = LocalUriHandler.current
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		DefaultButtonIconLeft(
			text = stringResource(R.string.open_url),
			icon = Icons.Default.Share,
			onClick = {
				save(((get().toIntOrNull() ?: 0) + 1).toString())
				uriHandler.openUri(input.getFilledUrl())
			}
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewShareView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"url": "https://example.org/[[USER_ID]]"}
	""")
	ESMiraSurface {
		ShareView(input, {"70"}) {}
	}
}