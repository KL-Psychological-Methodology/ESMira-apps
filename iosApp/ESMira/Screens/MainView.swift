//
// Created by JodliDev on 25.05.20.
//

import SwiftUI
import sharedCode

struct MainView: View {
	enum Screens: Hashable {
		case questionnaire
		case statistics
		case rewards
		case messages
		case settings
	}
	
	@EnvironmentObject var appState: AppState
	@State private var barTitle: String = NSLocalizedString("questionnaires", comment: "")
	
	@State private var selectedTab: Screens = .questionnaire
	@State private var pressedNum = 0
	
	@State private var askDevPassword = false
	@State private var devPassword = ""
	
	//in ios14, when studies are added for the time, this questionnaire list is not updated. So we do it manually in onDisapear
	@State var studies: [Study] = DbLogic().getJoinedStudies()
	
	var body: some View {
		VStack {
			NavigationLink(destination: AddStudyView(connectData: self.appState.connectData).environmentObject(appState), isActive: self.$appState.addStudyOpened, label: { EmptyView() }).isDetailLink(false)
			NavigationLink(destination: QuestionnaireView(questionnaire: self.appState.questionnaire, pageIndex: 0).environmentObject(appState), isActive: self.$appState.questionnaireOpened, label: { EmptyView() }).isDetailLink(false)
			NavigationLink(destination: MessagesView(study: self.appState.messageStudy).environmentObject(appState), isActive: self.$appState.messageOpened, label: { EmptyView() })
			
			if(DbLogic().countUnreadMessages() != 0 && self.selectedTab == .questionnaire){
				Button("info_new_message") {
					self.selectedTab = .messages
				}.padding()
			}
			
			TabView(selection: Binding<Screens>(
				get: {
					self.selectedTab
				},
				set: { s in
					self.selectedTab = s
					if(s == .settings) {
						self.pressedNum += 1
						if(self.pressedNum >= 10) {
							if(DbLogic().isDev()) {
								DbLogic().setDev(enabled: false, pass: "")
								self.appState.showTranslatedToast("info_dev_inactive")
							}
							else {
								self.askDevPassword = true
							}
							self.pressedNum = 0
						}
					}
					else {
						self.pressedNum = 0
					}
				})
			) {
				ListQuestionnairesView(studies: self.$studies)
					.tabItem {
						Image(systemName: "house.fill")
						Text("questionnaires")
					}
					.onAppear {
						self.barTitle = NSLocalizedString("questionnaires", comment: "")
					}
					.tag(Screens.questionnaire)
				if(DbLogic().hasStudiesWithStatistics()) {
					ListStatisticsView()
						.tabItem {
							Image(systemName: "chart.pie.fill")
							Text("statistics")
						}
						.onAppear {
							self.barTitle = NSLocalizedString("statistics", comment: "")
						}
						.tag(Screens.statistics)
				}
				if(DbLogic().hasStudiesForMessages()) {
					ListMessagesView()
						.tabItem {
							Image(systemName: "message.fill")
							Text("messages")
						}
						.onAppear {
							self.barTitle = NSLocalizedString("messages", comment: "")
						}
						.tag(Screens.messages)
				}
				if(DbLogic().hasStudiesWithRewards()) {
					ListRewardsView()
						.tabItem {
							Image(systemName: "rosette")
							Text("rewards")
						}
						.onAppear {
							self.barTitle = NSLocalizedString("rewards", comment: "")
						}
						.tag(Screens.rewards)
				}
				SettingsView()
					.tabItem {
						Image(systemName: "gear")
						Text("settings")
					}
					.onAppear {
						self.barTitle = NSLocalizedString("settings", comment: "")
					}
					.tag(Screens.settings)
			}
				.navigationBarTitle(Text(barTitle), displayMode: .inline)
				.accentColor(Color("Accent"))
		}
			
			.textFieldAlert(isPresented: self.$askDevPassword, text: self.$devPassword, title: "password") {
				if (DbLogic().setDev(enabled: true, pass: self.devPassword)) {
					self.appState.showTranslatedToast("info_dev_active")
				}
			}
	}
}
