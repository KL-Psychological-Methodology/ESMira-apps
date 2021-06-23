package at.jodlidev.esmira

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Study
import java.lang.ref.WeakReference

/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_listMessages : Base_fragment() {
	private class ListAdapter constructor(
			f: Fragment_listMessages,
			private var studies: List<Study>
		) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
		
		class ViewHolder constructor(vG: ViewGroup) : RecyclerView.ViewHolder(vG) {
			var title: TextView = vG.findViewById(R.id.text1)
			var badge: TextView = vG.findViewById(R.id.badge1)
		}
		
		private val f: WeakReference<Fragment_listMessages> = WeakReference(f)

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val vG: ViewGroup = LayoutInflater.from(parent.context).inflate(R.layout.item_text_badge, parent, false) as ViewGroup
			vG.setOnClickListener(object : View.OnClickListener {
				override fun onClick(v: View) {
					if(f.get() == null)
						return
					val s: Study = studies[v.tag as Int]
					f.get()!!.openMessages(s, false)
				}
			})
			return ViewHolder(vG)
		}
		
		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			val s: Study = studies[position]
			holder.title.text = s.title
			holder.itemView.tag = position
			
			val count = DbLogic.countUnreadMessages(s.id)
			if(count == 0)
				holder.badge.visibility = View.GONE
			else {
				holder.badge.visibility = View.VISIBLE
				holder.badge.text = count.toString()
			}
		}
		
		override fun getItemCount(): Int {
			return studies.size
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.basic_list, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
		val studies: List<Study> = DbLogic.getStudiesForMessages()
		if(studies.size == 1)
			openMessages(studies[0], true)
		else {
			recyclerView.adapter = ListAdapter(this, studies)
		}
		
		setTitle(R.string.messages)
	}
	
	private fun openMessages(s: Study, skip: Boolean) {
		val b = Bundle()
		b.putLong(Fragment_messages.KEY_STUDY_ID, s.id)
		if(skip)
			goToAsRoot(Activity_main.SITE_MESSAGES_DETAIL, b)
		else
			goToAsSub(Activity_main.SITE_MESSAGES_DETAIL, b)
	}
}