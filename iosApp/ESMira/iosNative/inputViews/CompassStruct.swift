//
// Created by JodliDev on 21.04.23.
//

import Foundation
import SwiftUI
import sharedCode
import Combine
import UIKit
import CoreLocation

class CompassManager: NSObject, ObservableObject, CLLocationManagerDelegate {
	var objectWillChange = PassthroughSubject<Void, Never>()
	var didReceiveValue = false
	var degrees: Double = .zero {
		didSet {
			objectWillChange.send()
			didReceiveValue = true
		}
	}
	
	private let locationManager: CLLocationManager
	
	override init() {
		self.locationManager = CLLocationManager()
		super.init()
		
		self.locationManager.delegate = self
		locationManager.headingFilter = 1
	}
	
	func start() {
		self.locationManager.requestWhenInUseAuthorization()
		
		if(CLLocationManager.headingAvailable()) {
//			self.locationManager.startUpdatingLocation()
			self.locationManager.startUpdatingHeading()
		}
	}
	func stop() {
//		self.locationManager.stopUpdatingLocation()
		self.locationManager.stopUpdatingHeading()
	}
	
	func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
		self.degrees = newHeading.magneticHeading
	}
}

struct CompassStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	@ObservedObject var compassManager = CompassManager()
	@State private var isScanning = false
	@State private var didReceiveReading = false
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
	}
		
	var body: some View {
		VStack(alignment: .center) {
			if(isScanning) {
				CompassContent(
					rotation: compassManager.degrees,
					showValue: viewModel.input.showValue,
					infoLabel: "\(Int(compassManager.degrees))°",
					buttonLabel: NSLocalizedString("stop_scanning", comment: "stop_scanning"),
					buttonAction: {
						compassManager.stop()
						if compassManager.didReceiveValue {
							if viewModel.input.numberHasDecimal {
								viewModel.value = String(compassManager.degrees)
							} else {
								viewModel.value = String(Int(compassManager.degrees))
							}
						}
						isScanning = false
					}
				)
			}
			else if(viewModel.value == "") {
				CompassContent(
					rotation: 0.0,
					showValue: false,
					infoLabel: "",
					buttonLabel: NSLocalizedString("start_scanning", comment: "start_scanning"),
					buttonAction: {
						isScanning = true
						compassManager.start()
					}
				)
			}
			else {
				let value = Double(viewModel.value) ?? 0
				CompassContent(
					rotation: value,
					showValue: viewModel.input.showValue,
					infoLabel: "\(Int(value))°",
					buttonLabel: NSLocalizedString("start_scanning", comment: "start_scanning"),
					buttonAction: {
						isScanning = true
						compassManager.start()
					}
				)
			}
		}
	}
}

struct CompassContent: View {
	let rotation: Double
	let showValue: Bool
	let infoLabel: String
	let buttonLabel: String
	let buttonAction: () -> Void
	
	var body: some View {
		ZStack(alignment: .center) {
			ZStack(alignment: .center) {
				Circle()
					.stroke(Color("Outline"), style: StrokeStyle(lineWidth: 2, dash: [10]))
					.frame(width: 200, height: 200)
				if(showValue) {
					VStack {
						Text("north_abr")
						Spacer()
						Text("south_abr").rotationEffect(Angle(degrees: 180))
					}.padding(5)
					HStack {
						Text("west_abr").rotationEffect(Angle(degrees: 270))
						Spacer()
						Text("east_abr").rotationEffect(Angle(degrees: 90))
					}.padding(10)
				}
			}
			.rotationEffect(Angle(degrees: -rotation))
			
			VStack {
				Image(systemName: "location.north.line").resizable().frame(width: 30, height: 30)
				Spacer()
			}.padding(.top, 25)
			
			VStack {
				if(showValue) {
					Text(infoLabel).font(.title)
				}
				Button(buttonLabel, action: buttonAction)
			}
		}
		.frame(width: 200, height: 200)
	}
}
