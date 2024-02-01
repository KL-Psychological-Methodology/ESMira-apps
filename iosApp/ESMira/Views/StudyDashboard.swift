//
//  StudyDashboard.swift
//  ESMira
//
//  Created by JodliDev on 13.03.23.
//

import Foundation
import SwiftUI
import sharedCode

struct ImportantBox: FixedGridItem {
	let content: String
	func fillsLine() -> Bool { return true }
	
	var view: some View {
		HStack {
			Text(content)
				.fontWeight(.bold)
				.foregroundColor(Color("Accent"))
				.padding(10)
				.frame(minWidth: 0, maxWidth: .infinity, alignment: .center)
				.border(Color("Accent"))
		}
			.padding(5)
			.frame(minWidth: 0, maxWidth: .infinity, alignment: .center)
	}
}
struct HeaderLine: FixedGridItem {
	let content: String
	func fillsLine() -> Bool { return true }
	var action: (() -> Void)? = nil
	
	var view: some View {
		VStack {
			VStack {
				Divider()
					.background(Color("Outline"))
				HStack {
					Text(content)
					Spacer()
					if(action != nil) {
						Button(action: self.action!) {
							HStack {
								Image(systemName: "ellipsis")
							}.frame(minWidth: 40, minHeight: 40)
						}
					}
				}
				.padding([.horizontal], 20)
				
				Divider()
					.background(Color("Outline"))
			}
			.padding(0)
			.foregroundColor(Color("onSurface"))
			.background(Color("Surface"))
			.shadow(color: Color(.sRGBLinear, white: 0, opacity: 0.05), radius: 5, y: 5)
		}
			.padding([.vertical], 5)
			.frame(minWidth: 0, maxWidth: .infinity, alignment: .center)
	}
}

struct ClickableContent: View {
	let header: String
	let icon: String
	var important: Bool = false
	var badge: Int = 0
	
	var body: some View {
		ZStack(alignment:.topTrailing) {
			VStack(alignment: .center) {
				Spacer()
				Image(systemName: self.icon)
				Text(NSLocalizedString(header, comment: ""))
					.font(.caption)
					.fontWeight(.bold)
					.padding([.top], 2)
				Spacer()
			}
			if(self.badge != 0) {
				ZStack {
					Capsule()
						.fill(Color.red)
						.frame(maxWidth: 20, maxHeight: 20)
					Text(String(self.badge))
						.font(.caption)
						.foregroundColor(.white)
				}
					.padding([.top, .trailing], 10)
			}
		}
			.frame(minWidth: 0, maxWidth: .infinity, minHeight: 80, alignment: .center)
			.background(Color(self.important ? "AccentLight" : "Surface"))
			.foregroundColor(self.important ? .white : Color("onSurface"))
	}
}
struct NavigationBox<Content: View>: FixedGridItem {
	let header: String
	let icon: String
	var destinationView: () -> Content
	var badge: Int = 0
	
	func fillsLine() -> Bool { return false }
	
	var view: some View {
		NavigationLink(
			destination: destinationView(),
			label: {
				ClickableContent(header: self.header, icon: self.icon, badge: self.badge)
			}
		)
			.padding(5)
	}
}

struct ActionBox: FixedGridItem {
	let header: String
	let icon: String
	let action: () -> Void
	var important: Bool = false
	var badge: Int = 0
	
	func fillsLine() -> Bool { return false }
	
	var view: some View {
		
		Button(
			action: self.action,
			label: {
				ClickableContent(header: self.header, icon: self.icon, important: self.important, badge: self.badge)
			}
		)
			.padding(5)
	}
}

struct ContentLine<Content: View>: FixedGridItem {
	let viewContent: Content
	func fillsLine() -> Bool { return true }
	var view: some View {
		viewContent
			.padding(5)
	}
}


struct StudyDashboard: View {
	@EnvironmentObject var appState: AppState
	@EnvironmentObject var navigationState: NavigationState
	var study: Study
	
	@State var showQuestionnaireMoreMenu = false
	@State var showStudySelector = false
	@State var showSettingsMenu = false
	@State var showAlert = false
	@State var showSheet = false
	
	@State private var showLeaveDialog = false {
		didSet {
			if(self.showLeaveDialog) {
				self.showAlert = true
				self.alertContent = Alert(
					title: Text("dialogTitle_leave_study"),
					message: Text("dialogDesc_leave_study"),
					primaryButton: .destructive(Text("leave")) {
						self.study.leave()
						self.navigationState.reloadStudy()
					},
					secondaryButton: .cancel()
				)
			}
			else {
				self.showAlert = false
			}
		}
	}
	@State private var showDeleteDialog = false {
		didSet {
			if(self.showDeleteDialog) {
				self.showAlert = true
				self.alertContent = Alert(
					title: Text("delete_study"),
					message: Text("confirm_delete_study"),
					primaryButton: .destructive(Text("delete_")) {
						self.study.delete()
						self.navigationState.switchStudy(DbUser().getCurrentStudyId())
					},
					secondaryButton: .cancel()
				)
			}
			else {
				self.showAlert = false
			}
		}
	}
	
