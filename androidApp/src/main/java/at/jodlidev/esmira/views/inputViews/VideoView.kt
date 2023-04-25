package at.jodlidev.esmira.views.inputViews

import android.annotation.SuppressLint
import android.os.Build
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 23.01.2023.
 */


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoView(input: Input, get: () -> String, save: (String) -> Unit) {
	val hasError = remember { mutableStateOf(false) }
	
	
	if(hasError.value) {
		Column(modifier = Modifier.fillMaxWidth().height(250.dp), horizontalAlignment = Alignment.CenterHorizontally) {
			Text(stringResource(R.string.error_loading_failed))
			DefaultButton(
				text = stringResource(R.string.reload),
				onClick = {
					hasError.value = false
				}
			)
		}
	}
	else {
			AndroidView(
				factory = { context ->
					WebView(context).apply {
						layoutParams = ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.MATCH_PARENT
						)
						webViewClient = object: WebViewClient() {
							override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError) {
								super.onReceivedError(view, request, error)
								hasError.value = true
							}
							override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
								super.onReceivedHttpError(view, request, errorResponse)
								hasError.value = true
							}
						}
						// unfortunately, youtube (and probably others) can only be embedded via iFrame (as recommended by google).
						// But that only works when javascript is enabled
						// More information here:
						// https://pierfrancesco-soffritti.medium.com/how-to-play-youtube-videos-in-your-android-app-c40427215230
						settings.javaScriptEnabled = true
						settings.loadWithOverviewMode = true
						settings.useWideViewPort = true

						loadUrl(input.url)
					}
				},
				update = { webView ->
					webView.loadUrl(input.url)
				},
				modifier = Modifier.height(250.dp).clip(RectangleShape)
			)
	}
}