package at.jodlidev.esmira

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Message
import at.jodlidev.esmira.sharedCode.data_structure.Study
import java.lang.ref.WeakReference


/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_messages : Base_fragment() {
	private class ListAdapter constructor(
		f: Fragment_messages,
		private val messages: List<Message>,
		private val sendMessagesAllowed: Boolean
	) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
		class MsgHolder constructor(vG: View) : RecyclerView.ViewHolder(vG) {
			var content: TextView = vG.findViewById(R.id.content)
			var sent: TextView = vG.findViewById(R.id.sent)
		}
		class FooterHolder constructor(vG: View, f: WeakReference<Fragment_messages>) : RecyclerView.ViewHolder(vG) {
			init {
				val btn = vG.findViewById<Button>(R.id.button1)
				btn.text = vG.context.getString(R.string.send_message_to_researcher)
				btn.setOnClickListener {
					f.get()?.goToNewMessage()
				}
			}
		}
		
		private val f: WeakReference<Fragment_messages> = WeakReference(f)
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
			return when(viewType) {
				FOOTER ->
					FooterHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_button, parent, false), f)
				else ->
					MsgHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false))
			}
		}
		
		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
			if(position != messages.size) {
				val msgHolder = holder as MsgHolder
				val m = messages[position]
				msgHolder.content.text = m.content
				msgHolder.sent.text = NativeLink.formatDateTime(m.sent)
				
				val lp = LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT
				)
				
				val backgroundDrawable = if(m.fromServer) {
					lp.setMargins(20, 10, 150, 5)
					if(m.isNew) {
						m.markAsRead()
						R.drawable.message_from_server_unread
					}
					else
						R.drawable.message_from_server
				}
				else {
					lp.setMargins(150, 10, 20, 5)
					R.drawable.message_from_client
				}
				holder.itemView.layoutParams = lp
				holder.itemView.background =
					ContextCompat.getDrawable(holder.itemView.context, backgroundDrawable)
			}
		}
		
		override fun getItemViewType(position: Int): Int {
			return if(position == messages.size) FOOTER else NORMAL
		}
		
		override fun getItemCount(): Int {
			return if(sendMessagesAllowed) messages.size + 1 else messages.size
		}
		
		companion object {
			private const val NORMAL = 1
			private const val FOOTER = 2
		}
	}
	
	private lateinit var study: Study

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val arguments: Bundle = arguments ?: return null

		study = DbLogic.getStudy(arguments.getLong(KEY_STUDY_ID)) ?: return null
		return inflater.inflate(R.layout.basic_list, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val messages: List<Message> = DbLogic.getMessages(study.id)
		val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
		
		val linearLayoutManager = LinearLayoutManager(activity)
		linearLayoutManager.stackFromEnd = true
		recyclerView.layoutManager = linearLayoutManager
		
		recyclerView.adapter = ListAdapter(this, messages, study.sendMessagesAllowed)
		
		setTitle(R.string.messages)
	}
	
	private fun goToNewMessage() {
		val b = Bundle()
		b.putLong(Fragment_messageNew.KEY_STUDY_ID, study.id)
		goToAsSub(Activity_main.SITE_MESSAGE_NEW, b)
	}
	
	
	override fun onPause() {
		super.onPause()
		activity.let { a -> (a as Activity_main).updateNavigationBadges() }
		
	}
	
	companion object {
		const val KEY_STUDY_ID: String = "study_id"
	}
}