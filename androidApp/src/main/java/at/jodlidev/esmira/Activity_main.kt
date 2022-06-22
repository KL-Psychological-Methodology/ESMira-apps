package at.jodlidev.esmira

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import at.jodlidev.esmira.sharedCode.DbLogic
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class Activity_main : AppCompatActivity(), ActivityTopInterface {
	private lateinit var navigation: BottomNavigationView
	private lateinit var currentFragment: Fragment
	private var timesClickedForAdmin = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		DbLogic.startupApp()
		ScreenTrackingService.startService(applicationContext)
		
		if(BuildConfig.DEBUG)
			DbLogic.setDev(true, DbLogic.ADMIN_PASSWORD)
		
		if(DbLogic.hasNoStudies()) {
			Activity_WelcomeScreen.start(this)
		}
		
		setContentView(R.layout.activity_main)
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)
		if(supportActionBar != null) {
			supportActionBar!!.setDisplayHomeAsUpEnabled(false)
			supportActionBar!!.setDisplayShowHomeEnabled(false)
			toolbar.setNavigationOnClickListener { onBackPressed() }
		}
		
		navigation = findViewById(R.id.navigation)
		navigation.setOnNavigationItemSelectedListener { item ->
			val site = item.itemId
			if(site == SITE_SETTINGS) {
				if(++timesClickedForAdmin > 10) {
					if(DbLogic.isDev()) {
						DbLogic.setDev(false)
						Toast.makeText(applicationContext, R.string.info_dev_inactive, Toast.LENGTH_SHORT).show()
						gotoSite(SITE_SETTINGS)
					}
					else {
						val editText = EditText(this@Activity_main)
						MaterialAlertDialogBuilder(this@Activity_main, R.style.AppTheme_ActivityDialog)
							.setTitle(R.string.colon_password)
							.setCancelable(false)
							.setView(editText)
							.setPositiveButton(android.R.string.ok) { _, _ ->
								if(DbLogic.setDev(true, editText.text.toString())) {
									Toast.makeText(applicationContext, R.string.info_dev_active, Toast.LENGTH_SHORT).show()
									gotoSite(SITE_SETTINGS)
								}
							}
							.setNegativeButton(android.R.string.cancel, null).show()
						timesClickedForAdmin = 0
					}
					timesClickedForAdmin = 0
				}
			}
			else
				timesClickedForAdmin = 0
			gotoSite(site, null, false)

			true
		}
		if(savedInstanceState == null)
			navigation.selectedItemId = R.id.navigation_questionnaire
		
		
		updateNavigationIcon()
