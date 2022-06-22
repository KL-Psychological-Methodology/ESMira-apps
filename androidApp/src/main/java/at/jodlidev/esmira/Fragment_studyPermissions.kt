package at.jodlidev.esmira

import android.Manifest
import android.animation.LayoutTransition
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.jodlidev.esmira.permissionBoxes.APermissionBox
import at.jodlidev.esmira.permissionBoxes.InformedConsentPermission
import at.jodlidev.esmira.permissionBoxes.AppUsagePermission
import at.jodlidev.esmira.sharedCode.data_structure.Study
import java.lang.ref.WeakReference


/**
 * Created by JodliDev on 11.04.2019.
 */
class Fragment_studyPermissions : Base_fragment() {
	private lateinit var study: Study
	private var permissionBoxes = ArrayList<WeakReference<APermissionBox>>()
	private var permissionProgress = 0
	private var currentBox: WeakReference<APermissionBox> = WeakReference(null)
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_study_permissions, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val b: Bundle? = arguments
		if(b == null) {
			goToAsRoot(Activity_addStudy.SITE_LIST_STUDIES)
			return
		}
		
		
		val index = b.getInt(Activity_addStudy.KEY_LIST_INDEX)
		study = (activity as Activity_addStudy).studies[index]
		
		
		
		var headerCount = 0
		if(study.hasInformedConsent())
			permissionBoxes.add(WeakReference(InformedConsentPermission(requireContext(), study, ++headerCount)))
		
		if(study.usesPostponedActions() || study.hasNotifications())
			permissionBoxes.add(WeakReference(NotificationsPermission(requireContext(), ++headerCount)))
		
		if(study.hasScreenOrAppTracking())
			permissionBoxes.add(WeakReference(AppUsagePermission(this, ++headerCount)))
		
		requestPermissions(arrayOf(Manifest.permission.PACKAGE_USAGE_STATS),
			Fragment_studyPermissions.REQUEST_PERMISSION
		)
		
		addPermissionBoxes(rootView)
		doProgress()
	}
	
	private fun addPermissionBoxes(rootView: View) {
		val container = rootView.findViewById<ViewGroup>(R.id.container)
		var hasPermissions = false
		for(box in permissionBoxes) {
			if(box.get() == null)
				continue;
			
			hasPermissions = true
			container.addView(box.get())
		}
		
		rootView.findViewById<View>(R.id.btn_back).setOnClickListener {
			activity?.onBackPressed()
		}
		val btnParticipate = rootView.findViewById<View>(R.id.btn_participate)
		if(hasPermissions) {
			btnParticipate.isEnabled = false
			container.layoutTransition = LayoutTransition()
		}
		btnParticipate.setOnClickListener {
			studyParticipate()
		}
	}
	
	private fun doProgress() {
		if(permissionProgress < permissionBoxes.size) {
			currentBox = permissionBoxes[permissionProgress]
			val view = currentBox.get()
			++permissionProgress
			
			if(view == null) {
				doProgress()
				return
			}
			view.enable(this::doProgress)
		}
		else {
			val rootView = view ?: return
			rootView.findViewById<View>(R.id.setup_ended).visibility = View.VISIBLE
			rootView.findViewById<View>(R.id.btn_participate).isEnabled = true
		}
	}
	
	private fun studyParticipate() {
//		val context = context ?: return

		study.join()
		ScreenTrackingService.startService(requireContext(), true)
		val b = Bundle()
		if(study.needsJoinedScreen()) {
			b.putLong(Fragment_studyRegistered.KEY_STUDY_ID, study.id)
			goToAsRoot(Activity_addStudy.SITE_STUDY_REGISTERED, b)
		}
		else {
			activity?.let { Activity_main.start(it) }
		}
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		if(requestCode == REQUEST_PERMISSION) {
			if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				currentBox.get()?.permissionGranted()
			else
				currentBox.get()?.permissionGranted(false)
		}
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if(requestCode == REQUEST_PERMISSION)
			currentBox.get()?.handleResult(resultCode)
	}
	
	companion object {
		public const val REQUEST_PERMISSION = 101
	}
}