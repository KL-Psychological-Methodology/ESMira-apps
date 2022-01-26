//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

//struct ListSingleStruct: View {
//	@Binding var value: String
//	@State var dropdownShown: Bool = false
//	let input: Input
//	
//	func createDropDown() -> [ActionSheet.Button] {
//		var r = [ActionSheet.Button]()
//		
//		for choice in self.input.listChoices {
//			r.append(ActionSheet.Button.default(Text(choice)) {
//				self.value = choice
//			})
//		}
//		return r
//	}
//	
//	var body: some View {
//		VStack(alignment: self.input.asDropDown ? .center : .leading) {
//			TextStruct(input: self.input)
//			if(self.input.asDropDown) {
//				Button(action: {
//					self.dropdownShown = true
//				}) {
//					if(value.isEmpty) {
//						Text("please_select").bold().padding()
//					}
//					else {
//						Text(value).bold().padding()
//					}
//				}
//					.foregroundColor(Color("Accent"))
//					.actionSheet(isPresented: self.$dropdownShown) {
//						ActionSheet(title: Text("please_select"), buttons: self.createDropDown())
//					}
//			}
//			else {
//				ForEach(self.input.listChoices, id: \.self) { choice in
//					RadioButtonView(state: self.$value, label: choice, value: choice)
//				}
//			}
//		}
//	}
//}


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
		return r
	}
	
	var body: some View {
		VStack(alignment: self.viewModel.input.asDropDown ? .center : .leading) {
			TextStruct(viewModel: self.viewModel)
			if(self.viewModel.input.asDropDown) {
				Button(action: {
					self.dropdownShown = true
				}) {
					if(self.viewModel.value.isEmpty) {
						Text("please_select").bold().padding()
					}
					else {
						Text(self.viewModel.value).bold().padding()
					}
				}
				.foregroundColor(Color("Accent"))
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
