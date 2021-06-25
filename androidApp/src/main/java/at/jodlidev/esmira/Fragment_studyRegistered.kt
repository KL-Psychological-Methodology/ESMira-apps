package at.jodlidev.esmira

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Alarm
import at.jodlidev.esmira.sharedCode.data_structure.Study

/**
 * Created by JodliDev on 11.04.2019.
 */
class Fragment_studyRegistered : Base_fragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_study_registered, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val activity: FragmentActivity = activity ?: return
		val b: Bundle? = arguments
		if(b == null) {
			activity.finish()
			return
		}
		val id: Long = b.getLong(KEY_STUDY_ID, 0)
		if(id == 0L) {
			activity.finish()
			return
		}
		val study: Study = DbLogic.getStudy(id) ?: return
		if(study.postInstallInstructions.isNotEmpty())
			rootView.findViewById<TextView>(R.id.instructions).text = HtmlHandler.fromHtml(study.postInstallInstructions)
		rootView.findViewById<View>(R.id.btn_complete).setOnClickListener {
			val intent = Intent(context, Activity_main::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			startActivity(intent)
			activity.finish()
		}
		if(study.hasSchedules()) {
			if(study.hasEditableSchedules()) {
				rootView.findViewById<View>(R.id.btn_change_schedules).setOnClickListener {
					Activity_editSchedules.start(requireContext(), study.id)
				}
			}
			else
				rootView.findViewById<View>(R.id.btn_change_schedules).visibility = View.GONE
			
			if(study.hasNotifications()) {
				val alarms: List<Alarm> = DbLogic.getNextAlarms(study.id)
				val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
				recyclerView.adapter = NextNotificationsAdapter(requireContext(), alarms, false)
			}
			else
				rootView.findViewById<View>(R.id.schedules_info).visibility = View.GONE
		}
		else {
			rootView.findViewById<View>(R.id.btn_change_schedules).visibility = View.GONE
			rootView.findViewById<View>(R.id.schedules_info).visibility = View.GONE
		}
	}
	
	companion object {
		const val KEY_STUDY_ID: String = "study_id"
	}
}