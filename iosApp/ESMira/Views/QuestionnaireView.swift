//
// Created by JodliDev on 12.08.20.
//

import SwiftUI
import sharedCode

struct QuestionnaireView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
	let questionnaire: sharedCode.Questionnaire
	let firstScreen: Bool
	
	@State var pageIndex: Int
	@State var formStarted: Int64
	
	@State var nextPageWithoutBack = false
	@State var nextPage = false

	@State private var action = ScrollAction.idle
	@State private var footerIsReady = false
	@State private var headerIsReady = false

	@State private var readyCounter = 0
	private let waitCounter: Int
	
	
	/**
	 * Only called from ContentView
	 */
	init(questionnaire: sharedCode.Questionnaire) {
		self.init(questionnaire: questionnaire, pageIndex: Int(questionnaire.getFirstPageIndex()), formStarted: Int64(Date().timeIntervalSince1970 * 1000), firstScreen: true)
	}
	init(questionnaire: sharedCode.Questionnaire, pageIndex: Int, formStarted: Int64, firstScreen: Bool = false) {
		self.firstScreen = firstScreen
		self.questionnaire = questionnaire
		self._pageIndex = State(initialValue: pageIndex)
		self._formStarted = State(initialValue: formStarted)
		
		self.waitCounter = questionnaire.getPage(pageNumber: Int32(pageIndex)).inputs.count
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
	
	private func drawInnerQuestionnaire(page: Page, width: CGFloat) -> some View {
		let inputs = page.inputs
		return VStack {
			if(!page.header.isEmpty) {
				HtmlTextView(html: page.header, isReady: self.$headerIsReady)
					.padding()
					.frame(width: width)
					.background(Color("ListColor1"))
			}

			ForEach(0..<inputs.count, id: \.self) { i in
				InputView(input: inputs[i], readyCounter: self.$readyCounter)
					.padding()
					.frame(width: width)
					.uiTag(i)
					.background(getBackgroundColor(i))

			}

			
			if(!page.footer.isEmpty) {
				HtmlTextView(html: page.footer, isReady: self.$footerIsReady)
					.padding()
					.frame(width: width)
					.background(getBackgroundColor(inputs.count))
			}
			
			VStack(alignment: .leading) {
				if(questionnaire.questionnairePageHasRequired(index: Int32(pageIndex))) {
					Text("info_required")
						.padding(10)
				}
				if(!self.questionnaire.isLastPage(pageNumber: Int32(pageIndex))) {
					NavigationLink(
						destination: QuestionnaireView(questionnaire: self.questionnaire, pageIndex: self.pageIndex + 1, formStarted: self.formStarted),
						isActive: self.$nextPage,
						label: { EmptyView() }
					)
					
					HStack {
						Spacer()
						Button(action: {
							if(self.noMissings()) {
								QuestionnaireCache().savePage(questionnaireId: questionnaire.id, pageNumber: Int32(self.pageIndex + 1))
								if(questionnaire.isBackEnabled) {
									self.nextPage = true
								}
								else {
									self.nextPageWithoutBack = true
								}
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
				.background(getBackgroundColor(page.footer.isEmpty ? inputs.count : inputs.count + 1))
		}
			.animation(.none)
	}

	func drawQuestionnaire() -> some View {
		let page = self.questionnaire.getPage(pageNumber: Int32(self.pageIndex))
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
					self.drawInnerQuestionnaire(page: page, width: geometry.size.width).opacity(isDisabled ? 0 : 1).animation(.easeIn(duration: 0.8))
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
			if(self.nextPageWithoutBack) {
				QuestionnaireView(questionnaire: self.questionnaire, pageIndex: self.pageIndex + 1, formStarted: self.formStarted)
			}
			else {
				self.drawQuestionnaire()
					.onAppear {
						if(self.firstScreen) { //The screen used by ContentView is cached. So pageIndex needs to be checked anytime
							self.pageIndex = Int(questionnaire.getFirstPageIndex())
						}
					}
			}
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
