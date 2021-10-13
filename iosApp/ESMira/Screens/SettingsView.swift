//
// Created by JodliDev on 25.05.20.
//

import SwiftUI
import sharedCode
import BackgroundTasks

struct SettingsView: View {
	struct SettingsButton: View {
		let action: () -> Void
		let iconName: String
		let header: Text
		var desc: Text? = nil
		
		var body: some View {
			Button(action: self.action) {
				HStack {
					Image(systemName: self.iconName).padding(.trailing)
					
					VStack(alignment: .leading) {
						self.header
							.font(.system(size: 20))
						if(desc != nil) {
							self.desc!
								.foregroundColor(Color.gray)
								.font(.system(size: 16))
						}
					}
				}
			}.padding(.bottom)
		}
	}
	
	enum ActiveDialog: Identifiable {
		case load, save, nextNotifications
		
		var id: Int {
			self.hashValue
		}
	}
	
	
	@EnvironmentObject var appState: AppState
	let hasNoStudies: Bool
	let hasEditableSchedules: Bool
	@State var unSyncedCount: Int32
	var errorCount: Int32
	let isDev: Bool
	
	@State var activeDialog: ActiveDialog?
	
	init() {
		self.hasNoStudies = DbLogic().hasNoStudies()
		self.hasEditableSchedules = !self.hasNoStudies && DbLogic().hasEditableSchedules()
		self._unSyncedCount = State(initialValue: DbLogic().getUnSyncedDataSetCount())
		self.errorCount = DbLogic().getErrorCount()
		self.isDev = DbLogic().isDev()
	}
	
	func createSaveDialog() -> some View {
		let data = try! FileManager.default.url(
			for: .documentDirectory,
			in: .userDomainMask,
			appropriateFor: nil,
			create: false
		).appendingPathComponent(DbLogic().DATABASE_NAME)
		
		return VStack {SaveFilePickerView(activityItems: [data])}
	}
	
	var body: some View {
		List {
			SettingsButton(
				action: {
					self.appState.openChangeSchedule()
				},
				iconName: "clock",
				header: Text("change_schedules"),
				desc: Text("desc_update_schedules")
			).disabled(!self.hasEditableSchedules)
			
			SettingsButton(
				action: {
					Web.Companion().updateStudiesAsync {updatedCount in
						DispatchQueue.main.async {
							if(updatedCount != -1) {
								self.appState.showToast(String(format: NSLocalizedString("info_update_complete", comment: ""), Int(truncating: updatedCount)))
							}
							else {
								self.appState.showTranslatedToast("info_update_failed")
							}
						}
					}
				},
				iconName: "arrow.clockwise",
				header: Text("update_studies"),
				desc: Text("desc_update_studies")
			).disabled(self.hasNoStudies)
			
			SettingsButton(
				action:{
					Web.Companion().syncDataSetsAsync { syncedCount in
						DispatchQueue.main.async {
							if(syncedCount != -1) {
								self.appState.showToast(String(format: NSLocalizedString("info_sync_complete", comment: ""), Int(truncating: syncedCount)))
							}
							else {
								self.appState.showTranslatedToast("info_sync_failed")
							}
							self.unSyncedCount = DbLogic().getUnSyncedDataSetCount()
						}
					}
				},
				iconName: "arrow.2.circlepath",
				header: Text("sync_now"),
				desc: Text(
					self.unSyncedCount != 0 ?
						String(format: NSLocalizedString("info_number_to_sync", comment: ""), self.unSyncedCount) :
						NSLocalizedString("info_nothing_to_sync", comment: "")
				)
			)
			
			if(self.isDev) {
				SettingsButton(
					action: {
						self.activeDialog = .save
					},
					iconName: "icloud.and.arrow.up",
					header: Text("backup")
				)
				
				SettingsButton(
					action: {
						self.activeDialog = .load
					},
					iconName: "icloud.and.arrow.down",
					header: Text("load_backup")
				)
				
				SettingsButton(
					action: {
						self.activeDialog = .nextNotifications
					},
					iconName: "clock",
					header: Text("next_notifications")
				)
			}
			Button(action: {
				self.appState.openScreen = .errorReport

//                BGTaskScheduler.shared.getPendingTaskRequests(completionHandler: { tasks in
//                    print("Tasks", tasks.count, tasks);
//                })
			}) {
				HStack {
					Image(systemName: "ant").padding(.trailing)
					
					VStack(alignment: .leading) {
						Text("send_error_report")
							.font(.system(size: 20))
						
						Text(
							self.unSyncedCount != 0 ?
								NSLocalizedString("info_no_errors", comment: "") :
								String(format: NSLocalizedString("info_detected_x_errors", comment: ""), self.unSyncedCount)
						)
							.foregroundColor(Color.gray)
							.font(.system(size: 16))
					}
				}
			}.padding(.bottom)
			
			VStack(alignment: .leading) {
				HStack {
					Text(String(format: NSLocalizedString("ios_user_id", comment: ""), String(DbLogic().getUid())))
					Button(action: {
						UIPasteboard.general.string = DbLogic().getUid()
						self.appState.showTranslatedToast(String(format: NSLocalizedString("ios_info_copied_x_to_clipboard", comment: ""), DbLogic().getUid()))
					}) {
						Image(systemName: "doc.on.clipboard")
					}
				}
					.foregroundColor(Color("Accent"))
				Text(String(format: NSLocalizedString("ios_version", comment: ""), "\(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Error")", DbLogic().getVersion()))
					.foregroundColor(Color("Accent"))
			}
		}
			.sheet(item: self.$activeDialog) {item in
				self.getSheet(item)
			}
	}
	
	func getSheet(_ item: ActiveDialog) -> some View {
		switch(item) {
			case .load:
				return AnyView(
					VStack {
						Button("cancel") {
							self.activeDialog = nil
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
							
							self.activeDialog = nil
						}
					}
				)
			case .save:
				let data = try! FileManager.default.url(
					for: .documentDirectory,
					in: .userDomainMask,
					appropriateFor: nil,
					create: false
				).appendingPathComponent(DbLogic().DATABASE_NAME)
				
				return AnyView(SaveFilePickerView(activityItems: [data]))
			case .nextNotifications:
				return AnyView(NextNotificationsView())
		}
	}
}
