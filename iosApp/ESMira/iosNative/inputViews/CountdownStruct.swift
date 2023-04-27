//
// Created by JodliDev on 21.04.23.
//

import Foundation
import SwiftUI
import sharedCode
import Combine
import AVFoundation

struct CountdownStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	@State private var timerIsRunning = false {
		didSet {
			if(timerIsRunning) {
				timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
			}
			else if(timer != nil){
				timer!.upstream.connect().cancel()
				timer = nil
			}
		}
	}
	@State private var ticks: Int
	@State private var timer: Publishers.Autoconnect<Timer.TimerPublisher>? = nil
//	@State private var timer: Publishers.Autoconnect<Timer.TimerPublisher> = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
		self._ticks = State(initialValue: Int(viewModel.input.timeoutSec))
	}
	
	var body: some View {
		VStack {
			if(viewModel.value == "1") {
				Image(systemName: "checkmark.circle.fill").foregroundColor(Color.green)
			}
			else if(timerIsRunning) {
				Text(String(ticks)).font(.title)
					.onReceive(timer!) { time in
						ticks -= 1
						if(ticks <= 0) {
							viewModel.value = "1"
							timerIsRunning = false
							if(viewModel.input.playSound) {
								AudioServicesPlaySystemSound(1322)
							}
						}
					}
				}
			else {
				DefaultIconButton(icon: "play.circle.fill", label: "start_timer") {
					timerIsRunning = true
					viewModel.value = "0"
				}
			}
		}
	}
}
