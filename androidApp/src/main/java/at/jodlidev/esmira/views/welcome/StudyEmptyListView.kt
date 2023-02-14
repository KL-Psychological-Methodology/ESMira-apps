package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun StudyEmptyListView(accessKey: String, gotoPrevious: () -> Unit) {
	ConstraintLayout(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
		val (text, navigation) = createRefs()
		
		Text(
			if(accessKey.isNotEmpty())
				stringResource(R.string.android_info_no_studies_withAccessKey, accessKey)
			else
				stringResource(R.string.info_no_studies_noAccessKey),
			textAlign = TextAlign.Center,
			modifier = Modifier.constrainAs(text) {
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				top.linkTo(parent.top)
				bottom.linkTo(navigation.top)
				width = Dimension.fillToConstraints
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
fun PreviewStudyEmptyListViewWithoutAccessKey() {
	ESMiraSurface {
		StudyEmptyListView("") {}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyEmptyListViewWithAccessKey() {
	ESMiraSurface {
		StudyEmptyListView("key") {}
	}
}