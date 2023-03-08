//
// Created by JodliDev on 01.09.20.
//

import Foundation
import SwiftUI
import sharedCode


struct NextNotificationsView: View {
	private let dueDateFormatter: DueDateFormatter
//	private let alarms: [Alarm]
	@State private var alarms: [Alarm]
	private let groupByQuestionnaires: Bool
	
	init() {
		self.init(studyId: -1)
	}
	init(studyId: Int64) {
		let notExact = studyId != -1
		self.dueDateFormatter = !notExact ? DueDateFormatter() : DueDateFormatter(
			soonString: NSLocalizedString("soon", comment: ""),
			todayString: NSLocalizedString("today", comment: ""),
			tomorrowString: NSLocalizedString("tomorrow", comment: ""),
			inXDaysString: NSLocalizedString("in_x_days", comment: "")
		)
		self._alarms = State(initialValue: DbLogic().getQuestionnaireAlarmsWithNotifications(studyId: studyId))
		self.groupByQuestionnaires = notExact
	}
	
	var body: some View {
		VStack {
			if(self.groupByQuestionnaires) {
				ForEach(self.alarms, id: \.id) { (alarm: Alarm) in
					HStack {
						if(alarm.actionTrigger.hasNotifications()) {
							Text(alarm.actionTrigger.questionnaire.title).bold()
							Spacer()
							Text(self.dueDateFormatter.get(timestamp: alarm.timestamp))
						}
					}
				}
			}
			else {
				List(self.alarms, id: \.id) { alarm in
					if(alarm.actionTrigger.hasNotifications()) {
						VStack {
                            HStack {
                                Text(alarm.actionTrigger.questionnaire.title).bold()
                                Spacer()
                                Text(alarm.type.name)
                            }
							HStack {
								Text("\(alarm.label) (\(alarm.id))")
								Spacer()
								Text(self.dueDateFormatter.get(timestamp: alarm.timestamp))
							}
						}.padding()
					}
				}
			}
		}
	}
}
