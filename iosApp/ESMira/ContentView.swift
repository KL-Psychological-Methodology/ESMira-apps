import SwiftUI
import sharedCode

struct ContentView: View {
	@EnvironmentObject var appState: AppState
	
	init() {
		let appearance = UINavigationBarAppearance()
		appearance.configureWithOpaqueBackground()
		appearance.backgroundColor = UIColor(named: "PrimaryLight")
		appearance.titleTextAttributes = [.foregroundColor: UIColor.white]
		appearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
			
		UINavigationBar.appearance().standardAppearance = appearance
		UINavigationBar.appearance().scrollEdgeAppearance = appearance
		UINavigationBar.appearance().compactAppearance = appearance
		UINavigationBar.appearance().tintColor = UIColor.white
	}
	
	func getScreenDialogView() -> some View {
		switch(self.appState.openScreen) {
			case .errorReport:
				return AnyView(SendErrorReportView().environmentObject(self.appState))
			case .changeSchedule:
				return AnyView(
					ChangeSchedulesView(isShown: Binding(
						get: {
							self.appState.openScreen == .changeSchedule
						},
						set: { value in
							self.appState.openScreen = value ? .changeSchedule : nil
						}
					), studyId: self.appState.scheduleStudyId).environmentObject(self.appState)
				)
			default:
				return AnyView(Text("error"))
		}
	}
	
	var body: some View {
		NavigationView {
			MainView().environmentObject(appState)
		}
			.onAppear {
				if(DbLogic().hasNoStudies()) {
					self.appState.addStudyOpened = true
				}

				let missedNotifications = DbLogic().getMissedInvitations()
				if(missedNotifications != 0) {
					self.appState.showToast(String(format: NSLocalizedString("ios_info_missed_notifications", comment: ""), missedNotifications))
					DbLogic().resetMissedInvitations()
				}
				DbLogic().checkLeaveStudies()
			}
			.sheet(item: self.$appState.openScreen) { item in
				self.getScreenDialogView()
			}
			.toast(isPresented: self.$appState.toastShown) {
				Text(self.appState.toastMsg).multilineTextAlignment(.center).font(.system(size: 14))
			}
			.alert(isPresented: self.$appState.dialogShown) {
				Alert(title: Text(self.appState.title), message: Text(self.appState.msg), dismissButton: .default(Text("OK")))
			}
			
			.accentColor(Color("Accent"))
			.navigationViewStyle(.stack)
			.navigationViewStyle(StackNavigationViewStyle())
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
