package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
	ConstraintLayout(modifier = Modifier.fillMaxSize()) {
		
		val (icon, questionMarkText, instructionsText, buttonNo, buttonYes, divider, buttonPrev) = createRefs()
		
		Image(
			painter = painterResource(id = R.drawable.example_qr_code),
			contentDescription = "",
			modifier = Modifier
				.constrainAs(icon) {
					top.linkTo(parent.top, margin = 20.dp)
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
				start.linkTo(parent.start, margin = 20.dp)
				end.linkTo(parent.end, margin = 20.dp)
				width = Dimension.fillToConstraints
			}
		)
		
		
		createHorizontalChain(buttonNo, buttonYes, chainStyle = ChainStyle.Spread)
		
		DefaultButton(
			onClick = gotoNo,
			modifier = Modifier
				.constrainAs(buttonNo) {
					start.linkTo(parent.start)
					end.linkTo(buttonYes.start)
					top.linkTo(instructionsText.bottom, margin = 20.dp)
				}
		
		) {
			Text(stringResource(R.string.no))
		}
		
		DefaultButton(
			onClick = gotoYes,
			modifier = Modifier
				.constrainAs(buttonYes) {
					end.linkTo(parent.end)
					start.linkTo(buttonYes.end)
					top.linkTo(instructionsText.bottom, margin = 20.dp)
				}
		
		) {
			Text(stringResource(R.string.yes))
		}
		
		
		Divider(
			color = MaterialTheme.colors.primary,
			thickness = 1.dp,
			modifier = Modifier
				.constrainAs(divider) {
					start.linkTo(parent.start, margin = 20.dp)
					end.linkTo(parent.end, margin = 20.dp)
					bottom.linkTo(buttonPrev.top, margin = 5.dp)
					width = Dimension.fillToConstraints
				}
		)
		
		TextButton(
			onClick = gotoPrevious,
			modifier = Modifier
				.constrainAs(buttonPrev) {
					start.linkTo(divider.start)
					bottom.linkTo(parent.bottom, margin = 20.dp)
				}
		
		) {
			Icon(
				Icons.Default.KeyboardArrowLeft,
				contentDescription = "",
				modifier = Modifier.size(ButtonDefaults.IconSize)
			)
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Text(stringResource(R.string.back))
		}
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