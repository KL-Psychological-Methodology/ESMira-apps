//
//  Created by JodliDev on 02.03.21.
//

import SwiftUI
import sharedCode


struct NewMessageView: View {
	let study: Study
	
	@Environment(\.presentationMode) private var presentationMode
	@EnvironmentObject private var appState: AppState
	@State private var content: String = ""
	@State private var loadingState: LoadingState = .hidden
	
	var body: some View {
		ScrollView {
			VStack {
				MultilineTextField(text: self.$content).frame(height: 100)
					.border(Color.primary)
				Button(action: {
					self.loadingState = .loading
				}) {
					Text("send")
				}
			}
		}
		.padding()
		
		.customLoader(isShowing: self.$loadingState,
			onShowing: {
				Web.Companion().sendMessageAsync(content: self.content, study: self.study, onError: {msg in
					DispatchQueue.main.async {
						self.loadingState = .error
						self.appState.showToast(msg)
					}
				}, onSuccess: {
					DispatchQueue.main.async {
						self.loadingState = .hidden
						self.presentationMode.wrappedValue.dismiss() //TODO: this does the animation twice for some reason...
						self.appState.showTranslatedToast("info_message_sent")
					}
				})
			},
			onCancel: {
				
			}
		)
	}
	
}

