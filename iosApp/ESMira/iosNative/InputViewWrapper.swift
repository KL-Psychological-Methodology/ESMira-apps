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
			self.input.value = self.value
		}
	}
	
//	@Published var isReady: Bool = false
	
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
		self.value = input.value
		self._readyCounter = readyCounter
	}
}

struct InputView: View {
	let input: Input
	let questionnaire: sharedCode.Questionnaire
	@Binding var readyCounter: Int
//	@State var value: String = ""
//	@Binding var value: String
	
	@ObservedObject var viewModel: InputViewModel
	
	init(input: Input, questionnaire: sharedCode.Questionnaire, readyCounter: Binding<Int>) {
		self._viewModel = ObservedObject(initialValue: InputViewModel(input, readyCounter: readyCounter))
		self.input = input
		self.questionnaire = questionnaire
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
			case Input.TYPES.dateOld:
				return AnyView(DateStruct_old(viewModel: self.viewModel))
			case Input.TYPES.dynamicInput:
				return AnyView(DynamicStruct(viewModel: self.viewModel, questionnaire: questionnaire, readyCounter: self.$readyCounter))
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
			   return AnyView(PhotoStruct(viewModel: self.viewModel, studyId: self.questionnaire.studyId))
			case Input.TYPES.textInput:
				return AnyView(TextInputStruct(viewModel: self.viewModel))
			case Input.TYPES.time:
				return AnyView(TimeStruct(viewModel: self.viewModel))
			case Input.TYPES.timeOld:
				return AnyView(TimeStruct_old(viewModel: self.viewModel))
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
//			.environmentObject(self.viewModel)
		
		
		
//		ZStack {
//			getInput()
//				.environmentObject(self.viewModel)
//			if(!self.viewModel.isReady) {
//				Rectangle().foregroundColor(Color(.white)).frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
//			}
//		}
		
//		if(self.viewModel.isReady) {
//			getInput()
//				.environmentObject(self.viewModel)
//				.padding()
//		}
//		else {
//
//			getInput()
//				.environmentObject(self.viewModel)
//				.frame(height: 0)
//				.hidden()
//		}
//		getInput()
//			.environmentObject(self.viewModel)
//			.opacity(self.viewModel.isReady ? 1 : 0)
	}
}
