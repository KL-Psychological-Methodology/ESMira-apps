package at.jodlidev.esmira.input_views

import android.content.Context
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 16.04.2019.
 */
class LikertView(context: Context) : TextElView(context, R.layout.view_input_likert) {
	private val radioBox: RadioGroup = findViewById(R.id.list_box_radio)
	private val leftEl: TextView = findViewById(R.id.left)
	private val rightEl: TextView = findViewById(R.id.right)
	
	init {
		radioBox.setOnCheckedChangeListener { _, _ ->
			if(isBound) {
				val selection = radioBox.checkedRadioButtonId
				input.value = if(selection == -1 || findViewById<View?>(selection) == null) "" else findViewById<View>(selection).tag.toString()
			}
		}
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		isBound = false //in case this view was reused
		radioBox.removeAllViews()
		
		leftEl.text = HtmlHandler.fromHtml(input.leftSideLabel)
		rightEl.text = HtmlHandler.fromHtml(input.rightSideLabel)
		val steps = input.likertSteps
		
		val s = input.value
		for(i in 1..steps) {
			val radio = RadioButton(context)
			radio.isFocusable = false
			radio.id = ViewCompat.generateViewId()
			radio.tag = i
			if(s == i.toString()) {
				radio.isSelected = true
				radio.isChecked = true
			}
			radioBox.addView(radio)
		}
		
		
		super.bindData(input, questionnaire)
	}
}