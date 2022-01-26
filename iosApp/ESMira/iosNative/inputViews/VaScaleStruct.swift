//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct VaScaleStruct: View {
//	@Binding var value: String
//	let input: Input
//	
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			CustomSliderView(value: self.$value)
//		}
//	}
//}

struct VaScaleStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			
			HStack {
				Text(self.viewModel.input.leftSideLabel)
				Spacer()
				Text(self.viewModel.input.rightSideLabel)
			}
			
			CustomSliderView(value: self.$viewModel.value)
		}
	}
}
