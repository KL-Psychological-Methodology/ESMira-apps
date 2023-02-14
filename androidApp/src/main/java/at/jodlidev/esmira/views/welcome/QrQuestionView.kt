package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.DefaultButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun QrQuestionView(gotoNo: () -> Unit, gotoYes: () -> Unit, gotoPrevious: () -> Unit) {
	ConstraintLayout(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
		
		val (icon, questionMarkText, instructionsText, buttonNo, buttonYes, navigation) = createRefs()
		
		Image(
			painter = painterResource(id = R.drawable.example_qr_code),
			contentDescription = "",
			modifier = Modifier
				.constrainAs(icon) {
					top.linkTo(parent.top)
					start.linkTo(parent.start)
					end.linkTo(parent.end)
				}
		)
		Text(
			text = stringResource(id = R.string.questionMark),
			fontSize = 64.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.constrainAs(questionMarkText) {
					top.linkTo(icon.top)
					bottom.linkTo(icon.bottom)
					start.linkTo(icon.end, margin = 10.dp)
				}
		)
		
		Text(
			text = stringResource(id = R.string.welcome_qr_question),
			textAlign = TextAlign.Center,
			modifier = Modifier.constrainAs(instructionsText) {
				top.linkTo(icon.bottom, margin = 20.dp)
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				width = Dimension.fillToConstraints
			}
		)
		
		
		createHorizontalChain(buttonNo, buttonYes, chainStyle = ChainStyle.Spread)
		
		DefaultButton(stringResource(R.string.no),
			onClick = gotoNo,
			modifier = Modifier
				.constrainAs(buttonNo) {
					start.linkTo(parent.start)
					end.linkTo(buttonYes.start)
					top.linkTo(instructionsText.bottom, margin = 20.dp)
				}
		
		)
		
		DefaultButton(stringResource(R.string.yes),
			onClick = gotoYes,
			modifier = Modifier
				.constrainAs(buttonYes) {
					end.linkTo(parent.end)
					start.linkTo(buttonYes.end)
					top.linkTo(instructionsText.bottom, margin = 20.dp)
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
fun PreviewQrQuestionView() {
	ESMiraSurface {
		QrQuestionView({}, {}, {})
	}
}