package at.jodlidev.esmira.input_views

import android.content.Context
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.ViewCompat
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 16.04.2019.
 */
class ListSingleView(context: Context) : TextElView(context, R.layout.view_input_list_single) {
	private var listBoxDropdown: Spinner = findViewById(R.id.list_box_dropdown)
	private var listBoxRadio: RadioGroup = findViewById(R.id.list_box_radio)
	private var asDropDown = false
	
	init {
		isFocusable = false
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		isBound = false //in case this view was reused
		asDropDown = input.asDropDown
		
		val choices = input.listChoices
		
		if(input.asDropDown) {
			listBoxDropdown.visibility = VISIBLE
			listBoxRadio.visibility = GONE //in case this view was reused
			
			val data: MutableList<String> = choices.toMutableList()
			data.add(0, context.getString(R.string.please_select))
			
			listBoxDropdown.adapter = ArrayAdapter(context, R.layout.item_text, data)
			listBoxDropdown.setSelection(data.indexOf(input.value))
			
			listBoxDropdown.onItemSelectedListener = object : OnItemSelectedListener {
				override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
					if(isBound) {
						input.value = if(position == 0) "" else listBoxDropdown.selectedItem as String
					}
				}
				override fun onNothingSelected(parent: AdapterView<*>?) {}
			}
		}
		else {
			listBoxRadio.visibility = VISIBLE
			listBoxDropdown.visibility = GONE //in case this view was reused
			listBoxRadio.removeAllViews()
			
			val s = input.value
			for(text in choices) {
				val radio = RadioButton(context)
				radio.isFocusable = false
				radio.id = ViewCompat.generateViewId()
				radio.tag = text
				radio.text = text
				if(s == text) {
					radio.isSelected = true
					radio.isChecked = true
				}
				listBoxRadio.addView(radio)
			}
			
			listBoxRadio.setOnCheckedChangeListener { _, _ ->
				if(isBound) {
					val selection = listBoxRadio.checkedRadioButtonId
					input.value = if(selection == -1 || findViewById<View?>(selection) == null) "" else findViewById<View>(selection).tag.toString()
				}
			}
		}
		super.bindData(input, questionnaire)
	}
}