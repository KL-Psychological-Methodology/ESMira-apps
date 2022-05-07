package at.jodlidev.esmira

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.permissionBoxes.APermissionBox
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Created by JodliDev on 28.04.2019.
 */
class NotificationsPermission(context: Context, attrs: AttributeSet?, num: Int) :
	APermissionBox(context, attrs, num, R.layout.element_setup_notifications, R.string.notifications, R.string.notification_setup_desc) {
	enum class ProgressPos {
		Disabled,
		ModelSettings,
		AskModelSettings,
		
		SendNotification,
		AskNotifications,
		
		AdditionalSettings,
		
		IsSetup,
		FailedToSetup
	}
	
	constructor(context: Context, num: Int) : this(context, null, num)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	
	private val fixedPosition: ProgressPos?
	
	private val finishedSettings = BooleanArray(POWERMANAGER_INTENTS.size)
	
	private var container: ViewGroup
	private var progressPos = ProgressPos.ModelSettings
	
	var setupEndWasReached = false
	
	
	init {
		container = findViewById(R.id.container)
		
		val theme = context.theme.obtainStyledAttributes(null, R.styleable.Element_SetupNotifications, 0, 0)
		val fixedPositionString: Int
		try {
			fixedPositionString = theme.getInt(R.styleable.Element_SetupNotifications_fixedPosition, -1)
		}
		finally {
			theme.recycle()
		}
		
		fixedPosition = if(fixedPositionString != -1) ProgressPos.values()[fixedPositionString] else null
		skipToProgress(fixedPosition ?: ProgressPos.Disabled)
	}
	
	override fun enable(continueCallback : () -> Unit) {
		super.enable(continueCallback)
		skipToProgress(ProgressPos.ModelSettings)
	}
	
	
	private fun createView(res: Int): View {
		val view = View.inflate(context, res, null)
		container.addView(view)
		return view
	}
	
	private fun setupOnClick(view: View, res: Int, listener: OnClickListener) {
		view.findViewById<Button>(res).setOnClickListener(listener)
	}
	private fun setupProgressButton(view: View, resId: Int, target: ProgressPos) {
		setupOnClick(view, resId, OnClickListener {
			animateProgress(target)
		})
	}
	
	private fun animateProgress(pos: ProgressPos) {
		if(fixedPosition != null)
			return
		val oldAnimRes: Int
		val newAnimRes: Int
		if(progressPos.ordinal >= pos.ordinal) {
			oldAnimRes = R.anim.vanish_to_0
			newAnimRes = R.anim.appear_from_0
		}
		else {
			oldAnimRes = R.anim.slide_to_left
			newAnimRes = R.anim.slide_from_right
		}
		
		progressPos = pos
		
		if(container.childCount == 0) {
			drawProgress()
		}
		else {
			val view = container.getChildAt(0)
			val anim = AnimationUtils.loadAnimation(context, oldAnimRes)
			anim.setAnimationListener(object : Animation.AnimationListener {
				override fun onAnimationStart(animation: Animation?) {}
				override fun onAnimationEnd(animation: Animation?) {
					view.post {
						container.removeView(view)
					}
				}
				
				override fun onAnimationRepeat(animation: Animation?) {}
			})
			view.startAnimation(anim)
			view.tag = "animation"
			drawProgress(newAnimRes)
		}
	}
	private fun skipToProgress(pos: ProgressPos, newAnimRes: Int = -1) {
		for(i in 0 until container.childCount) {
			val view = container.getChildAt(i)
			if(view.tag != "animation")
				container.removeView(view)
		}
		progressPos = pos
		drawProgress(newAnimRes)
	}
	
	private fun drawProgress(newAnimRes: Int = -1) {
		val activity = context as AppCompatActivity
		val view: View
		when(progressPos) {
			ProgressPos.Disabled -> {
				return
			}
			ProgressPos.ModelSettings -> {
				val excluded = BooleanArray(POWERMANAGER_INTENTS.size)
				var componentIndex = getComponentIndex()
				if(componentIndex == -1) {
					if(fixedPosition == null) { //we want to show R.id.no_settings when fixedPosition != null
						skipToProgress(ProgressPos.SendNotification)
						return
					}
				}
				view = createView(R.layout.view_notifications_model_settings)
				
				if(fixedPosition != null) {
					findViewById<View>(R.id.instructions).visibility = GONE
					if(componentIndex == -1) {
						findViewById<View>(R.id.no_settings).visibility = View.VISIBLE //we have to do that after createView()
						return
					}
				}
				
				val buttonCompleteList = ArrayList<Boolean>()
				val buttonContainer = view.findViewById<ViewGroup>(R.id.model_settings_container)
				do {
					val intentIndex = componentIndex
					val completeIndex = buttonCompleteList.size
					buttonCompleteList.add(false)
					excluded[intentIndex] = true
//					val button = MaterialButton(ContextThemeWrapper(context, R.style.AppTheme_DefaultButton))
					val button = MaterialButton(context)
					button.text = if(buttonCompleteList.size >= 2) resources.getString(R.string.open_device_specific_settings_numbered, buttonCompleteList.size) else resources.getString(R.string.open_device_specific_settings)
					button.setOnClickListener {
						button.icon = ContextCompat.getDrawable(context, R.drawable.ic_success_green_24dp)
						buttonCompleteList[completeIndex] = true
						val entry = POWERMANAGER_INTENTS[intentIndex]
						val intent = Intent().setComponent(entry.first)
						
						val dialogView = LayoutInflater.from(context).inflate(if(entry.second == 0) R.layout.device_layout_general else entry.second, null)
						MaterialAlertDialogBuilder(context, R.style.AppTheme_ActivityDialog)
							.setView(dialogView)
							.setCancelable(false)
							.setPositiveButton(android.R.string.ok) { _, _ ->
								try {
									activity.startActivityForResult(intent, 0)
								}
								catch(e: SecurityException) { //Most likely cause: "Permission Denial: starting Intent not exported from uid XXX" - so we will fall back to the stock Android settings
									try {
										activity.startActivityForResult(Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings")), 0)
									}
									catch(e2: Exception) {
										ErrorBox.error("Setup_notification", "starting general settings Intent failed", e2)
									}
								}
								catch(e: Exception) {
									ErrorBox.error("Setup_notification", "starting model settings Intent failed", e)
								}
								
								
								for(value in buttonCompleteList) {
									if(!value)
										return@setPositiveButton
								}
								animateProgress(ProgressPos.AskModelSettings)
							}.show()
					}
					
					buttonContainer.addView(button)
					
					componentIndex = getComponentIndex(excluded)
				} while(componentIndex != -1)
			}
			ProgressPos.AskModelSettings -> {
				view = createView(R.layout.view_notifications_ask_model)
				
				setupProgressButton(view, R.id.btn_yes, ProgressPos.SendNotification)
				setupProgressButton(view, R.id.btn_no, ProgressPos.ModelSettings)
			}
			ProgressPos.SendNotification -> {
				view = createView(R.layout.view_notifications_send_notification)
				
				setupOnClick(view, R.id.btn_test_notification, OnClickListener {
					NativeLink.notifications.remove(8675309)
					NativeLink.notifications.fire(
						context.getString(R.string.notification_test_name),
						context.getString(R.string.notification_test_desc),
						8675309
					)
					animateProgress(ProgressPos.AskNotifications)
				})
				
			}
			ProgressPos.AskNotifications -> {
				view = createView(R.layout.view_notifications_ask_notification)
				setupProgressButton(view, R.id.btn_yes, ProgressPos.IsSetup)
				setupProgressButton(view, R.id.btn_no, ProgressPos.AdditionalSettings)
				
			}
			ProgressPos.AdditionalSettings -> {
				view = createView(R.layout.view_notifications_additional_settings)
				
				setupOnClick(view, R.id.btn_open_sound_settings, OnClickListener {
					val notificationIntent = Intent()
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//						intent.setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
//						intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel);
						notificationIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
						notificationIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
						if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
							notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					}
					else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						notificationIntent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
						notificationIntent.putExtra("app_package", context.packageName)
						notificationIntent.putExtra("app_uid", context.applicationInfo.uid)
					}
					else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
						notificationIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
						notificationIntent.addCategory(Intent.CATEGORY_DEFAULT)
						notificationIntent.data = Uri.parse("package:" + context.packageName)
					}
					activity.startActivityForResult(notificationIntent, ACTIVITY_RESULT)
				})
				setupOnClick(view, R.id.btn_open_notification_settings, OnClickListener {
					activity.startActivityForResult(Intent(Settings.ACTION_SOUND_SETTINGS), ACTIVITY_RESULT)
				})
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					setupOnClick(view, R.id.btn_open_doNotDisturb_settings, OnClickListener {
						var dndIntent = Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
						if(intentExists(dndIntent))
							activity.startActivityForResult(dndIntent, ACTIVITY_RESULT)
						else {
							dndIntent = Intent("android.settings.ZEN_MODE_SETTINGS")
							if(intentExists(dndIntent))
								activity.startActivityForResult(dndIntent, ACTIVITY_RESULT)
						}
					})
				}
				else
					view.findViewById<Button>(R.id.btn_open_doNotDisturb_settings).isEnabled = false
				
				finishedSettings.fill(false)
				
				if(fixedPosition != null) {
					findViewById<View>(R.id.btn_ignore).visibility = GONE
					findViewById<View>(R.id.btn_try_again).visibility = GONE
				}
				else {
					setupProgressButton(view, R.id.btn_ignore, ProgressPos.FailedToSetup)
					setupProgressButton(view, R.id.btn_try_again, ProgressPos.ModelSettings)
				}
			}
			ProgressPos.IsSetup -> {
				NativeLink.notifications.remove(8675309)
				container.visibility = GONE

//				view = createView(R.layout.view_notifications_is_setup)
				setupEndWasReached = true
				setFinished()
				return
			}
			ProgressPos.FailedToSetup -> {
				view = createView(R.layout.view_notifications_failed)
				
				setupProgressButton(view, R.id.btn_try_again, ProgressPos.ModelSettings)
				MaterialAlertDialogBuilder(context, R.style.AppTheme_ActivityDialog)
					.setMessage(R.string.info_notification_setup_failed)
					.setPositiveButton(R.string.ok_, null)
					.show()
				setupEndWasReached = true
				setFinished()
			}
		}
		if(newAnimRes != -1)
			view.startAnimation(AnimationUtils.loadAnimation(context, newAnimRes))
		
	}
	
	
	
	
	private fun getComponentIndex(excluded: BooleanArray = BooleanArray(POWERMANAGER_INTENTS.size)): Int {
		for((i, entry) in POWERMANAGER_INTENTS.withIndex()) {
			if(!excluded[i] && intentExists(Intent().setComponent(entry.first))) {
				return i
			}
		}
		return -1
	}
	
	fun getComponentString(): String {
		val i = getComponentIndex()
		return if(i == -1) "" else POWERMANAGER_INTENTS[i].first.toString()
	}
	
	private fun intentExists(intent: Intent): Boolean {
		val list = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
		return list.size > 0
	}
	
	public override fun onSaveInstanceState(): Parcelable {
		val s = SavedState(super.onSaveInstanceState())
		s.progress = progressPos.name
		return s
	}
	
	public override fun onRestoreInstanceState(state: Parcelable) {
		val s = state as SavedState
		super.onRestoreInstanceState(s.superState)
		skipToProgress(ProgressPos.valueOf(s.progress))
	}
	
	private class SavedState : BaseSavedState {
		var progress: String = ""
		
		constructor(parcel: Parcel) : super(parcel) {
			progress = parcel.readString() ?: ProgressPos.ModelSettings.name
		}
		
		constructor (parcelable: Parcelable?) : super(parcelable)
		
		override fun writeToParcel(parcel: Parcel, flags: Int) {
			super.writeToParcel(parcel, flags)
			parcel.writeString(progress)
		}
		
		companion object CREATOR : Parcelable.Creator<SavedState> {
			override fun createFromParcel(parcel: Parcel): SavedState {
				return SavedState(parcel)
			}
			
			override fun newArray(size: Int): Array<SavedState?> {
				return arrayOfNulls(size)
			}
		}
	}
	
	companion object {
		//Thanks to:
		//https://stackoverflow.com/questions/48166206/how-to-start-power-manager-of-all-android-manufactures-to-enable-background-and
		//https://github.com/judemanutd/AutoStarter
		
		//if 0, then R.layout.device_layout_general is loaded instead:
		private val POWERMANAGER_INTENTS = arrayOf(
			Pair(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"), R.layout.device_layout_esmi),					//Xiaomi
			Pair(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"), R.layout.device_layout_huawei), 	//Huawei P20; this should be the same as below but it should not cause a securityException?
//			Pair(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"), R.layout.device_layout_huawei),			//Huawei P20; this should be the same as above but needs an additional permission (com.huawei.permission.external_app_settings.USE_COMPONENT) when run in non-debug-mode. Or maybe it does not work at all?
//			Pair(ComponentName("com.zte.powersavemode", "com.zte.powersavemode.appspowersave.AppRuleAutoRunActivity"), R.layout.device_layout_zte_blade_3),						//ZTE Blade A3; leads to: Permission Denial: starting Intent not exported from uid 1000; manual way (german): Einstellungen > Akku > App-Energiesparverwaltung > (Tab) Richtline > Autostart von Apps einschränken > ESMira: ZULASSEN
			Pair(ComponentName("com.android.settings", "com.android.settings.Settings\$SpecialAccessSettingsActivity"), R.layout.device_layout_zte_blade_3), 			//ZTE Blade A3
//			Pair(ComponentName("com.android.settings", "com.android.settings.SubSettings"), R.layout.device_layout_zte_blade_3),												//ZTE Blade A3; leads to: Permission Denial: starting Intent not exported from uid 1000 - but we can call "com.android.settings.Settings$SpecialAccessSettingsActivity" instead from which this activity can be opened
			Pair(ComponentName("com.sprd.heartbeatsynchronization", "com.sprd.heartbeatsynchronization.MainActivity"), R.layout.device_layout_zte_blade_l110), 		//ZTE BLADE L110; leads to: Permission Denial: starting Intent not exported from uid 10003
			Pair(ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"), 0),													//Letv
			Pair(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"), 0),											//Huawei P10
			Pair(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"), 0),									//Oppo
			Pair(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"), 0),											//Oppo
			Pair(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"), 0),														//Oppo
			Pair(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"), 0),														//Vivo
			Pair(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"), 0),															//Vivo
			Pair(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"), 0),									//Vivo
			Pair(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"), R.layout.device_layout_samsung),						//Samsung
			Pair(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"), R.layout.device_layout_samsung),						//Samsung
			Pair(ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity"), 0),
			Pair(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"), 0),
			Pair(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.powersaver.PowerSaverSettings"), 0),												//ASUS ROG
			Pair(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity"), 0),													//ASUS ROG
			Pair(ComponentName("com.evenwell.powersaving.g3", "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity"), 0),								//Nokia
			Pair(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"), 0)										//One plus
		)
		const val ACTIVITY_RESULT = 123
	}
}