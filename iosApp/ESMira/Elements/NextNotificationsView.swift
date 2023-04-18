//
// Created by JodliDev on 01.09.20.
//

import Foundation
import SwiftUI
import sharedCode


struct NextNotificationsView: View {
	private let dueDateFormatter: DueDateFormatter
	@State private var alarms: [Alarm]
	
	init() {
		self.init(studyId: -1)
	}
	init(studyId: Int64) {
		if(studyId == -1) {
			self.dueDateFormatter = DueDateFormatter()
			self._alarms = State(initialValue: DbLogic().getAlarms())
		}
		else {
			self.dueDateFormatter = DueDateFormatter(
				soonString: NSLocalizedString("soon", comment: ""),
				todayString: NSLocalizedString("today", comment: ""),
				tomorrowString: NSLocalizedString("tomorrow", comment: ""),
				inXDaysString: NSLocalizedString("in_x_days", comment: "")
			)
			self._alarms = State(initialValue: DbLogic().getQuestionnaireAlarmsWithNotifications(studyId: studyId))
		}
	}
	
	var body: some View {
		List(self.alarms, id: \.id) { alarm in
			HStack {
				Text(alarm.actionTrigger.questionnaire.title).bold()
				Spacer()
				Text(self.dueDateFormatter.get(timestamp: alarm.timestamp))
			}.padding()
		}
	}
}
