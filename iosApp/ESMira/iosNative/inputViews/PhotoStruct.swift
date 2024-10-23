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
							let percentage = min(max(self.viewModel.input.size, 1), 100)
							if percentage < 100 {
								let factor = CGFloat(percentage) / 100.0
								
								let newSize = CGSize(width: img.size.width * factor, height: img.size.height * factor)
								let renderer = UIGraphicsImageRenderer(size: newSize)
								let scaledImg = renderer.image { _ in
									img.draw(in: CGRect(origin: .zero, size: newSize))
								}
								try scaledImg.pngData()?.write(to: filePath, options: [])
							} else {
								try img.pngData()?.write(to: filePath, options: [])
							}
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

extension UIImage {
	func imageResized(percentage: Int32) -> UIImage {
		let factor = Double(percentage) / 100.0
		let newSize = CGSize(width: size.width.scaled(by: factor), height: size.height.scaled(by: factor))
		return UIGraphicsImageRenderer(size: newSize).image { _ in
			draw(in: CGRect(origin: .zero, size: newSize))
		}
	}
}
