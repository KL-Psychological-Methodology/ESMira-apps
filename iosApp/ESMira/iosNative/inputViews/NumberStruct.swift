//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct NumberStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			TextField("", text: self.$viewModel.value)
				.keyboardType(self.viewModel.input.numberHasDecimal ? .decimalPad : .numberPad)
				.frame(minWidth: 100, maxWidth: 100)
				.padding()
				.border(Color("Outline"))
		}
	}
}
