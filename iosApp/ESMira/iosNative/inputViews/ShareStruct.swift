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
                let urlString = viewModel.input.getFilledUrl()
                guard let url = URL(string: urlString) else {
                    ErrorBox.Companion().warn(title: "ShareStruct", msg: "\(urlString) is not a valid URL!")
                    return
                }
                UIApplication.shared.open(url, options: [:])
				viewModel.value = String((Int(viewModel.value) ?? 0) + 1)
			}
		}
	}
}
