package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.DefaultButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.QrInterpreter
import at.jodlidev.esmira.sharedCode.Web
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun QrScanningView(gotoPrevious: () -> Unit, gotoNext: (serverTitle: String, serverUrl: String, accessKey: String, studyId: Long) -> Unit) {
	val context = LocalContext.current
	val scanLauncher = rememberLauncherForActivityResult(
		contract = ScanContract(),
		onResult = { result ->
			if(result.contents != null) {
				val interpreter = QrInterpreter()
				val data = interpreter.check(result.contents)
				if(data != null)
					gotoNext(Web.getServerName(data.url), data.url, data.accessKey, data.studyId)
				else
					Toast.makeText(context, R.string.qrCodeInvalid, Toast.LENGTH_SHORT).show()
			}
		}
	)
	
	ConstraintLayout(modifier = Modifier.fillMaxSize()) {
		
		val (icon, instructionsText, divider, buttonScan, buttonPrev) = createRefs()
		
		Icon(
			Icons.Filled.PhotoCamera,
			contentDescription = "",
			modifier = Modifier
				.size(100.dp)
				.constrainAs(icon) {
					top.linkTo(parent.top, margin = 20.dp)
					start.linkTo(parent.start)
					end.linkTo(parent.end)
				}
		)
		
		Text(
			text = stringResource(id = R.string.welcome_qr_instructions),
			modifier = Modifier.constrainAs(instructionsText) {
				top.linkTo(icon.bottom, margin = 20.dp)
				start.linkTo(parent.start, margin = 20.dp)
				end.linkTo(parent.end, margin = 20.dp)
				width = Dimension.fillToConstraints
			}
		)
		
		DefaultButton(
			onClick = { scanLauncher.launch(ScanOptions()) },
			modifier = Modifier
				.constrainAs(buttonScan) {
					start.linkTo(parent.start)
					end.linkTo(parent.end)
					top.linkTo(instructionsText.bottom, margin = 20.dp)
				}
		
		) {
			Text(stringResource(R.string.start))
		}
		
		Divider(
			color = MaterialTheme.colors.primary,
			thickness = 1.dp,
			modifier = Modifier
				.constrainAs(divider) {
					start.linkTo(parent.start, margin = 20.dp)
					end.linkTo(parent.end, margin = 20.dp)
					bottom.linkTo(buttonPrev.top, margin = 5.dp)
					width = Dimension.fillToConstraints
				}
		)
		
		TextButton(
			onClick = gotoPrevious,
			modifier = Modifier
				.constrainAs(buttonPrev) {
					start.linkTo(divider.start)
					bottom.linkTo(parent.bottom, margin = 20.dp)
				}
		
		) {
			Icon(
				Icons.Default.KeyboardArrowLeft,
				contentDescription = "",
				modifier = Modifier.size(ButtonDefaults.IconSize)
			)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.back))
		}
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewQrScanningView() {
	ESMiraSurface {
		QrScanningView({}, { _, _, _, _, -> })
	}
}