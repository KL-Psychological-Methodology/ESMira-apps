package at.jodlidev.esmira.input_views

import android.content.Context
import at.jodlidev.esmira.Element_DateTimeInput
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input
import java.util.*

/**
 * Created by JodliDev on 16.04.2019.
 */
class DateOldView(context: Context) : TextElView(context, R.layout.view_input_date) {
	private val inputElement: Element_DateTimeInput = findViewById(R.id.input)
	
	init {
		inputElement.setListener(object : Element_DateTimeInput.OnChangeListener {
			override fun onChanged(date: Calendar) {
				if(isBound)
					input.value = inputElement.getTimestamp().toString() //TODO: change to date string
			}
		})
	}
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		isBound = false //in case this view was reused
		
		if(input.value.isNotEmpty()) {
			try {
				val timestamp = input.value.toLong()
				if(timestamp != 0L)
					inputElement.setTimestamp(timestamp)
			}
			catch(e: Exception) {
				println(e.message)
			}
		}
		else
			inputElement.setTimestamp(null)
		
		super.bindData(input, questionnaire)
	}
}