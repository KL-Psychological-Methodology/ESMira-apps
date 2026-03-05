package at.jodlidev.esmira.views.inputViews

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButtonIconLeft
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.concurrent.thread
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sqrt

const val SAMPLE_RATE = 44100
const val REFERENCE = 32768.0 // Max value of a PCM sample at 16 bit

@Composable
fun NoiseLevelView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
    val context = LocalContext.current

    val duration = input.timeoutSec

    val isRecording = remember { mutableStateOf(false) }

    val record = {
        save("", mapOf())
        isRecording.value = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(isGranted){
            record()
        }
    }

    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(isRecording.value) {
            val progress = remember { mutableStateOf(0f) }
            LinearProgressIndicator(progress = { progress.value }, modifier = Modifier.padding(all = 10.dp))
            DefaultButtonIconLeft(
                text = stringResource(R.string.stop_audio_record),
                icon = Icons.Default.StopCircle,
                onClick = {
                    isRecording.value = false
                    println("Button Pressed")
                },
                modifier = Modifier.width(200.dp)
            )
            LaunchedEffect(isRecording.value) {
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
                    )
                )

                val samplesToRead = SAMPLE_RATE * duration
                val buffer = ShortArray(samplesToRead)

                audioRecord.startRecording()
                try {
                    var readTotal = 0
                    progress.value = 0f
                    val start = System.currentTimeMillis()
                    var lastFrame = start
                    while (readTotal < samplesToRead) {
                        if(!isRecording.value) {
                            return@LaunchedEffect
                        }
                        delay(10)
                        val now = System.currentTimeMillis()
                        val elapsed = (now - start).toFloat()
                        progress.value = elapsed / (duration * 1000).toFloat()
                        val diff = now - lastFrame
                        val frameSamples = min(samplesToRead - readTotal, ceil(SAMPLE_RATE.toDouble() / diff).toInt())
                        val read = audioRecord.read(
                            buffer,
                            readTotal,
                            frameSamples
                        )
                        readTotal += read
                        lastFrame = now
                    }

                    // Calculate AC RMS

                    var sumTotal = 0.0
                    val means: MutableList<Double> = mutableListOf()
                    for(i in 0..<duration) {
                        var sum = 0.0
                        for(j in 0..<SAMPLE_RATE) {
                            val sample = buffer[i+j].toDouble()
                            sum += sample
                        }
                        val mean = sum / SAMPLE_RATE
                        means.add(mean)
                        sumTotal += sum
                    }

                    val meanTotal = sumTotal / readTotal

                    var minSum = Double.MAX_VALUE
                    var maxSum = Double.MIN_VALUE

                    sumTotal = 0.0
                    for(i in 0..<duration) {
                        var sum = 0.0
                        for(j in 0..<SAMPLE_RATE) {
                            val sample = buffer[i+j].toDouble()
                            val dFrame = sample - means[i]
                            sum += dFrame * dFrame
                            val dTotal = sample - meanTotal
                            sumTotal += dTotal * dTotal
                        }
                        minSum = min(minSum, sum)
                        maxSum = max(maxSum, sum)
                    }

                    val rmsTotal = sqrt(sumTotal / readTotal)
                    val dBFStotal = 20.0 * log10(rmsTotal / REFERENCE)

                    val rmsMinFrame = sqrt(minSum / SAMPLE_RATE)
                    val dBFSfminFrame = 20.0 * log10(rmsMinFrame / REFERENCE)

                    val rmsMaxFrame = sqrt(maxSum / SAMPLE_RATE)
                    val dBFSmaxFrame = 20.0 * log10(rmsMaxFrame / REFERENCE)

                    save(
                        (round(dBFStotal * 100.0) / 100.0).toString(),
                        mapOf(
                            "min" to (round(dBFSfminFrame * 100.0) / 100.0).toString(),
                            "max" to (round(dBFSmaxFrame * 100.0) / 100.0).toString()
                        ))
                    isRecording.value = false
                } finally {
                    audioRecord.stop()
                    audioRecord.release()
                }
            }
        } else {
            if(get().isNotEmpty()) {
                Text(stringResource(R.string.measured_noise_level, get()))
            }
            DefaultButtonIconLeft(
                text = stringResource(R.string.start_audio_record),
                icon = Icons.Default.Mic,
                onClick = {
                    if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        record()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.width(200.dp)
            )
        }
    }
}