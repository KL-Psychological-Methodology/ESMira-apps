package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun EntranceView(gotoNext: () -> Unit) {
	ConstraintLayout(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
		
		val (icon, helloText, instructionsText, navigation) = createRefs()
		
		createHorizontalChain(icon, helloText, chainStyle = ChainStyle.Packed)
		Image(
			painter = painterResource(id = R.drawable.ic_launcher_round),
			contentDescription = "",
			modifier = Modifier
				.size(50.dp)
				.constrainAs(icon) {
					top.linkTo(parent.top)
					start.linkTo(parent.start)
					end.linkTo(helloText.start)
				}
		)
		Text(
			text = stringResource(id = R.string.welcome_hello),
			fontSize = MaterialTheme.typography.headlineLarge.fontSize,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.padding(start = 10.dp) //workaround: createHorizontalChain() removes all margins
				.constrainAs(helloText) {
					top.linkTo(icon.top)
					bottom.linkTo(icon.bottom)
					start.linkTo(icon.end)
					end.linkTo(parent.end)
				}
		)
		
		Text(
			text = stringResource(id = R.string.welcome_first_instructions),
			modifier = Modifier.constrainAs(instructionsText) {
				top.linkTo(icon.bottom, margin = 20.dp)
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				width = Dimension.fillToConstraints
			}
		)
		
		NavigationView(
			gotoPrevious = null,
			gotoNext = gotoNext,
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
fun PreviewEntranceView() {
	ESMiraSurface {
		EntranceView {}
	}
}