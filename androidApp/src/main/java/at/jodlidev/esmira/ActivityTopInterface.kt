package at.jodlidev.esmira

import android.os.Bundle

/**
 * Created by JodliDev on 09.04.2019.
 */
interface ActivityTopInterface {
	fun gotoSite(site: Int, data: Bundle? = null, asSubSite: Boolean = true)
	fun message(s: String)
	
	fun changeNavigationBar(enabled: Boolean)
}