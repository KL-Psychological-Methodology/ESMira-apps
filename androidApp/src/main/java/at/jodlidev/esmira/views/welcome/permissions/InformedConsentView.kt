package at.jodlidev.esmira.views.welcome.permissions

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 20.12.2022.
 */
@Composable
fun InformedConsentView(study: Study, num: Int, currentNum: MutableState<Int>) {
	val openInformedConsent = remember { mutableStateOf(false) }
	val success = rememberSaveable { mutableStateOf(true) }
	if(openInformedConsent.value) {
		ConsentDialog(
			study = study,
			onCancel = {
				openInformedConsent.value = false
			},
			onConsent = {
				success.value = true
				openInformedConsent.value = false
				++currentNum.value
			}
		)
	}
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		PermissionHeaderView(
			num = num,
			currentNum = currentNum,
			success = success,
			modifier = Modifier.fillMaxWidth(),
			header = stringResource(id = R.string.informed_consent)
		)
		
		if(currentNum.value == num) {
			Spacer(modifier = Modifier.width(10.dp))
			Text(stringResource(id = R.string.informed_consent_desc))
			Spacer(modifier = Modifier.width(10.dp))
			DefaultButton(stringResource(R.string.show_informed_consent),
				onClick = { openInformedConsent.value = true }
			)
		}
		
	}
}

@Composable
fun ConsentDialog(study: Study, onCancel: () -> Unit, onConsent: () -> Unit) {
	ESMiraDialog(
		onDismissRequest = onCancel,
		title = stringResource(R.string.informed_consent),
		confirmButtonLabel = stringResource(R.string.i_agree),
		onConfirmRequest = onConsent,
		dismissButtonLabel = stringResource(R.string.cancel)
	) {
		Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
			Text(study.informedConsentForm)
		}
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewInformedConsentView() {
	ESMiraSurface {
		val currentNum = remember { mutableStateOf(1) }
		InformedConsentView(
			Study.newInstance("", "",
				"""{"id":1, "informedConsentForm": "consent"}"""
			),
			1,
			currentNum
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewConsentDialog() {
	ESMiraSurface {
		ConsentDialog(
			study = Study.newInstance("", "",
				"""{"id":1, "informedConsentForm": "This\n is\n a\n consent\n form"}"""
			),
			onCancel = {},
			onConsent = {}
		)
	}
}