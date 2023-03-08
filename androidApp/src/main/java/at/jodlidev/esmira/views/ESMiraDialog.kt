package at.jodlidev.esmira.views

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.ESMiraTheme

/**
 * Created by JodliDev on 02.03.2023.
 */

@Composable
fun  ESMiraDialog(
	confirmButtonLabel: String,
	onConfirmRequest: () -> Unit,
	title: String? = null,
	dismissButtonLabel: String? = null,
	onDismissRequest: (() -> Unit)? = null,
	contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
	content: @Composable ColumnScope.() -> Unit,
) {
	Dialog(
		onDismissRequest = onDismissRequest ?: onConfirmRequest
	) {
		ESMiraDialogContent(
			confirmButtonLabel = confirmButtonLabel,
			onConfirmRequest = onConfirmRequest,
			title = title,
			dismissButtonLabel = dismissButtonLabel,
			onDismissRequest = onDismissRequest,
			contentPadding = contentPadding,
			content = content
		)
	}
}




@Composable
fun ESMiraDialogContent(
	confirmButtonLabel: String,
	onConfirmRequest: () -> Unit,
	title: String? = null,
	dismissButtonLabel: String? = null,
	onDismissRequest: (() -> Unit)? = null,
	contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
	content: @Composable ColumnScope.() -> Unit,
) {
	val darkTheme = isSystemInDarkTheme()
	ESMiraTheme(darkTheme) {
		Surface(color = if(darkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background) {
			ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
				val (titleEl, contentEl, dismissButtonEl, confirmButtonEl) = createRefs()
				
				if(title != null) {
					Text(title,
						fontSize = MaterialTheme.typography.titleLarge.fontSize,
						modifier = Modifier.constrainAs(titleEl) {
							top.linkTo(parent.top, margin = 20.dp)
//							bottom.linkTo(contentEl.top)
							start.linkTo(parent.start, margin = 20.dp)
							end.linkTo(parent.end, margin = 20.dp)
							width = Dimension.fillToConstraints
						}
					)
				}
				
				//Workaround: Scrollable content seem to be buggy
				// As far as I can tell, the box is sized correctly (full screen minus top and bottom constraints)
				// but the scroll-state ignores top and bottom constraints. Meaning you cant scroll down all the way because it assumes more space available
				// Workaround: We dont use top and bottom constraints and instead use a fixed padding
				Column(modifier = Modifier
					.padding(contentPadding)
					.constrainAs(contentEl) {
						top.linkTo(parent.top)
						bottom.linkTo(parent.bottom)
						start.linkTo(parent.start)
						end.linkTo(parent.end)
						width = Dimension.fillToConstraints
						height = Dimension.wrapContent
						
						//Would be buggy with scrollable content:
//						top.linkTo(if(title != null) titleEl.bottom else parent.top, margin = 20.dp)
//						bottom.linkTo(confirmButtonEl.top)
//						height = Dimension.preferredWrapContent
					}
					.padding(top = if(title != null) 60.dp else 10.dp, bottom = 60.dp) //Workaround
				) {
					content()
				}
				
				DialogButton(confirmButtonLabel,
					onClick = onConfirmRequest,
					modifier = Modifier.constrainAs(confirmButtonEl) {
						bottom.linkTo(parent.bottom, margin = 5.dp)
						end.linkTo(parent.end, margin = 15.dp)
					}
				)
				
				if(dismissButtonLabel != null) {
					DialogButton(dismissButtonLabel,
						onClick = onDismissRequest ?: onConfirmRequest,
						modifier = Modifier.constrainAs(dismissButtonEl) {
							top.linkTo(confirmButtonEl.top)
							bottom.linkTo(confirmButtonEl.bottom)
							end.linkTo(confirmButtonEl.start)
						}
					)
				}
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewESMiraDialog() {
	ESMiraDialog(
		confirmButtonLabel = "Ok",
		onConfirmRequest = {},
		dismissButtonLabel = "Cancel",
		title = "Title"
	) {
		Text("Some content")
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewESMiraDialogWithoutTitle() {
	ESMiraDialog(
		confirmButtonLabel = "Ok",
		onConfirmRequest = {},
		dismissButtonLabel = "Cancel",
	) {
		Text("Some content")
	}
}