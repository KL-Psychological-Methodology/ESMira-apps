//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct BinaryStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			
			HStack {
				Button(action: {
					self.viewModel.value = "0"
				}) {
					HStack {
						Image(systemName: self.viewModel.value == "0" ? "largecircle.fill.circle" : "circle")
							.resizable()
							.aspectRatio(contentMode: .fit)
							.frame(width: 20, height: 20)
							.foregroundColor(Color("PrimaryDark"))
						Text(self.viewModel.input.leftSideLabel)
							.padding()
					}
					.frame(maxWidth: UIScreen.main.bounds.width/2 - 20)
					.border(Color("Outline"))
				}
				
				Button(action: {
					self.viewModel.value = "1"
				}) {
					HStack {
						Text(self.viewModel.input.rightSideLabel)
							.padding()
						Image(systemName: self.viewModel.value == "1" ? "largecircle.fill.circle" : "circle")
							.resizable()
							.aspectRatio(contentMode: .fit)
							.frame(width: 20, height: 20)
							.foregroundColor(Color("PrimaryDark"))
					}
					.frame(maxWidth: UIScreen.main.bounds.width/2 - 20)
					.border(Color("Outline"))
				}
			}
			Spacer()
		}
	}
}
