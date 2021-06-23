//
// Created by JodliDev on 28.08.20.
//

import SwiftUI
import UIKit
import MobileCoreServices

struct OpenFilePickerView: UIViewControllerRepresentable {
	var callback: (URL) -> ()
	func makeUIViewController(context: Context) -> UIDocumentPickerViewController {
		let controller = UIDocumentPickerViewController(documentTypes: [String(kUTTypeItem)], in: .import)
		controller.delegate = context.coordinator
		
		return controller
	}
	
	func updateUIViewController(_ uiViewController: UIDocumentPickerViewController, context: UIViewControllerRepresentableContext<OpenFilePickerView>) {
	
	}
	
	
	func makeCoordinator() -> Coordinator {
		Coordinator(self)
	}
	
	class Coordinator: NSObject, UIDocumentPickerDelegate {
		let parent: OpenFilePickerView
		
		init(_ parent: OpenFilePickerView) {
			self.parent = parent
		}
		
		func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentAt url: URL) {
			parent.callback(url)
		}
		
		func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
			print("Canceled")
		}

//		func documentPicker(didPickDocumentsAt: [URL]) {
//			parent.callback(didPickDocumentsAt[0])
//		}
	}
}
