//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct ListSingleStruct: View {
	@ObservedObject var viewModel: InputViewModel
	@State var value: String = "" //only used in RadioButton version and for saving other text
	
	@State var dropdownShown: Bool = false
	@State var otherText: String = ""
	@State var otherSelected: Bool = false

	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
		self.otherText = self.viewModel.input.getAdditional(key: "other") ?? ""
		if self.viewModel.input.forceInt {
			self.otherSelected = self.viewModel.input.getValue() == String(self.viewModel.input.listChoices.count + 1)
		} else {
			self.otherSelected = self.viewModel.input.getValue() == "other"
		}
	}
	
	func createDropDown() -> [ActionSheet.Button] {
		var r = [ActionSheet.Button]()
		
		for (i, choice) in self.viewModel.input.listChoices.enumerated() {
			let actualValue = self.viewModel.input.forceInt ? String(i+1) : choice
			r.append(ActionSheet.Button.default(Text(choice)) {
				self.viewModel.setAdditionalValue(value: actualValue, additionalValues: ["other": ""])
				self.otherSelected = false
			})
		}
		if self.viewModel.input.other {
			let actualValue = self.viewModel.input.forceInt ? String(self.viewModel.input.listChoices.count + 1) : "other"
			r.append(ActionSheet.Button.default(Text("option_other")) {
				self.value = actualValue
				self.otherSelected = true
				self.viewModel.setAdditionalValue(value: actualValue, additionalValues: ["other": otherText])
			})
		}
		r.append(ActionSheet.Button.cancel())
		return r
	}
	
	func getDropdownLabel() -> String {
		var value = ""
		if(self.viewModel.input.forceInt) {
			if let i = Int(self.viewModel.value) {
				if i == self.viewModel.input.listChoices.count + 1 {
					value = String(NSLocalizedString("option_other", comment: ""))
				} else {
					value = self.viewModel.input.listChoices[i-1]
				}
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
				if self.otherSelected {
					VStack(alignment: .leading){
						TextField("option_other", text: Binding(get: { self.otherText }, set: {
							otherText = $0
							self.viewModel.setAdditionalValue(value: value, additionalValues: ["other": otherText])
						}))
							.padding()
							.border(Color("Outline"))
					}
				}
			}
			else {
				HStack(alignment: .center){
					Spacer()
					VStack(alignment: .leading) {
						ForEach(Array(self.viewModel.input.listChoices.enumerated()), id: \.element) { i, choice in
							let actualValue = self.viewModel.input.forceInt ? String(i+1) : choice
							RadioButtonView(state: self.$value, label: choice, value: actualValue) { value in
								self.viewModel.setAdditionalValue(value: value, additionalValues: ["other": ""])
								self.otherSelected = false
							}
						}
						.onAppear {
							self.value = self.viewModel.value
						}
						if self.viewModel.input.other {
							let actualValue = self.viewModel.input.forceInt ? String(self.viewModel.input.listChoices.count + 1) : "other"
							RadioButtonView(state: self.$value, label: String(NSLocalizedString("option_other", comment: "")), value: actualValue) { value in
								self.viewModel.setAdditionalValue(value: value, additionalValues: ["other": otherText])
								self.otherSelected = true
							}
						}
					}
					Spacer()
				}
				if self.otherSelected {
					VStack(alignment: .leading){
						TextField("option_other", text: Binding(get: { self.otherText }, set: {
							otherText = $0
							self.viewModel.setAdditionalValue(value: value, additionalValues: ["other": otherText])
						}))
							.padding()
							.border(Color("Outline"))
					}
				}
			}
		}
	}
}
