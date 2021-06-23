package at.jodlidev.esmira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Created by JodliDev on 09.04.2019.
 */
open class Base_fragment : Fragment() {
	fun goToAsSub(site: Int, data: Bundle? = null) {
		activity?.runOnUiThread {
			(activity as ActivityTopInterface).gotoSite(site, data)
		}
	}
	
	fun goToAsRoot(site: Int, data: Bundle? = null) {
		activity?.runOnUiThread {
			(activity as ActivityTopInterface).gotoSite(site, data, false)
		}
	}
	fun popBackStack() {
		requireActivity().supportFragmentManager.popBackStack()
	}
	
	fun setTitle(s: String, navigationBar: Boolean = true) {
		activity?.runOnUiThread {
			val actionBar = (activity as AppCompatActivity).supportActionBar
			if(actionBar != null)
				actionBar.title = s
			(activity as ActivityTopInterface).changeNavigationBar(navigationBar)
		}
	}
	fun setTitle(res: Int, navigationBar: Boolean = true) {
		setTitle(getString(res), navigationBar)
	}
	
	fun disableActionBar() {
		(activity as ActivityTopInterface).changeNavigationBar(false)
	}
	
	fun message(s: String) {
		activity?.runOnUiThread {
			(activity as ActivityTopInterface).message(s)
		}
	}
	
	fun message(res: Int) {
		val c = context
		if(c != null)
			message(c.getString(res))
	}
}