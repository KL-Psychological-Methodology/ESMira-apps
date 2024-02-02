//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct ListMultipleStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	@State var checkedList: [Bool] = []
	
	var body: some View {
		VStack(alignment: .leading) {
			if(self.checkedList.count == self.viewModel.input.listChoices.indices.count) {
				ForEach(self.viewModel.input.listChoices.indices, id: \.self) { i in
					CheckBoxView(label: self.viewModel.input.listChoices[i], state: self.$checkedList[i]) { checked in
						var export = ""
						var dictionary = Dictionary<String, String>()

							for j in self.checkedList.indices {
								let key = self.viewModel.input.listChoices[j]
								dictionary[key] = self.checkedList[j] ? "1" : "0" // Old format
								dictionary[String(j+1)] = self.checkedList[j] ? "1" : "0" // New format
							}

							export = self.viewModel.input.listChoices.indices.filter({checkedList[$0]}).map({self.viewModel.input.listChoices[$0]}).joined(separator: ", ")
						self.viewModel.setAdditionalValue(value: export, additionalValues: dictionary)
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


