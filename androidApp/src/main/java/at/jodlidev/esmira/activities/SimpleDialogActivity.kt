package at.jodlidev.esmira.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.DialogButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.NativeLink

/**
 * Created by JodliDev on 26.04.2019.
 */
class SimpleDialogActivity: ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val extras = intent.extras
		if(extras == null || !extras.containsKey(EXTRAS_TITLE) || !extras.containsKey(EXTRAS_MSG)) {
			finish()
			return
		}
		
		
		setContent {
			ESMiraSurface {
				Dialog(
					title = extras.getString(EXTRAS_TITLE) ?: "",
					content = extras.getString(EXTRAS_MSG) ?: "",
					notificationId = extras.getInt(EXTRAS_NOTIFICATION_ID) ?: -1
				)
			}
		}
	}
	
	@Composable
	fun Dialog(title: String, content: String, notificationId: Int = -1) {
		ConstraintLayout(modifier = Modifier.fillMaxWidth().height(300.dp)) {
			val (titleEl, box, button) = createRefs()
			
			if(title.isNotEmpty()) {
				Text(
					stringResource(R.string.send_error_report),
					fontSize = 18.sp,
					modifier = Modifier.constrainAs(titleEl) {
						top.linkTo(parent.top, margin = 20.dp)
						start.linkTo(parent.start, margin = 20.dp)
						end.linkTo(parent.end, margin = 20.dp)
					}
				)
			}
			Column(
				modifier = Modifier
					.constrainAs(box) {
						top.linkTo(titleEl.bottom, margin = 20.dp)
						start.linkTo(parent.start, margin = 20.dp)
						end.linkTo(parent.end, margin = 20.dp)
						bottom.linkTo(button.top, margin = 20.dp)
						width = Dimension.fillToConstraints
						height = Dimension.fillToConstraints
					}
					.verticalScroll(rememberScrollState())
			) {
				HtmlHandler.HtmlText(html = content, modifier = Modifier.fillMaxWidth())
			}
			DialogButton(stringResource(R.string.ok_),
				onClick = {
					if(notificationId != -1)
						NativeLink.notifications.remove(notificationId)
					finish()
				},
				modifier = Modifier
					.constrainAs(button) {
						start.linkTo(parent.start, margin = 5.dp)
						end.linkTo(parent.end, margin = 5.dp)
						bottom.linkTo(parent.bottom, margin = 5.dp)
						width = Dimension.fillToConstraints
					}
			
			)
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewDialog() {
		ESMiraSurface {
			Dialog(title = "Dialog title", content = "This<br>is<br>definitely<br>a<br>Very<br>long<br>text<br>ith<br>a<br>lot<br>of<br>lines!")
		}
	}
	
	companion object {
		const val EXTRAS_TITLE = "title"
		const val EXTRAS_MSG = "msg"
		const val EXTRAS_NOTIFICATION_ID = "notification_id"
		
		fun start(context: Context, title: String, msg: String) {
			val intent = Intent(context, SimpleDialogActivity::class.java)
			if(context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			intent.putExtra(EXTRAS_TITLE, title)
			intent.putExtra(EXTRAS_MSG, msg)
			
			val notificationId = (Math.random() * 1000).toInt()
			NativeLink.notifications.fire(title, msg, notificationId)
			intent.putExtra(EXTRAS_NOTIFICATION_ID, notificationId)
			context.startActivity(intent)
		}
		
		fun updateMsg(context: Context) {
			start(context, context.getString(R.string.error_app_update_needed_title), context.getString(R.string.error_app_update_needed_msg))
		}
	}
}