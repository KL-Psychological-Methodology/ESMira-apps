package at.jodlidev.esmira

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar

/**
 * Created by JodliDev on 08.04.2019.
 */
class Activity_addStudy : AppCompatActivity(), ActivityTopInterface {
	var studies: List<Study> = ArrayList()
	var serverTitle: String = ""
	var accessKey: String = ""

	public override fun onCreate(savedInstance: Bundle?) {
		super.onCreate(savedInstance)
		setContentView(R.layout.activity_add_study)
		
		if(intent.extras == null) {
			finish()
		}
		val extras = intent.extras ?: return
		
		val listString = Fragment_settings.loadPreference(this, KEY_STUDY_LIST_JSON)
		serverTitle = extras.getString(KEY_SERVER_TITLE) ?: ""
		val serverUrl = extras.getString(KEY_SERVER_URL)?: ""
		accessKey = extras.getString(KEY_ACCESS_KEY) ?: ""
		val studyId = extras.getLong(KEY_STUDY_ID)
		val qId = extras.getLong(KEY_QUESTIONNAIRE_ID)
		
		studies = Study.getFilteredStudyList(listString, serverUrl, accessKey, studyId, qId)
		
		if(savedInstance == null) {
			if(studies.size == 1)
				openStudy(0, false)
			else
				gotoSite(SITE_LIST_STUDIES, extras, false)
			
		}
	}
	override fun changeNavigationBar(enabled: Boolean) {}
	
	
	fun openStudy(index: Int, asSubSIte: Boolean = true) {
		val b = Bundle()
		b.putInt(KEY_LIST_INDEX, index)
		
		gotoSite(SITE_STUDY_DETAIL, b, asSubSIte)
	}
	
	override fun gotoSite(site: Int, data: Bundle?, asSubSite: Boolean) {
		val f: Fragment = when(site) {
			SITE_LIST_STUDIES -> Fragment_listNewStudies()
			SITE_STUDY_DETAIL -> Fragment_studyDetail()
			SITE_STUDY_PERMISSIONS -> Fragment_studyPermissions()
			SITE_STUDY_REGISTERED -> Fragment_studyRegistered()
			else -> return
		}
		
		f.arguments = data
		
		val fm = supportFragmentManager
		if(asSubSite) {
			fm.beginTransaction()
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(R.id.fragment_container, f, FRAGMENT_TAG)
				.addToBackStack(null)
//				.commit()
				.commitAllowingStateLoss() //IllegalStateException: should fix "Can not perform this action after onSaveInstanceState" by bots
		}
		else {
			fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
			fm.beginTransaction()
				.setCustomAnimations(R.anim.appear_from_0, R.anim.vanish_to_0, R.anim.appear_from_0, R.anim.vanish_to_0)
				.replace(R.id.fragment_container, f)
//				.commit()
				.commitAllowingStateLoss() //IllegalStateException: should fix "Can not perform this action after onSaveInstanceState" by bots
		}
	}
	
	override fun message(s: String) {
		Snackbar.make(findViewById(R.id.fragment_container), s, Snackbar.LENGTH_LONG).show()
		Log.e("error", s)
	}
	
	override fun onBackPressed() {
		val fm = supportFragmentManager
		val backStackCount = fm.backStackEntryCount
		if(backStackCount > 0)
			fm.popBackStack()
		else
			super.onBackPressed()
	}
	
	override fun finish() {
		Fragment_settings.clearPreference(applicationContext, KEY_STUDY_LIST_JSON)
		super.finish()
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
	}
	
	companion object {
		const val SITE_LIST_STUDIES = 1
		const val SITE_STUDY_DETAIL = 2
		const val SITE_STUDY_PERMISSIONS = 3
		const val SITE_STUDY_REGISTERED = 4

		const val EXTRA_HIDE_BACK_BTN = "hide_back_btn"
		const val SAVED_SITE = "current_site"
		const val REQUEST_CONNECT_SERVER = 100

		
		const val KEY_STUDY_LIST_JSON: String = "study_list_json"
		const val KEY_SERVER_TITLE: String = "server_title"
		const val KEY_SERVER_URL: String = "server_url"
		const val KEY_ACCESS_KEY: String = "access_key"
		const val KEY_STUDY_ID: String = "study_id"
		const val KEY_QUESTIONNAIRE_ID: String = "questionnaire_id"
		const val KEY_LIST_INDEX: String = "list_index"

		private const val FRAGMENT_TAG = "fragment_container"
		
		
		fun start(activity: Activity, urlTitle: String, url: String, accessKey: String, studyId: Long = 0, qId: Long = 0, failListener: (() -> Unit)? = null) {
//			val loadingDialog = BottomSheetDialog(activity)
			val loadingDialog = BottomSheetDialog(activity, R.style.AppTheme_BottomSheetDialog_with_roundCorners)
			loadingDialog.setContentView(R.layout.dialog_loading)
			
			
			loadingDialog.show()
			val web = Web.loadStudies(url, accessKey, onError = { msg, e ->
				activity.runOnUiThread {
					Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
					loadingDialog.dismiss()
					if(failListener != null)
						failListener()
				}
			}, onSuccess = { studyString, urlFormatted ->
				loadingDialog.dismiss()
				
				val intent = Intent(activity, Activity_addStudy::class.java)
				Fragment_settings.savePreference(activity, KEY_STUDY_LIST_JSON, studyString) //when studies get too complicated the string gets too long
				
				intent.putExtra(KEY_SERVER_TITLE, urlTitle)
				intent.putExtra(KEY_SERVER_URL, urlFormatted)
				intent.putExtra(KEY_ACCESS_KEY, accessKey)
				intent.putExtra(KEY_STUDY_ID, studyId)
				intent.putExtra(KEY_QUESTIONNAIRE_ID, qId)
				activity.startActivity(intent)
				activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
			})
			loadingDialog.setOnDismissListener {
				web.cancel()
				if(failListener != null)
					failListener()
			}
		}
	}
}