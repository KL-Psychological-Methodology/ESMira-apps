package at.jodlidev.esmira

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import at.jodlidev.esmira.sharedCode.data_structure.Study


/**
 * Created by JodliDev on 11.04.2019.
 */
class Fragment_studyDetail : Base_fragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_study_detail, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val b: Bundle? = arguments
		if(b == null) {
			goToAsRoot(Activity_addStudy.SITE_LIST_STUDIES)
			return
		}
		
		
		val index = b.getInt(Activity_addStudy.KEY_LIST_INDEX)
		val study = (activity as Activity_addStudy).studies[index]
		
		rootView.findViewById<TextView>(R.id.title).text = study.title
		
		val emailEl = rootView.findViewById<TextView>(R.id.email)
		emailEl.text = study.contactEmail
		emailEl.setOnClickListener {
			val intent = Intent(Intent.ACTION_SEND)
			intent.putExtra(Intent.EXTRA_EMAIL, study.contactEmail)
			intent.putExtra(Intent.EXTRA_SUBJECT, context?.getString(R.string.android_email_study_subject, study.title))
			intent.type = "plain/text"
			try {
				startActivity(intent)
			}
			catch(e: Exception) {
				Toast.makeText(context, R.string.error_no_email_client, Toast.LENGTH_LONG).show()
			}
		}
		HtmlHandler.setHtml(study.studyDescription, rootView.findViewById<TextView>(R.id.desc))
		
		if(study.needsPermissionScreen()) {
			rootView.findViewById<View>(R.id.btn_permissions).setOnClickListener {
				gotoPermissions(index)
			}
		}
		else {
			val btnParticipate = rootView.findViewById<View>(R.id.btn_participate)
			btnParticipate.visibility = View.VISIBLE
			btnParticipate.setOnClickListener {
				studyParticipate(study)
			}
			rootView.findViewById<View>(R.id.btn_permissions).visibility = View.GONE
		}
		rootView.findViewById<View>(R.id.btn_back).setOnClickListener {
			activity?.onBackPressed()
		}
	}
	
	private fun gotoPermissions(index: Int) {
		val bundle = Bundle()
		bundle.putInt(Activity_addStudy.KEY_LIST_INDEX, index)
		goToAsSub(Activity_addStudy.SITE_STUDY_PERMISSIONS, bundle)
	}
	private fun studyParticipate(study: Study) {
		study.join()
		ScreenTrackingService.startService(requireContext(), true)
		val b = Bundle()
		if(study.needsJoinedScreen()) {
			activity?.let { Activity_main.start(it) }
		}
		else {
			b.putLong(Fragment_studyRegistered.KEY_STUDY_ID, study.id)
			goToAsRoot(Activity_addStudy.SITE_STUDY_REGISTERED, b)
		}
	}
}