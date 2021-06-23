package at.jodlidev.esmira.input_views

import android.annotation.SuppressLint
import android.content.Context
import at.jodlidev.esmira.Element_DateTimeInput
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by JodliDev on 16.04.2019.
 */
class TimeView(context: Context) : TextElView(context, R.layout.view_input_time) {
	private val inputElement: Element_DateTimeInput = findViewById(R.id.input)
	@SuppressLint("SimpleDateFormat")
	private val format = SimpleDateFormat("HH:mm")
	private var saveMinutes: Boolean = false
	
	init {
		inputElement.setListener(object : Element_DateTimeInput.OnChangeListener {
			override fun onChanged(date: Calendar) {
				if(isBound) {
					input.value = if(saveMinutes) (date.get(Calendar.HOUR_OF_DAY)*60 + date.get(Calendar.MINUTE)).toString() else format.format(date.time)
				}
			}
		})
	}
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		isBound = false //in case this view was reused
		saveMinutes = input.forceInt
		
		if(input.value.isNotEmpty()) {
			try {
				val timestamp: Long
				if(saveMinutes) {
					val cal = Calendar.getInstance()
					val num = input.value.toInt()
					cal[Calendar.HOUR_OF_DAY] = num / 60
					cal[Calendar.MINUTE] = num % 60
					timestamp = cal.timeInMillis
				}
				else {
					val date = format.parse(input.value) ?: Date()
					timestamp = date.time
				}
				inputElement.setTimestamp(timestamp)
			}
			catch(e: Exception) {
				ErrorBox.log("TimeView", "Could not parse ${input.name} in questionnaire ${questionnaire.title}: ${e.message}")
			}
		}
		else
			inputElement.setTimestamp(null)
		
		super.bindData(input, questionnaire)
	}
}