package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButtonIconLeft

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
				val url = input.getFilledUrl()

				if(url.isNotEmpty()) {
					try {
						uriHandler.openUri(url)
						save(((get().toIntOrNull() ?: 0) + 1).toString())
					}
					catch(e: Throwable) {
						ErrorBox.warn("ShareView", "$url is not a valid URL!")
					}
				}
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