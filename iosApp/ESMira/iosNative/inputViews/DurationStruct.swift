//
//  DurationStruct.swift
//  ESMira
//
//  Created by Karl Landsteiner Privatuniversität on 17.07.24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import sharedCode

struct DurationStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	private var hours: Int? {
		get {
			if let valueInt = Int(self.viewModel.value) {
				return valueInt / 60
			}
			return nil
		}
	}
	
	private var minutes: Int? {
		get {
			if let valueInt = Int(self.viewModel.value) {
				return valueInt % 60
			}
			return nil
		}
	}
	
	private func valueFromHM(hours: Int?, minutes: Int?) -> String {
		var hoursInt = 0
		var minutesInt = 0
		var bothNil = true
		if let h = hours {
			bothNil = false
			hoursInt = h
			if h < 0 {
				return self.viewModel.value
			}
		}
		if let m = minutes {
			bothNil = false
			minutesInt = m
			if m < 0 || m >= 60 {
				return self.viewModel.value
			}
		}
		if bothNil {
			return ""
		}
		return String(hoursInt * 60 + minutesInt)
	}
	
	var body: some View {
		VStack {
			HStack {
				
				Text("duration_hours_abbreviation")
				
				TextField("", text: Binding(
					get: { if let hoursInt = self.hours {
						return String(hoursInt)
					} else {
						return ""
					} },
					set: { self.viewModel.value = self.valueFromHM(hours: Int($0), minutes: self.minutes) }
				))
					.keyboardType(.numberPad)
					.frame(minWidth: 100, maxWidth: 100)
					.padding()
					.border(Color("Outline"))
				
				Text("duration_minutes_abbreviation")
				
				TextField("", text: Binding(
					get: { if let minutesInt = self.minutes {
						return String(minutesInt)
					} else {
						return ""
					} },
					set: { self.viewModel.value = self.valueFromHM(hours: self.hours, minutes: Int($0)) }
				))
					.keyboardType(self.viewModel.input.numberHasDecimal ? .decimalPad : .numberPad)
					.frame(minWidth: 100, maxWidth: 100)
					.padding()
					.border(Color("Outline"))
				
			}
		}
	}
}
