//
// Created by JodliDev on 05.07.23.
//

import Foundation
import SwiftUI
import sharedCode

struct FileUploadStruct: View {
	@EnvironmentObject var appState: AppState
	@ObservedObject var viewModel: InputViewModel
	let filename: String

	@State private var showSheet = false
//	@State private var uiImage: UIImage?
	@State private var fileURL: URL?
	
	init(viewModel: InputViewModel) {
		self.viewModel = viewModel
		if let filename = viewModel.input.getFileName() {
			self.filename = filename
			self._fileURL = State(initialValue: getURL(filename))
		}
		else {
			self.filename = String(Date().timeIntervalSince1970) + ".png"
//			self._uiImage = State(initialValue: nil)
			self._fileURL = State(initialValue: nil)
		}
	}
	private func getImageData() -> UIImage? {
		if(self.fileURL == nil) {
			return nil
		}
		do {
			let data = try Data(contentsOf: self.fileURL!)
			return UIImage(data: data)
		}
		catch {
			return nil
		}
	}
	
	var body: some View {
		VStack {
			if let image = self.getImageData() {
				Image(uiImage: image)
					.resizable()
					.scaledToFit()
					.frame(height: 300)
			}
//			if(self.fileURL != nil) {
//				Image(uiImage: self.uiImage!)
//					.resizable()
//					.scaledToFit()
//					.frame(height: 300)
//			}
			
			DefaultIconButton(icon: "camera", label: "select_picture") {
				self.showSheet = true
			}
		}
		
			.sheet(isPresented: self.$showSheet) {
				VStack {
					Button("cancel") {
						self.showSheet = false
					}.padding()
					OpenFilePickerView() { url in
						let outsideAccess = url.startAccessingSecurityScopedResource()
						defer {
							if(outsideAccess) {
								url.stopAccessingSecurityScopedResource()
							}
						}
						
						do {
							let toUrl = getURL(self.filename)
							try FileManager.default.copyItem(at: url, to: toUrl)
							self.viewModel.input.setFile(filePath: self.filename, dataType: FileUpload.DataTypes.image)
							self.fileURL = toUrl
						}
						catch {
							//do nothing
						}
						
						self.showSheet = false
					}
				}
			}
	}
	
	private func getURL(_ filename: String) -> URL {
		let documentsUrl =  FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
		return documentsUrl.appendingPathComponent(filename)
	}
}
