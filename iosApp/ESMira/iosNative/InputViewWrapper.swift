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
	
	init(_ input: Input) {
		self.input = input
		self.value = input.getValue()
	}
}

struct InputView: View {
	let input: Input
	
	@ObservedObject var viewModel: InputViewModel
	
	init(input: Input) {
		self._viewModel = ObservedObject(initialValue: InputViewModel(input))
		self.input = input
	}
	
	func getInput() -> some View {
		switch (input.type) {
			case Input.TYPES.binary:
				return AnyView(BinaryStruct(viewModel: self.viewModel))
			case Input.TYPES.bluetoothDevices:
				return AnyView(BluetoothDevicesStruct(viewModel: self.viewModel))
			case Input.TYPES.countdown:
				return AnyView(CountdownStruct(viewModel: self.viewModel))
			case Input.TYPES.compass:
				return AnyView(CompassStruct(viewModel: self.viewModel))
			case Input.TYPES.date:
				return AnyView(DateStruct(viewModel: self.viewModel))
			case Input.TYPES.fileUpload:
				return AnyView(FileUploadStruct(viewModel: self.viewModel))
			case Input.TYPES.dynamicInput:
				return AnyView(DynamicStruct(viewModel: self.viewModel))
			case Input.TYPES.image:
				return AnyView(ImageStruct(viewModel: self.viewModel))
			case Input.TYPES.likert:
				return AnyView(LikertStruct(viewModel: self.viewModel))
			case Input.TYPES.listSingle:
				return AnyView(ListSingleStruct(viewModel: self.viewModel))
			case Input.TYPES.listMultiple:
				return AnyView(ListMultipleStruct(viewModel: self.viewModel))
			case Input.TYPES.location:
				return AnyView(LocationStruct(viewModel: self.viewModel))
			case Input.TYPES.number:
				return AnyView(NumberStruct(viewModel: self.viewModel))
			case Input.TYPES.photo:
			   return AnyView(PhotoStruct(viewModel: self.viewModel))
			case Input.TYPES.recordAudio:
			   return AnyView(RecordAudioStruct(viewModel: self.viewModel))
			case Input.TYPES.share:
			   return AnyView(ShareStruct(viewModel: self.viewModel))
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
		VStack {
			TextStruct(viewModel: self.viewModel)
			if(input.type != Input.TYPES.text) {
				getInput()
			}
		}
	}
}
