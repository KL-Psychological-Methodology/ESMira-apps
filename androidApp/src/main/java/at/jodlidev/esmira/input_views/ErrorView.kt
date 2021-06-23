package at.jodlidev.esmira.input_views

import android.content.Context
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 16.04.2019.
 */
class ErrorView(context: Context) : TextElView(context, R.layout.view_input_error) {
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		super.bindData(input, questionnaire)
		descEl.text = "${context.getString(R.string.error_input)}: ${input.type}"
	}
}