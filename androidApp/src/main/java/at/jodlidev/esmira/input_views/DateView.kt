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
class DateView(context: Context) : TextElView(context, R.layout.view_input_date) {
	private val inputElement: Element_DateTimeInput = findViewById(R.id.input)
	@SuppressLint("SimpleDateFormat")
	private val format = SimpleDateFormat("yyyy-MM-dd")
	
	init {
		inputElement.setListener(object : Element_DateTimeInput.OnChangeListener {
			override fun onChanged(date: Calendar) {
				if(isBound) {
					input.value = format.format(date.time)
				}
			}
		})
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		isBound = false //in case this view was reused
		
		if(input.value.isNotEmpty()) {
			try {
				val date = format.parse(input.value) ?: Date()
				inputElement.setTimestamp(date.time)
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