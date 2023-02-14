package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.ESMiraSurfaceM2
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.welcome.permissions.AppTrackingView
import at.jodlidev.esmira.views.welcome.permissions.InformedConsentView
import at.jodlidev.esmira.views.welcome.permissions.NotificationsView

/**
 * Created by JodliDev on 15.12.2022.
 */


@Composable
fun StudyPermissionsView(study: Study, gotoPrevious: () -> Unit, gotoNext: () -> Unit) {
	ConstraintLayout(modifier = Modifier
		.fillMaxSize()
		.padding(all = 20.dp)
	) {
		val (permissions, navigation) = createRefs()
		
		var num = 0
		val currentNum = rememberSaveable { mutableStateOf(1) }
		val isFinished = remember { derivedStateOf { currentNum.value > num } }
		
		Column(modifier = Modifier
			.verticalScroll(rememberScrollState())
			.constrainAs(permissions) {
				top.linkTo(parent.top)
				bottom.linkTo(navigation.top)
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				width = Dimension.fillToConstraints
				height = Dimension.fillToConstraints
			}
		) {
			if(study.hasInformedConsent())
				InformedConsentView(study, ++num, currentNum)
			
			if(study.usesPostponedActions() || study.hasNotifications())
				NotificationsView(++num, currentNum)
			
			if(study.hasScreenOrAppTracking())
				AppTrackingView(++num, currentNum)
			
			
			
			if(isFinished.value) {
				Spacer(modifier = Modifier.height(20.dp))
				Text(stringResource(id = R.string.info_study_permissionSetup_ended))
			}
		}
		
//		val isFinished = remember { derivedStateOf { currentNum.value > num } }
//		Spacer(modifier = Modifier.weight(1f))
//		if(isFinished.value) {
//			Text(stringResource(id = R.string.info_study_permissionSetup_ended))
//			Spacer(modifier = Modifier.height(10.dp))
//		}
		
		NavigationView(
			gotoPrevious = gotoPrevious,
			gotoNext = { gotoNext() },
			modifier = Modifier
				.fillMaxWidth()
				.constrainAs(navigation) {
					start.linkTo(parent.start)
					end.linkTo(parent.end)
					bottom.linkTo(parent.bottom)
					width = Dimension.fillToConstraints
				},
			nextEnabled = { isFinished.value },
			nextIcon = { Icons.Default.Check },
			nextLabel = stringResource(id = R.string.participate)
		)
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyPermissionsView() {
	ESMiraSurface {
		StudyPermissionsView(
			Study.newInstance("", "",
				"""{"id":1, "title": "Study1", "informedConsentForm": "consent"}"""
			),
			{}, {})
	}
}