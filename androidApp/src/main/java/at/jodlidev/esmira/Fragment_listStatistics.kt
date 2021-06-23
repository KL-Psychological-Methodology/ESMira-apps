package at.jodlidev.esmira

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.DataSet
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_listStatistics constructor() : Base_fragment() {
	private lateinit var adapter: ListAdapter
	
	private class ListAdapter constructor(f: Fragment_listStatistics, studies: List<Study>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
		class ViewHolder constructor(vG: ViewGroup) : RecyclerView.ViewHolder(vG) {
			var title: TextView = vG.findViewById(R.id.text1)
			var btnMore: View = vG.findViewById(R.id.btn_more)

		}
		
		private var studies: List<Study>
		private val f: WeakReference<Fragment_listStatistics> = WeakReference(f)

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val vG: ViewGroup = LayoutInflater.from(parent.context).inflate(R.layout.item_text_options, parent, false) as ViewGroup
			vG.setOnClickListener(object : View.OnClickListener {
				override fun onClick(v: View) {
					if(f.get() == null)
						return
					val s: Study = studies[v.tag as Int]
					f.get()!!.openStatistic(s, false)
				}
			})
			vG.findViewById<View>(R.id.btn_more).setOnClickListener(object : View.OnClickListener {
				override fun onClick(v: View) {
					val popup = PopupMenu(v.context, v)
					val menu: Menu = popup.menu
					popup.menuInflater.inflate(R.menu.statistic_popup, menu)
					popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
						override fun onMenuItemClick(menuItem: MenuItem): Boolean {
							when(menuItem.itemId) {
								R.id.title_delete_statistics -> MaterialAlertDialogBuilder(v.context, R.style.AppTheme_ActivityDialog)
										.setTitle(R.string.dialogTitle_delete_statistics)
										.setMessage(R.string.dialogDesc_delete_statistics)
										.setPositiveButton(R.string.delete_
										) { _, _ ->
											val s: Study = studies[v.tag as Int]
											s.delete()
											studies = DbLogic.getStudiesWithStatistics()
											notifyDataSetChanged()
										}
									.setNegativeButton(android.R.string.cancel, null).show()
								else -> return false
							}
							return true
						}
					})
					popup.show()
				}
			})
			return ViewHolder(vG)
		}
		
		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			val s: Study = studies[position]
			holder.title.text = s.title
			if(s.state == Study.STATES.Joined)
				holder.btnMore.visibility = View.GONE
			holder.itemView.tag = position
			holder.btnMore.tag = position
		}
		
		override fun getItemCount(): Int {
			return studies.size
		}
		
		init {
			this.studies = studies
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.basic_list, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
		val studies: List<Study> = DbLogic.getStudiesWithStatistics()
		if(studies.isEmpty()) {
			val infoEmptyList: TextView = rootView.findViewById(R.id.info_emptyList)
			infoEmptyList.text = getString(R.string.info_no_statistics)
			infoEmptyList.visibility = View.VISIBLE
		}
		else if(studies.size == 1 && studies[0].state == Study.STATES.Joined) {
			openStatistic(studies[0], true)
		}
		else {
			adapter = ListAdapter(this, studies)
			recyclerView.adapter = adapter
		}
		
		setTitle(R.string.statistics)
	}
	
	private fun openStatistic(s: Study, skip: Boolean) {
		if(s.state == Study.STATES.Joined)
			DataSet.createShortDataSet(DataSet.TYPE_STATISTIC_VIEWED, s)
		val b = Bundle()
		b.putLong(Fragment_statisticsRoot.KEY_STUDY_ID, s.id)
		if(skip)
			goToAsRoot(Activity_main.SITE_STATISTICS_DETAIL, b)
		else
			goToAsSub(Activity_main.SITE_STATISTICS_DETAIL, b)
	}
}