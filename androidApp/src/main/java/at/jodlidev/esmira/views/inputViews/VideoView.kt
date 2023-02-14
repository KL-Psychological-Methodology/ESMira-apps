package at.jodlidev.esmira.views.inputViews

import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import at.jodlidev.esmira.DefaultButton
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun VideoView(input: Input, get: () -> String, save: (String) -> Unit) {
	val error = remember { mutableStateOf(false) }
	
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		if(error.value) {
			Text(stringResource(R.string.error_loading_failed))
			DefaultButton(
				text = stringResource(R.string.reload),
				onClick = {
					error.value = false
				}
			)
		}
		else {
			AndroidView(
				factory = { context ->
					WebView(context).apply {
						layoutParams = ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.MATCH_PARENT
						)
						webViewClient = WebViewClient()
						
						// TODO: We need to be careful about JavaScript:
						// https://developer.android.com/reference/android/webkit/WebView#addJavascriptInterface(java.lang.Object,%20java.lang.String)
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
							settings.javaScriptEnabled = true
						settings.loadWithOverviewMode = true
						settings.useWideViewPort = true
						
						loadUrl(input.url)
					}
				},
				update = { webView ->
					webView.loadUrl(input.url)
				},
				modifier = Modifier.height(250.dp)
			)
		}
	}
}