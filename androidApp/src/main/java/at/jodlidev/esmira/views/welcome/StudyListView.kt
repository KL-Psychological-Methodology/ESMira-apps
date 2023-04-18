package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun StudyListItemView(study: Study, modifier: Modifier = Modifier, gotoStudy: () -> Unit) {
	Column(
		modifier = modifier
			.clickable(onClick = gotoStudy)
			.padding(horizontal = 20.dp, vertical = 5.dp)
			.fillMaxWidth()
	) {
		Text(study.title,
			fontSize = MaterialTheme.typography.labelLarge.fontSize,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(study.contactEmail, fontSize = MaterialTheme.typography.labelLarge.fontSize, modifier = Modifier.padding(start = 20.dp))
		Divider(modifier = Modifier.padding(all = 10.dp))
	}
}

@Composable
fun StudyListView(studies: List<Study>, gotoPrevious: () -> Unit, gotoNext: (index: Int) -> Unit) {
	ConstraintLayout(modifier = Modifier.fillMaxSize()) {
		val (header, studyListElement, navigation) = createRefs()
		
		Text(stringResource(id = R.string.colon_choose_study),
			fontWeight = FontWeight.Bold,
			fontSize = MaterialTheme.typography.labelLarge.fontSize,
			modifier = Modifier.constrainAs(header) {
				top.linkTo(parent.top, margin = 10.dp)
				start.linkTo(parent.start, margin = 20.dp)
				end.linkTo(parent.end)
				width = Dimension.fillToConstraints
			})
		
		LazyColumn(modifier = Modifier.constrainAs(studyListElement) {
			top.linkTo(header.bottom, margin = 20.dp)
			bottom.linkTo(navigation.top)
			start.linkTo(parent.start)
			end.linkTo(parent.end)
			width = Dimension.fillToConstraints
			height = Dimension.fillToConstraints
		}) {
			itemsIndexed(studies, { i, _ -> i }) { i, study: Study ->
				StudyListItemView(study) {
					gotoNext(i)
				}
			}
		}
		
		
		NavigationView(
			gotoPrevious = gotoPrevious,
			gotoNext = null,
			modifier = Modifier.constrainAs(navigation) {
				start.linkTo(parent.start, margin = 20.dp)
				end.linkTo(parent.end, margin = 20.dp)
				bottom.linkTo(parent.bottom, margin = 20.dp)
				width = Dimension.fillToConstraints
			}
		)
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewStudyListView() {
	ESMiraSurface {
		StudyListView(listOf(
			Study.newInstance("", "", """{"id":1, "title": "Study1", "contactEmail": "contact@email"}"""),
			Study.newInstance("", "", """{"id":2, "title": "Study2"}"""),
			Study.newInstance("", "", """{"id":3, "title": "Study3"}""")
		), {}, {})
	}
}