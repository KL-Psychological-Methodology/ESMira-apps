//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct ListMultipleStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	@State var checkedList: [Bool] = []
	@State var otherText = ""
	@State var otherSelected = false
	
	
	
	var body: some View {
		
		let saveChoices = { (checked: Bool) -> () in
			var export = ""
			var dictionary = Dictionary<String, String>()
			
			for j in self.checkedList.indices {
				let key = self.viewModel.input.listChoices[j]
				dictionary[key] = self.checkedList[j] ? "1" : "0" // Old format
				dictionary[String(j+1)] = self.checkedList[j] ? "1" : "0" // New format
			}
			
			var selectedList = self.viewModel.input.listChoices.indices.filter({checkedList[$0]}).map({self.viewModel.input.listChoices[$0]})
			
			if self.viewModel.input.other {
				dictionary["other"] = self.otherSelected ? "1" : "0"
				dictionary["other_text"]  = self.otherSelected ? self.otherText : ""
				if self.otherSelected {
					selectedList.append(String(NSLocalizedString("option_other", comment: "")))
				}
			}
			
			export = selectedList.joined(separator: ", ")
			self.viewModel.setAdditionalValue(value: export, additionalValues: dictionary)
			
		}
		
		VStack(alignment: .leading) {
			if(self.checkedList.count == self.viewModel.input.listChoices.indices.count) {
				HStack {
					Spacer()
					VStack(alignment: .leading) {
						ForEach(self.viewModel.input.listChoices.indices, id: \.self) { i in
							CheckBoxView(label: self.viewModel.input.listChoices[i], state: self.$checkedList[i], callback: saveChoices)
						}
						if self.viewModel.input.other {
							CheckBoxView(label: String(NSLocalizedString("option_other", comment: "")), state: self.$otherSelected, callback: saveChoices)
						}
					}
					Spacer()
				}
				if self.otherSelected {
					TextField("option_other", text: Binding(get: { self.otherText }, set: {
						otherText = $0
						saveChoices(true)
					}))
					.padding()
					.border(Color("Outline"))
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
