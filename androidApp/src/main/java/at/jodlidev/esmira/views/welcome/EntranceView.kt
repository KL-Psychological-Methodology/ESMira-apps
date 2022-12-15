package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
	ConstraintLayout(modifier = Modifier.fillMaxSize()) {
		
		val (icon, helloText, instructionsText, divider, button) = createRefs()
		
		createHorizontalChain(icon, helloText, chainStyle = ChainStyle.Packed)
		Image(
			painter = painterResource(id = R.drawable.ic_launcher_round),
			contentDescription = "",
			modifier = Modifier
				.size(50.dp)
				.constrainAs(icon) {
					top.linkTo(parent.top, margin = 20.dp)
					start.linkTo(parent.start)
					end.linkTo(helloText.start)
				}
		)
		Text(
			text = stringResource(id = R.string.welcome_hello),
			fontSize = 32.sp,
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
				start.linkTo(parent.start, margin = 20.dp)
				end.linkTo(parent.end, margin = 20.dp)
				width = Dimension.fillToConstraints
			}
		)
		
		Divider(
			color = MaterialTheme.colors.primary,
			thickness = 1.dp,
			modifier = Modifier
				.constrainAs(divider) {
					start.linkTo(parent.start, margin = 20.dp)
					end.linkTo(parent.end, margin = 20.dp)
					bottom.linkTo(button.top, margin = 5.dp)
					width = Dimension.fillToConstraints
				}
		)
		
		TextButton(
			onClick = gotoNext,
			modifier = Modifier
				.constrainAs(button) {
					end.linkTo(divider.end)
					bottom.linkTo(parent.bottom, margin = 20.dp)
				}
		
		) {
			Text(stringResource(R.string.continue_))
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Icon(
				Icons.Default.KeyboardArrowRight,
				contentDescription = "",
				modifier = Modifier.size(ButtonDefaults.IconSize)
			)
		}
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