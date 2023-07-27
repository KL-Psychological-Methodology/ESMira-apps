//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct VaScaleStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		VStack {
			HStack {
				Text(self.viewModel.input.leftSideLabel)
					.font(.system(size: 14))
					.multilineTextAlignment(.leading)
				Spacer()
				Text(self.viewModel.input.rightSideLabel)
					.font(.system(size: 14))
					.multilineTextAlignment(.trailing)
			}
			
			if(self.viewModel.input.showValue) {
				HStack {
					Spacer()
					Text(self.viewModel.value.isEmpty ? " " : self.viewModel.value)
					Spacer()
				}
			}
			
			CustomSliderView(value: self.$viewModel.value, maxValue: (self.viewModel.input.maxValue > 1 ? Int(self.viewModel.input.maxValue) : 100))
		}
	}
}
