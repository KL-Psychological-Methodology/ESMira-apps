//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct DateStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		DateWindowView(value: self.$viewModel.value)
	}
}
