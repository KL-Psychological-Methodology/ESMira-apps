//
// Created by JodliDev on 25.02.22.
//

import Foundation
import SwiftUI
import sharedCode

struct PhotoStruct: View {
	@ObservedObject var viewModel: InputViewModel
	let filename: String

	@State private var isShown = false
	@State private var uiImage: UIImage?
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
		if let filename = viewModel.input.getFileName() {
			self.filename = filename
			do {
				let data = try Data(contentsOf: getURL(filename))
				self._uiImage = State(initialValue: UIImage(data: data))
			}
			catch {
				self._uiImage = State(initialValue: nil)
			}
		}
		else {
			self.filename = String(Date().timeIntervalSince1970) + ".png"
			self._uiImage = State(initialValue: nil)
		}
	}
	var body: some View {
		VStack {
			if(self.uiImage != nil) {
				Image(uiImage: self.uiImage!)
					.resizable()
					.scaledToFit()
					.frame(height: 300)
			}
			
			DefaultIconButton(icon: "camera", label: "take_picture") {
				self.isShown = true
			}
		}

			.sheet(isPresented: self.$isShown) {
				if UIImagePickerController.isSourceTypeAvailable(.camera) {
					CameraView { img in
						self.uiImage = img
						self.isShown = false
						
						let filePath = getURL(self.filename)
						do {
							try img.pngData()?.write(to: filePath, options: [])
							self.viewModel.input.setFile(filePath: self.filename, dataType: FileUpload.DataTypes.image)
						} catch {
							ErrorBox.Companion().error(title: "PhotoStruct", msg: "Could not save image from camera. Error: \(error)")
						}
					}
				}
				else {
					Text("error_no_camera")
				}
			}
	}
	
	private func getURL(_ filename: String) -> URL {
		let documentsUrl =  FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
		return documentsUrl.appendingPathComponent(filename)
	}
}
