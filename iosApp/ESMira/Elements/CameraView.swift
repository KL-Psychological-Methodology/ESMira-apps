//
// Created by JodliDev on 25.02.22.
// Thanks to https://itnext.io/building-a-lightweight-camera-app-in-swiftui-66db47b3537f
//

import Foundation
import UIKit
import SwiftUI

struct CameraView: UIViewControllerRepresentable {
	typealias UIViewControllerType = UIImagePickerController

	var callback: (UIImage) -> ()

	func makeUIViewController(context: Context) -> UIViewControllerType {
		let viewController = UIViewControllerType()
		viewController.delegate = context.coordinator
		viewController.sourceType = .camera
		return viewController
	}

	func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {

	}

	func makeCoordinator() -> CameraView.Coordinator {
		return Coordinator(self)
	}
}

extension CameraView {
	class Coordinator : NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
		var parent: CameraView

		init(_ parent: CameraView) {
			self.parent = parent
		}

		func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
			print("Cancel pressed")
		}

		func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
			if let image = info[UIImagePickerController.InfoKey.originalImage] as? UIImage {
				parent.callback(image)
			}
		}
	}
}
