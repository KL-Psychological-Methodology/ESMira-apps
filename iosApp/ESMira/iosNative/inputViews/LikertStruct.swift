//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode
	
struct LikertStruct: View {
	@ObservedObject var viewModel: InputViewModel
	@State var value: String = ""
	
	var body: some View {
		VStack {
			HStack(alignment: .top) {
				Text(self.viewModel.input.leftSideLabel)
					.font(.system(size: 14))
					.multilineTextAlignment(.leading)
					.frame(maxWidth: 125, alignment: .leading)
				Spacer()
				Text(self.viewModel.input.rightSideLabel)
					.font(.system(size: 14))
					.frame(width: 125, alignment: .trailing)
					.multilineTextAlignment(.trailing)
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
