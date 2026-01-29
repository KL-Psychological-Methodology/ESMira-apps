//
//  Created by JodliDev on 01.03.21.
//

import SwiftUI
import sharedCode

struct FlipEffect: GeometryEffect {
	func effectValue(size: CGSize) -> ProjectionTransform {
		let t = CGAffineTransform(a: 1, b: 0, c: 0, d: -1, tx: 0, ty: size.height)
		return ProjectionTransform(t)
	}
}

struct MessagesView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
	@State var draftNewMessage = false
	@State private var loadingState: LoadingState = .hidden
	@State private var newMessageContent: String = ""
	@State var messages: [Message]
	
	let study: Study
	
	init(study: Study) {
		self.study = study
		self._messages = State(initialValue: DbLogic().getMessages(id: study.id))
	}
	
	var body: some View {
		ScrollView {
			VStack {
				ForEach(messages, id: \.id) { (msg: Message) in
					ChatBubble(
						position: msg.fromServer ? .left : .right,
						isNew: msg.isNew,
						content: msg.content
					)
					.onDisappear {
						if(msg.isNew) {
							msg.markAsRead()
							self.navigationState.reloadStudy()
						}
					}
				}
				if(draftNewMessage) {
					HStack {
						Button(action: { draftNewMessage = false }) {
							Text("cancel")
						}
						.padding(.horizontal)
						
						Spacer()
						
						Button(action: {
							self.loadingState = .loading
						}) {
							Text("send")
						}
						.padding(.horizontal)
					}
					
					MultilineTextField(text: self.$newMessageContent).frame(height: 100)
						.border(Color("Outline"))
						.padding(.horizontal)
				}
				else {
					DefaultIconButton(
						icon: "message.fill",
						label: "write_message_to_researcher",
						action: { draftNewMessage = true }
					)
					.padding(.top)
				}
			}
			.modifier(FlipEffect())
			.padding()
		}
		.modifier(FlipEffect())
		.onAppear {
			let oldUnreadMessagesCount = DbLogic().countUnreadMessages(id: self.study.id)
			Web.Companion().updateStudiesAsync(forceStudyUpdate: false, filterStudies: KotlinArray<KotlinLong>(size: 0, init: {_ in KotlinLong(0)})) {updatedCount in
				DispatchQueue.main.async {
					if(DbLogic().countUnreadMessages(id: self.study.id) != oldUnreadMessagesCount) {
						self.navigationState.reloadStudy()
					}
				}
			}
		}
		.customLoader(isShowing: self.$loadingState,
			onShowing: {
				Web.Companion().sendMessageAsync(content: self.newMessageContent, study: self.study, onError: {msg in
					DispatchQueue.main.async {
						self.loadingState = .error
						self.appState.showToast(msg)
					}
				}, onSuccess: {
					DispatchQueue.main.async {
						self.loadingState = .hidden
						self.appState.showTranslatedToast("info_message_sent")
						self.messages = DbLogic().getMessages(id: study.id)
						self.draftNewMessage = false
					}
				})
			},
			onCancel: {
				
			}
		)
	}
	
}

