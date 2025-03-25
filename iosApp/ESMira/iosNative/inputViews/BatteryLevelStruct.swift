//
//  BatteryLevelStruct.swift
//  ESMira
//
//  Created by Karl Landsteiner Privatuniversität on 03.03.25.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import sharedCode

struct BatteryLevelStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	@State var charging = ""
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
		self.charging = self.viewModel.input.getAdditional(key: "charging") ?? ""
	}
	
	private func getText() -> String {
		var text = "- %"
		if !self.viewModel.value.isEmpty {
			text = String(self.viewModel.value) + " %"
			if self.charging == "1" {
				text += " (" + NSLocalizedString("charging", comment: "") + ")"
			}
		}
		return text
	}
	
	var body: some View {
		VStack {
			Text(self.getText())
			Spacer()
			DefaultButton(String(NSLocalizedString("measure", comment: ""))) {
				UIDevice.current.isBatteryMonitoringEnabled = true
				let level = Int(UIDevice.current.batteryLevel * 100)
				let state = UIDevice.current.batteryState
				let charging = (state == UIDevice.BatteryState.charging || state == UIDevice.BatteryState.charging) ? "1" : "0"
				self.charging = charging
				self.viewModel.setAdditionalValue(value: String(level), additionalValues: ["charging": charging])
				UIDevice.current.isBatteryMonitoringEnabled = false
			}
		}
	}
	
	
}
