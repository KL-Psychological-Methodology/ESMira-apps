//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct BinaryStruct: View {
//	private let activeColor = Color("PrimaryLight")
//	private let inactiveColor = Color.black.opacity(0.1)
//	@State private var leftColor: Color
//	@State private var rightColor: Color
//	
//	@Binding var value: String
//	let input: Input
//	
//	init(value: Binding<String>, input: Input) {
//		self._value = value
//		self.input = input
//		switch(value.wrappedValue) {
//			case "0":
//				self._leftColor = State(initialValue: self.activeColor)
//				self._rightColor = State(initialValue: self.inactiveColor)
//			case "1":
//				self._leftColor = State(initialValue: self.inactiveColor)
//				self._rightColor = State(initialValue: self.activeColor)
//			default:
//				self._leftColor = State(initialValue: self.inactiveColor)
//				self._rightColor = State(initialValue: self.inactiveColor)
//		}
//	}
//	
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			
//			HStack {
//				Button(action: {
//					self.value = "0"
//					self.leftColor = self.activeColor
//					self.rightColor = self.inactiveColor
//				}) {
//					HStack {
//						Image(systemName: self.leftColor == self.activeColor ? "checkmark.circle.fill" : "circle")
//							.renderingMode(.original)
//							.resizable()
//							.aspectRatio(contentMode: .fit)
//							.frame(width: 20, height: 20)
//						Text(self.input.leftSideLabel)
//							.padding()
//					}
//						.frame(maxWidth: UIScreen.main.bounds.width/2 - 20)
//						.background(self.leftColor)
//				}
//				
//				Button(action: {
//					self.value = "1"
//					self.leftColor = self.inactiveColor
//					self.rightColor = self.activeColor
//				}) {
//					HStack {
//						Text(self.input.rightSideLabel)
//							.padding()
//						Image(systemName: self.rightColor == self.activeColor ? "checkmark.circle.fill" : "circle")
//							.renderingMode(.original)
//							.resizable()
//							.aspectRatio(contentMode: .fit)
//							.frame(width: 20, height: 20)
//					}
//						.frame(maxWidth: UIScreen.main.bounds.width/2 - 20)
//						.background(self.rightColor)
//				}
//			}
//			Spacer()
//		}
//	}
//}

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
