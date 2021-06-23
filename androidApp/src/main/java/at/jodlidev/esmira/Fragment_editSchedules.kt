package at.jodlidev.esmira

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.Element_DateTimeInput.OnChangeListener
import at.jodlidev.esmira.sharedCode.data_structure.SignalTime
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.snackbar.Snackbar
import java.util.*

/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_editSchedules : Base_fragment() {
	private lateinit var study: Study
	
	private class ListAdapter constructor(val signalTimes: List<SignalTime>) :
		RecyclerView.Adapter<RecyclerView.ViewHolder>() {
		
		private class ViewHolderTrigger constructor(vG: ViewGroup) : RecyclerView.ViewHolder(vG) {
			var timeStart: Element_DateTimeInput = vG.findViewById(R.id.time_start)
			var timeEnd: Element_DateTimeInput = vG.findViewById(R.id.time_end)
			var frequencyHeader: TextView = vG.findViewById(R.id.frequency_header)
			var labelHeader: TextView = vG.findViewById(R.id.label_header)
			var frequencyConnector: View = vG.findViewById(R.id.frequency_connector)
		}
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
			return ViewHolderTrigger(
				LayoutInflater.from(parent.context)
					.inflate(R.layout.item_schedule, parent, false) as ViewGroup
			)
		}
		
		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
			val signalTime = signalTimes[position]
			
			
			val context: Context = holder.itemView.context
			val triggerHolder: ViewHolderTrigger = holder as ViewHolderTrigger
			triggerHolder.timeStart.setTimestamp(signalTime.getStart())
			triggerHolder.timeStart.setListener(object : OnChangeListener {
				override fun onChanged(date: Calendar) {
					signalTime.setStart(date.timeInMillis)
					trySave(triggerHolder, signalTime)
				}
			})
			triggerHolder.labelHeader.text = signalTime.label + ":"
			if(signalTime.random) {
				triggerHolder.frequencyHeader.text =
					if(signalTime.schedule.dailyRepeatRate == 1)
						context.getString(
							R.string.colon_frequency_header_daily,
							signalTime.frequency
						)
					else
						context.getString(
							R.string.colon_frequency_header_multiple_days,
							signalTime.frequency,
							signalTime.schedule.dailyRepeatRate
						)
				triggerHolder.timeEnd.setTimestamp(signalTime.getEnd())
				triggerHolder.timeEnd.setListener(object : OnChangeListener {
					override fun onChanged(date: Calendar) {
						signalTime.setEnd(date.timeInMillis)
						trySave(triggerHolder, signalTime)
					}
				})
			}
			else {
				if(signalTime.schedule.dailyRepeatRate == 1)
					triggerHolder.frequencyHeader.text =
						context.getString(R.string.colon_frequency_header_one_time_daily)
				else
					triggerHolder.frequencyHeader.text = context.getString(
						R.string.colon_frequency_header_one_time_multiple_days,
						signalTime.schedule.dailyRepeatRate
					)
				triggerHolder.timeEnd.visibility = View.GONE
				triggerHolder.frequencyConnector.visibility = View.GONE
			}
		}
		
		override fun getItemCount(): Int {
			return signalTimes.size
		}
		
		private fun trySave(triggerHolder: ViewHolderTrigger, signalTime: SignalTime) {
			//TODO: make sure the timespan is inside a timeframe that can be set on the server
			val view: View = triggerHolder.itemView
			
			if(signalTime.isFaulty()) {
				Snackbar.make(
					view,
					R.string.error_schedule_time_window_too_small,
					Snackbar.LENGTH_SHORT
				).show()
				view.findViewById<TextView>(R.id.label_header)
					.setCompoundDrawablesWithIntrinsicBounds(
						null,
						null,
						ContextCompat.getDrawable(view.context, R.drawable.ic_failed_red_24dp),
						null
					)
			}
			else {
				view.findViewById<TextView>(R.id.label_header)
					.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
			}
		}
	}
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val arguments: Bundle = arguments ?: return null
		
		study = (activity as Activity_editSchedules).studies[arguments.getInt(KEY_POSITION)] //we keep the data in the activity in case the fragment gets destroyed by the ViewPager
		
		return inflater.inflate(R.layout.basic_list, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
		recyclerView.adapter = ListAdapter(study.editableSignalTimes)
	}
	
	fun save(resetSchedules: Boolean): Int {
		return if(study.saveSchedules(resetSchedules))
			if(resetSchedules)
				SAVE_STATE_POSTPONED
			else
				SAVE_STATE_DONE_NOW
		else
			SAVE_STATE_ERROR
//		return if(DbLogic.saveSchedulesFromActionTriggers(adapter.triggers, resetSchedules))
//			if(resetSchedules)
//				SAVE_STATE_POSTPONED
//			else
//				SAVE_STATE_DONE_NOW
//		else
//			SAVE_STATE_ERROR
	}
	
	
	companion object {
		const val KEY_POSITION: String = "study_id"
		const val SAVE_STATE_ERROR: Int = 0
		const val SAVE_STATE_POSTPONED: Int = 1
		const val SAVE_STATE_DONE_NOW: Int = 2
	}
}