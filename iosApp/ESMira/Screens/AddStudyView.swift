//
// Created by JodliDev on 25.05.20.
//


import SwiftUI
import sharedCode
import CodeScanner


struct AddStudyView: View {
	struct WelcomeView: View {
		@EnvironmentObject var studyState: StudyState
		@Environment(\.presentationMode) var mode: Binding<PresentationMode>
		@State var showAlert: Bool = false
		
		var body: some View {
			VStack {
				HStack {
					Image("roundAppIcon")
					Text("welcome_hello").font(.system(size: 52))
				}
				Text("welcome_first_instructions").padding(.top)
				Spacer()
				Divider()
				HStack {
					Spacer()
					NavigationLink(destination: QrExistsView().environmentObject(self.studyState)) {
						Text("continue_")
						Image(systemName: "chevron.compact.right")
					}.isDetailLink(false)
				}
			}
			.padding()
			.alert(isPresented: self.$showAlert) {
				Alert(
					title: Text("welcome_exit_questionTitle"),
					message: Text("welcome_exit_questionDesc"),
					primaryButton: .default(Text("ok_")) {
						self.mode.wrappedValue.dismiss()
					},
					secondaryButton: .cancel()
				)
			}
			.navigationBarBackButtonHidden(true)
			.navigationBarItems(leading: Button(action:{
				self.showAlert = true
			}) {
				Image(systemName: "xmark")
				Text("close")
			})
		}
	}
	
	
	struct QrExistsView: View {
		@EnvironmentObject var studyState: StudyState
		
		var body: some View {
			VStack {
				HStack {
					Image("QrCode")
					Text("questionMark").font(.system(size: 64))
				}
				Text("welcome_qr_question").padding(.vertical)
				HStack {
					NavigationLink("no", destination: ServerQuestion().environmentObject(self.studyState)).isDetailLink(false).padding()
					Spacer()
					NavigationLink("yes", destination: StudyLoader(type: .QrScanning).environmentObject(self.studyState)).isDetailLink(false).padding()
				}
				Spacer()
			}
			.padding()
		}
	}
	
	struct StudyLoader: View {
		enum ViewType {
			case QrScanning, AccessKey, Immediately
		}
		@EnvironmentObject var appState: AppState
		
		@EnvironmentObject var studyState: StudyState
		
		@State private var loadingState: LoadingState = .hidden
		@State private var loadingMessage = ""
		@State private var openStudyList = false
		@State var type: ViewType
		
		@State var askAccessKey = false
		@State var openQrScanner = false
		let interpreter = QrInterpreter()
		
		func gotoStudyList(serverUrl: String, accessKey: String = "", studyWebId: Int64 = 0, qId: Int64 = 0) {
			self.studyState.serverUrl = serverUrl
			self.studyState.accessKey = accessKey
			self.studyState.studyWebId = studyWebId
			self.studyState.qId = qId
			self.loadingState = .loading
		}
		func onLoaderShowing() {
			self.studyState.web = Web.Companion().loadStudies(
				serverUrl: self.studyState.serverUrl,
				accessKey: self.studyState.accessKey,
				onError: { msg, e in
					self.loadingMessage = msg
					self.loadingState = .error
					
					
//					self.appState.showToast(msg)
				},
				onSuccess: { studyString, urlFormatted in
					self.studyState.studiesList = Study.Companion().getFilteredStudyList(
						json: studyString,
						url: urlFormatted,
						accessKey: self.studyState.accessKey,
						studyWebId: self.studyState.studyWebId,
						qId: self.studyState.qId
					)
					self.loadingState = .hidden
					self.openStudyList = true
				}
			)
		}
		func onLoaderCancel() {
			self.studyState.web?.cancel()
		}
		
		
		func getContent() -> some View {
			return Group {
				if(self.type == .AccessKey) {
					VStack {
						HStack {
							Image(systemName: "lock.fill").font(.system(size: 80))
							Text("questionMark").font(.system(size: 64))
						}
						Text("welcome_accessKey_question").padding(.vertical)
						Button("yes") {
							self.askAccessKey = true
						}.padding()
						Button("welcome_join_public_study") {
							self.gotoStudyList(serverUrl: self.studyState.serverUrl)
						}.padding()
						Spacer()
					}
					.padding()
					.textFieldAlert(isPresented: self.$askAccessKey, text: self.$studyState.accessKey, title: "colon_accessCode") {
						self.gotoStudyList(serverUrl: self.studyState.serverUrl, accessKey: self.studyState.accessKey)
					}
				}
				else if(self.type == .QrScanning) {
					VStack {
						Image(systemName: "camera.fill").font(.system(size: 80))
						Text("welcome_qr_instructions").padding(.vertical)
						Button("start") {
							self.openQrScanner = true
						}
						Spacer()
					}
					.padding()
					.sheet(isPresented: self.$openQrScanner) {
						CodeScannerView(codeTypes: [.qr]) { result in
							switch result {
							case .success(let code):
								let r = self.interpreter.check(s: code)
								if(r != nil) {
									self.gotoStudyList(
										serverUrl: r!.url,
										accessKey: r!.accessKey,
										studyWebId: r!.studyId,
										qId: r!.qId
									)
									self.openQrScanner = false
								}
								
								print("Found code: \(code)")
							case .failure(let error):
								self.appState.showToast(error.localizedDescription)
							}
						}
					}
				}
				else {
					LoadingSpinner(isAnimating: .constant(true), style: .large)
						.onAppear {
							self.onLoaderShowing()
						}
						.onDisappear() {
							self.onLoaderCancel()
						}
				}
			}
		}
		
