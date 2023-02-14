package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.DefaultButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.ESMiraSurfaceM2
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.nativeAsync
import java.net.URL

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun ImageView(input: Input, get: () -> String, save: (String) -> Unit) {
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
			val image = produceState<Bitmap?>(null) {
				try {
					if(input.url.startsWith("data")) {
						val data = input.url.substring(input.url.indexOf(",")  + 1)
						val decodedString: ByteArray = Base64.decode(data, Base64.DEFAULT)
						value = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
					}
					else {
						val url = URL(input.url)
						nativeAsync {
							try {
								value = BitmapFactory.decodeStream(url.openConnection().getInputStream())
							}
							catch(e: Throwable) {
								e.printStackTrace()
								error.value = true
							}
						}
					}
				}
				catch(e: Throwable) {
					e.printStackTrace()
					error.value = true
				}
			}
			if(image.value == null) {
				CircularProgressIndicator(modifier = Modifier.padding(all = 10.dp))
			}
			else {
				Image(
					bitmap = image.value!!.asImageBitmap(),
					contentDescription = ""
				)
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewImageView() {
	val input = DbLogic.createJsonObj<Input>("""
		{"url": "https://esmira.kl.ac.at/frontend/assets/favicon.ico"}
	""")
	ESMiraSurface {
		ImageView(input, {""}) {}
	}
}