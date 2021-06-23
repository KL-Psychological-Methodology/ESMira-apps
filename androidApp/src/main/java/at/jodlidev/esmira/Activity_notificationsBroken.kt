package at.jodlidev.esmira

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by JodliDev on 24.04.2019.
 */
public class Activity_notificationsBroken : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.activity_notifications_broken)
		
		if(intent.hasExtra(EXTRA_CALLED_MANUALLY) && intent.extras?.getBoolean(EXTRA_CALLED_MANUALLY) == true) {
			findViewById<View>(R.id.header).visibility = View.GONE
			findViewById<View>(R.id.desc).visibility = View.GONE
			findViewById<View>(R.id.additionalSettings).visibility = View.VISIBLE
			
		}
		findViewById<TextView>(R.id.dontkillmyapp).movementMethod = LinkMovementMethod.getInstance()
		findViewById<TextView>(R.id.footer).movementMethod = LinkMovementMethod.getInstance()
		
		findViewById<TextView>(R.id.btn_ok).setOnClickListener {
			finish()
		}
	}
	
	
	
	companion object {
		private const val EXTRA_CALLED_MANUALLY = "with_error"
		
		fun start(context: Context, calledManually: Boolean = false) {
			val intent = Intent(context, Activity_notificationsBroken::class.java)
			if(context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			intent.putExtra(EXTRA_CALLED_MANUALLY, calledManually)
			context.startActivity(intent)
		}
	}
}