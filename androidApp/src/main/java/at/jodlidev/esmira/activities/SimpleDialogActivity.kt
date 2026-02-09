package at.jodlidev.esmira.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.views.ESMiraDialogContent

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
			ESMiraDialogContent(
				confirmButtonLabel = stringResource(R.string.ok_),
				onConfirmRequest = { finish() },
				title = extras.getString(EXTRAS_TITLE)
			) {
				HtmlHandler.HtmlText(
					html = extras.getString(EXTRAS_MSG) ?: "",
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
	
	companion object {
		const val EXTRAS_TITLE = "title"
		const val EXTRAS_MSG = "msg"
		const val EXTRAS_NOTIFICATION_ID = "notification_id"
		
		fun start(context: Context, title: String, msg: String, triggerNotification: Boolean = true) {
			val intent = Intent(context, SimpleDialogActivity::class.java)
			if(context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			intent.putExtra(EXTRAS_TITLE, title)
			intent.putExtra(EXTRAS_MSG, msg)

            if(triggerNotification) {
                val notificationId = (Math.random() * 1000).toInt()
                NativeLink.notifications.fire(title, msg, notificationId)
                intent.putExtra(EXTRAS_NOTIFICATION_ID, notificationId)
            }
			context.startActivity(intent)
		}
		
		fun updateMsg(context: Context) {
			start(context, context.getString(R.string.error_app_update_needed_title), context.getString(R.string.error_app_update_needed_msg))
		}
	}
}