//
// Created by JodliDev on 12.08.20.
//

import SwiftUI
import sharedCode

struct QuestionnaireView: View {
	@EnvironmentObject var appState: AppState
	
	let questionnaire: sharedCode.Questionnaire?
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
	
	
	
	init(questionnaire: sharedCode.Questionnaire?, pageIndex: Int) {
		self.init(questionnaire: questionnaire, pageIndex: pageIndex, formStarted: Int64(Date().timeIntervalSince1970 * 1000))
	}
	init(questionnaire: sharedCode.Questionnaire?, pageIndex: Int, formStarted: Int64) {
		self.questionnaire = questionnaire
		self.pageIndex = pageIndex
		self._formStarted = State(initialValue: formStarted)
//		self.waitCounter = (questionnaire != nil) ? self.questionnaire!.pages[self.pageIndex].inputs.count : 0
		if(questionnaire == nil || questionnaire?.pages.count == 0) {
			self.waitCounter = 0
			self.inputs = []
			self.page = nil
		}
		else {
			self.page = self.questionnaire!.pages[self.pageIndex]
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
		let errorIndex = self.questionnaire!.checkQuestionnaire(pageI: Int32(self.pageIndex))
		if(errorIndex == -1) {
			return true
		}
		
		print("changeAction")
		self.action = .toTag(tag: Int(errorIndex))
		self.appState.showToast(NSLocalizedString("error_missing_fields", comment: ""))
		return false
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
				VStack {
					InputView(input: self.inputs[i], questionnaire: self.questionnaire!, readyCounter: self.$readyCounter)
						.padding()
						.frame(width: width)
						.uiTag(i)
				}
					.background((i % 2 != 0) ? Color("ListColor1") : Color("ListColor2"))

			}

			if(!self.page!.footer.isEmpty) {
				HtmlTextView(html: self.page!.footer, isReady: self.$footerIsReady)
					.padding()
					.frame(width: width)
					.background((self.inputs.count % 2 != 0) ? Color("ListColor1") : Color("ListColor2"))
			}
			if(questionnaire!.questionnairePageHasRequired(index: Int32(pageIndex))) {
				Text("info_required")
			}
			if(self.pageIndex < self.questionnaire!.pages.count - 1) {
				NavigationLink(destination: QuestionnaireView(questionnaire: self.questionnaire, pageIndex: self.pageIndex + 1, formStarted: self.formStarted), isActive: self.$nextPage, label: { EmptyView() })
					.isDetailLink(false)

				HStack {
					Spacer()
					Button("continue_", action: {
						if(self.noMissings()) {
							self.nextPage = true
						}
					})
				}
				.padding(.horizontal)
				.padding(.vertical, 20)
			}
			else {
				HStack {
					Spacer()
					Button("save", action: {
						if(self.noMissings()) {
							self.questionnaire!.saveQuestionnaire(formStarted: self.formStarted)
							self.appState.questionnaireOpened = false
							self.appState.openScreen = .questionnaireSavedSuccessfully
						}
					})
				}
				.padding(.horizontal)
				.padding(.vertical, 20)
			}
		}.animation(.none)
	}
	
	func drawQuestionnaire() -> some View {
		let page = questionnaire!.pages[pageIndex]
		let preinputs = page.inputs
		let isDisabled = self.readyCounter < self.waitCounter || (!page.header.isEmpty && !self.headerIsReady) || (!page.footer.isEmpty && !self.footerIsReady)
		let isZero = self.waitCounter == 0 || self.readyCounter == 0
//		let isDisabled = false
		
		var inputs: [Input] = []
		for input in preinputs {
			if(input.type !== Input.TYPES.appUsage) {
				inputs.append(input)
			}
		}
		
		
        return GeometryReader { geometry in
//			ScrollViewWrapper(action: self.$action) {
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
//			}
			.scrollAction(self.$action)
		}
	}
	
	var body: some View {
		VStack {
			if(self.questionnaire != nil) {
				self.drawQuestionnaire()
			}
			
		}
			.navigationBarTitle(self.questionnaire?.getQuestionnaireTitle(pageIndex: Int32(self.pageIndex)) ?? "")
			.onAppear {
				self.appState.disableLandscape = true
				if(!(self.questionnaire?.canBeFilledOut(now: NativeLink().getNowMillis()) ?? false)) {
					self.appState.questionnaireOpened = false
				}
			}
			.onDisappear {
				self.appState.disableLandscape = false
			}
	}
}
