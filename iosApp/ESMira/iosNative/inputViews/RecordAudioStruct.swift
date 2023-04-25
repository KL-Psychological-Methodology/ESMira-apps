//
// Created by JodliDev on 25.02.22.
//

import Foundation
import SwiftUI
import sharedCode
import AVFoundation


private class AudioClass : NSObject, ObservableObject, AVAudioPlayerDelegate, AVAudioRecorderDelegate {

	private let filePath: URL
	var audioRecorder : AVAudioRecorder!
	var audioPlayer : AVAudioPlayer!

	@Published var isRecording : Bool = false
	@Published var isPlaying : Bool = false

	init(filename: String){
		let path = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
		self.filePath = path.appendingPathComponent(filename)

		super.init()
	}
	
	
	func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
		isPlaying = false
	}
	
	private func initSession(_ onError: @escaping (String) -> Void) -> Bool {
		let recordingSession = AVAudioSession.sharedInstance()
		
		do {
			try recordingSession.setCategory(.playAndRecord, mode: .default)
			try recordingSession.setActive(true)
		} catch {
			onError("Failed to start session")
			return false
		}
		return true
	}
	
	private func startRecordingAfterPermission(_ onError: @escaping (String) -> Void) {
		let settings = [
			AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
			AVSampleRateKey: 12000,
			AVNumberOfChannelsKey: 1,
			AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
		]


		do {
			audioRecorder = try AVAudioRecorder(url: self.filePath, settings: settings)
			audioRecorder.delegate = self
			audioRecorder.prepareToRecord()
			audioRecorder.record()
			isRecording = true
		} catch {
			print("Could not start recording: \(error.localizedDescription)")
			onError("Could not start recording: \(error.localizedDescription)")
		}
	}

	func startRecording(onError: @escaping (String) -> Void) {
		if(!initSession(onError)) {
			return
		}
		let recordingSession = AVAudioSession.sharedInstance()
		
		recordingSession.requestRecordPermission() { [unowned self] isGranted in
			guard isGranted else {
				let settingURL = URL(string: UIApplication.openSettingsURLString)!
				UIApplication.shared.open(settingURL, options: [:], completionHandler: nil)
				return
			}
			self.startRecordingAfterPermission(onError)
		}
	}

	func stopRecording() {
		if(audioRecorder != nil) {
			audioRecorder.stop()
			isRecording = false
		}
	}
	
	func startAudio(_ onError: @escaping (String) -> Void) {
		if(!initSession(onError)) {
			return
		}
		let playSession = AVAudioSession.sharedInstance()
		
		do {
			try playSession.overrideOutputAudioPort(AVAudioSession.PortOverride.speaker)
		} catch {
			onError("Could not start play session")
			return
		}
		
		do {
			audioPlayer = try AVAudioPlayer(contentsOf: self.filePath)
			audioPlayer.delegate = self
			audioPlayer.prepareToPlay()
			audioPlayer.play()
			isPlaying = true
		}
		catch {
			onError("Failed to play")
		}
	}
	
	func stopAudio() {
		if(audioRecorder != nil) {
			audioPlayer.stop()
			isPlaying = false
		}
	}
}

struct RecordAudioStruct: View {
	@EnvironmentObject var appState: AppState
	@ObservedObject var viewModel: InputViewModel

	@State private var fileExists: Bool
	
	private let filename: String
	@ObservedObject private var audioClass: AudioClass
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
		if let filename = viewModel.input.getFileName() {
			self._fileExists = State(initialValue: true)
			self.filename = filename
		}
		else {
			self._fileExists = State(initialValue: false)
			self.filename = "\(Date().timeIntervalSince1970).mp4"
		}
		self.audioClass = AudioClass(filename: self.filename)
	}
	
	
	
	var body: some View {
		VStack {
			if(audioClass.isRecording) {
				DefaultIconButton(icon: "stop.circle", label: "stop_audio_record", maxWidth: 200) {
					self.fileExists = true
					self.audioClass.stopRecording()
					self.viewModel.input.setFile(filePath: self.filename, dataType: FileUpload.DataTypes.audio)
				}
			}
			else {
				DefaultIconButton(icon: "mic", label: "start_audio_record", maxWidth: 200, disabled: self.audioClass.isPlaying) {
					self.audioClass.startRecording { msg in
						self.appState.showToast(msg)
					}
				}
			}
			
			
			if(audioClass.isPlaying) {
				DefaultIconButton(icon: "stop.circle", label: "stop_playing_audio", maxWidth: 200) {
					self.audioClass.stopAudio()
				}
			}
			else {
				DefaultIconButton(icon: "play.circle.fill", label: "start_playing_audio", maxWidth: 200, disabled: !self.fileExists || self.audioClass.isRecording) {
					self.audioClass.startAudio{ msg in
						self.appState.showToast(msg)
					}
				}
			}
		}
	}
}
