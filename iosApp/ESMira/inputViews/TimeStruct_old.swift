//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct TimeStruct_old: View {
//	@Binding var value: String
//	let input: Input
//	
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			DateWindowView(value: self.$value, typeMode: .time, saveMode: .asTimestamp)
//		}
//	}
//}

struct TimeStruct_old: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			DateWindowView(value: self.$viewModel.value, typeMode: .time, saveMode: .asTimestamp)
		}
	}
}