		var body: some View {
			Group {
				if(self.type == .Immediately) {
					if(self.openStudyList) {
						StudyListView(studiesList: self.studyState.studiesList).environmentObject(self.studyState)
					}
					else {
						LoadingSpinner(isAnimating: .constant(true), style: .large)
							.onAppear {
								self.onLoaderShowing()
							}
							.onDisappear() {
								self.onLoaderCancel()
							}
					}
				}
				else {
					VStack {
						NavigationLink(destination: StudyListView(studiesList: self.studyState.studiesList).environmentObject(self.studyState), isActive: self.$openStudyList) { EmptyView() }.isDetailLink(false)
						self.getContent()
					}
					.customLoader(isShowing: self.$loadingState, onShowing: self.onLoaderShowing, onCancel: self.onLoaderCancel, message: self.loadingMessage)
				}
			}
		}
	}
	
	struct ServerQuestion: View {
		@EnvironmentObject var studyState: StudyState
		@State var serverUrl = ""
		@State var manualUrl = ""
		@State var askManualUrl = false
		let serverList = Web.Companion().serverList
		
		var body: some View {
			VStack {
				HStack {
					Image(systemName: "globe").font(.system(size: 80))
					Text("questionMark").font(.system(size: 64))
				}
				Text("welcome_server_question").padding(.vertical)
				ScrollView {
					VStack(alignment: .leading) {
						ForEach(self.serverList, id: \.self) { (pair: KotlinPair) in
							HStack {
								RadioButtonView(state: self.$serverUrl, value: pair.second! as String, labelEl: VStack(alignment: .leading) {
									Text(pair.first! as String).bold()
									Text(pair.second! as String).padding(.leading).font(.system(size: 14)).opacity(0.8)
								}) { _ in
									self.studyState.serverUrl = self.serverUrl
								}
							}
						}
						HStack {
							RadioButtonView(state: self.$serverUrl, value: self.manualUrl, labelEl: VStack(alignment: .leading) {
								Text("enter_manually").bold()
								Text(self.manualUrl).padding(.leading).font(.system(size: 14)).opacity(0.8)
							}) { _ in
								self.askManualUrl = true
							}
						}
					}
				}.foregroundColor(.primary)
				Spacer()
				
				Divider()
				HStack {
					Spacer()
					NavigationLink(destination: StudyLoader(type: .AccessKey).environmentObject(self.studyState)) {
						Text("continue_")
						Image(systemName: "chevron.compact.right")
					}.isDetailLink(false)
				}
			}
			.padding()
			.onAppear {
				self.serverUrl = self.serverList[0].second! as String
			}
			.textFieldAlert(isPresented: self.$askManualUrl, text: self.$manualUrl, title: "colon_enter_manually") {
				self.serverUrl = self.manualUrl
				self.studyState.serverUrl = self.serverUrl
			}
		}
	}
	
	struct StudyListView: View {
		@EnvironmentObject var appState: AppState
		@EnvironmentObject var studyState: StudyState
		@State var studiesList: [Study]
		@State var loadingState: LoadingState = .hidden
		@State var openStudyList = false
		
		
		var body: some View {
			VStack {
				if(self.studiesList.count == 0) {
					if(self.studyState.accessKey.isEmpty) {
						Text("info_no_studies_noAccessKey").padding()
					}
					else {
						Text(String(format: NSLocalizedString("info_no_studies_withAccessKey", comment: ""), self.studyState.accessKey)).padding()
					}
				}
				else if(self.studiesList.count == 1) {
					StudyDetailView(study: self.studiesList[0]).environmentObject(self.studyState)
				}
				else {
					List(self.studiesList, id: \.webId) { study in
						NavigationLink(destination: StudyDetailView(study: study).environmentObject(self.studyState)) {
							VStack(alignment: .leading) {
								Text(study.title).bold()
								Text(study.contactEmail).offset(x: 10)
							}
						}.isDetailLink(false)
					}
				}
			}
			.navigationBarTitle(Text("studies"), displayMode: .inline)
		}
	}
	
	
	struct StudyDetailView: View {
		@EnvironmentObject private var appState: AppState
		@EnvironmentObject var studyState: StudyState
		
