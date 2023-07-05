package at.jodlidev.esmira.views.inputViews

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.*

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun FileUploadView(input: Input, get: () -> String, save: (String) -> Unit) {
	val context = LocalContext.current
	
	val folder = File(context.filesDir, "photos")
	folder.mkdir()
	val file = input.getFileName()?.let { File(it) } ?: File(folder, System.currentTimeMillis().toString())
	
	val confirmImage = {
		save(file.path)
	}
	val image = remember {
		mutableStateOf(
			if(file.exists()) { //happens when activity was restarted
				confirmImage()
				BitmapFactory.decodeFile(file.path)
			}
			else
				null
		)
	}
	val loadFilePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
		if(uri == null)
			return@rememberLauncherForActivityResult
		
		try {
			runBlocking {
				withContext(Dispatchers.IO) {
					val inputStream: InputStream = context.contentResolver.openInputStream(uri)
						?: throw IOException("InputStream was null")
					val outputStream: OutputStream = FileOutputStream(file)
					
					inputStream.copyTo(outputStream, 1024)
					
					inputStream.close()
					outputStream.flush()
					outputStream.close()
					
					confirmImage()
					image.value = BitmapFactory.decodeFile(file.path)
				}
			}
		}
		catch(e: IOException) {
			Toast.makeText(context, context.getString(R.string.android_error_general, e.message), Toast.LENGTH_LONG).show()
		}
	}
	
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		if(image.value != null) {
			Image(
				bitmap = image.value!!.asImageBitmap(),
				contentDescription = ""
			)
		}
		DefaultButtonIconLeft(
			text = stringResource(R.string.select_picture),
			icon = Icons.Default.PhotoCamera,
			onClick = {
				loadFilePickerLauncher.launch("image/*")
			}
		)
	}
}