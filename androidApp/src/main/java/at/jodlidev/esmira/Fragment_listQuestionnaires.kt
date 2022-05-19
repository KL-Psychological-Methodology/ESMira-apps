package at.jodlidev.esmira

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by JodliDev on 09.04.2019.
 */
class Fragment_listQuestionnaires : Base_fragment() {
	private class ListAdapter internal constructor(f: Fragment_listQuestionnaires, studies: List<Study>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
		private lateinit var data: MutableList<Item>
		private val f: WeakReference<Fragment_listQuestionnaires> = WeakReference(f)
		
		private class Item {
			var type: Int
			lateinit var study: Study
			lateinit var questionnaire: Questionnaire
			
			constructor(study: Study) {
				type = TYPE_STUDY
				this.study = study
			}
			
			constructor(questionnaire: Questionnaire) {
				type = TYPE_QUESTIONNAIRE
				this.questionnaire = questionnaire
			}
			
			constructor() {
				type = TYPE_EMPTY
			}
		}
		init {
			load(studies)
		}
		
		private fun load(studies: List<Study>) {
			data = ArrayList()
			for(s: Study in studies) {
				add(s)
				val questionnaires = s.availableQuestionnaires
				if(questionnaires.isEmpty())
					addEmpty()
				else {
					for(g: Questionnaire in questionnaires) {
						add(g)
					}
				}
			}
			
			val fragment = f.get() ?: return
			if(studies.isEmpty()) {
				val infoEmptyList: TextView = fragment.infoEmptyList
				infoEmptyList.text = fragment.getString(R.string.info_no_studies_joined)
				infoEmptyList.visibility = View.VISIBLE
			}
		}
		
		class ViewHolderStudy constructor(vG: ViewGroup) : RecyclerView.ViewHolder(vG) {
			var title: TextView = vG.findViewById(R.id.text1)
			var btnMore: View = vG.findViewById(R.id.btn_more)
		}
		
		class ViewHolderQuestionnaire constructor(vG: View) : RecyclerView.ViewHolder(vG) {
			var title: TextView = vG as TextView

		}
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
			val vH: RecyclerView.ViewHolder
			when(viewType) {
				TYPE_STUDY -> {
					vH = ViewHolderStudy(LayoutInflater.from(parent.context).inflate(R.layout.item_header_options, parent, false) as ViewGroup)
					vH.btnMore.setOnClickListener(object : View.OnClickListener {
						override fun onClick(v: View) {
							val item: Item = data[v.tag as Int]
							if(f.get() == null || item.type != TYPE_STUDY)
								return
							val context: Context = v.context
							val study: Study = item.study

							val popup = PopupMenu(v.context, v)
							val menu: Menu = popup.menu
							popup.menuInflater.inflate(R.menu.study_popup, menu)
							if(study.informedConsentForm.isEmpty())
								menu.findItem(R.id.informed_consent).isEnabled = false
							if(study.contactEmail.isEmpty())
								menu.findItem(R.id.contact_researcher).isEnabled = false
							else
								menu.findItem(R.id.contact_researcher).title = context.getString(R.string.android_contact_email, study.contactEmail)

							popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
								override fun onMenuItemClick(menuItem: MenuItem): Boolean {
									when(menuItem.itemId) {
										R.id.contact_researcher -> {
											val intent = Intent(Intent.ACTION_SEND)
											intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(study.contactEmail))
											intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.android_email_study_subject, study.title))
											intent.type = "plain/text"
											try {
												v.context.startActivity(intent)
											}
											catch(e: Exception) {
												Toast.makeText(context, R.string.error_no_email_client, Toast.LENGTH_LONG).show()
												return false
											}
										}
										R.id.informed_consent -> if(study.informedConsentForm.isNotEmpty())
											MaterialAlertDialogBuilder(context, R.style.AppTheme_ActivityDialog)
												.setTitle(R.string.informed_consent)
												.setMessage(study.informedConsentForm)
												.setPositiveButton(android.R.string.ok, null).show()
										R.id.leave_study -> {
											MaterialAlertDialogBuilder(context, R.style.AppTheme_ActivityDialog)
													.setTitle(R.string.dialogTitle_leave_study)
													.setMessage(R.string.dialogDesc_leave_study)
													.setPositiveButton(R.string.leave) { _, _ ->
														study.leave()
														reload()
													}
												.setNegativeButton(android.R.string.cancel, null).show()
											return false
										}
										else ->
											return false
									}
									return true
								}
							})
							popup.show()
						}
					})
				}
				TYPE_QUESTIONNAIRE -> {
					vH = ViewHolderQuestionnaire(LayoutInflater.from(parent.context).inflate(R.layout.item_text, parent, false))
					vH.itemView.setOnClickListener(object : View.OnClickListener {
						override fun onClick(v: View) {
							val item: Item = data[v.tag as Int]
							if(f.get() == null || item.type != TYPE_QUESTIONNAIRE)
								return
							val b = Bundle()
							b.putLong(Fragment_questionnaireDetail.KEY_QUESTIONNAIRE, item.questionnaire.id)
							f.get()!!.goToAsSub(Activity_main.SITE_QUESTIONNAIRE_DETAIL, b)
						}
					})
				}
				TYPE_EMPTY ->
					return ViewHolderQuestionnaire(LayoutInflater.from(parent.context).inflate(R.layout.item_text_center_small, parent, false))
				else ->
					return ViewHolderQuestionnaire(LayoutInflater.from(parent.context).inflate(R.layout.item_text_center_small, parent, false))
			}
			return vH
		}
		
		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
			val item: Item = data[position]
			when(item.type) {
				TYPE_STUDY -> {
					(holder as ViewHolderStudy).title.text = item.study.title
					holder.btnMore.tag = position
				}
				TYPE_QUESTIONNAIRE -> {
					(holder as ViewHolderQuestionnaire).title.text = item.questionnaire.title
					holder.itemView.tag = position
				}
				TYPE_EMPTY ->
					(holder as ViewHolderQuestionnaire).title.setText(R.string.info_no_questionnaires)
				else ->
					(holder as ViewHolderQuestionnaire).title.setText(R.string.info_no_questionnaires)
			}
		}
		
		override fun getItemViewType(position: Int): Int {
			return data[position].type
		}
		
		override fun getItemCount(): Int {
			return data.size
		}
		
		fun add(study: Study) {
			data.add(Item(study))
			notifyItemInserted(data.size - 1)
		}
		
		fun add(questionnaire: Questionnaire) {
			data.add(Item(questionnaire))
			notifyItemInserted(data.size - 1)
		}
		
		fun addEmpty() {
			data.add(Item())
			notifyItemInserted(data.size - 1)
		}
		
		fun reload() {
			load(DbLogic.getJoinedStudies())
			notifyDataSetChanged()
		}
		
		companion object {
			private const val TYPE_STUDY: Int = 1
			private const val TYPE_QUESTIONNAIRE: Int = 2
			private const val TYPE_EMPTY: Int = 3
		}
	}
	
	private val timer = Timer()
	private lateinit var adapter: ListAdapter
	private lateinit var infoEmptyList: TextView
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_list_questionnaires, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		infoEmptyList = rootView.findViewById(R.id.info_emptyList)
		val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
		val studies: List<Study> = DbLogic.getJoinedStudies()
		adapter = ListAdapter(this, studies)
		recyclerView.adapter = adapter
		if(studies.isEmpty()) {
			infoEmptyList.text = getString(R.string.info_no_studies_joined)
			infoEmptyList.visibility = View.VISIBLE
		}
		rootView.findViewById<Button>(R.id.btn_add_study).setOnClickListener {
//			goToAsSub(Activity_main.SITE_CONNECT_SERVER)
			context?.let { it1 -> Activity_WelcomeScreen.start(it1, true) }
		}
		
		setTitle(R.string.questionnaires)
		
		AlarmBox.registerReceiver(requireContext()) {
			adapter.reload()
		}
		timer.scheduleAtFixedRate(object : TimerTask() {
			override fun run() {
				activity?.runOnUiThread {
					adapter.reload()
				}
			}
		}, 10000, 10000)
	}
	
	override fun onResume() {
		super.onResume()
		(activity as AppCompatActivity?)!!.supportActionBar!!.setTitle(R.string.questionnaires)
		if(this::adapter.isInitialized)
			adapter.reload()
	}
}