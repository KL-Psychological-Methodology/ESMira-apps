//
// Created by JodliDev on 12.08.20.
//

import SwiftUI
import sharedCode

struct QuestionnaireView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
	let questionnaire: sharedCode.Questionnaire
	let pageIndex: Int


	private let page: Page?
	private let inputs: [Input]
	@State var formStarted: Int64

	@State var nextPage = false

	@State private var action = ScrollAction.idle
	@State private var footerIsReady = false
	@State private var headerIsReady = false

	@State private var readyCounter = 0
	private let waitCounter: Int
	
	
	
	init(questionnaire: sharedCode.Questionnaire) {
		self.init(questionnaire: questionnaire, pageIndex: 0, formStarted: Int64(Date().timeIntervalSince1970 * 1000))
	}
	init(questionnaire: sharedCode.Questionnaire, pageIndex: Int, formStarted: Int64) {
		self.questionnaire = questionnaire
		self.pageIndex = pageIndex
		self._formStarted = State(initialValue: formStarted)

		if(questionnaire.pages.count == 0) {
			self.waitCounter = 0
			self.inputs = []
			self.page = nil
		}
		else {
			self.page = self.questionnaire.pages[self.pageIndex]
			let preInputs = self.page!.inputs
			var inputs: [Input] = []
			for input in preInputs {
				if(input.type !== Input.TYPES.appUsage) {
					inputs.append(input)
				}
			}
			self.inputs = inputs
			self.waitCounter = inputs.count
		}
	}
	
	private func noMissings() -> Bool {
		let errorIndex = self.questionnaire.checkQuestionnaire(pageI: Int32(self.pageIndex))
		if(errorIndex == -1) {
			return true
		}

		self.action = .toTag(tag: Int(errorIndex))
		self.appState.showToast(NSLocalizedString("error_missing_fields", comment: ""))
		return false
	}

	private func getBackgroundColor(_ i: Int) -> Color {
		return (i % 2 != 0) ? Color("ListColor1") : Color("ListColor2")
	}
	
	private func drawInnerQuestionnaire(width: CGFloat) -> some View {

		return VStack {
			if(!self.page!.header.isEmpty) {
				HtmlTextView(html: self.page!.header, isReady: self.$headerIsReady)
					.padding()
					.frame(width: width)
					.background(Color("ListColor1"))
			}

			ForEach(0..<self.inputs.count, id: \.self) { i in
				InputView(input: self.inputs[i], readyCounter: self.$readyCounter)
					.padding()
					.frame(width: width)
					.uiTag(i)
					.background(getBackgroundColor(i))

			}

			
			if(!self.page!.footer.isEmpty) {
				HtmlTextView(html: self.page!.footer, isReady: self.$footerIsReady)
					.padding()
					.frame(width: width)
					.background(getBackgroundColor(self.inputs.count))
			}
			
			VStack(alignment: .leading) {
				if(questionnaire.questionnairePageHasRequired(index: Int32(pageIndex))) {
					Text("info_required")
						.padding(10)
				}
				if(self.pageIndex < self.questionnaire.pages.count - 1) {
					NavigationLink(
						destination: QuestionnaireView(questionnaire: self.questionnaire, pageIndex: self.pageIndex + 1, formStarted: self.formStarted),
						isActive: self.$nextPage,
						label: { EmptyView() }
					)
					
					HStack {
						Spacer()
						Button(action: {
							if(self.noMissings()) {
								self.nextPage = true
							}
						}) {
							Text("continue_").bold()
							Image(systemName: "chevron.right")
						}
						Spacer()
					}
				}
				else {
					HStack {
						Spacer()
						Button(action: {
							if(self.noMissings()) {
								self.questionnaire.saveQuestionnaire(formStarted: self.formStarted)
								self.navigationState.questionnaireOpened = false
								self.navigationState.questionnaireSuccessfullOpened = true
							}
						}) {
							Image(systemName: "tray.and.arrow.down")
							Text("save").bold()
						}
						Spacer()
					}
				}
			}
				.padding(.vertical, 30)
				.background(getBackgroundColor(self.page!.footer.isEmpty ? self.inputs.count : self.inputs.count + 1))
		}.animation(.none)
	}

	func drawQuestionnaire() -> some View {
		let page = questionnaire.pages[pageIndex]
		let preinputs = page.inputs
		let isDisabled = self.readyCounter < self.waitCounter || (!page.header.isEmpty && !self.headerIsReady) || (!page.footer.isEmpty && !self.footerIsReady)
		let isZero = self.waitCounter == 0 || self.readyCounter == 0

		var inputs: [Input] = []
		for input in preinputs {
			if(input.type !== Input.TYPES.appUsage) {
				inputs.append(input)
			}
		}


        return GeometryReader { geometry in
            ScrollView {
				ZStack(alignment: .top) {
					self.drawInnerQuestionnaire(width: geometry.size.width).opacity(isDisabled ? 0 : 1).animation(.easeIn(duration: 0.8))
					if(isDisabled) {
						VStack {
							LoadingSpinner(isAnimating: .constant(true), style: .large).padding()
							HStack {
								Text(isZero ? "100" : String(Int(100 / self.waitCounter * self.readyCounter)))
								Text("%")
							}
						}
					}
				}
            }
			.resignKeyboardOnDragGesture()
			.scrollAction(self.$action)
		}
	}
	
	var body: some View {
		VStack {
			self.drawQuestionnaire()
		}
			.navigationBarTitle(self.questionnaire.getQuestionnaireTitle(pageIndex: Int32(self.pageIndex)))
			.onAppear {
				self.appState.disableLandscape = true
			}
			.onDisappear {
				self.appState.disableLandscape = false
			}
	}
}
