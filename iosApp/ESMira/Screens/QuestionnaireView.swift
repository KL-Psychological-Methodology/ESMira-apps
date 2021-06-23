//
// Created by JodliDev on 12.08.20.
//

import SwiftUI
import sharedCode

struct QuestionnaireView: View {
	@EnvironmentObject var appState: AppState
	
	let questionnaire: sharedCode.Questionnaire?
	let pageIndex: Int
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
		self.waitCounter = (questionnaire != nil) ? self.questionnaire!.pages[self.pageIndex].inputs.count : 0
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
	
	private func drawQuestionnaire(page: Page, inputs: [Input], width: CGFloat) -> some View {
		
		return VStack {
			if(!page.header.isEmpty) {
				HtmlTextView(html: page.header, isReady: self.$headerIsReady)
					.padding()
					.frame(width: width)
					.background(Color(.white))
			}

			ForEach(0..<inputs.count, id: \.self) { i in
				VStack {
					InputView(input: inputs[i], questionnaire: self.questionnaire!, readyCounter: self.$readyCounter)
						.padding()
						.frame(width: width)
						.uiTag(i)
						.foregroundColor(Color.black)
				}
					.background((i % 2 == 0) ? Color("LighterGray") : Color(.white))

			}

			if(!page.footer.isEmpty) {
				HtmlTextView(html: page.footer, isReady: self.$footerIsReady)
					.padding()
					.frame(width: width)
					.background((inputs.count % 2 == 0) ? Color("LighterGray") : Color(.white))
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
							self.appState.showToast(NSLocalizedString("info_questionnaire_success", comment: ""))
							self.appState.questionnaireOpened = false
						}
					})
				}
				.padding(.horizontal)
				.padding(.vertical, 20)
			}
		}.animation(.none)
	}
	
	func getInputs() -> some View {
		let page = questionnaire!.pages[pageIndex]
		let inputs = page.inputs
		let isDisabled = self.readyCounter < self.waitCounter || (!page.header.isEmpty && !self.headerIsReady) || (!page.footer.isEmpty && !self.footerIsReady)
		let isZero = self.waitCounter == 0 || self.readyCounter == 0
//		let isDisabled = false
		
        return GeometryReader { geometry in
//			ScrollViewWrapper(action: self.$action) {
            ScrollView {
				ZStack(alignment: .top) {
					self.drawQuestionnaire(page: page, inputs: inputs, width: geometry.size.width).opacity(isDisabled ? 0 : 1).animation(.easeIn(duration: 0.8))
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
				self.getInputs()
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



//struct QuestionnaireView_Previews: PreviewProvider {
//	static var previews: some View {
//		QuestionnaireView(questionnaire: Study.Companion().doNewInstance(
//			serverUrl: "test.at",
//			accessKey: "",
//			json: "{" +
//				"  \"questionnaire\": [" +
//				"    {" +
//				"      \"actionTriggers\": []," +
//				"      \"pages\": [" +
//				"        {" +
//				"          \"inputs\": [" +
//				"            {" +
//				"              \"name\": \"text\"," +
//				"              \"text\": \"t\ne\nx\nt\"," +
//				"              \"listChoices\": []" +
//				"            }" +
//				"          ]," +
//				"          \"header\": \"Das <b>ist</b>\n\n<br/>ein<br/>hoh\ne\nr<br/>Text\"" +
//				"        }" +
//				"      ]," +
//				"      \"sumScores\": []," +
//				"      \"name\": \"Gruppe 1\"" +
//				"    }" +
//				"  ]," +
//				"  \"publicStatistics\": {" +
//				"    \"charts\": []," +
//				"    \"observedVariables\": {}" +
//				"  }," +
//				"  \"personalStatistics\": {" +
//				"    \"charts\": []," +
//				"    \"observedVariables\": {}" +
//				"  }," +
//				"  \"id\": 1903," +
//				"  \"version\": 0," +
//				"  \"serverVersion\": 2," +
//				"  \"title\": \"test\"," +
//				"  \"accessKeys\": []" +
//				"}",
//			checkUpdate: false
//		).questionnaire[0], pageIndex: 0).environmentObject(AppState())
//	}
//}
