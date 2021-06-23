//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct ErrorStruct: View {
//	let input: Input
//	
//	var body: some View {
//		HStack {
//			Text("error_input").bold()
//			Text(String(describing: input.type)).bold()
//		}
//			.foregroundColor(Color.red)
//			.padding(.vertical)
//	}
//}

struct ErrorStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		HStack {
			Text("error_input").bold()
			Text(String(describing: self.viewModel.input.type)).bold()
		}
		.foregroundColor(Color.red)
		.padding(.vertical)
	}
}
