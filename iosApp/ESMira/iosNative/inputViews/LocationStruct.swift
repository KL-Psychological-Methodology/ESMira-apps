//
//  Created by SelinaDev on 28.02.24.
//

import Foundation
import SwiftUI
import sharedCode
import CoreLocation

var maxScanSeconds: Int64 = 60
var maxLocationAgeSeconds: TimeInterval = 5 * 60
var minResolution: Int32 = 0
var maxResolution: Int32 = 9

open class LocationScanner: NSObject, CLLocationManagerDelegate, ObservableObject {
	private var locationManager: CLLocationManager! = nil
	private var saveData: (String) -> Void = {_ in}
	private var resolution: Int32 = 0
	
	@Published var index: String = ""
	@Published var isScanning: Bool = false
	
	func startScanning(saveData: @escaping (String) -> Void, res: Int32) {
		self.isScanning = true
		self.locationManager = CLLocationManager()
		self.locationManager.requestWhenInUseAuthorization()
		self.locationManager.delegate = self
		self.saveData = saveData
		self.resolution = res
		
		let cachedLocation = getCachedLocation()
		if cachedLocation != nil {
			setLocation(location: cachedLocation!)
		} else {
			getCurrentLocation()
		}
	}
	
	func stopScanning() {
		locationManager.delegate = nil
		self.isScanning = false
	}
	
	private func getCachedLocation() -> CLLocation? {
		var cachedLocation: CLLocation? = locationManager.location
		if cachedLocation != nil {
			if abs(cachedLocation?.timestamp.timeIntervalSinceNow ?? maxLocationAgeSeconds + 1) > maxLocationAgeSeconds {
				cachedLocation = nil
			}
		}
		return cachedLocation
	}
	
	private func getCurrentLocation() {
		locationManager.requestLocation()
	}
	
	private func setLocation(location: CLLocation) {
		self.index = ""
		
		let geo = LatLng.companion.fromDegrees(latDegs: location.coordinate.latitude, lngDegs: location.coordinate.longitude)
		let effectiveResolution = min(maxResolution, max(minResolution, resolution))
		let h3 = geo.latLngToCell(res: effectiveResolution)
		if h3 != 0 {
			self.index = H3.companion.h3toString(h: h3)
			self.saveData(self.index)
		}
		self.isScanning = false
	}
	
	public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
		self.isScanning = false
	}
	
	public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
		if !locations.isEmpty {
			if let location = locations.first {
				setLocation(location: location)
			}
		}
	}
}

struct LocationStruct: View {
	@ObservedObject var viewModel: InputViewModel
	@ObservedObject var locationScanner = LocationScanner()
	let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
	@State private var progress = Progress(totalUnitCount: maxScanSeconds)
	@State private var scanningString = NSLocalizedString("scanning", comment: "")
	@State private var ellipsisDots = 1
	@State private var attemptedScan = false
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
	}
	
	var body: some View {
		VStack(alignment: .center) {
			if(locationScanner.isScanning) {
				Text("\(scanningString)").onReceive(timer) { timer in
					self.ellipsisDots = (self.ellipsisDots + 1) % 3
					self.scanningString = NSLocalizedString("scanning", comment: "")
					for i in 0...ellipsisDots {
						self.scanningString += "."
					}
					self.progress.completedUnitCount += 1
					if self.progress.isFinished {
						self.locationScanner.stopScanning()
					}
				}
			} else if self.attemptedScan {
				if self.viewModel.value.isEmpty {
					Text("could_not_determine_location")
				} else {
					Text(String(format: NSLocalizedString("found_location", comment: ""), self.viewModel.value))
				}
			}
			
			DefaultIconButton(icon: "location", label: "start_scanning", disabled: false) {
				self.progress.completedUnitCount = 0
				self.attemptedScan = true
				locationScanner.startScanning( saveData: { value in
					self.viewModel.value = value
				}, res: self.viewModel.input.resolution)
			}
		}
	}
}