		public let study: Study
		@State private var openStudyJoined = false
		
		init(study: Study) {
			self.study = study
		}
		
		var body: some View {
			VStack(alignment: .leading) {
				NavigationLink(destination: StudyJoinedView(study: self.study), isActive: self.$openStudyJoined, label: { EmptyView() }).isDetailLink(false)
				Text(study.title).bold()
				Text(study.contactEmail).offset(x: 10)
				ScrollableHtmlTextView(html: self.study.studyDescription)
				Spacer()
				Divider()
				HStack {
					Spacer()
					if(self.study.needsPermissionScreen()) {
						NavigationLink("consent", destination: StudyPermissions(study: study).environmentObject(self.studyState)).isDetailLink(false)
					}
					else {
						Button("participate") {
							self.study.join()
							
							if(self.study.needsJoinedScreen()) {
								self.openStudyJoined = true
							}
							else {
								self.appState.addStudyOpened = false
							}
						}
					}
				}
			}
			.padding()
			.navigationBarTitle(Text("add_a_study"), displayMode: .inline)
		}
	}
	
	
	struct StudyPermissions: View {
		class LineData {
			let header: String
			let desc: String
			let whatFor: String?
			let btn: String
			let btnAction: () -> Void
			
			var errorDesc = ""
			var errorAction: (() -> Void)? = nil
			var errorBtn = ""
			
			init(header: String, desc: String, btn: String, whatFor: String?, btnAction: @escaping () -> Void) {
				self.header = header
				self.desc = desc
				self.whatFor = whatFor
				self.btn = btn
				self.btnAction = btnAction
			}
		}
		
		@EnvironmentObject private var appState: AppState
		@EnvironmentObject var studyState: StudyState
		let study: Study
		
		
		init(study: Study) {
			self.study = study
		}
		
		@State private var progress = 0
		@State private var shownLines: [LineData] = []
		@State private var completedLines: [Int] = []
		@State private var failedLines: [Int] = []
		
		@State private var isDone = false
		@State private var openStudyJoined = false
		
		@State private var showAlert: Bool = false
		@State private var alertView: () -> Alert = { Alert(title: Text(""))}
		
		
		var body: some View {
			VStack(alignment: .leading) {
				NavigationLink(destination: StudyJoinedView(study: self.study), isActive: self.$openStudyJoined, label: { EmptyView() }).isDetailLink(false)
				ForEach(self.shownLines.indices, id: \.self) { index in
					self.createLine(index: index, line: self.shownLines[index])
				}
				Spacer()
				if(self.isDone) {
					Text("info_study_permissionSetup_ended")
				}
				Divider()
				HStack {
					Spacer()
					Button("participate") {
						self.study.join()
						
						if(self.study.needsJoinedScreen()) {
							self.openStudyJoined = true
						}
						else {
							self.appState.addStudyOpened = false
						}
						self.studyState.updateStudyList()
					}.disabled(!self.isDone)
				}
			}
			.onAppear {
				var shownLines: [LineData] = []
				if(self.study.hasInformedConsent()) {
					shownLines.append(LineData(header: "informed_consent", desc: "informed_consent_desc", btn: "show_informed_consent", whatFor: nil) {
						self.alertView = {Alert(title: Text("informed_consent"), message: Text(self.study.informedConsentForm), primaryButton: .default(Text("i_agree"), action: {
							self.nextProgress()
						}), secondaryButton: .cancel())}
						self.showAlert = true
					})
				}
				
				if(self.study.hasSchedules() || self.study.hasEvents()) {
					let line = LineData(header: "notifications", desc: "notification_permission_check", btn: "enable_notifications", whatFor: "notification_setup_desc") {
						Notifications.authorize { success in
							self.nextProgress(failed: !success)
						}
					}
					line.errorDesc = "ios_dialogDesc_notifications_disabled"
					line.errorBtn = "open_settings"
					line.errorAction = {
						self.openSettings()
					}
					
					shownLines.append(line)
				}
				
				if(shownLines.count == 0) {
					self.isDone = true
				}
				self.shownLines = shownLines
			}
			.alert(isPresented: self.$showAlert, content: self.alertView)
			.padding()
			.navigationBarTitle(Text("add_a_study"), displayMode: .inline)
		}
		
