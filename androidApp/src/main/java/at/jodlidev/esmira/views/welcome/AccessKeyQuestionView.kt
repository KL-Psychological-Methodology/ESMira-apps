package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.DefaultButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun AccessKeyDialog(accessKey: String, openState: MutableState<Boolean>, gotoNext: (accessKey: String) -> Unit) {
	val rememberAccessKey = remember { mutableStateOf(accessKey) }
	AlertDialog(
		onDismissRequest = {
			openState.value = false
		},
		title = {
			Text(stringResource(R.string.colon_accessCode))
		},
		text = {
			TextField(
				value = rememberAccessKey.value,
				onValueChange = {
					rememberAccessKey.value = it
				}
			)
		},
		confirmButton = {
			TextButton(
				onClick = {
					openState.value = false
					gotoNext(rememberAccessKey.value)
				}
			) {
				Text(stringResource(R.string.ok_))
			}
		},
	)
}

@Composable
fun AccessKeyQuestionView(accessKey: String, gotoPrevious: () -> Unit, gotoNext: (accessKey: String) -> Unit) {
	ConstraintLayout(modifier = Modifier.fillMaxSize()) {
		val openAccessKeyDialog = remember { mutableStateOf(false) }
		
		if(openAccessKeyDialog.value)
			AccessKeyDialog(accessKey, openAccessKeyDialog, gotoNext)
		
		val (icon, questionMarkText, instructionsText, divider, buttonYes, buttonNo, buttonPrev) = createRefs()
		
		Icon(
			Icons.Filled.Key,
			contentDescription = "",
			modifier = Modifier
				.size(100.dp)
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
			text = stringResource(id = R.string.welcome_accessKey_question),
			textAlign = TextAlign.Center,
			modifier = Modifier.constrainAs(instructionsText) {
				top.linkTo(icon.bottom, margin = 20.dp)
				start.linkTo(parent.start, margin = 20.dp)
				end.linkTo(parent.end, margin = 20.dp)
				width = Dimension.fillToConstraints
			}
		)
		
		DefaultButton(
			onClick = {
				openAccessKeyDialog.value = true
			},
			modifier = Modifier
				.constrainAs(buttonYes) {
					start.linkTo(parent.start, margin = 40.dp)
					end.linkTo(parent.end, margin = 40.dp)
					top.linkTo(instructionsText.bottom, margin = 20.dp)
					width = Dimension.fillToConstraints
				}
		) {
			Text(stringResource(R.string.yes))
		}
		
		DefaultButton(
			onClick = {
				gotoNext("")
			},
			modifier = Modifier
				.constrainAs(buttonNo) {
					start.linkTo(parent.start, margin = 40.dp)
					end.linkTo(parent.end, margin = 40.dp)
					top.linkTo(buttonYes.bottom, margin = 10.dp)
					width = Dimension.fillToConstraints
				}
		) {
			Text(stringResource(R.string.welcome_join_public_study), textAlign = TextAlign.Center)
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
fun PreviewAccessKeyQuestionView() {
	ESMiraSurface {
		AccessKeyQuestionView("", {}, { _ -> })
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAccessKeyDialog() {
	val open = remember { mutableStateOf(false) }
	
	ESMiraSurface {
		AccessKeyDialog("key", open, {})
	}
}