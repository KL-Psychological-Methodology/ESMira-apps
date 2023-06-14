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
		
		for choice in self.viewModel.input.listChoices {
			r.append(ActionSheet.Button.default(Text(choice)) {
				self.viewModel.value = choice
			})
		}
		r.append(ActionSheet.Button.cancel())
		return r
	}
	
	var body: some View {
		VStack(alignment: self.viewModel.input.asDropDown ? .center : .leading) {
			if(self.viewModel.input.asDropDown) {
				DefaultButton(
					self.viewModel.value.isEmpty ? "please_select" : self.viewModel.value,
					action: {
						self.dropdownShown = true
					}
				)
					.actionSheet(isPresented: self.$dropdownShown) {
						ActionSheet(title: Text("please_select"), buttons: self.createDropDown())
					}
			}
			else {
				ForEach(self.viewModel.input.listChoices, id: \.self) { choice in
					RadioButtonView(state: self.$value, label: choice, value: choice) { value in
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
