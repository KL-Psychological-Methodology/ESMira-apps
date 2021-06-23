//
// Created by JodliDev on 22.02.21.
//

import SwiftUI
import sharedCode

struct ListMessagesView: View {
	@EnvironmentObject var appState: AppState
	@State private var studies: [Study] = []
	@State private var currentStudy: Study? = nil
	@State private var showDeleteAlert = false
	
	func getTitle(_ study: Study) -> some View {
		let count = DbLogic().countUnreadMessages(id: study.id)
		
		return HStack {
			Text(study.title)
			if(count != 0) {
				ZStack {
					Circle()
						.foregroundColor(.red)
					
					Text(String(count))
						.foregroundColor(.white)
						.font(Font.system(size: 12))
				}.frame(width: 20, height: 20)
			}
		}
	}
	var body: some View {
		Group {
			if(studies.count == 1 && self.studies[0].state == .joined) {
				MessagesView(study: self.studies[0])
			}
			else {
				List(studies, id: \.webId) { study in
					NavigationLink(destination: MessagesView(study: study)) {
						return self.getTitle(study)
					}
				}
			}
		}
		.onAppear {
			self.studies = DbLogic().getStudiesForMessages()
		}
	}
}
