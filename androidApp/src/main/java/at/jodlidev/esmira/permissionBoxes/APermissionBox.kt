package at.jodlidev.esmira.permissionBoxes

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import at.jodlidev.esmira.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Created by JodliDev on 20.12.2021.
 */
abstract class APermissionBox(context: Context, attrs: AttributeSet?, num: Int, layoutRes: Int, headerRes: Int, msgRes: Int = -1) :
	LinearLayout(context, attrs) {
	private val root = View.inflate(context, R.layout.item_permission, this)
	private var continueCallback: () -> Unit = { }
	
	constructor(context: Context, num: Int, layoutRes: Int, headerRes: Int, msgRes: Int = -1) :
		this(context, null, num, layoutRes, headerRes, msgRes)
	
	init {
		val headerText = root.findViewById<TextView>(R.id.header)
		val numText = root.findViewById<TextView>(R.id.num)
		headerText.text = resources.getString(headerRes)
		
		if(num != 0) {
			numText.text = num.toString()
			if(num != 1)
				rootView.alpha = 0.3f
		}

		if(msgRes != -1) {
			root.findViewById<View>(R.id.btn_whatFor).setOnClickListener {
				MaterialAlertDialogBuilder(context, R.style.AppTheme_ActivityDialog)
					.setMessage(msgRes)
					.setPositiveButton(R.string.close, null)
					.show()
			}
		}
		else
			root.findViewById<View>(R.id.btn_whatFor).visibility = View.GONE
		
		View.inflate(context, layoutRes, findViewById<LinearLayout>(R.id.content))
	}
	
	open fun enable(continueCallback : () -> Unit) {
		this.continueCallback = continueCallback
		root.alpha = 1F
	}
	
	open fun permissionGranted(granted: Boolean = true) {}
	open fun handleResult(resultCode: Int) {}
	
	open fun setFinished() {
		findViewById<View>(R.id.completed)?.visibility = View.VISIBLE
		this.continueCallback()
	}
	open fun setFailed() {
		findViewById<View>(R.id.failed)?.visibility = View.VISIBLE
		this.continueCallback()
	}
}