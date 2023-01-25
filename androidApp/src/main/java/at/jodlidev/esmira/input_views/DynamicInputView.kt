package at.jodlidev.esmira.input_views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 17.02.2020.
 */
class DynamicInputView(context: Context) : TextElView(context, R.layout.view_input_dynamic_input) {
	private val dynamicContent: ViewGroup = findViewById(R.id.dynamic_content)
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
//		val dynamicInput = input.getDynamicInput(questionnaire)
//
//		val view: AndroidInputViewInterface = InputViewChooser.getView(context, dynamicInput.type.ordinal)
//		dynamicContent.removeAllViews()
//		dynamicContent.addView(view as View)
//
//		view.bindData(dynamicInput, questionnaire)
		super.bindData(input, questionnaire)
	}
}