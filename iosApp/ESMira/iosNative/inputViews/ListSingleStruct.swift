//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct ListSingleStruct: View {
	@ObservedObject var viewModel: InputViewModel
	@State var value: String = "" //only used in RadioButton version
	
	@State var dropdownShown: Bool = false
	
	func createDropDown() -> [ActionSheet.Button] {
		var r = [ActionSheet.Button]()
		
		for (i, choice) in self.viewModel.input.listChoices.enumerated() {
			let actualValue = self.viewModel.input.forceInt ? String(i+1) : choice
			r.append(ActionSheet.Button.default(Text(choice)) {
				self.viewModel.value = actualValue
			})
		}
		r.append(ActionSheet.Button.cancel())
		return r
	}
	
	func getDropdownLabel() -> String {
		var value = ""
		if(self.viewModel.input.forceInt) {
			if let i = Int(self.viewModel.value) {
				value = self.viewModel.input.listChoices[i-1]
			}
		}
		else {
			value = self.viewModel.value
		}
		return value.isEmpty ? "please_select" : value
	}
	
	var body: some View {
		VStack(alignment: self.viewModel.input.asDropDown ? .center : .leading) {
			if(self.viewModel.input.asDropDown) {
				DefaultButton(
					self.getDropdownLabel(),
					action: {
						self.dropdownShown = true
					}
				)
					.actionSheet(isPresented: self.$dropdownShown) {
						ActionSheet(title: Text("please_select"), buttons: self.createDropDown())
					}
			}
			else {
				ForEach(Array(self.viewModel.input.listChoices.enumerated()), id: \.element) { i, choice in
					let actualValue = self.viewModel.input.forceInt ? String(i+1) : choice
					RadioButtonView(state: self.$value, label: choice, value: actualValue) { value in
						self.viewModel.value = value
					}
				}
				.onAppear {
					self.value = self.viewModel.value
				}
			}
		}
	}
}
