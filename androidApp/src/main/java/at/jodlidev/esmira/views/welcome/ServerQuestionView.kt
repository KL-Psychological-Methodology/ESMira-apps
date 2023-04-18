package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.views.ESMiraDialog
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R

/**
 * Created by JodliDev on 15.12.2022.
 */

@Composable
fun ServerOptionLineView(title: String, url: String, isSelected: () -> Boolean, onSelected: () -> Unit) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.selectable(
				selected = isSelected(),
				onClick = onSelected,
				role = Role.RadioButton
			)
			.padding(8.dp)
	) {
		RadioButton(
			selected = isSelected(),
			onClick = null
		)
		Spacer(modifier = Modifier.width(10.dp))
		Column {
			Text(title, fontWeight = FontWeight.Bold)
			if(url.isNotEmpty())
				Text(url, fontSize = MaterialTheme.typography.labelLarge.fontSize, modifier = Modifier.padding(start = 10.dp))
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualServerDialog(openState: MutableState<Boolean>, manualServerUrl: MutableState<String>, rememberServerUrl: (serverUrl: String) -> Unit) {
	ESMiraDialog(
		onDismissRequest = {
			openState.value = false
		},
		title = stringResource(R.string.colon_enter_manually),
		dismissButtonLabel = stringResource(R.string.cancel),
		confirmButtonLabel = stringResource(R.string.ok_),
		onConfirmRequest = {
			if(manualServerUrl.value.isNotEmpty())
				rememberServerUrl(manualServerUrl.value)
			openState.value = false
		}
	) {
		Spacer(modifier = Modifier.height(20.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(stringResource(R.string.https))
			OutlinedTextField(
				modifier = Modifier.weight(1f),
				value = manualServerUrl.value,
				onValueChange = {
					manualServerUrl.value = it
				}
			)
		}
	}
}

@Composable
fun ServerQuestionView(
	getServerList: () -> List<Pair<String, String>>,
	initialServerUrl: String,
	gotoPrevious: () -> Unit,
	gotoNext: (serverUrl: String) -> Unit
) {
	var selectedServerUrl = initialServerUrl
	val serverList = remember {
		val list = getServerList()
		if(list.isNotEmpty() && selectedServerUrl.isEmpty()) {
			selectedServerUrl = list[0].second
		}
		list
	}
	ConstraintLayout(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
		val (icon, questionMarkText, instructionsText, serverListElement, navigation) = createRefs()
		val serverTitle = remember { mutableStateOf("") }
		val serverUrl = remember { mutableStateOf(selectedServerUrl) }
		
		val openManualServerDialog = remember { mutableStateOf(false) }
		val manualServerUrl = remember {
			val value = if(selectedServerUrl.isNotEmpty() && serverList.all { pair -> pair.second != selectedServerUrl})
				selectedServerUrl
			else
				""
			mutableStateOf(value)
		}
		
		if(openManualServerDialog.value)
			ManualServerDialog(openManualServerDialog, manualServerUrl) { _serverUrl: String ->
				serverTitle.value = ""
				serverUrl.value = _serverUrl
			}
		
		Icon(
			Icons.Filled.Public,
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
			text = stringResource(id = R.string.welcome_server_question),
			textAlign = TextAlign.Center,
			modifier = Modifier.constrainAs(instructionsText) {
				top.linkTo(icon.bottom, margin = 20.dp)
				start.linkTo(parent.start)
				end.linkTo(parent.end)
				width = Dimension.fillToConstraints
			}
		)
		
		LazyColumn(modifier = Modifier.constrainAs(serverListElement) {
			top.linkTo(instructionsText.bottom, margin = 20.dp)
			bottom.linkTo(navigation.top)
			start.linkTo(parent.start)
			end.linkTo(parent.end)
			width = Dimension.fillToConstraints
			height = Dimension.fillToConstraints
		}) {
			items(serverList) { serverPair: Pair<String, String> ->
				ServerOptionLineView(
					title = serverPair.first,
					url = serverPair.second,
					isSelected = {
						serverUrl.value == serverPair.second
					},
					onSelected = {
						serverTitle.value = serverPair.first
						serverUrl.value = serverPair.second
					}
				)
			}
			item {
				ServerOptionLineView(
					stringResource(id = R.string.enter_manually),
					manualServerUrl.value,
					isSelected = {
						serverUrl.value == manualServerUrl.value
					},
					onSelected = {
						openManualServerDialog.value = true
					}
				)
			}
		}
		
		NavigationView(
			gotoPrevious = gotoPrevious,
			gotoNext = { gotoNext(serverUrl.value) },
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
fun PreviewManualServerDialog() {
	val open = remember { mutableStateOf(false) }
	val manualServerUrl = remember { mutableStateOf("example.url") }
	
	ESMiraSurface {
		ManualServerDialog(open, manualServerUrl, {})
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewServerQuestionView() {
	val serverList = arrayListOf(
		Pair("Karl Landsteiner Privatuniv.", "esmira.kl.ac.at"),
		Pair("Another Server", "example.url")
	)
	
	ESMiraSurface {
		ServerQuestionView(
			initialServerUrl =  "example.url",
			getServerList = { serverList },
			gotoPrevious = {},
			gotoNext = { _ -> }
		)
	}
}


@Preview
@Composable
fun PreviewServerQuestionViewWitManual() {
	val serverList = arrayListOf(
		Pair("Karl Landsteiner Privatuniv.", "esmira.kl.ac.at"),
		Pair("Another Server", "example.url")
	)
	
	ESMiraSurface {
		ServerQuestionView(
			initialServerUrl =  "manual.url",
			getServerList = { serverList },
			gotoPrevious = {},
			gotoNext = { _ -> }
		)
	}
}