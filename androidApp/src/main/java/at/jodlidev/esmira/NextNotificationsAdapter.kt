package at.jodlidev.esmira

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.DueDateFormatter
import at.jodlidev.esmira.sharedCode.data_structure.Alarm

class NextNotificationsAdapter internal constructor(context: Context, private val alarms: List<Alarm>, private val exactDate: Boolean) : RecyclerView.Adapter<NextNotificationsAdapter.ViewHolder>() {
	private val dueDateFormatter = if(exactDate) DueDateFormatter()
	else DueDateFormatter(
		soonString = context.getString(R.string.soon),
		todayString = context.getString(R.string.today),
		tomorrowString = context.getString(R.string.tomorrow),
		inXDaysString = context.getString(R.string.in_x_days)
	)
	
	class ViewHolder constructor(vG: ViewGroup) : RecyclerView.ViewHolder(vG) {
		var title: TextView = vG.findViewById(R.id.title)
		var text: TextView = vG.findViewById(R.id.text)
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val vG: ViewGroup = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_horizontal_title_text, parent, false) as ViewGroup
		return ViewHolder(vG)
	}
	
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val alarm: Alarm = alarms[position]
		val actionTrigger = alarm.actionTrigger
		if(!actionTrigger.hasNotifications())
			return
		holder.title.text = actionTrigger.questionnaire.title + ":"
		
		holder.text.text = dueDateFormatter.get(alarm.timestamp)
		holder.itemView.tag = position
	}
	
	override fun getItemCount(): Int {
		return alarms.size
	}
	
	companion object {
		private const val ONE_DAY: Int = 1000 * 60 * 60 * 24
	}
}