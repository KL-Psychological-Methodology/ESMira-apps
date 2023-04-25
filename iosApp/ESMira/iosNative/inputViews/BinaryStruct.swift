//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct BinaryStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	
	var body: some View {
		HStack {
			DefaultIconButton(
				icon: self.viewModel.value == "0" ? "largecircle.fill.circle" : "circle",
				label: self.viewModel.input.leftSideLabel,
				maxWidth: UIScreen.main.bounds.width/2 - 20
			) {
				self.viewModel.value = "0"
			}
			
			
			DefaultIconRightButton(
				icon: self.viewModel.value == "1" ? "largecircle.fill.circle" : "circle",
				label: self.viewModel.input.rightSideLabel,
				maxWidth: UIScreen.main.bounds.width/2 - 20
			) {
				self.viewModel.value = "1"
			}
		}
	}
}