	@State private var showNextNotificationSheet = false {
		didSet {
			if(self.showNextNotificationSheet) {
				self.showSheet = true
				self.sheetContent = NextNotificationsView()
			}
			else {
				self.showSheet = false
			}
		}
	}
	
	@State private var showLoadBackupSheet = false {
		didSet {
			if(self.showLoadBackupSheet) {
				self.sheetContent = VStack {
					Button("cancel") {
						self.showLoadBackupSheet = false
					}.padding()
					OpenFilePickerView() { url in
						let outsideAccess = url.startAccessingSecurityScopedResource()
						defer {
							if(outsideAccess) {
								url.stopAccessingSecurityScopedResource()
							}
						}
						
						do {
							NativeLink().sql.close()
							let toUrl = SQLiteHelper.getFileUrl()
							try FileManager.default.removeItem(at: toUrl)
							try FileManager.default.copyItem(at: url, to: toUrl)
						}
						catch {
							print(error.localizedDescription)
							self.appState.showToast("Failed to copy file")
						}
						NativeLink().resetSql(sql: SQLiteHelper())
						
						self.showSheet = false
					}
				}
				self.showSheet = true
			}
			else {
				self.showSheet = false
			}
		}
	}
	
	@State private var showSaveBackupSheet = false {
		didSet {
			if(self.showSaveBackupSheet) {
				self.showSheet = true
				let data = try! FileManager.default.url(
					for: .documentDirectory,
					in: .userDomainMask,
					appropriateFor: nil,
					create: false
				)
				
				self.sheetContent = SaveFilePickerView(activityItems: [data])
			}
			else {
				self.showSheet = false
			}
		}
	}
	
	@State private var alertContent: Alert = Alert(title: Text("Error"))
	@State private var sheetContent: any View = Text("Error")
	
	
	private func getDashboardList() -> [any FixedGridItem] {
		
		var list: [any FixedGridItem] = []
		
		if(self.study.state == Study.STATES.quit) {
			list.append(ImportantBox(content: NSLocalizedString("info_study_not_active_anymore", comment: "")))
		}
		
		let questionnaireList = DbLogic().getEnabledQuestionnaires(studyId: self.study.id)
		
		list.append(HeaderLine(content: NSLocalizedString("questionnaires", comment: "questionnaires"), action: {
			self.showQuestionnaireMoreMenu = true
		}))
		if(!questionnaireList.isEmpty) {
			list.append(ContentLine(viewContent: VStack {
				ForEach(questionnaireList, id: \.id) { questionnaire in
					QuestionnaireLineView(questionnaire: questionnaire)
				}
			}))
		} else {
			list.append(ContentLine(viewContent: Text("no_active_questionnaires").foregroundColor(Color("onSurface"))))
		}
		
		
		list.append(HeaderLine(content: NSLocalizedString("extras", comment: "extras")))
		
		list.append(NavigationBox(
			header: "study_information",
			icon: "exclamationmark.circle",
			destinationView: { StudyInformation(study: self.study) }
		))
		
		if(study.hasStatistics()) {
			list.append(NavigationBox(
				header: "statistics",
				icon: "chart.pie.fill",
				destinationView: { StatisticsView(self.study) }
			))
		}
		
		if(study.hasMessages()) {
			list.append(ActionBox(
				header: "messages",
				icon: "message.fill",
				action: {
					self.navigationState.openMessages(self.study.id)
				},
				badge: Int(DbLogic().countUnreadMessages(id: self.study.id))
			))
		}
		
		if(study.hasRewards()) {
			list.append(NavigationBox(
				header: "rewards",
				icon: "rosette",
				destinationView: { RewardView(study: self.study) }
			))
		}
		
		
		list.append(HeaderLine(content: NSLocalizedString("settings", comment: "study settings")))
		
		if(study.hasEditableSchedules()) {
			list.append(ActionBox(
				header: "change_schedules",
				icon: "clock",
				action: { navigationState.openChangeSchedules(study.id)}
			))
		}
		
		list.append(NavigationBox(
			header: "upload_protocol",
			icon: "book.circle",
			destinationView: { UploadProtocol(study: self.study) }
		))
		
		if(study.state == Study.STATES.joined) {
			list.append(ActionBox(
				header: "leave_study",
				icon: "arrow.right.circle",
				action: { self.showLeaveDialog = true },
				important: true
			))
		}
		else {
			list.append(ActionBox(
				header: "rejoin_study",
				icon: "arrow.counterclockwise.circle",
				action: {
					self.navigationState.openAddStudy(
						QrInterpreter.ConnectData(
							url: self.study.serverUrl,
							accessKey: self.study.accessKey,
							studyId: self.study.webId,
							qId: 0
						)
					)
				}
			))
			list.append(ActionBox(
				header: "delete_study",
				icon: "trash",
				action: {
					if(DbLogic().hasUnSyncedDataSets(studyId: self.study.id)) {
						self.appState.showToast(NSLocalizedString("info_unsynced_datasets", comment: ""))
					}
					else {
						self.showDeleteDialog = true
					}
				},
				important: true
			))
		}
		
		return list
	}
	
