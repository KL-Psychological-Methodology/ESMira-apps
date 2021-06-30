package at.jodlidev.esmira.input_views

import android.content.Context
import android.os.Build
import android.webkit.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 16.04.2019.
 */
class VideoView(context: Context) : TextElView(context, R.layout.view_input_video) {
	private val video: WebView = findViewById(R.id.video)
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		super.bindData(input, questionnaire)
//		if(this::videoBox.isInitialized) { //if no WebView is installed (for example in emulator)
//			error = true
//			return
//		}

		video.webViewClient = object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
				return false
			}
			
			override fun onPageFinished(view: WebView, url: String) {
				input.value = "loaded"
			}
		}
		val webSettings = video.settings
		
		// We need to be careful about JavaScript:
		// https://developer.android.com/reference/android/webkit/WebView#addJavascriptInterface(java.lang.Object,%20java.lang.String)
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			webSettings.javaScriptEnabled = true
		webSettings.loadWithOverviewMode = true
		webSettings.useWideViewPort = true
		video.loadUrl(input.url)
	}
}