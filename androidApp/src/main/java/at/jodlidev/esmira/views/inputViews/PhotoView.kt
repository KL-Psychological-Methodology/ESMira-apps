package at.jodlidev.esmira.views.inputViews

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import java.io.File

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun PhotoView(input: Input, get: () -> String, save: (String) -> Unit) {
	val context = LocalContext.current
	
	val folder = File(context.filesDir, "photos")
	folder.mkdir()
	val file = input.getFileName()?.let { File(it) } ?: File(folder, System.currentTimeMillis().toString())
	val tempFile = File(folder, ".temp")
	
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
	
	val cameraLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.TakePicture(),
		onResult = { success ->
			println("onResult $success")
			if(success) {
				if(tempFile.exists()) {
					if(file.exists()) {
						file.delete()
					}
					tempFile.renameTo(file)
				}
				confirmImage()
				image.value = BitmapFactory.decodeFile(file.path)
			}
		}
	)
	val launchCamera = {
		tempFile.createNewFile()
		cameraLauncher.launch(
			FileProvider.getUriForFile(context, "at.jodlidev.esmira.provider", tempFile)
		)
	}
	
	val permissionLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		if(isGranted)
			launchCamera()
	}
	
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		if(image.value != null) {
			Image(
				bitmap = image.value!!.asImageBitmap(),
				contentDescription = ""
			)
		}
		DefaultButtonIconLeft(
			text = stringResource(R.string.take_picture),
			icon = Icons.Default.PhotoCamera,
			onClick = {
				if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
					launchCamera()
				else
					permissionLauncher.launch(Manifest.permission.CAMERA)
			}
		)
	}
}