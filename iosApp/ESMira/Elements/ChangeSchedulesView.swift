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
		@State private var startTime: String
		@State private var endTime: String
		@State private var isFaulty: Bool
		
		init(_ signalTime: SignalTime) {
			self.signalTime = signalTime

			self._startTime = State(initialValue: String(signalTime.getStart()))
			self._endTime = State(initialValue: String(signalTime.getEnd()))
			self._isFaulty = State(initialValue: signalTime.isFaulty())
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
				Text(self.signalTime.questionnaire.title).bold()
				if(self.signalTime.random) {
					Text("colon_between").padding(.top, 10)
					HStack(alignment: .center) {
						if(self.isFaulty) {
							Image(systemName: "exclamationmark.circle.fill").foregroundColor(.red)
						}
						Spacer()
						DateWindowView(value: self.$startTime, typeMode: .time, saveMode: .asTimestamp) { value in
							self.signalTime.setStart(timestamp: Int64(value) ?? 0)
							self.checkFaulty()
						}
						Text("word_and")
						DateWindowView(value: self.$endTime, typeMode: .time, saveMode: .asTimestamp) { value in
							self.signalTime.setEnd(timestamp: Int64(value) ?? 0)
							self.checkFaulty()
						}
						Spacer()
					}
				}
				else {
					HStack() {
						Spacer()
						DateWindowView(value: self.$startTime, typeMode: .time, saveMode: .asTimestamp) { value in
							self.signalTime.setStart(timestamp: Int64(value) ?? 0)
							self.checkFaulty()
						}
						Spacer()
					}
				}
			}
				.padding()
		}
	}
	
	@EnvironmentObject var appState: AppState
	@Binding var isShown: Bool
	let study: Study
	let resetSchedules: Bool
	
	
	func drawSignalTimes() -> some View {
		return List(self.study.editableSignalTimes, id: \.self) { signalTime in
			SignalTimeView(signalTime)
		}.fixButtons()
		
	}
	
	var body: some View {
		VStack {
			self.drawSignalTimes()
			
			HStack {
				DefaultButton("cancel") {
					self.isShown = false
				}.padding()
				DefaultButton("save") {
					if(study.saveSchedules(rescheduleNow: self.resetSchedules)) {
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
