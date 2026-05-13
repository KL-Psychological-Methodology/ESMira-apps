import SwiftUI
import sharedCode

// Overrides the nav bar to use the Background color on the add-study onboarding
// screens, matching Android's WelcomeScreenActivity which uses colorScheme.background
// there instead of the primary blue used everywhere else.
// iOS 16+: toolbarBackground modifier handles this natively.
// iOS 13–15: a UIViewControllerRepresentable reaches into the nav controller directly.
private struct NavBarBackgroundHelper: UIViewControllerRepresentable {
	func makeUIViewController(context: Context) -> Impl { Impl() }
	func updateUIViewController(_: Impl, context: Context) {}

	final class Impl: UIViewController {
		override func viewWillAppear(_ animated: Bool) {
			super.viewWillAppear(animated)
			guard let bar = navigationController?.navigationBar else { return }
			let a = UINavigationBarAppearance()
			a.configureWithOpaqueBackground()
			a.backgroundColor = UIColor(named: "Background")
			let fg = UIColor(named: "onSurface") ?? .label
			a.titleTextAttributes = [.foregroundColor: fg]
			a.largeTitleTextAttributes = [.foregroundColor: fg]
			bar.standardAppearance = a
			bar.scrollEdgeAppearance = a
			bar.compactAppearance = a
			bar.tintColor = fg
		}

		override func viewWillDisappear(_ animated: Bool) {
			super.viewWillDisappear(animated)
			guard let bar = navigationController?.navigationBar else { return }
			let a = UINavigationBarAppearance()
			a.configureWithOpaqueBackground()
			a.backgroundColor = UIColor(named: "PrimaryDark")
			a.titleTextAttributes = [.foregroundColor: UIColor.white]
			a.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
			bar.standardAppearance = a
			bar.scrollEdgeAppearance = a
			bar.compactAppearance = a
			bar.tintColor = .white
		}
	}
}

extension View {
	@ViewBuilder
	func ESMiraBackgroundNavBar() -> some View {
		if #available(iOS 16.0, *) {
			self
				.toolbarBackground(Color("Background"), for: .navigationBar)
				.toolbarBackground(.visible, for: .navigationBar)
		} else {
			self.background(NavBarBackgroundHelper().frame(width: 0, height: 0))
		}
	}
}

struct ContentView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	
//	let updateTimer = Timer.publish(every: 10, on: .main, in: .common).autoconnect()
	
	init() {
		let appearance = UINavigationBarAppearance()
		appearance.configureWithOpaqueBackground()
		appearance.backgroundColor = UIColor(named: "PrimaryDark")
		appearance.titleTextAttributes = [.foregroundColor: UIColor.white]
		appearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
			
		UINavigationBar.appearance().standardAppearance = appearance
		UINavigationBar.appearance().scrollEdgeAppearance = appearance
		UINavigationBar.appearance().compactAppearance = appearance
		UINavigationBar.appearance().tintColor = .white

		UIScrollView.appearance().backgroundColor = UIColor(named: "Background")
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
		case .expiredNotification:
			return AnyView(
				ExpiredNotificationView()
					.environmentObject(appState)
					.environmentObject(navigationState)
					.accentColor(Color("onSurface"))
			)
				
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
