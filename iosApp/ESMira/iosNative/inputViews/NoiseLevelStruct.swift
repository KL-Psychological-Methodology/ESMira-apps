import SwiftUI
import AVFAudio
import sharedCode

private class NoiseLevelMeasurement: NSObject, ObservableObject {
	private let engine: AVAudioEngine!
	private let duration: Int
	private var currentWindow: Int
	private var currentSample: Int
	private var sumWindow: Double
	private var sumSquaresWindow: Double
	private var sumTotal: Double
	private var sumSquaresTotal: Double
	private var rmsMin: Double
	private var rmsMax: Double
	private var saveVaules: (String, Dictionary<String, String>) -> Void = {_, _ in }
	
	@Published var progress: Progress
	
	init(duration: Int) {
		self.engine = AVAudioEngine()
		self.duration = duration
		self.currentWindow = 0
		self.currentSample = 0
		self.sumWindow = 0.0
		self.sumSquaresWindow = 0.0
		self.sumTotal = 0.0
		self.sumSquaresTotal = 0.0
		self.rmsMin = Double.nan
		self.rmsMax = Double.nan
		self.progress = Progress(totalUnitCount: 0)
		
		super.init()
	}
	
	func start(stopRecording: @escaping () -> Void, saveValues: @escaping (String, Dictionary<String, String>) -> Void) {
		if(self.engine.isRunning){
			return
		}
		self.saveVaules = saveValues
		let recordingSession = AVAudioSession.sharedInstance()
		
		recordingSession.requestRecordPermission() { isGranted in
			guard isGranted else {
				let settingURL = URL(string: UIApplication.openSettingsURLString)!
				UIApplication.shared.open(settingURL, options: [:], completionHandler: nil)
				return
			}
		}
		
		self.saveVaules("", ["min": "", "max": ""])
		self.currentWindow = 0
		self.currentSample = 0
		self.sumWindow = 0.0
		self.sumSquaresWindow = 0.0
		self.sumTotal = 0.0
		self.sumSquaresTotal = 0.0
		self.rmsMin = Double.nan
		self.rmsMax = Double.nan
		
		
		let input = self.engine.inputNode
		let bus = 0
		let format = input.inputFormat(forBus: bus)
		let sampleRate = format.sampleRate
		
		self.progress = Progress(totalUnitCount: Int64(self.duration * Int(sampleRate)))
		self.engine.inputNode.removeTap(onBus: bus)
		input.installTap(onBus: bus, bufferSize: 1024, format: format) { buffer, _ in
			let samples = buffer.floatChannelData![0]
			let frames = Int(buffer.frameLength)
			
			for i in 0..<frames {
				self.currentSample += 1
				
				if(self.currentSample > Int(sampleRate)) {
					self.currentSample = 0
					let mean = self.sumWindow / sampleRate
					let meanOfSquares = self.sumSquaresWindow / sampleRate
					let rms = sqrt(meanOfSquares - mean * mean)
					if(self.rmsMax.isNaN || self.rmsMax < rms) {
						self.rmsMax = rms
					}
					if(self.rmsMin.isNaN || self.rmsMin > rms) {
						self.rmsMin = rms
					}
					self.sumTotal += self.sumWindow
					self.sumSquaresTotal += self.sumSquaresWindow
					self.sumWindow = 0.0
					self.sumSquaresWindow = 0.0
					
					self.currentWindow += 1
				
					if(self.currentWindow >= self.duration) {
						self.stop()
						DispatchQueue.main.async {
							stopRecording()
							self.finish(sampleRate: sampleRate)
						}
						return
					}
					
				}
				
				let sample = Double(samples[i])
				self.sumWindow += sample
				self.sumSquaresWindow += sample * sample
			}
			DispatchQueue.main.async {
				self.progress.completedUnitCount += Int64(frames)
			}
		}
		
		do {
			try engine.start()
		} catch {
			return
		}
	}
	
	private func finish(sampleRate: Double) {
		let mean = self.sumTotal / (sampleRate * Double(self.duration))
		let meanOfSquares = self.sumSquaresTotal / (sampleRate * Double(self.duration))
		let rms = sqrt(meanOfSquares - mean * mean)
		let dbfsTotal = round(20.0 * log10(rms) * 100.0) / 100.0
		let dbfsMin = round(20.0 * log10(self.rmsMin) * 100.0) / 100.0
		let dbfsMax = round(20.0 * log10(self.rmsMax) * 100.0) / 100.0
		
		self.saveVaules(String(dbfsTotal), ["min": String(dbfsMin), "max": String(dbfsMax)])
	}
	
	func stop() {
		self.engine.inputNode.removeTap(onBus: 0)
		self.engine.stop()
	}
	
	
}

struct NoiseLevelStruct: View {
	@ObservedObject var viewModel: InputViewModel
	private var duration: Int
	@State private var measurement: NoiseLevelMeasurement
	@State private var isRecording: Bool = false
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
		self.duration = Int(viewModel.input.timeoutSec)
		self.measurement = NoiseLevelMeasurement(duration: self.duration)
	}
	
	var body: some View {
		VStack {
			if(self.isRecording) {
				CustomProgressBarView(self.measurement.progress, showUnitCount: false)
				DefaultIconButton(icon: "stop.circle", label: "stop_audio_record", maxWidth: 200) {
					self.measurement.stop()
					self.isRecording = false
				}
			} else {
				if(!self.viewModel.value.isEmpty) {
					Text(String(format: NSLocalizedString("measured_noise_level", comment: ""), self.viewModel.value))
				}
				DefaultIconButton(icon: "mic", label: "start_audio_record", maxWidth: 200) {
					self.measurement.start(stopRecording: {
						self.isRecording = false
					}) { value, additionalValues in
						self.viewModel.setAdditionalValue(value: value, additionalValues: additionalValues)
					}
					self.isRecording = true
				}
			}
		}
	}
}
