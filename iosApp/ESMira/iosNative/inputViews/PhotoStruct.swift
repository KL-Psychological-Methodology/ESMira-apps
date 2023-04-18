//
// Created by JodliDev on 25.02.22.
//

import Foundation
import SwiftUI
import sharedCode

struct PhotoStruct: View {
	@ObservedObject var viewModel: InputViewModel

	@State private var isShown = false
	@State private var uiImage: UIImage? = nil
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			
			if(self.uiImage != nil) {
				Image(uiImage: self.uiImage!)
					.resizable()
					.scaledToFit()
					.frame(height: 300)
			}
			
			Button(action: {
				self.isShown = true
			}) {
				HStack {
					Image(systemName: "camera")
					Text("take_picture").bold()
				}
				.foregroundColor(Color("PrimaryDark"))
			}
				.padding()
				.border(Color("Outline"))
		}

			.sheet(isPresented: self.$isShown) {
				if UIImagePickerController.isSourceTypeAvailable(.camera) {
					CameraView { img in
						self.uiImage = img
						self.isShown = false
						let filename = String(Date().timeIntervalSince1970) + ".png"
						let documentsUrl =  FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
						let filePath = documentsUrl.appendingPathComponent(filename)
						do {
							try img.pngData()?.write(to: filePath, options: [])
							self.viewModel.value = ""
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
}
