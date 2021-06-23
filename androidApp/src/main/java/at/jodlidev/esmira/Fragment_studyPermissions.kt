package at.jodlidev.esmira

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference


/**
 * Created by JodliDev on 11.04.2019.
 */
class Fragment_studyPermissions : Base_fragment() {
	enum class Progress {
		Consent, Notifications, Done
	}
	private var progress: Progress = Progress.Consent
	private lateinit var study: Study
	
	private var headerCount: Int = 0
	private lateinit var headerInformedConsent: WeakReference<View>
	private lateinit var headerNotifications: WeakReference<View>
	private lateinit var setupNotifications: WeakReference<Element_SetupNotifications>
	
	
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
		
		
		val container = rootView.findViewById<ViewGroup>(R.id.container)
		var hasPermissions = false
		if(study.hasInformedConsent()) {
			headerInformedConsent = createHeader(container, R.string.informed_consent, -1, false)
			
			val view = View.inflate(context, R.layout.view_informed_consent, container)
			view.setPadding(0, 0, 0, 20)
			hasPermissions = true
		}
		
		if(study.usesPostponedActions() || study.hasNotifications()) {
			headerNotifications = createHeader(container, R.string.notifications, R.string.notification_setup_desc)
			
			val setupNotifications = Element_SetupNotifications(requireContext(), null)
			this.setupNotifications = WeakReference(setupNotifications)
			container.addView(setupNotifications)
			setupNotifications.alpha = 0.3f
			hasPermissions = true
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
		
		doProgress()
	}
	
	
	private fun createHeader(container: ViewGroup, headerString: Int, msgString: Int = -1, alpha: Boolean = true): WeakReference<View> {
		val header = View.inflate(context, R.layout.item_header_whatfor, null)
		val headerText = header.findViewById<TextView>(R.id.header)
		headerText.text = "${++headerCount}. ${getString(headerString)}"
		if(alpha)
			header.alpha = 0.3f
		if(msgString != -1)
			context?.let {it1 ->
				header.findViewById<View>(R.id.btn_whatFor).setOnClickListener {
					MaterialAlertDialogBuilder(it1, R.style.AppTheme_ActivityDialog)
						.setMessage(msgString)
						.setPositiveButton(R.string.close, null)
						.show()
				}
			}
		else
			header.findViewById<View>(R.id.btn_whatFor).visibility = View.GONE
		
		container.addView(header)
		return WeakReference<View>(header)
	}
	
	private fun nextProgress() {
		if(this.progress == Progress.Done)
			return
		this.progress = Progress.values()[progress.ordinal+1]
		doProgress()
	}
	
	private fun doProgress() {
		val rootView = view ?: return
		when(progress) {
			Progress.Consent -> {
				val btnConsent = rootView.findViewById<View>(R.id.btn_consent) ?: return nextProgress()
				
				btnConsent.setOnClickListener {
					context?.let { it1 ->
						MaterialAlertDialogBuilder(it1, R.style.AppTheme_ActivityDialog)
							.setTitle(getString(R.string.informed_consent))
							.setMessage(study.informedConsentForm)
							.setPositiveButton(R.string.i_agree) { _, _ ->
								nextProgress()
								btnConsent.visibility = View.GONE
								headerInformedConsent.get()?.findViewById<View>(R.id.completed)?.visibility = View.VISIBLE
//								headerInformedConsent.get()?.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(it1, R.drawable.ic_success_green_24dp), null)
								rootView.findViewById<View>(R.id.desc_informed_consent).visibility = View.GONE
							}
							.setNegativeButton(android.R.string.cancel, null).show()
					}
				}
			}
			Progress.Notifications -> {
				if(!this::setupNotifications.isInitialized)
					return nextProgress()
				val setupNotifications = setupNotifications.get() ?: return nextProgress()
				
				headerNotifications.get()?.alpha = 1f
				setupNotifications.alpha = 1f
				setupNotifications.setListener {
					context?.let {
						headerNotifications.get()?.findViewById<View>(R.id.completed)?.visibility = View.VISIBLE
//						headerNotifications.get()
//							?.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(it, if(success) R.drawable.ic_success_green_24dp else R.drawable.ic_error_red_24dp), null)
					}
					nextProgress()
				}
			}
			Progress.Done -> {
				rootView.findViewById<View>(R.id.setup_ended).visibility = View.VISIBLE
				rootView.findViewById<View>(R.id.btn_participate).isEnabled = true
			}
		}
	}
	
	private fun studyParticipate() {
//		val context = context ?: return

		study.join()
		val b = Bundle()
		if(study.needsJoinedScreen()) {
			b.putLong(Fragment_studyRegistered.KEY_STUDY_ID, study.id)
			goToAsRoot(Activity_addStudy.SITE_STUDY_REGISTERED, b)
		}
		else {
			activity?.let { Activity_main.start(it) }
		}
	}
}