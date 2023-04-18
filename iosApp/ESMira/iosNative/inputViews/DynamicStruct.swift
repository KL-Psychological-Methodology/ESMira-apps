//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode


struct DynamicStruct: View {
	@ObservedObject var viewModel: InputViewModel
	@Binding var readyCounter: Int
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			InputView(input: self.viewModel.input.getDynamicInput(), readyCounter: self.$readyCounter)
		}
	}
}