//		if(!DbLogic.hasStudiesWithStatistics())
//			navigation.menu.findItem(R.id.navigation_statistics).isVisible = false
//
//		if(!DbLogic.hasStudiesForMessages())
//			navigation.menu.findItem(R.id.navigation_messages).isVisible = false
//		else
//			updateNavigationBadges()
		
		val extras = intent.extras
		if(extras != null) {
			if(extras.containsKey(EXTRA_OPEN_STUDY_MESSAGES)) {
				val bundle = Bundle()
				bundle.putLong(Fragment_messages.KEY_STUDY_ID, extras.getLong(EXTRA_OPEN_STUDY_MESSAGES))
				navigation.selectedItemId = R.id.navigation_messages
				gotoSite(SITE_MESSAGES_DETAIL, bundle, true)
			}
			else if(extras.containsKey(EXTRA_OPEN_QUESTIONNAIRE)) {
				val bundle = Bundle()
				bundle.putLong(Fragment_questionnaireDetail.KEY_QUESTIONNAIRE, extras.getLong(EXTRA_OPEN_QUESTIONNAIRE))
				gotoSite(SITE_QUESTIONNAIRE_DETAIL, bundle, true)
			}
		}
	}
	
	fun updateNavigationBadges() {
		val count = DbLogic.countUnreadMessages()
		if(count == 0)
			navigation.removeBadge(R.id.navigation_messages)
		else
			navigation.getOrCreateBadge(R.id.navigation_messages).number = count
	}
	fun updateNavigationIcon() {
		navigation.menu.findItem(R.id.navigation_statistics).isVisible = DbLogic.hasStudiesWithStatistics()
		navigation.menu.findItem(R.id.navigation_messages).isVisible = DbLogic.hasStudiesForMessages()
		updateNavigationBadges()
	}
	
	override fun message(s: String) {
		Snackbar.make(findViewById(R.id.fragment_container), s, Snackbar.LENGTH_LONG).show()
		Log.e("error", s)
	}
	
	override fun changeNavigationBar(enabled: Boolean) {
		if(!this::navigation.isInitialized)
			return
		
		navigation.visibility = if(enabled) View.VISIBLE else View.GONE
	}
	
	override fun gotoSite(site: Int, data: Bundle?, asSubSite: Boolean) {
		val f: Fragment = when(site) {
			SITE_QUESTIONNAIRE_DETAIL -> Fragment_questionnaireDetail()
			SITE_LIST_QUESTIONNAIRES -> Fragment_listQuestionnaires()
			SITE_LIST_STATISTICS -> Fragment_listStatistics()
			SITE_LIST_MESSAGES -> Fragment_listMessages()
			SITE_STATISTICS_DETAIL -> Fragment_statisticsRoot()
			SITE_MESSAGES_DETAIL -> Fragment_messages()
			SITE_MESSAGE_NEW -> Fragment_messageNew()
			SITE_SETTINGS -> Fragment_settings()
			else -> return
		}
		
		f.arguments = data
		
		val fm = supportFragmentManager
		if(asSubSite) {
			fm.beginTransaction()
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(R.id.fragment_container, f, FRAGMENT_TAG)
				.addToBackStack(null)
				.commit()
			updateBackButton(true)
		}
		else {
			fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
			fm.beginTransaction()
				.setCustomAnimations(R.anim.appear_from_0, R.anim.vanish_to_0, R.anim.appear_from_0, R.anim.vanish_to_0)
				.replace(R.id.fragment_container, f)
				.commit()
			updateBackButton(false)
		}
		currentFragment = f
	}
	
	public override fun onResume() {
		super.onResume()
		val missedNotifications = DbLogic.getMissedInvitations()
		if(missedNotifications != 0) {
			val snackbar = Snackbar.make(
				findViewById(R.id.main_window),
				resources.getQuantityString(R.plurals.android_info_missed_notifications, missedNotifications, missedNotifications),
				Snackbar.LENGTH_INDEFINITE
			)
			snackbar.setAction(R.string.understood) { snackbar.dismiss() }
			snackbar.show()
			DbLogic.resetMissedInvitations()
		}
		
		DbLogic.checkLeaveStudies()
		updateNavigationIcon()
	}
	
	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(this::currentFragment.isInitialized)
			currentFragment.onActivityResult(requestCode, resultCode, data)
	}
	
	private fun updateBackButton(enabled: Boolean) {
		supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
		supportActionBar?.setDisplayShowHomeEnabled(enabled)
	}
	
	override fun onBackPressed() {
		val fm = supportFragmentManager
		val backStackCount = fm.backStackEntryCount
		if(backStackCount > 0) {
			fm.popBackStack()
			updateBackButton(backStackCount-1 >= 1)
		}
		else
			super.onBackPressed()
	}
	
	companion object {
		const val EXTRA_OPEN_QUESTIONNAIRE = "extra_questionnaire"
		const val EXTRA_OPEN_STUDY_MESSAGES = "extra_study_message"
		const val SITE_LIST_QUESTIONNAIRES = R.id.navigation_questionnaire
		const val SITE_LIST_STATISTICS = R.id.navigation_statistics
		const val SITE_LIST_MESSAGES = R.id.navigation_messages
		const val SITE_SETTINGS = R.id.navigation_settings
		const val SITE_QUESTIONNAIRE_DETAIL = -5
		const val SITE_STATISTICS_DETAIL = -6
		const val SITE_MESSAGES_DETAIL = -7
		const val SITE_MESSAGE_NEW = -8
		private const val FRAGMENT_TAG = "fragment_container"
		
		fun start(activity: Activity) {
			val intent = Intent(activity, Activity_main::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			activity.startActivity(intent)
			activity.finish()
		}
	}
}