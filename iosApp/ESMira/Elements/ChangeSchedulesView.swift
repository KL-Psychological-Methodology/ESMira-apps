//
// Created by JodliDev on 02.09.20.
//

import Foundation
import SwiftUI
import sharedCode

struct ChangeSchedulesView: View {
	struct SignalTimeView: View {
		@EnvironmentObject var appState: AppState

		private let signalTime: SignalTime
//		private let study: Study
		@State private var startTime: String
		@State private var endTime: String
//		@State private var startTime: String = ""
//		@State private var endTime: String = ""

		@State private var isFaulty: Bool = false
		
//		init(_ study: Study, _ i: Int) {
//			self.signalTime = study.editableSignalTimes[i]
//
//			self._startTime = State(initialValue: String(signalTime.getStart()))
//			self._endTime = State(initialValue: String(signalTime.getEnd()))
//			self.study = study
//			print(String(signalTime.getStart()))
//		}
		
//		init(_ signalTime: inout SignalTime) {
		init(_ signalTime: SignalTime) {
			self.signalTime = signalTime

			self._startTime = State(initialValue: String(signalTime.getStart()))
			self._endTime = State(initialValue: String(signalTime.getEnd()))
		}

		func checkFaulty() {
			if(self.signalTime.isFaulty()) {
            	self.isFaulty = true
            	self.appState.showTranslatedToast("error_schedule_time_window_too_small")
            }
            else {
            	self.isFaulty = false
            }
		}

		var body: some View {
			VStack(alignment: .leading) {
				Text(self.signalTime.label).bold()
				if(self.signalTime.random) {
					Text(self.signalTime.schedule.dailyRepeatRate == 1 ?
						String(format: NSLocalizedString("colon_frequency_header_daily", comment: ""), self.signalTime.frequency) :
						String(format: NSLocalizedString("colon_frequency_header_multiple_days", comment: ""), self.signalTime.frequency, self.signalTime.schedule.dailyRepeatRate)
					)
					HStack {
						if(self.isFaulty) {
							Image(systemName: "exclamationmark.circle.fill").foregroundColor(.red)
						}
						DateWindowView(value: self.$startTime, typeMode: .time, saveMode: .asTimestamp) { value in
							self.signalTime.setStart(timestamp: Int64(value) ?? 0)
							self.checkFaulty()
						}
						Text("word_and")
						DateWindowView(value: self.$endTime, typeMode: .time, saveMode: .asTimestamp) { value in
							self.signalTime.setEnd(timestamp: Int64(value) ?? 0)
							self.checkFaulty()						}
					}
				}
				else {
					if(self.signalTime.schedule.dailyRepeatRate == 1) {
						Text("colon_frequency_header_one_time_daily")
					}
					else {
						Text(String(format: NSLocalizedString("colon_frequency_header_one_time_multiple_days", comment: ""), self.signalTime.schedule.dailyRepeatRate))
					}
					
					DateWindowView(value: self.$startTime, typeMode: .time, saveMode: .asTimestamp) { value in
						self.signalTime.setStart(timestamp: Int64(value) ?? 0)
						self.checkFaulty()
					}
				}
			}
				.padding()
		}
	}
	
	@EnvironmentObject var appState: AppState
	@Binding var isShown: Bool
	private let studies: [Study]
	private let resetSchedules: Bool
	@State private var currentStudy: Study
	
	init(isShown: Binding<Bool>, studyId: Int64 = -1) {
		self._isShown = isShown
		self.studies = DbLogic().getStudiesWithEditableSchedules()
		
		if(studyId != -1) {
			self.resetSchedules = studyId != -1
			for study in studies {
				if(studyId == study.id) {
					self._currentStudy = State(initialValue: study)
					return
				}
			}
			self._currentStudy = State(initialValue: studies[0]) //this should never be reached
		}
		else {
			self.resetSchedules = false
			self._currentStudy = State(initialValue: studies[0])
		}
	}
	
	func drawSignalTimes() -> some View {
		//Swift is call by value. We need to force it to use call by reference because we want all changed data stored in self.studies:

//		return List(self.currentStudy.editableSignalTimes.indices, id: \.self) { i in
//			SignalTimeView(self.currentStudy, i)
////			SignalTimeView(&signalTimes[i])
//		}.fixButtons()
		return List(self.currentStudy.editableSignalTimes, id: \.id) { signalTime in
			SignalTimeView(signalTime)
//			SignalTimeView(&signalTimes[i])
		}.fixButtons()
		
	}
	
	var body: some View {
		VStack {
			ScrollView(.horizontal) {
				HStack {
					ForEach(self.studies, id: \.id) { study in
						Button(action: {
							self.currentStudy = study
						}) {
							Text(study.title)
								.bold()
								.font(.system(size: 14))
						}
							.foregroundColor(study == self.currentStudy ? Color("Accent") : Color.white)
							.padding()
					}
					Spacer()
				}
			}
			.background(Color("PrimaryLight"))
			Divider()
			self.drawSignalTimes()
			
			HStack {
				Button("cancel") {
					self.isShown = false
				}.padding()
				Button("save") {
					var error = false
					for study in self.studies {
						if(!study.saveSchedules(rescheduleNow: self.resetSchedules)) {
							error = true
						}
					}
					if(!error) {
						self.isShown = false
						if(!self.resetSchedules) {
							self.appState.showTranslatedToast("info_schedule_changed_after_one_day")
						}
					}
				}.padding()
			}
		}
	}
}
