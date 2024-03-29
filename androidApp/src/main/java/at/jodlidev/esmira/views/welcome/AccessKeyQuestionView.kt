package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
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
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 15.12.2022.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessKeyDialog(accessKey: String, openState: MutableState<Boolean>, gotoNext: (accessKey: String) -> Unit) {
	val rememberAccessKey = remember { mutableStateOf(accessKey) }
	ESMiraDialog(
		onDismissRequest = {
			openState.value = false
		},
		title = stringResource(R.string.colon_accessCode),
		confirmButtonLabel = stringResource(R.string.ok_),
		onConfirmRequest = {
			openState.value = false
			gotoNext(rememberAccessKey.value)
		}
	) {
		OutlinedTextField(
			value = rememberAccessKey.value,
			onValueChange = {
				rememberAccessKey.value = it
			}
		)
	}
}

@Composable
fun AccessKeyQuestionView(accessKey: String, gotoPrevious: () -> Unit, gotoNext: (accessKey: String) -> Unit) {
	ConstraintLayout(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
		val openAccessKeyDialog = remember { mutableStateOf(false) }
		
		if(openAccessKeyDialog.value)
			AccessKeyDialog(accessKey, openAccessKeyDialog, gotoNext)
		
		val (icon, questionMarkText, instructionsText, buttonYes, buttonNo, navigation) = createRefs()
		
		Icon(
			Icons.Filled.Key,
			contentDescription = "",
			modifier = Modifier
				.size(100.dp)
				.constrainAs(icon) {
					top.linkTo(parent.top)
					start.linkTo(parent.start)
					end.linkTo(parent.end)
				}
		)
		Text(
			text = stringResource(id = R.string.questionMark),
			fontSize = MaterialTheme.typography.displayLarge.fontSize,
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
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				width = Dimension.fillToConstraints
			}
		)
		
		DefaultButton(
			text = stringResource(R.string.yes),
			onClick = {
				openAccessKeyDialog.value = true
			},
			modifier = Modifier
				.constrainAs(buttonYes) {
					start.linkTo(parent.start, margin = 20.dp)
					end.linkTo(parent.end, margin = 20.dp)
					top.linkTo(instructionsText.bottom, margin = 20.dp)
					width = Dimension.fillToConstraints
				}
		)
		
		DefaultButton(
			text = stringResource(R.string.welcome_join_public_study),
			onClick = {
				gotoNext("")
			},
			modifier = Modifier
				.constrainAs(buttonNo) {
					start.linkTo(parent.start, margin = 20.dp)
					end.linkTo(parent.end, margin = 20.dp)
					top.linkTo(buttonYes.bottom, margin = 10.dp)
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