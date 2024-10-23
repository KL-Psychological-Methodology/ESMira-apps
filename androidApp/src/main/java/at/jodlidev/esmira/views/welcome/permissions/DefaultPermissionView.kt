package at.jodlidev.esmira.views.welcome.permissions

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.views.DefaultButton


enum class DefaultPermissionState {
	PERMISSION, SUCCESS, FAILED, SKIPPED
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DefaultPermissionView(
	num: Int,
	header: String,
	whatFor: String,
	description: String,
	buttonLabel: String,
	state: MutableState<DefaultPermissionState>,
	isActive: () -> Boolean,
	isCurrent: () -> Boolean,
	goNext: () -> Unit,
	onClick: () -> Unit,
	overrideView: @Composable (() -> Boolean)? = null
) {

	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		PermissionHeaderView(
			num = num,
			isActive = isActive,
			state = state,
			header = header,
			whatFor = whatFor,
			modifier = Modifier.fillMaxWidth()
		)
		if(isCurrent() || (isActive() && state.value != DefaultPermissionState.SUCCESS)) {
			Spacer(modifier = Modifier.width(10.dp))
			
			AnimatedContent(
				targetState = state.value,
				transitionSpec = {
					if(targetState > initialState){
						ContentTransform(
							targetContentEnter = slideInHorizontally { width -> width } + fadeIn(),
							initialContentExit = slideOutHorizontally { width -> -width } + fadeOut()
						)
					}
					else {
						ContentTransform(
							targetContentEnter = slideInHorizontally { width -> -width } + fadeIn(),
							initialContentExit = slideOutHorizontally { width -> width } + fadeOut()
						)
					}
				}
			) { currentState ->
				if(overrideView != null && overrideView())
					return@AnimatedContent

				when (currentState) {
					DefaultPermissionState.PERMISSION -> {
						DefaultPermissionQuestionView(
							description,
							buttonLabel,
							onClick
						)
					}

					DefaultPermissionState.FAILED -> {
						Column {
							DefaultPermissionTryAgainView(
								tryAgain = {
									state.value = DefaultPermissionState.PERMISSION
								},
								ignore = {
									state.value = DefaultPermissionState.SKIPPED
									goNext()
								}
							)
						}
					}

					DefaultPermissionState.SKIPPED -> {
						DefaultPermissionSkippedView {
							state.value = DefaultPermissionState.PERMISSION
						}
					}

					DefaultPermissionState.SUCCESS -> {
						// show nothing
					}
				}

			}
		}
	}
}

@Composable
fun DefaultPermissionQuestionView(
	description: String,
	buttonLabel: String,
	onClick: () -> Unit
) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(description)
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(buttonLabel,
			onClick = onClick
		)
	}
}

@Composable
fun DefaultPermissionTryAgainView(tryAgain: () -> Unit, ignore: () -> Unit) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.info_schedule_setup_skipped))
		Spacer(modifier = Modifier.width(10.dp))
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
			DefaultButton(stringResource(R.string.ignore),
				onClick = ignore
			)

			DefaultButton(stringResource(R.string.try_again),
				onClick = tryAgain
			)
		}
	}
}

@Composable
fun DefaultPermissionSkippedView(tryAgain: () -> Unit) {
	Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(stringResource(id = R.string.info_schedule_setup_skipped))
		Spacer(modifier = Modifier.width(10.dp))
		DefaultButton(stringResource(R.string.try_again),
			onClick = tryAgain
		)
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewDefaultPermissionView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.PERMISSION) }
		DefaultPermissionView(
			num = 1,
			header = "Header",
			whatFor = "whatFor",
			description = "description",
			buttonLabel = "buttonLabel",
			state = state,
			isActive = { true },
			isCurrent = { true },
			goNext = {},
			onClick = {},
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewDisabledDefaultPermissionView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.PERMISSION) }
		DefaultPermissionView(
			num = 2,
			header = "Header",
			whatFor = "whatFor",
			description = "description",
			buttonLabel = "buttonLabel",
			state = state,
			isActive = { false },
			isCurrent = { false },
			goNext = {},
			onClick = {},
		)
	}
}
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewSuccessDefaultPermissionView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.SUCCESS) }
		DefaultPermissionView(
			num = 1,
			header = "Header",
			whatFor = "whatFor",
			description = "description",
			buttonLabel = "buttonLabel",
			state = state,
			isActive = { true },
			isCurrent = { false },
			goNext = {},
			onClick = {},
		)
	}
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewFailedDefaultPermissionView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.FAILED) }
		DefaultPermissionView(
			num = 1,
			header = "Header",
			whatFor = "whatFor",
			description = "description",
			buttonLabel = "buttonLabel",
			state = state,
			isActive = { true },
			isCurrent = { false },
			goNext = {},
			onClick = {},
		)
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewSkippedDefaultPermissionView() {
	ESMiraSurface {
		val state = remember { mutableStateOf(DefaultPermissionState.SKIPPED) }
		DefaultPermissionView(
			num = 1,
			header = "Header",
			whatFor = "whatFor",
			description = "description",
			buttonLabel = "buttonLabel",
			state = state,
			isActive = { true },
			isCurrent = { false },
			goNext = {},
			onClick = {},
		)
	}
}