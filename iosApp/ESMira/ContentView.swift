import SwiftUI
import sharedCode

struct ContentView: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState

	/// 0 = follow system, 1 = force light, 2 = force dark
	@AppStorage("themeOverride") private var themeOverride: Int = 0

	private var preferredScheme: ColorScheme? {
		switch themeOverride {
		case 1: return .light
		case 2: return .dark
		default: return nil
		}
	}
	
//	let updateTimer = Timer.publish(every: 10, on: .main, in: .common).autoconnect()
	
	init() {
		func makeGradientImage(colors: [CGColor], locations: [NSNumber]? = nil) -> UIImage {
			let layer = CAGradientLayer()
			layer.colors = colors
			layer.locations = locations
			layer.startPoint = CGPoint(x: 0, y: 0.5)
			layer.endPoint   = CGPoint(x: 1, y: 0.5)
			layer.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 100)
			return UIGraphicsImageRenderer(size: layer.frame.size).image { ctx in
				layer.render(in: ctx.cgContext)
			}.resizableImage(withCapInsets: .zero, resizingMode: .stretch)
		}

		// Light mode: same pink as dark mode (#DC4E9D) → blue
		let lightImage = makeGradientImage(colors: [
			UIColor(red: 0.863, green: 0.306, blue: 0.616, alpha: 1).cgColor, // #DC4E9D pink
			UIColor(red: 0.169, green: 0.596, blue: 0.792, alpha: 1).cgColor  // #2B98CA blue
		])
		let lightAppearance = UINavigationBarAppearance()
		lightAppearance.configureWithOpaqueBackground()
		lightAppearance.backgroundImage = lightImage
		lightAppearance.titleTextAttributes      = [.foregroundColor: UIColor.white]
		lightAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]

		// Dark mode: darker pink (#DC4E9D, matches AccentLight dark) → blue
		let darkImage = makeGradientImage(colors: [
			UIColor(red: 0.863, green: 0.306, blue: 0.616, alpha: 1).cgColor, // #DC4E9D dark pink
			UIColor(red: 0.169, green: 0.596, blue: 0.792, alpha: 1).cgColor  // #2B98CA blue
		])
		let darkAppearance = UINavigationBarAppearance()
		darkAppearance.configureWithOpaqueBackground()
		darkAppearance.backgroundImage = darkImage
		darkAppearance.titleTextAttributes      = [.foregroundColor: UIColor.white]
		darkAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]

		let lightTraits = UITraitCollection(userInterfaceStyle: .light)
		let darkTraits  = UITraitCollection(userInterfaceStyle: .dark)

		UINavigationBar.appearance(for: lightTraits).standardAppearance   = lightAppearance
		UINavigationBar.appearance(for: lightTraits).scrollEdgeAppearance = lightAppearance
		UINavigationBar.appearance(for: lightTraits).compactAppearance    = lightAppearance
		UINavigationBar.appearance(for: lightTraits).tintColor            = .white

		UINavigationBar.appearance(for: darkTraits).standardAppearance   = darkAppearance
		UINavigationBar.appearance(for: darkTraits).scrollEdgeAppearance = darkAppearance
		UINavigationBar.appearance(for: darkTraits).compactAppearance    = darkAppearance
		UINavigationBar.appearance(for: darkTraits).tintColor            = .white

		// Apply background tint globally to scroll/list views.
		// Background.colorset: light = white, dark = #0A1F3A (dark navy).
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
			.preferredColorScheme(preferredScheme)
	}
}
