//
// Created by JodliDev on 04.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct SendErrorReportView: View {
	@EnvironmentObject var appState: AppState
	
	@State var loadingState: LoadingState = .hidden
	@State var comment: String = ""
	
	
	func newErrors() -> some View {
		DbLogic().setErrorsAsReviewed()
		return Text("info_error_report_desc").bold()
	}
	
	func createErrorLine(error: ErrorBox) -> some View {
		let color: Color
		switch(error.severity) {
			case ErrorBox.Companion().SEVERITY_ERROR:
				color = Color.red
			case ErrorBox.Companion().SEVERITY_WARN:
				color = Color.orange
			case ErrorBox.Companion().SEVERITY_LOG:
				color = Color.black
			default:
				color = Color.yellow
		}
		return VStack(alignment: .leading) {
			HStack {
				Text(error.getFormattedDateTime())
				Text(error.title)
					.fontWeight(.bold)
					.foregroundColor(color)
			}
			Spacer()
			Text(error.msg)
		}
	}
	
	var body: some View {
		NavigationView {
			VStack(alignment: .leading, spacing: 20) {
				if(DbLogic().hasNewErrors()) {
					self.newErrors()
				}
				Text("hint_error_comment")
				MultilineTextField(text: self.$comment).frame(height: 100)
					.border(Color.primary)
				NavigationLink(destination:
					List() {
						ForEach(DbLogic().getErrors(), id: \.id) { error in
							self.createErrorLine(error: error)
								.padding()
						}
					}
						.navigationBarTitle(Text("what_is_sent"), displayMode: .inline)
				
				) {
					Text("what_is_sent").padding()
				}
				HStack(alignment: .center) {
					Spacer()
					Button(action: {
						self.appState.openScreen = nil
					}) {
						Text("cancel")
					}.padding()
					Button(action: {
						self.loadingState = .loading
					}) {
						Text("ok_")
					}.padding()
				}
				Spacer()
			}
				.padding()
				.customLoader(isShowing: self.$loadingState,
					onShowing: {
						Web.Companion().sendErrorReportAsync(
							comment: self.comment,
							onError: { msg in
								DispatchQueue.main.async {
									self.loadingState = .error
									self.appState.showToast(msg)
								}
							},
							onSuccess: {
								DispatchQueue.main.async {
									self.loadingState = .hidden
									self.appState.openScreen = nil
									self.appState.showTranslatedToast("info_thank_you")
								}
							}
						)
					},
					onCancel: {
						self.appState.openScreen = nil
					}
				)
		}
	}
}
