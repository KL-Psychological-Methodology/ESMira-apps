//
// Created by JodliDev on 12.08.20.
//

import SwiftUI
import sharedCode
import Combine

struct QuestionnaireView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
	let questionnaire: sharedCode.Questionnaire
	let firstScreen: Bool
	
	@State var pageIndex: Int
	
	@State var nextPageWithoutBack = false
	@State var nextPage = false

	@State private var action = ScrollAction.idle

	@State private var pageIsActive = true
	
	@State var skipPageTimer: DispatchSourceTimer? = nil
	@State var didRemindOfEmptyResponses = false
	
	/**
	 * Only called from ContentView
	 */
	init(questionnaire: sharedCode.Questionnaire) {
		self.init(
			questionnaire: questionnaire,
			pageIndex: Int(questionnaire.getFirstPageIndex()),
			firstScreen: true
		)
	}
	init(questionnaire: sharedCode.Questionnaire, pageIndex: Int, firstScreen: Bool = false) {
		self.firstScreen = firstScreen
		self.questionnaire = questionnaire
		self._pageIndex = State(initialValue: pageIndex)
	}
	
	private func noMissings() -> Bool {
		let errorIndex = self.questionnaire.checkQuestionnaire(pageI: Int32(self.pageIndex), checkAll: false)
		if(errorIndex == -1) {
			return true
		}

		self.action = .toTag(tag: Int(errorIndex))
		self.appState.showToast(NSLocalizedString("error_missing_fields", comment: ""))
		return false
	}
	
	private func hintMissings() -> Bool {
		if(didRemindOfEmptyResponses) {
			return true
		}
		let emptyRespnoseIndex = self.questionnaire.checkQuestionnaire(pageI: Int32(self.pageIndex), checkAll: true)
		if(emptyRespnoseIndex == -1) {
			return true
		}
		self.action = .toTag(tag: Int(emptyRespnoseIndex))
		self.appState.showToast(NSLocalizedString("hint_missing_fields", comment: ""))
		didRemindOfEmptyResponses = true
		return false
	}

	private func getBackgroundColor(_ i: Int) -> Color {
		return (i % 2 != 0) ? Color("ListColor1") : Color("ListColor2")
	}
	
//	private func drawInnerQuestionnaire(page: Page, width: CGFloat) -> some View {
	private func drawInnerQuestionnaire(page: Page) -> some View {
		let activeInputs = page.activeInputs
		
		return VStack {
			if(!page.header.isEmpty) {
				HtmlTextView(html: page.header)
					.padding()
//					.frame(width: width)
					.background(Color("ListColor1"))
			}

			ForEach(0..<activeInputs.count, id: \.self) { i in
				InputView(input: activeInputs[i])
					.padding()
//					.frame(width: width)
					.uiTag(i)
					.background(getBackgroundColor(i))

			}

			
			if(!page.footer.isEmpty) {
				HtmlTextView(html: page.footer)
					.padding()
//					.frame(width: width)
					.background(getBackgroundColor(activeInputs.count))
			}
			
			VStack(alignment: .leading) {
				if(questionnaire.questionnairePageHasRequired(index: Int32(pageIndex))) {
					Text("info_required")
						.padding(10)
				}
				if(!self.questionnaire.isLastPage(pageNumber: Int32(pageIndex))) {
					NavigationLink(
						destination: QuestionnaireView(questionnaire: self.questionnaire, pageIndex: self.pageIndex + 1),
						isActive: self.$nextPage,
						label: { EmptyView() }
					)
					
					HStack {
						Spacer()
						Button(action: {
							if(self.noMissings() && self.hintMissings()) {
								goNext()
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
							if(self.noMissings() && self.hintMissings()) {
								self.pageIsActive = false
								goNext()
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
				.background(getBackgroundColor(page.footer.isEmpty ? activeInputs.count : activeInputs.count + 1))
		}
			.animation(.none)
	}

	func drawQuestionnaire() -> some View {
		let page = self.questionnaire.getPage(pageNumber: Int32(self.pageIndex))
		
		return ScrollView {
			self.drawInnerQuestionnaire(page: page)
		}
			.onAppear {
				if(page.skipAfterSecs != 0) {
					let timer = DispatchSource.makeTimerSource(queue: DispatchQueue.main)
					timer.schedule(deadline: .now() + TimeInterval(page.skipAfterSecs))
					timer.setEventHandler {
						if(self.pageIsActive) {
							goNext()
						}
					}
					skipPageTimer = timer
					timer.resume()
				}
			}
			.onDisappear {
				if(page.skipAfterSecs != 0) {
					self.skipPageTimer?.cancel()
				}
			}
			.resignKeyboardOnDragGesture()
			.scrollAction(self.$action)
		
		
//        return GeometryReader { geometry in
//			ZStack(alignment: .top) {
//				ScrollView {
//					self.drawInnerQuestionnaire(page: page, width: geometry.size.width)
//				}
//				if(page.skipAfterSecs != 0) {
//					VStack {}
//						.onAppear {
//							let timer = DispatchSource.makeTimerSource(queue: DispatchQueue.main)
//							timer.schedule(deadline: .now() + TimeInterval(page.skipAfterSecs))
//							timer.setEventHandler {
//								if(self.pageIsActive) {
//									goNext()
//								}
//							}
//							skipPageTimer = timer
//							timer.resume()
//						}
//						.onDisappear {
//							self.skipPageTimer?.cancel()
//						}
//				}
//            }
//			.resignKeyboardOnDragGesture()
//			.scrollAction(self.$action)
//
//		}
	}
	
	private func goNext() {
		let nextRelevantPageIndex = self.questionnaire.getNextRelevantPageIndex(fromPageIndex: Int32(pageIndex))
		if(nextRelevantPageIndex > 0 && nextRelevantPageIndex - Int32(pageIndex) > 1) {
			let skippedPages = nextRelevantPageIndex - Int32(pageIndex) - 1
			if self.questionnaire.showSkipToast {
				if skippedPages == 1 {
					self.appState.showTranslatedToast(NSLocalizedString("toast_skipped_one_page", comment: ""))
				} else {
					self.appState.showTranslatedToast(String(format: NSLocalizedString("toast_skipped_pages", comment: ""), skippedPages))
				}
			}
		}
		if(nextRelevantPageIndex >= 0) {
			self.pageIndex = Int(nextRelevantPageIndex - 1)
			QuestionnaireCache().savePage(questionnaireId: questionnaire.id, pageNumber: Int32(self.pageIndex + 1))
			if(questionnaire.isBackEnabled) {
				self.nextPage = true
			}
			else {
				self.nextPageWithoutBack = true
			}
		}
		else {
			if(!self.questionnaire.isLastPage(pageNumber: Int32(self.pageIndex))) {
				self.appState.showTranslatedToast(NSLocalizedString("toast_skipped_to_end", comment: ""))
			}
			self.questionnaire.saveQuestionnaire()
			self.navigationState.questionnaireOpened = false
			self.navigationState.questionnaireSuccessfullOpened = true
		}
	}
	
	var body: some View {
		VStack {
			if(self.nextPageWithoutBack) {
				QuestionnaireView(questionnaire: self.questionnaire, pageIndex: self.pageIndex + 1)
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
