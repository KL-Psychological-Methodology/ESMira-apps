package at.jodlidev.esmira.input_views

import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 13.08.2020.
 */
interface AndroidInputViewInterface {
	fun bindData(input: Input, questionnaire: Questionnaire)
}