package at.jodlidev.esmira

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.androidNative.SQLite
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.Web
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.*

/**
 * Created by JodliDev on 23.04.2019.
 */
class Fragment_settings : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.settings, rootKey)
		val hasNoStudies: Boolean = DbLogic.hasNoStudies()
		
		//
		//Edit schedule
		//
		val changeSchedulesBox: Preference = findPreference(KEY_CHANGE_SCHEDULES) ?: return
		if(hasNoStudies || !DbLogic.hasEditableSchedules())
			changeSchedulesBox.isEnabled = false
		else
			changeSchedulesBox.onPreferenceClickListener = this
		
		//
		//Update studies:
		//
		val btnUpdateStudies: Preference = findPreference(KEY_SEND_UPDATE_STUDIES) ?: return
		if(hasNoStudies)
			btnUpdateStudies.isEnabled = false
		else
			btnUpdateStudies.onPreferenceClickListener = this
		
		//
		//Sync:
		//
		val btnSyncNow: Preference = findPreference(KEY_SEND_SYNC_NOW) ?: return
		btnSyncNow.onPreferenceClickListener = this
		val unSyncedCount: Int = DbLogic.getUnSyncedDataSetCount()
		if(unSyncedCount != 0)
			btnSyncNow.summary = getString(R.string.info_number_to_sync, unSyncedCount)
		
		//
		//Errors
		//
		val btnSendErrors: Preference = findPreference(KEY_SEND_ERRORS) ?: return
		btnSendErrors.onPreferenceClickListener = this
		val errorCount: Int = DbLogic.getErrorCount()
