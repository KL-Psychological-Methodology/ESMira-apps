package at.jodlidev.esmira.views.inputViews

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import java.io.File

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun PhotoView(input: Input, questionnaire: Questionnaire, get: () -> String, save: (String) -> Unit) {
	val context = LocalContext.current
	
	val fileName = rememberSaveable { System.currentTimeMillis().toString() }
	val folder = File(context.filesDir, "photos")
	folder.mkdir()
	val file = File(folder, fileName)
	val tempFile = File(folder, ".temp")
	
	val confirmImage = {
		input.addImage(file.path, questionnaire.studyId)
		save(input.value)
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
				if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
					launchCamera()
				else
					permissionLauncher.launch(android.Manifest.permission.CAMERA)
			}
		)
	}
}