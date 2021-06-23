//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct LikertStruct: View {
//	@Binding var value: String
//	let input: Input
//	
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			
//			HStack {
//				Text(self.input.leftSideLabel)
//				Spacer()
//				Text(self.input.rightSideLabel)
//			}
//			HStack {
//				ForEach(1...self.input.likertSteps, id: \.self) { index in
//					RadioButtonView(state: self.$value, label: "", value: String(index))
//				}
//			}
//		}
//	}
//}

struct LikertStruct: View {
	@ObservedObject var viewModel: InputViewModel
	@State var value: String = ""
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			HStack {
				Text(self.viewModel.input.leftSideLabel)
				Spacer()
				Text(self.viewModel.input.rightSideLabel)
			}
			HStack {
				ForEach(1...self.viewModel.input.likertSteps, id: \.self) { index in
					RadioButtonView(state: self.$value, label: "", value: String(index)) { value in
						self.viewModel.value = value
					}
				}
			}
		}
		.onAppear {
			self.value = self.viewModel.value
		}
	}
}
