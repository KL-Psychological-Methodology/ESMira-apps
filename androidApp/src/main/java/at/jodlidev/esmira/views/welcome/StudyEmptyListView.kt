package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.QrInterpreter
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

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