//
// Created by JodliDev on 17.08.20.
//

import Foundation
import SwiftUI

struct DateWindowView: View {
	enum TypeModes: Hashable {
		case date
		case time
	}
	enum SaveModes {
		case asString
		case asTimestamp
		case timeAsMinutes
	}
	
	private let dateFormatter: DateFormatter = DateFormatter()
	@State private var isShown = false
	@State private var selectedDate: Date
	
	@Binding var value: String
	let mode: TypeModes
	let asTimestamp: Bool
	let timeAsMinutes: Bool
	let callback: ((String) -> ())?
	
	init(value: Binding<String>, typeMode: TypeModes = .date, saveMode: SaveModes = .asString, callback:((String) -> ())? = nil) {
		self._value = value
		self.mode = typeMode
		self.dateFormatter.dateFormat = (typeMode == .time) ? "HH:mm" : "yyyy-MM-dd"
		self.asTimestamp = saveMode == .asTimestamp
		self.timeAsMinutes = saveMode == .timeAsMinutes
		self.callback = callback
		
		let initialValue: Date
		if(value.wrappedValue != "") {
			if(self.asTimestamp) {
				initialValue = Date(timeIntervalSince1970: (Double(value.wrappedValue) ?? 0) / 1000)
			}
			else if(self.timeAsMinutes) {
				let num = Int(value.wrappedValue) ?? 0
				let cal = Calendar.current
				initialValue = cal.date(bySettingHour: num / 60, minute: num % 60, second: 0, of: Date()) ?? Date()
			}
			else {
				initialValue = self.dateFormatter.date(from: value.wrappedValue) ?? Date()
			}
		}
		else if(typeMode == .time) {
			initialValue = self.dateFormatter.date(from: "00:00") ?? Date()
		}
		else {
			initialValue = Date()
		}
		self._selectedDate = State(initialValue: initialValue)
	}
	
	private func getValue(_ date: Date) -> String {
		if(self.asTimestamp) {
			return String(Int64(date.timeIntervalSince1970*1000))
		}
		else if(self.timeAsMinutes) {
			let cal = Calendar.current
			return String(cal.component(.hour, from: date) * 60 + cal.component(.minute, from: date))
		}
		else {
			return self.dateFormatter.string(from: date)
		}
	}
	
	private func getTimeString(_ minutes: Int) -> String {
		let cal = Calendar.current
		let date = cal.date(bySettingHour: minutes / 60, minute: minutes % 60, second: 0, of: Date()) ?? Date()
		
		return self.dateFormatter.string(from: date)
	}
	var body: some View {
		HStack {
			let dateBinding = Binding<Date>(
				get: {
					return self.selectedDate
				},
				set: { date in
					self.selectedDate = date
					self.value = getValue(selectedDate)
					if(self.callback != nil) {
						self.callback?(self.value)
					}
				}
			)
			if(self.mode == .time) {
				Image(systemName: "clock")
				DatePicker("asd", selection: dateBinding, displayedComponents: .hourAndMinute).labelsHidden()
			}
			else {
				Image(systemName: "calendar")
				DatePicker("qwe", selection: dateBinding, displayedComponents: .date).labelsHidden()
			}
		}
	}
}
