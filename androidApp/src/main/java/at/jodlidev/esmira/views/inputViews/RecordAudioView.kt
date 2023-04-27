package at.jodlidev.esmira.views.inputViews

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.FileUpload
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.concurrent.thread


/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun RecordAudioView(input: Input) {
	val context = LocalContext.current
	
	val isRecording = remember { mutableStateOf(false) }
	val isPlaying = remember { mutableStateOf(false) }
	val fileExists = remember { mutableStateOf(input.getFileName() != null) }
	
	val folder = File(context.filesDir, "audio")
	folder.mkdir()
	val filePath = remember {
		val file = input.getFileName()?.let { File(it) } ?: File(folder, System.currentTimeMillis().toString())
		file.absolutePath
	}
	
	val mediaRecorder = remember {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
	}
	val mediaPlayer = remember {
		MediaPlayer()
	}
	mediaPlayer.setOnCompletionListener {
		isPlaying.value = false
	}
	
	
	
	val startRecording = {
		thread(start = true, isDaemon = true) {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
			mediaRecorder.setOutputFile(filePath)
			mediaRecorder.prepare()
			mediaRecorder.start()
			isRecording.value = true
		}
	}
	
	val playAudio = {
		mediaPlayer.reset()
		mediaPlayer.setAudioAttributes(
			AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_MEDIA)
				.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
				.build()
		)
		
		mediaPlayer.setDataSource(filePath)
		mediaPlayer.prepare()
		mediaPlayer.start()
		isPlaying.value = true
	}
	
	
	val permissionLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		if(isGranted)
			startRecording()
	}
	
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if(isRecording.value) {
			DefaultButtonIconLeft(
				text = stringResource(R.string.stop_audio_record),
				icon = Icons.Default.StopCircle,
				onClick = {
					mediaRecorder.stop()
					isRecording.value = false
					fileExists.value = true
					input.setFile(filePath, FileUpload.DataTypes.Audio)
				},
				modifier = Modifier.width(200.dp)
			)
		}
		else {
			DefaultButtonIconLeft(
				text = stringResource(R.string.start_audio_record),
				icon = Icons.Default.Mic,
				onClick = {
					if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
						startRecording()
					else
						permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
				},
				modifier = Modifier.width(200.dp),
				enabled = !isPlaying.value
			)
		}
		
		if(isPlaying.value) {
			DefaultButtonIconLeft(
				text = stringResource(R.string.stop_playing_audio),
				icon = Icons.Default.StopCircle,
				onClick = {
					mediaPlayer.stop()
					isPlaying.value = false
				},
				modifier = Modifier.width(200.dp)
			)
		}
		else {
			DefaultButtonIconLeft(
				text = stringResource(R.string.start_playing_audio),
				icon = Icons.Default.PlayCircleFilled,
				onClick = {
					playAudio()
				},
				modifier = Modifier.width(200.dp),
				enabled = fileExists.value && !isRecording.value
			)
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAudioRecorderView() {
	val input = DbLogic.createJsonObj<Input>("""{}""")
	ESMiraSurface {
		RecordAudioView(input)
	}
}