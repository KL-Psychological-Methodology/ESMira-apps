package at.jodlidev.esmira.permissionBoxes

import android.os.Build
import android.view.View
import androidx.fragment.app.Fragment
import at.jodlidev.esmira.Fragment_studyPermissions
import at.jodlidev.esmira.R
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import android.widget.TextView


/**
 * Created by JodliDev on 20.12.2021.
 */
class AppUsagePermission(val fragment: Fragment, num: Int) :
	APermissionBox(fragment.requireContext(), num, R.layout.view_app_usage_permission, R.string.app_usage, R.string.app_usage_whatFor) {
	
	init {
	
	}
	
	override fun permissionGranted(granted: Boolean) {
		if(granted)
			setFinished()
		else
			setFailed()
	}
	
	override fun handleResult(resultCode: Int) {
		if(checkPermission())
			setFinished()
		else
			setFailed()
	}
	
	private fun checkPermission() : Boolean {
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
			return false
		
		val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
		
		val mode = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
			appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), context.packageName)
		else
			appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
		
		return mode == AppOpsManager.MODE_ALLOWED
	}
	
	override fun setFinished() {
		super.setFinished()
		rootView.findViewById<View>(R.id.desc).visibility = View.GONE
		rootView.findViewById<View>(R.id.btn_permission).visibility = View.GONE
	}
	
	override fun enable(continueCallback : () -> Unit) {
		super.enable(continueCallback)
		val btn = findViewById<View>(R.id.btn_permission)
		
		btn.setOnClickListener {
			if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
				findViewById<TextView>(R.id.desc).text = resources.getString(R.string.feature_is_not_supported)
				setFailed()
			}
			else {
				if(checkPermission())
					setFinished()
				else
					fragment.startActivityForResult(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), Fragment_studyPermissions.REQUEST_PERMISSION)
			}
		}
	}
	
}