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
				.foregroundColor(Color("Accent"))
			}
		}

			.sheet(isPresented: self.$isShown) {
				if UIImagePickerController.isSourceTypeAvailable(.camera) {
					CameraView { img in
						self.viewModel.value = img.pngData()?.base64EncodedString() ?? ""
						self.uiImage = img
						self.isShown = false
					}
				}
				else {
					Text("error_no_camera")
				}
			}
	}
}
