package at.jodlidev.esmira.input_views

import android.content.Context
import android.view.ViewGroup
import android.widget.CheckBox
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input
import java.util.*

/**
 * Created by JodliDev on 16.04.2019.
 */
class ListMultipleView(context: Context) : TextElView(context, R.layout.view_input_list_multiple) {
	private val listBox: ViewGroup = findViewById(R.id.list_box)
	private var checkBoxes: MutableList<CheckBox> = ArrayList()

	private fun getValue(): String {
		val s = StringBuilder()
		for(checkBox in checkBoxes) {
			if(checkBox.isChecked) {
				s.append(checkBox.text)
				s.append(',')
			}
		}
		return s.toString()
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		super.bindData(input, questionnaire)
		listBox.removeAllViews()
		
		checkBoxes = ArrayList()
		val s = input.value
		val choices = input.listChoices
		for(value in choices) {
			val checkBox = CheckBox(context)
			checkBox.isFocusable = false
			checkBox.text = value
			if(s.contains(checkBox.text))
				checkBox.isChecked = true
			checkBox.setOnCheckedChangeListener { _, _ ->
				input.value = getValue()
			}
			checkBoxes.add(checkBox)
			listBox.addView(checkBox)
		}
	}
}