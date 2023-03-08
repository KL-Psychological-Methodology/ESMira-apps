package at.jodlidev.esmira.views.inputViews

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import at.jodlidev.esmira.androidNative.ChooseInputView
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire

/**
 * Created by JodliDev on 23.01.2023.
 */


@Composable
fun DynamicView(input: Input) {
	ChooseInputView(input.questionnaire, input.getDynamicInput(), Modifier)
}