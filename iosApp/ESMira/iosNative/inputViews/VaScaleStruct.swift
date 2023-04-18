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
			TextStruct(viewModel: self.viewModel)
			
			HStack {
				Text(self.viewModel.input.leftSideLabel)
					.font(.system(size: 14))
					.multilineTextAlignment(.leading)
				Spacer()
				Text(self.viewModel.input.rightSideLabel)
					.font(.system(size: 14))
					.multilineTextAlignment(.trailing)
			}
			
			CustomSliderView(value: self.$viewModel.value)
		}
	}
}
