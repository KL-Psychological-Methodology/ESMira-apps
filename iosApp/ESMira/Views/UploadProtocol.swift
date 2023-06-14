//
//  UploadProtocol.swift
//  ESMira
//
//  Created by JodliDev on 11.04.23.
//

import Foundation
import SwiftUI
import sharedCode

struct UploadProtocol: View {
	@EnvironmentObject var appState: AppState
	
	let study: Study
	
	@State var uploadDataList: [UploadData]
	
	init(study: Study) {
		self.study = study
		self._uploadDataList = State(initialValue: DbLogic().getSortedUploadData(studyId: study.id))
	}
	
	var body: some View {
		List(self.uploadDataList, id: \.self) { uploadData in
			DataSetLine(uploadData: uploadData) {
				self.uploadDataList = DbLogic().getSortedUploadData(studyId: study.id)
			}
		}
			.fixButtons()
			.navigationBarItems(
				trailing: Button(action: {
					Web.Companion().syncDataSetsAsync { success in
						DispatchQueue.main.async {
							self.appState.showTranslatedToast(success as! Bool ? "info_sync_complete" : "info_sync_failed")
						}
					}
				}) {
					Image(systemName: "arrow.2.circlepath")
				}
			)
	}
}

struct DataSetLine: View {
	let uploadData: UploadData
	let reloadList: () -> Void
	
	@State private var showDeleteAlert: Bool = false
	@State private var deleteAlertView: () -> Alert = { Alert(title: Text("are_you_sure"))}
	
	var body: some View {
		VStack {
			HStack {
				Spacer()
				Text(NativeLink().formatDateTime(ms: uploadData.timestamp))
					.font(.caption)
			}
			HStack {
				switch(uploadData.synced) {
					case .synced:
						Image(systemName: "checkmark.circle.fill")
						.foregroundColor(.green)
					case .notSynced:
						Image(systemName: "clock")
					case .notSyncedError:
						Image(systemName: "exclamationmark.triangle.fill")
					case .notSyncedErrorDeletable:
						Image(systemName: "xmark.circle.fill")
						.foregroundColor(.red)
					default:
						Image(systemName: "")
				}
				VStack(alignment: .leading) {
					Text(uploadData.type.uppercased())
						.fontWeight(.bold)
					if(!uploadData.questionnaireName.isEmpty) {
						Text(uploadData.questionnaireName)
							.font(.caption)
					}
				}
				Spacer()
				if(uploadData.synced == UploadData.States.notSyncedErrorDeletable) {
					Button(action: {
						self.showDeleteAlert = true
					}) {
						Image(systemName: "trash.fill")
					}
				}
			}
		}
			.foregroundColor(Color("PrimaryDark"))
			.alert(isPresented: self.$showDeleteAlert) {
				Alert(title: Text("are_you_sure"),
					primaryButton: .destructive(Text("yes")) {
						uploadData.delete()
						reloadList()
					}, secondaryButton: .cancel())
			}
	}
}
