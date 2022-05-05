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
	let study: Study?
	@State private var messages: [Message] = []
	
	private func reloadMessages() {
		self.messages = DbLogic().getMessages(id: self.study?.id ?? -1)
	}
	
	var body: some View {
		ScrollView {
			VStack {
				ForEach(self.messages, id: \.id) { (msg: Message) in
					ChatBubble(
						position: msg.fromServer ? .left : .right,
						isNew: msg.isNew,
						content: msg.content
					)					.onAppear {
						msg.markAsRead()
					}
				}

				if(study != nil) {
					NavigationLink(
						destination: NewMessageView(study: study!)
					) {
						Image(systemName: "plus")
						Text("write_message_to_researcher")
					}
					.padding(.top)
				}
			}
			.modifier(FlipEffect())
			.padding()
		}
		.modifier(FlipEffect())
		.onAppear {
			reloadMessages()
		}
		.onReceive(appState.$updateLists) { _ in
			reloadMessages()
		}
	}
	
}

