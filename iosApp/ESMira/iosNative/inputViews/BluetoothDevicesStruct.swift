//
// Created by JodliDev on 11.07.23.
//

import Foundation
import SwiftUI
import sharedCode
import CoreBluetooth

var totalScanSeconds: Int64 = 60

open class BLEConnection: NSObject, CBCentralManagerDelegate, ObservableObject {
	@Published var discoveredDevices: Dictionary<String, Int> = Dictionary()
	@Published var isScanning: Bool = false
	
	private var centralManager: CBCentralManager! = nil
	private var saveData: (String, Dictionary<String, String>) -> Void = {_, _ in }
	
	
	func startCentralManager(saveData: @escaping (String, Dictionary<String, String>) -> Void) {
		self.saveData = saveData
		self.centralManager = CBCentralManager(delegate: self, queue: nil)
	}
	
	func stopScanning() {
		self.centralManager.stopScan()
		self.isScanning = false
		
		do {
			let jsonData = try JSONSerialization.data(withJSONObject: self.discoveredDevices, options: .prettyPrinted)
			
			if let jsonString = String(data: jsonData, encoding: .utf8) {
				var additionalValues = Dictionary<String, String>()
				additionalValues["devices"] = jsonString
				self.saveData(String(self.discoveredDevices.count), additionalValues)
			}
		}
		catch {
			
		}
	}
	
	public func centralManagerDidUpdateState(_ central: CBCentralManager) {
		if(self.centralManager.state == .poweredOn) {
			self.centralManager.scanForPeripherals(withServices: nil, options: nil)
			self.isScanning = true
		}
	}


	// Handles the result of the scan
	public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
		self.discoveredDevices[Input.Companion().anonymizeValue(s: peripheral.identifier.uuidString)] = RSSI.intValue
	}
}


struct BluetoothDevicesStruct: View {
	
	@ObservedObject var viewModel: InputViewModel
	
	@ObservedObject var bleConnection = BLEConnection()
	@State var showData = false
	
	@State private var progress = Progress(totalUnitCount: totalScanSeconds)
	let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
	}
	
	var body: some View {
		VStack(alignment: .center) {
			if(bleConnection.isScanning) {
				CustomProgressBarView(self.progress, showUnitCount: false)
					.onReceive(timer) { timer in
						self.progress.completedUnitCount += 1
						if(self.progress.isFinished) {
							bleConnection.stopScanning()
						}
					}
			}
			else if(!viewModel.value.isEmpty) {
				DefaultButton("show_data") {
					self.showData = true
				}
			}
			
			DefaultIconButton(icon: "antenna.radiowaves.left.and.right", label: "start_scanning", disabled: self.bleConnection.isScanning) {
				self.progress.completedUnitCount = 0
				bleConnection.startCentralManager() { value, additionalValues in
					self.viewModel.setAdditionalValue(value: value, additionalValues: additionalValues)
				}
			}
		}
		.sheet(isPresented: self.$showData) {
			VStack {
				self.getDataList()
			}
		}
	}
	
	func getDataList() -> some View {
		var dict: [String: Int]
		do {
			print(viewModel.input.getAdditional(key: "devices") ?? "no")
			let decoded = try JSONSerialization.jsonObject(with: (viewModel.input.getAdditional(key: "devices") ?? "").data(using: .utf8) ?? Data(), options: [])
			
			dict = decoded as? [String: Int] ?? Dictionary()
			
		}
		catch {
			dict = Dictionary()
			print("error")
		}
		return ScrollView {
			HStack {
				VStack(alignment: .trailing) {
					Text("anonymised_device").bold()
					Spacer()
					ForEach(Array(dict.keys), id: \.self) { key in
						Text(key)
					}
				}
				VStack(alignment: .leading) {
					Text("distance_rssi").bold()
					Spacer()
					ForEach(Array(dict.keys), id: \.self) { key in
						let rssi = dict[key] ?? 0
						Text(String(format: NSLocalizedString("distance_rssi_content", comment: ""), Input.Companion().rssiToDistance(rssi: Int32(rssi)), rssi))
//						Text(String(format: "\(Input.Companion().rssiToDistance(rssi: Int32(rssi))) (%.2d)", rssi))
					}
				}.padding(.leading, 10)
			}
		}.padding()
	}
}
