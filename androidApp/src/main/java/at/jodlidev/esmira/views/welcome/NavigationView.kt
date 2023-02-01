package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.TextButtonIconLeft
import at.jodlidev.esmira.TextButtonIconRight

/**
 * Created by JodliDev on 19.12.2022.
 */
@Composable
fun NavigationView(
	gotoPrevious: (() -> Unit)?,
	gotoNext: (() -> Unit)?,
	modifier: Modifier,
	prevIcon: () -> ImageVector = { Icons.Default.KeyboardArrowLeft },
	nextIcon: () -> ImageVector = { Icons.Default.KeyboardArrowRight },
	prevLabel: String = stringResource(R.string.back),
	nextLabel: String = stringResource(R.string.continue_),
	nextEnabled: () -> Boolean = { true },
) {
	ConstraintLayout(modifier = modifier) {
		val (divider, buttonPrev, buttonNext) = createRefs()
		Divider(
			color = MaterialTheme.colors.primary,
			thickness = 1.dp,
			modifier = Modifier
				.constrainAs(divider) {
					start.linkTo(parent.start)
					end.linkTo(parent.end)
					bottom.linkTo(buttonPrev.top, margin = 5.dp)
					width = Dimension.fillToConstraints
				}
		)

		if(gotoPrevious != null) {
			TextButtonIconLeft(
				text = prevLabel,
				icon = prevIcon(),
				onClick = gotoPrevious,
				modifier = Modifier
					.constrainAs(buttonPrev) {
						start.linkTo(divider.start)
						bottom.linkTo(parent.bottom)
					}

			)
		}

		if(gotoNext != null) {
			TextButtonIconRight(
				text = nextLabel,
				icon = nextIcon(),
				onClick = gotoNext,
				modifier = Modifier
					.constrainAs(buttonNext) {
						end.linkTo(divider.end)
						bottom.linkTo(parent.bottom)
					},
				enabled = nextEnabled()
			)
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewNavigationView() {
	ESMiraSurface {
		NavigationView({}, {}, Modifier.fillMaxWidth())
	}
}

@Preview
@Composable
fun PreviewNavigationView2() {
	ESMiraSurface {
		NavigationView({}, null, Modifier.fillMaxWidth())
	}
}

@Preview
@Composable
fun PreviewNavigationView3() {
	ESMiraSurface {
		NavigationView(null, {}, Modifier.fillMaxWidth())
	}
}