//		val warningCount: Int = DbLogic.getWarnCount()
		
		if(errorCount != 0) {
//			val s = SpannableStringBuilder(getString(R.string.colon_detected))
//			s.append(" ")
//			if(errorCount != 0) {
//				val start: Int = s.length
//				s.append(errorCount.toString())
//				s.append(" ")
//				s.append(getString(R.string.word_errors))
//				s.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.error)), start, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//			}
//			if(warningCount != 0) {
//				if(errorCount != 0) s.append(" / ")
//				val start: Int = s.length
//				s.append(warningCount.toString())
//				s.append(" ")
//				s.append(getString(R.string.word_warnings))
//				s.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.warn)), start, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//			}
//			btnSendErrors.summary = s
			
			btnSendErrors.summary = getString(R.string.info_detected_x_errors, errorCount)
		}
		
		//
		//notifications broken
		//
		findPreference<Preference>(KEY_NOTIFICATIONS_BROKEN)?.onPreferenceClickListener = this
		
		
		//
		//Backup
		//
		val btnBackup: Preference = findPreference(KEY_BACKUP) ?: return
		val btnLoadBackup: Preference = findPreference(KEY_LOAD_BACKUP) ?: return
		val btnNextNotification: Preference = findPreference(KEY_NEXT_NOTIFICATION) ?: return
		val btnShowWelcome: Preference = findPreference(KEY_SHOW_WELCOME) ?: return
		
		if(DbLogic.isDev()) {
			btnBackup.isVisible = true
			btnLoadBackup.isVisible = true
			btnNextNotification.isVisible = true
			btnShowWelcome.isVisible = true
			
			btnBackup.onPreferenceClickListener = this
			btnLoadBackup.onPreferenceClickListener = this
			btnNextNotification.onPreferenceClickListener = this
			btnShowWelcome.onPreferenceClickListener = this
		}
		else {
			btnBackup.isVisible = false
			btnLoadBackup.isVisible = false
			btnNextNotification.isVisible = false
			btnShowWelcome.isVisible = false
		}
		
		//
		//User-Id
		//
		val userId = findPreference<Preference>(KEY_USER_ID)
		userId?.title = getString(R.string.user_id, DbLogic.getUid())
		userId?.onPreferenceClickListener = this
		
		//
		//App-Version
		//
		findPreference<Preference>(KEY_APP_VERSION)!!.title = getString(R.string.app_version, BuildConfig.VERSION_NAME, DbLogic.getVersion())
		
		setTitle(R.string.settings)
	}
	
	override fun onPreferenceClick(preference: Preference): Boolean {
		when(preference.key) {
			KEY_SEND_ERRORS ->
                Activity_errorReport.start(requireActivity(), true)
			KEY_NOTIFICATIONS_BROKEN ->
                Activity_notificationsBroken.start(requireActivity(), true)
			KEY_SEND_SYNC_NOW ->
				Web.syncDataSetsAsync { syncCount ->
					val a = activity ?: return@syncDataSetsAsync
					a.runOnUiThread {
						if(syncCount != -1)
							Toast.makeText(context, requireContext().getString(R.string.info_sync_complete, syncCount), Toast.LENGTH_SHORT).show()
						else
							Toast.makeText(context, requireContext().getString(R.string.info_sync_failed), Toast.LENGTH_SHORT).show()
						
						(a as ActivityTopInterface).gotoSite(Activity_main.SITE_SETTINGS) //reload page
					}
				}
			KEY_SEND_UPDATE_STUDIES ->
				Web.updateStudiesAsync { updatedCount ->
					val a = activity ?: return@updateStudiesAsync
					a.runOnUiThread {
						if(updatedCount != -1)
							Toast.makeText(context, requireContext().getString(R.string.info_update_complete, updatedCount), Toast.LENGTH_SHORT).show()
						else
							Toast.makeText(context, requireContext().getString(R.string.info_update_failed), Toast.LENGTH_SHORT).show()
						(a as ActivityTopInterface).gotoSite(Activity_main.SITE_SETTINGS) //reload page
					}
					
				}
			KEY_CHANGE_SCHEDULES ->
                Activity_editSchedules.start(requireActivity())
			KEY_BACKUP ->
                if(Build.VERSION.SDK_INT >= 19) {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
					intent.type = "application/x-sqlite3"
                    intent.putExtra(Intent.EXTRA_TITLE, "backup.db")
                    startActivityForResult(intent, INTENT_CREATE_BACKUP)
                }
			KEY_LOAD_BACKUP -> {
				val intentRestore = Intent(Intent.ACTION_GET_CONTENT)
				intentRestore.type = "*/*"
				startActivityForResult(intentRestore, INTENT_LOAD_BACKUP)
			}
			KEY_NEXT_NOTIFICATION -> {
				val context: Context = context ?: return false

				val recyclerView = RecyclerView(context)
				recyclerView.setPadding(20, 10, 20, 10)
				recyclerView.layoutManager = LinearLayoutManager(context)
				val alarms = DbLogic.getNextAlarms()
				recyclerView.adapter = NextNotificationsAdapter(context, alarms, true)
				MaterialAlertDialogBuilder(context, R.style.AppTheme_ActivityDialog)
						.setTitle(R.string.next_notifications)
						.setView(recyclerView)
						.setPositiveButton(android.R.string.ok, null).show()
			}
			KEY_SHOW_WELCOME -> {
				context?.let { Activity_WelcomeScreen.start(it) }
			}
			KEY_USER_ID -> {
				context?.let {
					val userId = DbLogic.getUid()
					val clipBoard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
					val clipData = ClipData.newPlainText("label", userId)
					clipBoard.setPrimaryClip(clipData)
					Toast.makeText(it, it.getString(R.string.info_copied_x_to_clipboard, userId), Toast.LENGTH_SHORT).show()
				}
			}
		}
		return false
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val context = context ?: return
		if(resultCode != Activity.RESULT_OK)
            return
		when(requestCode) {
			INTENT_CREATE_BACKUP -> {
				val uri: Uri = data?.data ?: return
				try {
					val input: InputStream = FileInputStream(context.getDatabasePath(DbLogic.DATABASE_NAME))
					val output: OutputStream = context.contentResolver.openOutputStream(uri)
						?: throw IOException("OutputStream was null")
					
					input.copyTo(output, 1024)
					
					input.close()
					output.flush()
					output.close()
				}
				catch(e: IOException) {
					Toast.makeText(context, getString(R.string.error_general, e.message), Toast.LENGTH_LONG).show()
				}
			}
			INTENT_LOAD_BACKUP -> {
				val selectedDB = data?.data ?: return

				try {
					val input: InputStream = context.contentResolver.openInputStream(selectedDB)
						?: throw IOException("InputStream was null")
					val output: OutputStream = FileOutputStream(context.getDatabasePath(DbLogic.DATABASE_NAME))
					
					input.copyTo(output, 1024)
					
					input.close()
					output.flush()
					output.close()
					
					
					
//					val target: File = context.getDatabasePath(DbLogic.DATABASE_NAME)
//					val output = FileOutputStream(target)
//					val input: InputStream = context.contentResolver.openInputStream(selectedDB) ?: throw IOException()
//
//					val buffer = ByteArray(1024)
//					FileInputStream(context.getDatabasePath(DbLogic.DATABASE_NAME)).use {
//						output.write(buffer, 0, input.read(buffer))
//					}


//					val buffer = ByteArray(1024)
//					var length = 0
//					while((input.read(buffer).also({ length = it })) > 0) {
//						output.write(buffer, 0, length)
//					}
					input.close()
					output.close()
					NativeLink.resetSql(SQLite(context))
					val intent = Intent(activity, Activity_main::class.java)
					startActivity(intent)
					requireActivity().finish()
				}
				catch(e: IOException) {
					Toast.makeText(context, getString(R.string.error_general, e.message), Toast.LENGTH_LONG).show()
				}
			}
		}
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
	
	companion object {
		private const val INTENT_CREATE_BACKUP = 99
		private const val INTENT_LOAD_BACKUP = 101
		private const val KEY_SEND_ERRORS = "btn_send_errors"
		private const val KEY_NOTIFICATIONS_BROKEN = "btn_notificationsBroken"
		private const val KEY_SEND_SYNC_NOW = "btn_sync_now"
		private const val KEY_SEND_UPDATE_STUDIES = "btn_update_studies"
		private const val KEY_CHANGE_SCHEDULES = "change_schedules_box"
		private const val KEY_BACKUP = "btn_backup"
		private const val KEY_LOAD_BACKUP = "btn_load_backup"
		private const val KEY_NEXT_NOTIFICATION = "btn_next_notification"
		private const val KEY_SHOW_WELCOME = "btn_show_welcome"
		private const val KEY_USER_ID = "user_id"
		private const val KEY_APP_VERSION = "app_version"
		private const val KEY_IS_ADMIN = "is_admin"
		
		private const val KEY_TEMP_BIG_DATA = "temp_data" //for strings that are too large to save in an intent
		
//		private const val BROADCAST: String = "broadcast"
//		private const val BROADCAST_SUCCESS: String = "broadcast_success"
//		private const val BROADCAST_MSG: String = "broadcast_msg"
//
//		fun broadcast(context: Context, success: Boolean, msg: String) {
//			val intent = Intent(BROADCAST)
//			intent.putExtra(BROADCAST_SUCCESS, success)
//			intent.putExtra(BROADCAST_MSG, msg)
//			context.sendBroadcast(intent)
//		}
		
		
		@SuppressLint("ApplySharedPref")
		fun savePreference(context: Context?, key: String, value: String) {
			val edit = PreferenceManager.getDefaultSharedPreferences(context).edit()
			edit.putString(key, value)
			edit.commit() //needs to happen immediately
		}
		fun loadPreference(context: Context, key: String): String {
			return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "") ?: ""
		}
		fun clearPreference(context: Context, key: String) {
			val edit = PreferenceManager.getDefaultSharedPreferences(context).edit()
			edit.putString(key, "")
			edit.apply()
		}
	}
}