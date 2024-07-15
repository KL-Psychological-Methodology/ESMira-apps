package at.jodlidev.esmira.views.welcome.permissions

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 20.12.2022.
 */
@Composable
fun InformedConsentView(study: Study, num: Int, isActive: () -> Boolean, isCurrent: () -> Boolean, goNext: () -> Unit) {
	val state = rememberSaveable { mutableStateOf(DefaultPermissionState.PERMISSION) }
	val openInformedConsent = remember { mutableStateOf(false) }

	if(openInformedConsent.value) {
		ConsentDialog(
			study = study,
			onCancel = {
				openInformedConsent.value = false
			},
			onConsent = {
				state.value = DefaultPermissionState.SUCCESS
				openInformedConsent.value = false
				goNext()
			}
		)
	}

	DefaultPermissionView(
		num = num,
		header = stringResource(id = R.string.informed_consent),
		whatFor = "",
		description = stringResource(id = R.string.informed_consent_desc),
		buttonLabel = stringResource(id = R.string.show_informed_consent),
		state = state,
		isActive = isActive,
		isCurrent = isCurrent,
		goNext = goNext,
		onClick = {
			openInformedConsent.value = true
		}
	)
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
		InformedConsentView(
			Study.newInstance("", "",
				"""{"id":1, "informedConsentForm": "consent"}"""
			),
			1,
			{ true },
			{ true },
			{},
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