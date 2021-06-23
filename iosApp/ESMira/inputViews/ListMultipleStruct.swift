//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct ListMultipleStruct: View {
//	@Binding var value: String
//	let input: Input
//	@State var checkedList: [Bool]
//	
//	init(value: Binding<String>, input: Input) {
//		self._value = value
//		self.input = input
//		
//		var a = Array(repeating: false, count: input.listChoices.count)
//		let valueString = value.wrappedValue
//		let choices = input.listChoices
//		for i in input.listChoices.indices {
//			if(valueString.contains(choices[i])) {
//				a[i] = true
//			}
//		}
//		
//		self._checkedList = State(initialValue: a)
//	}
//	
//	var body: some View {
//		VStack(alignment: .leading) {
//			TextStruct(input: self.input)
//			ForEach(self.input.listChoices.indices, id: \.self) { i in
//				CheckBoxView(label: self.input.listChoices[i], state: self.$checkedList[i]) { checked in
//					var export = ""
//					for j in self.checkedList.indices {
//						if(self.checkedList[j]) {
//							export.append(self.input.listChoices[j])
//							export.append(",")
//						}
//					}
//					self.value = export
//				}
//			}
//		}
//	}
//}

struct ListMultipleStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	@State var checkedList: [Bool] = []
	
	var body: some View {
		VStack(alignment: .leading) {
			TextStruct(viewModel: self.viewModel)
			if(self.checkedList.count == self.viewModel.input.listChoices.indices.count) {
				ForEach(self.viewModel.input.listChoices.indices, id: \.self) { i in
					CheckBoxView(label: self.viewModel.input.listChoices[i], state: self.$checkedList[i]) { checked in
						var export = ""
						for j in self.checkedList.indices {
							if(self.checkedList[j]) {
								export.append(self.viewModel.input.listChoices[j])
								export.append(",")
							}
						}
						self.viewModel.value = export
					}
				}
			}
		}
		.onAppear {
			var a = Array(repeating: false, count: self.viewModel.input.listChoices.count)
			let valueString = self.viewModel.value
			let choices = self.viewModel.input.listChoices
			for i in self.viewModel.input.listChoices.indices {
				if(valueString.contains(choices[i])) {
					a[i] = true
				}
			}

			self.checkedList = a
		}
	}
}