	func generateStudyDropdown(_ studyList: [Study]) -> [ActionSheet.Button] {
		var r = [ActionSheet.Button]()
		
		for study in studyList {
			r.append(ActionSheet.Button.default(Text(study.title)) {
				self.navigationState.switchStudy(study.id)
			})
		}
		
		r.append(ActionSheet.Button.destructive(Text("add_a_study")) {
			self.navigationState.addStudyOpened = true
		})
		
		r.append(ActionSheet.Button.cancel())
		return r
	}
	
	func generateSettingsMenu() -> [ActionSheet.Button] {
		var r = [ActionSheet.Button]()
		
		
		r.append(ActionSheet.Button.default(Text("send_error_report")) {
			self.navigationState.openErrorReport()
		})
		r.append(ActionSheet.Button.default(Text("update_studies")) {
			Web.Companion().updateStudiesAsync(forceStudyUpdate: false) {updatedCount in
				DispatchQueue.main.async {
					if(updatedCount != -1) {
						self.appState.showToast(String(format: NSLocalizedString("info_update_complete", comment: ""), Int(truncating: updatedCount)))
					}
					else {
						self.appState.showTranslatedToast("info_update_failed")
					}
				}
			}
		})
		r.append(ActionSheet.Button.default(Text("about_ESMira")) {
			self.navigationState.aboutESMiraOpened = true
		})
		
		if(DbUser().isDev()) {
			r.append(ActionSheet.Button.default(Text("next_notifications")) {
				self.showNextNotificationSheet = true
			})
			
			r.append(ActionSheet.Button.default(Text("backup")) {
				self.showSaveBackupSheet = true
			})
			
			r.append(ActionSheet.Button.default(Text("load_backup")) {
				self.showLoadBackupSheet = true
			})
		}
		
		r.append(ActionSheet.Button.cancel())
		return r
	}
	
	var body: some View {
		VStack {
			NavigationLink(
				destination: InactiveQuestionnairesView(questionnaires: DbLogic().getHiddenQuestionnaires(studyId: self.study.id)),
				isActive: self.$navigationState.inactiveQuestionnaireOpened,
				label: { EmptyView() }
			)
			NavigationLink(
				destination: AboutESMira(),
				isActive: self.$navigationState.aboutESMiraOpened,
				label: { EmptyView() }
			)
			ScrollView {
				FixedGridView(columns: 2, list: getDashboardList())
			}
				.navigationBarTitle(Text(self.study.title), displayMode: .inline)
				.navigationBarItems(
					trailing: HStack {
						let studyList = DbLogic().getAllStudies()
						if(studyList.count <= 1) {
							Button(
								action: { self.navigationState.openAddStudy() },
								label: { Image(systemName: "plus")}
							)
						}
						else {
							Button(action: {
								self.showStudySelector = true
							}) {
								Image(systemName: "arrow.left.arrow.right")
							}
							.actionSheet(isPresented: self.$showStudySelector) {
								ActionSheet(title: Text("please_select"), buttons: self.generateStudyDropdown(studyList))
							}
						}
						
						Spacer(minLength: 20)

						Button(action: {
							self.showSettingsMenu = true
						}) {
							Image(systemName: "gear")
						}
						.actionSheet(isPresented: self.$showSettingsMenu) {
							ActionSheet(title: Text("please_select"), buttons: self.generateSettingsMenu())
						}
					}
						.accentColor(Color("onSurface"))
				)
				.alert(isPresented: self.$showAlert) {
					self.alertContent
				}
				.actionSheet(isPresented: self.$showQuestionnaireMoreMenu) {
					ActionSheet(title: Text(""), buttons: [
						.default(Text("show_inactive_questionnaires")) {
							self.navigationState.inactiveQuestionnaireOpened = true
						},
						.cancel()
					])
				}
				.sheet(isPresented: self.$showSheet) {
					AnyView(self.sheetContent)
				}
		}
	}
}
