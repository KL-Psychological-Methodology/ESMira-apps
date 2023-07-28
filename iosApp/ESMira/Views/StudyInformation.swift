//
//  StudyInformation.swift
//  ESMira
//
//  Created by JodliDev on 13.03.23.
//

import Foundation
import SwiftUI
import sharedCode

struct DividerBox: FixedGridItem {
	func fillsLine() -> Bool {
		return true
	}
	
	var view: some View {
		VStack {
			Divider()
				.background(Color("Outline"))
				.padding(5)
		}
	}
}

struct InformationButtonBox: FixedGridItem {
	let content: String
	let action: () -> Void
	var fillsLineState: Bool = false
	func fillsLine() -> Bool { return self.fillsLineState }

	var view: some View {
		HStack {
			Button(action: self.action) {
				Text(self.content)
					.multilineTextAlignment(.center)
					.foregroundColor(Color.primary)
			}
				.padding(10)
				.frame(minWidth: 0, maxWidth: .infinity, alignment: .center)
				.border(Color("Outline"))
		}
		.padding(5)
	}
}
private struct RowHeaderItem: FixedGridItem {
	let content: String
	
	func fillsLine() -> Bool { return false }
	
	var view: some View {
		Text(content)
			.foregroundColor(Color("Outline"))
			.frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
			.padding(.vertical, 10)
			.padding(.horizontal, 5)
	}
}
private struct RowContentItem: FixedGridItem {
	let content: String
	let action: (() -> Void)?
	let icon: String?
	
	init(content: String, action: (() -> Void)? = nil, icon: String? = nil) {
		self.content = content
		self.action = action
		self.icon = icon
	}
	
	func fillsLine() -> Bool { return false }
	
	var view: some View {
		HStack {
			if(action == nil) {
				Text(content)
			}
			else {
				Button(action: self.action!) {
					Text(content)
						.foregroundColor(Color.primary)
					if(icon != nil) {
						Image(systemName: icon!)
					}
				}
			}
		}
		.frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
			  .padding(.vertical, 10)
	}
}

struct InformationBox: FixedGridItem {
	let header: String
	var content: String
	var fillsLineState: Bool = false
	func fillsLine() -> Bool { return self.fillsLineState }

	var view: some View {
		HStack {
			VStack {
				Text(header)
					.bold()
					.foregroundColor(Color("Outline"))
					.multilineTextAlignment(.center)
				Text(content)
					.multilineTextAlignment(.center)
			}
				.padding(10)
				.frame(minWidth: 0, maxWidth: .infinity, minHeight: 90, alignment: .center)
				.border(Color("Outline"))
		}
			.padding(5)
	}
}

struct StudyInformation: View {
	@EnvironmentObject var appState: AppState
	
	let study: Study
	
	private func getList() -> [any FixedGridItem] {
		
		var list: [any FixedGridItem] = []


		//userId
		let userId = DbUser().getUid()
		list.append(RowHeaderItem(content: NSLocalizedString("user_id", comment: "")))
		list.append(
			RowContentItem(
				content: userId,
				action: {
					UIPasteboard.general.string = userId
					   self.appState.showTranslatedToast(String(format: NSLocalizedString("ios_info_copied_x_to_clipboard", comment: ""), userId))
					},
				icon: "doc.on.clipboard"
			)
		)

		//joined at
		list.append(RowHeaderItem(content: NSLocalizedString("joined_at", comment: "")))
		list.append(RowContentItem(content: NativeLink().formatDate(ms: study.joinedTimestamp)))

		//quit at
		if(study.state == Study.STATES.quit) {
			list.append(RowHeaderItem(content: NSLocalizedString("quit_at", comment: "")))
			list.append(RowContentItem(content: NativeLink().formatDate(ms: study.quitTimestamp)))
		}

		//completed questionnaires
		list.append(RowHeaderItem(content: NSLocalizedString("completed_questionnaires", comment: "")))
		list.append(RowContentItem(content: String(DbLogic().getQuestionnaireDataSetCount(studyId: study.id))))

		//next notification
		let alarm = DbLogic().getNextAlarmWithNotifications(studyId: study.id)
		if(alarm == nil) {
			list.append(RowHeaderItem(content: NSLocalizedString("next_notification", comment: "")))
			list.append(RowContentItem(content: NSLocalizedString("none", comment: "")))
		}
		else {
			let formatter = DueDateFormatter(
				soonString: NSLocalizedString("soon", comment: ""),
				todayString: NSLocalizedString("today", comment: ""),
				tomorrowString: NSLocalizedString("tomorrow", comment: ""),
				inXDaysString: NSLocalizedString("in_x_days", comment: "")
			)
			list.append(RowHeaderItem(content: NSLocalizedString("next_notification", comment: "")))
			list.append(RowContentItem(content: formatter.get(timestamp: alarm!.timestamp)))
		}
		
		
		
		
		//study description
		
		if(!self.study.studyDescription.isEmpty) {
			list.append(NavigationBox(
				header: "study_description",
				icon: "exclamationmark.circle",
				destinationView: { ScrollableHtmlTextView(html: self.study.studyDescription) }
			))
		}

		//informed consent
		
		if(!self.study.informedConsentForm.isEmpty) {
			list.append(NavigationBox(
				header: "informed_consent",
				icon: "doc.plaintext",
				destinationView: { ScrollView { Text(self.study.informedConsentForm) } }
			))
		}


		return list
	}
	
	var body: some View {
		ScrollView {
			FixedGridView(columns: 2, list: getList())
		}
	}
}
