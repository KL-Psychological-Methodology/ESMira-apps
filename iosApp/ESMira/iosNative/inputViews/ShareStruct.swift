//
// Created by JodliDev on 21.04.23.
//

import Foundation
import SwiftUI
import sharedCode
import Combine

struct ShareStruct: View {
	@ObservedObject var viewModel: InputViewModel
		
	var body: some View {
		VStack(alignment: .center) {
			DefaultIconButton(icon: "square.and.arrow.up", label: "open_url") {
				UIApplication.shared.open(URL(string: viewModel.input.getFilledUrl())!, options: [:])
				viewModel.value = "1"
			}
		}
	}
}
