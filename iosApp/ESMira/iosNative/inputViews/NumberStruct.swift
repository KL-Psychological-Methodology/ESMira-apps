//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct NumberStruct: View {
//	@Binding var value: String
//	let input: Input
//	
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			TextField("", text: self.$value)
//				.keyboardType(self.input.numberHasDecimal ? .decimalPad : .numberPad)
//				.padding()
//				.background(Color.black.opacity(0.1))
//		}
//	}
//}

struct NumberStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			TextField("", text: self.$viewModel.value)
				.keyboardType(self.viewModel.input.numberHasDecimal ? .decimalPad : .numberPad)
				.overlay(
					Divider()
						.frame(height: 1)
						.background(Color.accentColor), alignment: .bottom
				)
				.padding()
				.background(Color.black.opacity(0.1))
		}
	}
}
