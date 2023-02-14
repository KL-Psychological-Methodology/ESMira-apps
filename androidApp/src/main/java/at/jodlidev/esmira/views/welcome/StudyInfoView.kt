package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun StudyInfoView(study: Study, gotoPrevious: () -> Unit, gotoNext: () -> Unit) {
	ConstraintLayout(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
		val (title, contactEmail, desc, navigation) = createRefs()
		
		Text(study.title,
			fontSize = 20.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.constrainAs(title) {
				top.linkTo(parent.top)
				start.linkTo(parent.start)
			})
		Text(study.contactEmail,
			fontSize = 14.sp,
			modifier = Modifier.constrainAs(contactEmail) {
				top.linkTo(title.bottom, margin = 5.dp)
				start.linkTo(title.start, margin = 20.dp)
			})
		HtmlHandler.HtmlText(study.studyDescription,
			modifier = Modifier
				.constrainAs(desc) {
					top.linkTo(contactEmail.bottom, margin = 20.dp)
					bottom.linkTo(navigation.top)
					start.linkTo(parent.start)
					end.linkTo(parent.end)
					width = Dimension.fillToConstraints
					height = Dimension.fillToConstraints
				})
		
		
		NavigationView(
			gotoPrevious = gotoPrevious,
			gotoNext = gotoNext,
			modifier = Modifier.constrainAs(navigation) {
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				bottom.linkTo(parent.bottom)
				width = Dimension.fillToConstraints
			},
			nextIcon = { if(study.needsPermissionScreen()) Icons.Default.KeyboardArrowRight else Icons.Default.Check },
			nextLabel = if(study.needsPermissionScreen()) stringResource(id = R.string.consent) else stringResource(id = R.string.participate)
		)
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyInfoView() {
	ESMiraSurface {
		StudyInfoView(Study.newInstance("", "",
			"""{"id":1, "title": "Study1", "contactEmail": "contact@email", "studyDescription": "This<br>is<br>a<br>description"}"""),
			{}, {})
	}
}