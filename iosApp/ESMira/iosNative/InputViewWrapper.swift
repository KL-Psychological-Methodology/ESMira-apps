//
// Created by JodliDev on 12.08.20.
//

import SwiftUI
import sharedCode


class InputViewWrapper: InputViewInterface {
	@Binding var value: String
	
	init(value: Binding<String>) {
		self._value = value
	}
	
	func getValue() -> String? {
		(self.value == "") ? nil : self.value
	}
	
	func isEmpty() -> Bool {
		self.value == ""
	}
	
	func setValue(s: String?) {
		self.value = s ?? ""
	}
	
}

class InputViewModel: ObservableObject {
	let input: Input
	@Published var value: String {
		didSet {
			input.setValue(value: self.value, additionalValues: nil)
		}
	}
	
	func setAdditionalValue(value: String, additionalValues: Dictionary<String, String>) {
		input.setValue(value: self.value, additionalValues: additionalValues)
		self.value = value
		
	}
	
	func setFilePath(filePath: String) {
		input.setFile(filePath: filePath, dataType: .image)
	}
	
	@Binding var readyCounter: Int
	@Published var isReady: Bool = false {
		willSet {
			if(!self.isReady) {
				self.readyCounter += 1
			}
		}
	}
	
	init(_ input: Input, readyCounter: Binding<Int>) {
		self.input = input
		self.value = input.getValue()
		self._readyCounter = readyCounter
	}
}

struct InputView: View {
	let input: Input
	@Binding var readyCounter: Int
	
	@ObservedObject var viewModel: InputViewModel
	
	init(input: Input, readyCounter: Binding<Int>) {
		self._viewModel = ObservedObject(initialValue: InputViewModel(input, readyCounter: readyCounter))
		self.input = input
		self._readyCounter = readyCounter
	}
	
	func getInput() -> some View {
		switch (input.type) {
			case Input.TYPES.text:
				return AnyView(TextStruct(viewModel: self.viewModel))
			case Input.TYPES.binary:
				return AnyView(BinaryStruct(viewModel: self.viewModel))
			case Input.TYPES.date:
				return AnyView(DateStruct(viewModel: self.viewModel))
			case Input.TYPES.dynamicInput:
				return AnyView(DynamicStruct(viewModel: self.viewModel, readyCounter: self.$readyCounter))
			case Input.TYPES.image:
				return AnyView(ImageStruct(viewModel: self.viewModel))
			case Input.TYPES.likert:
				return AnyView(LikertStruct(viewModel: self.viewModel))
			case Input.TYPES.listSingle:
				return AnyView(ListSingleStruct(viewModel: self.viewModel))
			case Input.TYPES.listMultiple:
				return AnyView(ListMultipleStruct(viewModel: self.viewModel))
			case Input.TYPES.number:
				return AnyView(NumberStruct(viewModel: self.viewModel))
			case Input.TYPES.photo:
			   return AnyView(PhotoStruct(viewModel: self.viewModel))
			case Input.TYPES.textInput:
				return AnyView(TextInputStruct(viewModel: self.viewModel))
			case Input.TYPES.time:
				return AnyView(TimeStruct(viewModel: self.viewModel))
			case Input.TYPES.vaScale:
				return AnyView(VaScaleStruct(viewModel: self.viewModel))
			case Input.TYPES.video:
				return AnyView(VideoStruct(viewModel: self.viewModel))
			default:
				return AnyView(ErrorStruct(viewModel: self.viewModel))
		}
	}
	
	var body: some View {
		getInput()
	}
}
