//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct BinaryStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	private let activeColor = Color("PrimaryLight")
	private let inactiveColor = Color.black.opacity(0.1)
	@State private var leftColor = Color.black.opacity(0.1)
	@State private var rightColor = Color.black.opacity(0.1)
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			
			HStack {
				Button(action: {
					self.viewModel.value = "0"
					self.leftColor = self.activeColor
					self.rightColor = self.inactiveColor
				}) {
					HStack {
						Image(systemName: self.leftColor == self.activeColor ? "checkmark.circle.fill" : "circle")
							.renderingMode(.original)
							.resizable()
							.aspectRatio(contentMode: .fit)
							.frame(width: 20, height: 20)
						Text(self.viewModel.input.leftSideLabel)
							.padding()
					}
					.frame(maxWidth: UIScreen.main.bounds.width/2 - 20)
					.background(self.leftColor)
				}
				
				Button(action: {
					self.viewModel.value = "1"
					self.leftColor = self.inactiveColor
					self.rightColor = self.activeColor
				}) {
					HStack {
						Text(self.viewModel.input.rightSideLabel)
							.padding()
						Image(systemName: self.rightColor == self.activeColor ? "checkmark.circle.fill" : "circle")
							.renderingMode(.original)
							.resizable()
							.aspectRatio(contentMode: .fit)
							.frame(width: 20, height: 20)
					}
					.frame(maxWidth: UIScreen.main.bounds.width/2 - 20)
					.background(self.rightColor)
				}
			}
			Spacer()
		}
		.onAppear {
			switch(self.viewModel.value) {
			case "0":
				self.leftColor = self.activeColor
				self.rightColor = self.inactiveColor
			case "1":
				self.leftColor = self.inactiveColor
				self.rightColor = self.inactiveColor
			default:
				self.leftColor = self.inactiveColor
				self.rightColor = self.inactiveColor
			}
		}
	}
}
