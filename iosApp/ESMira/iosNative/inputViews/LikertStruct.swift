//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode
	
struct LikertStruct: View {
	@ObservedObject var viewModel: InputViewModel
	@State var value: String = ""

	private func drawLikertHorizontal() -> some View {
		let startValue = self.viewModel.input.useCustomStart ? self.viewModel.input.customStart : 1;
		return VStack {
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
				ForEach(startValue...(startValue + self.viewModel.input.likertSteps - 1), id: \.self) { index in
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

	private func drawLikertVertical() -> some View {
		let startValue = self.viewModel.input.useCustomStart ? self.viewModel.input.customStart : 1;
		return VStack {
			Text(self.viewModel.input.leftSideLabel)
				.font(.system(size: 14))
				.multilineTextAlignment(.center)
			VStack(alignment: .center, spacing: 15) {
				ForEach(startValue...(startValue + self.viewModel.input.likertSteps - 1), id: \.self) { index in
					HStack(alignment: .center) {
						RadioButtonView(state: self.$value, label: "", value: String(index)) { value in
							self.viewModel.value = value}
					}
				}
			}
			Text(self.viewModel.input.rightSideLabel)
				.font(.system(size: 14))
				.multilineTextAlignment(.center)
		}
		.onAppear {
			self.value = self.viewModel.value
		}
	}
	
	var body: some View {
		if (self.viewModel.input.vertical) {
			drawLikertVertical()
		} else {
			drawLikertHorizontal()
		}
	}
}
