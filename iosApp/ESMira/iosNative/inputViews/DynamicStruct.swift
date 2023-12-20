//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode


struct DynamicStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		InputView(input: self.viewModel.input.getDynamicInput())
	}
}
