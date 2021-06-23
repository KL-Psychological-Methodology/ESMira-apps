//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct TimeStruct: View {
//	@Binding var value: String
//	let input: Input
//	
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			DateWindowView(value: self.$value, typeMode: .time, saveMode: input.forceInt ? .timeAsMinutes : .asString)
//		}
//	}
//}

struct TimeStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			DateWindowView(value: self.$viewModel.value, typeMode: .time, saveMode: self.viewModel.input.forceInt ? .timeAsMinutes : .asString)
		}
	}
}
