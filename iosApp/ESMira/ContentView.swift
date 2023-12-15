import SwiftUI
import sharedCode

struct ContentView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
//	let updateTimer = Timer.publish(every: 10, on: .main, in: .common).autoconnect()
	
	init() {
		let appearance = UINavigationBarAppearance()
		appearance.configureWithOpaqueBackground()
		appearance.backgroundColor = UIColor(named: "Surface")
		appearance.titleTextAttributes = [.foregroundColor: UIColor(named: "onSurface") ?? UIColor.white]
		appearance.largeTitleTextAttributes = [.foregroundColor: UIColor(named: "onSurface") ?? UIColor.white]
			
		UINavigationBar.appearance().standardAppearance = appearance
		UINavigationBar.appearance().scrollEdgeAppearance = appearance
		UINavigationBar.appearance().compactAppearance = appearance
		UINavigationBar.appearance().tintColor = UIColor(named: "onSurface") ?? UIColor.white
	}
	
	func getScreenDialogView() -> some View {
		switch(self.navigationState.dialogOpened) {
			case .errorReport:
				return AnyView(
					SendErrorReportView().environmentObject(self.appState)
						.environmentObject(appState)
						.environmentObject(navigationState)
						.accentColor(Color("onSurface"))
				)
			case .changeSchedule:
				let study = self.navigationState.study
				if(study != nil) {
					return AnyView(
						ChangeSchedulesView(isShown: self.$navigationState.changeSchedulesOpened, study: study!, resetSchedules: self.navigationState.resetSchedules)
							.environmentObject(appState)
							.environmentObject(navigationState)
							.accentColor(Color("onSurface"))
					)
				}
				else {
					return AnyView(Text("error"))
				}
		case .faultyAccessKey:
			let study = self.navigationState.study
			if(study != nil) {
				return AnyView(
					FaultyAccessKey(study: study!)
						.environmentObject(appState)
						.environmentObject(navigationState)
						.accentColor(Color("onSurface"))
				)
			}
			else {
				return AnyView(Text("error"))
			}
				
			default:
				return AnyView(Text("error"))
		}
	}
	
	
	var body: some View {
		NavigationView {
			VStack {
				NavigationLink(
					destination: AddStudyView(connectData: self.navigationState.addStudyConnectData),
					isActive: self.$navigationState.addStudyOpened,
					label: { EmptyView() }
				)
				if(self.navigationState.questionnaire != nil) {
					NavigationLink(
						destination:
							QuestionnaireView(questionnaire: self.navigationState.questionnaire!),
						isActive: self.$navigationState.questionnaireOpened,
						label: { EmptyView() }
					)
				}
				if(self.navigationState.questionnaireSuccessfullOpened) {
					QuestionnaireSavedSuccessfully()
				}
				else if(self.navigationState.study != nil) {
					NavigationLink(
						destination: MessagesView(study: self.navigationState.study!),
						isActive:self.$navigationState.messagesOpened,
						label: { EmptyView() }
					)
					StudyDashboard(study: self.navigationState.study!)
				}
				else {
					AddStudyView(connectData: self.navigationState.addStudyConnectData)
				}
			}
		}
			.onAppear {
				let missedNotifications = DbLogic().getMissedInvitations()
				if(missedNotifications != 0) {
					self.appState.showToast(String(format: NSLocalizedString("ios_info_missed_notifications", comment: ""), missedNotifications))
					DbLogic().resetMissedInvitations()
				}
				DbLogic().checkLeaveStudies()
				self.navigationState.reloadStudy()
			}
//			.onReceive(updateTimer) { _ in
//				self.navigationState.reloadStudy() //will lead to the questionnaire being updated aswell. When using remote images there will be a "loading glitch" every time
//			}
			.sheet(item: self.$navigationState.dialogOpened) { item in
				self.getScreenDialogView()
			}
			.toast(isPresented: self.$appState.toastShown) {
				Text(self.appState.toastMsg).multilineTextAlignment(.center).font(.system(size: 14))
			}
			.alert(isPresented: self.$appState.dialogShown) {
				if(self.navigationState.dialogOpened != nil) {
					self.navigationState.dialogOpened = nil //.sheet() and .alert() dont work at the same time
				}
				return Alert(title: Text(self.appState.title), message: Text(self.appState.msg), dismissButton: .default(Text("OK")))
			}
			
			.accentColor(Color("onSurface"))
			.navigationViewStyle(.stack)
	}
}
