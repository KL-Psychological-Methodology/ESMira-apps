package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.QrInterpreter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun QrScanningView(gotoPrevious: () -> Unit, gotoNext: (serverUrl: String, accessKey: String, studyId: Long, qId: Long, fallbackUrl: String?) -> Unit) {
	val context = LocalContext.current
	val scanLauncher = rememberLauncherForActivityResult(
		contract = ScanContract(),
		onResult = { result ->
			if(result.contents != null) {
				val interpreter = QrInterpreter()
				val data = interpreter.check(result.contents)
				if(data != null)
					gotoNext(data.url, data.accessKey, data.studyId, data.qId, data.fallbackUrl)
				else
					Toast.makeText(context, R.string.qrCodeInvalid, Toast.LENGTH_SHORT).show()
			}
		}
	)
	
	ConstraintLayout(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
		
		val (icon, instructionsText, buttonScan, navigation) = createRefs()
		
		Icon(
			Icons.Filled.PhotoCamera,
			contentDescription = "",
			modifier = Modifier
				.size(100.dp)
				.constrainAs(icon) {
					top.linkTo(parent.top)
					start.linkTo(parent.start)
					end.linkTo(parent.end)
				}
		)
		
		Text(
			text = stringResource(id = R.string.welcome_qr_instructions),
			modifier = Modifier.constrainAs(instructionsText) {
				top.linkTo(icon.bottom, margin = 20.dp)
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				width = Dimension.fillToConstraints
			}
		)
		
		DefaultButton(stringResource(R.string.start),
			onClick = { scanLauncher.launch(ScanOptions()) },
			modifier = Modifier
				.constrainAs(buttonScan) {
					start.linkTo(parent.start)
					end.linkTo(parent.end)
					top.linkTo(instructionsText.bottom, margin = 20.dp)
				}
		
		)
		
		NavigationView(
			gotoPrevious = gotoPrevious,
			gotoNext = null,
			modifier = Modifier.constrainAs(navigation) {
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				bottom.linkTo(parent.bottom)
				width = Dimension.fillToConstraints
			}
		)
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewQrScanningView() {
	ESMiraSurface {
		QrScanningView({}, { _, _, _, _, _ -> })
	}
}