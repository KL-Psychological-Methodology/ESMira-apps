package at.jodlidev.esmira

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import at.jodlidev.esmira.sharedCode.NativeLink

/**
 * Created by JodliDev on 26.04.2019.
 */
public class Activity_dialog : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_dialog)
		val extras = intent.extras
		if(extras == null || !extras.containsKey(EXTRAS_TITLE) || !extras.containsKey(EXTRAS_MSG)) {
			finish()
			return
		}

		val msgEl = findViewById<TextView>(R.id.msg)
		msgEl.text = HtmlCompat.fromHtml(extras.getString(EXTRAS_MSG) ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
		msgEl.movementMethod = LinkMovementMethod.getInstance()
		
		title = extras.getString(EXTRAS_TITLE)
		findViewById<View>(R.id.btn_ok).setOnClickListener {
			if(extras.containsKey(EXTRAS_NOTIFICATION_ID))
				NativeLink.notifications.remove(extras.getInt(EXTRAS_NOTIFICATION_ID))
			finish()
		}
	}
	
	companion object {
		const val EXTRAS_TITLE = "title"
		const val EXTRAS_MSG = "msg"
		const val EXTRAS_NOTIFICATION_ID = "notification_id"

		fun start(context: Context, title: String, msg: String) {
			val intent = Intent(context, Activity_dialog::class.java)
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