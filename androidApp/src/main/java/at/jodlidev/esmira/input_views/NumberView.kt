package at.jodlidev.esmira.input_views

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 16.04.2019.
 */
class NumberView(context: Context) : TextElView(context, R.layout.view_input_number) {
	private var inputElement: EditText = findViewById(R.id.input)
	
	init {
		inputElement.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
			override fun afterTextChanged(s: Editable) {
				if(isBound)
					input.value = inputElement.text.toString()
			}
		})
	}
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		isBound = false //in case this view was reused
		inputElement.inputType = if(input.numberHasDecimal) InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_NUMBER
		
		inputElement.setText(input.value)
		super.bindData(input, questionnaire)
	}
}