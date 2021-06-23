package at.jodlidev.esmira

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.data_structure.Study

/**
 * Created by JodliDev on 09.04.2019.
 */
class Fragment_listNewStudies : Base_fragment() {
	private class ListAdapter(
		private val activity: Activity_addStudy,
		private val studies: List<Study>
	) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

		internal class ViewHolder constructor(vG: ViewGroup) : RecyclerView.ViewHolder(vG) {
			var title: TextView = vG.findViewById(R.id.title)
			var email: TextView = vG.findViewById(R.id.email)

		}
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val vG: ViewGroup = LayoutInflater.from(parent.context).inflate(R.layout.item_header_email, parent, false) as ViewGroup
			vG.setOnClickListener { v -> activity.openStudy(v.tag as Int) }
			return ViewHolder(vG)
		}
		
		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			val s: Study = studies[position]
			holder.email.text = s.contactEmail
			holder.title.text = s.title
			holder.itemView.tag = position
		}
		
		override fun getItemCount(): Int {
			return studies.size
		}

	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_list_new_studies, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val a = (activity as Activity_addStudy)
		val studies = a.studies
		if(studies.isEmpty()) {
			val accessKey = a.accessKey
			val infoEmptyList: TextView = rootView.findViewById(R.id.info_emptyList)
			infoEmptyList.text = if(accessKey.isEmpty()) getString(R.string.info_no_studies_noAccessKey) else getString(R.string.info_no_studies_withAccessKey, accessKey)
			infoEmptyList.visibility = View.VISIBLE
		}
		
		rootView.findViewById<View>(R.id.btn_back).setOnClickListener {
			activity?.onBackPressed()
		}
		val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerView)
		recyclerView.adapter = ListAdapter(activity as Activity_addStudy, studies)
	}
}