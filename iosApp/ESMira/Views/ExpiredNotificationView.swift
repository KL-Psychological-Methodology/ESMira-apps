
import Foundation
import SwiftUI
import sharedCode

struct ExpiredNotificationView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
	var body: some View {
		NavigationView {
			VStack(alignment: .leading, spacing: 20) {
				Text("notification_expired_info")
				
				
				HStack(alignment: .center) {
					Spacer()
					DefaultButton("ok_", action: { self.navigationState.closeScreenDialog() }).padding()
				}
				Spacer()
			}
		.padding()
		.navigationBarTitle(Text("notification_expired_title"), displayMode: .inline)
		}
	}
}
