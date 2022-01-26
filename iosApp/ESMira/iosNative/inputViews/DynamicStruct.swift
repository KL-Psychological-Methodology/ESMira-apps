//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct DynamicStruct: View {
//	@Binding var value: String
//	let input: Input
//	let dynamicInput: Input
//	let questionnaire: sharedCode.Questionnaire
//	@Binding var readyCounter: Int
//
//	init(value: Binding<String>, input: Input, questionnaire: sharedCode.Questionnaire, readyCounter: Binding<Int>) {
//		self._value = value
//		self.input = input
//		self.questionnaire = questionnaire
//		self.dynamicInput = input.getDynamicInput(questionnaire: questionnaire)
//		self._readyCounter = readyCounter
//	}
//
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			InputView(input: self.dynamicInput, questionnaire: self.questionnaire, readyCounter: self.$readyCounter)
//		}
//	}
//}

struct DynamicStruct: View {
	@ObservedObject var viewModel: InputViewModel
	let questionnaire: sharedCode.Questionnaire
	@Binding var readyCounter: Int
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			InputView(input: self.viewModel.input.getDynamicInput(questionnaire: questionnaire), questionnaire: self.questionnaire, readyCounter: self.$readyCounter)
		}
	}
}