		private func createLine(index: Int, line: LineData) -> some View {
			let isFailed = self.failedLines.contains(index)
			let isCompleted = self.completedLines.contains(index)
			let isCurrent = self.progress == index
			
			return VStack(alignment: .leading) {
				HStack {
					Text("\(index+1). \(NSLocalizedString(line.header, comment: ""))").font(.system(size: 22))
					if(line.whatFor != nil) {
						Spacer()
						Button("what_for") {
							self.alertView = {Alert(title: Text("what_for"), message: Text(NSLocalizedString(line.whatFor!, comment: "")), dismissButton: .default(Text("ok_")))}
							self.showAlert = true
						}
					}
					Spacer()
					if(isFailed) {
						Image(systemName: "xmark.circle.fill").foregroundColor(Color.red)
					}
					else if(isCompleted) {
						Image(systemName: "checkmark.circle.fill").foregroundColor(Color.green)
					}
				}
				if(isCurrent) {
					Text(NSLocalizedString(line.desc, comment: "")).padding(.vertical)
					Button(NSLocalizedString(line.btn, comment: ""), action: line.btnAction).padding()
				}
				else if(isFailed) {
					Text(NSLocalizedString(line.errorDesc, comment: "")).padding(.vertical)
					if(line.errorAction != nil) {
						Button(NSLocalizedString(line.errorBtn, comment: ""), action: line.errorAction!)
					}
				}
			}
			.opacity(isFailed || isCompleted || isCurrent ? 1 : 0.3)
		}
		
		private func nextProgress(failed: Bool = false) {
			if(failed) {
				self.failedLines.append(self.progress)
			}
			else {
				self.completedLines.append(self.progress)
			}
			withAnimation {
				self.progress += 1
				if(self.progress >= self.shownLines.count) {
					self.isDone = true
				}
			}
			
		}
		
		private func openSettings() {
			guard let settingsUrl = URL(string: UIApplication.openSettingsURLString) else {
				self.appState.showTranslatedToast("error_settings_not_opened")
				return
			}
			
			if(UIApplication.shared.canOpenURL(settingsUrl)) {
				UIApplication.shared.open(settingsUrl) { success in
					if(!success) {
						self.appState.showTranslatedToast("error_settings_not_opened")
					}
				}
			}
		}
	}
	
	
	struct StudyJoinedView: View {
		@EnvironmentObject var appState: AppState
		
		let study: Study
		var body: some View {
			VStack {
				ScrollableHtmlTextView(html: self.study.postInstallInstructions)
				Spacer()
				if(study.hasNotifications()) {
					Divider()
					Text("colon_next_expected_notification").bold().padding()
					NextNotificationsView(studyId: self.study.id)
				}
				Divider()
				HStack {
					Button(action: {
						self.appState.openChangeSchedule(self.study.id)
					}) {
						Text("schedules")
					}
					Spacer()
					Button(action: {
						self.appState.addStudyOpened = false
					}) {
						Text("complete")
					}
				}
			}
			.padding()
			.navigationBarBackButtonHidden(true)
			.navigationBarTitle(Text("complete"), displayMode: .inline)
		}
	}
	
	
	
	class StudyState: ObservableObject {
		var appState: AppState? = nil
		
		@Published var serverUrl: String = ""
		@Published var accessKey: String = ""
		@Published var studyWebId: Int64 = 0
		@Published var qId: Int64 = 0
		
		var web: Web? = nil
		@Published var studiesList: [Study] = []
		@Binding var studies: [Study]
		
		init(studies: Binding<[Study]>) {
			self._studies = studies
		}
		func updateStudyList() {
			self.studies = DbLogic().getJoinedStudies()
		}
	}
	
	@EnvironmentObject var appState: AppState
	
	@ObservedObject var studyState: StudyState
	
	let loadImmediately: Bool
	
	init(connectData: QrInterpreter.ConnectData?, studies: Binding<[Study]>) {
		self._studyState = ObservedObject(initialValue: StudyState(studies: studies))
		if(connectData == nil) {
			self.loadImmediately = false
		}
		else {
			self.loadImmediately = true
			self.studyState.serverUrl = connectData!.url
			self.studyState.accessKey = connectData!.accessKey
			self.studyState.studyWebId = connectData!.studyId
			self.studyState.qId = connectData!.qId
		}
	}
	
	var body: some View {
		Group {
			if(self.loadImmediately) {
				StudyLoader(type: .Immediately)
			}
			else {
				if(DbLogic().hasNoStudies()) {
					WelcomeView().environmentObject(self.studyState)
				}
				else {
					QrExistsView().environmentObject(self.studyState)
				}
			}
		}
		.onAppear { //@EnvironmentObject seems to fill its variable after init, so we improvise
			
			self.studyState.appState = self.appState
		}
	}
}






