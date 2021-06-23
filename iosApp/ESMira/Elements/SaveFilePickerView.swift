//
// Created by JodliDev on 28.08.20.
//

import UIKit
import SwiftUI

struct SaveFilePickerView: UIViewControllerRepresentable {
	var activityItems: [Any]
	var applicationActivities: [UIActivity]? = nil
	
	func makeUIViewController(context: UIViewControllerRepresentableContext<SaveFilePickerView>) -> UIActivityViewController {
		UIActivityViewController(activityItems: activityItems, applicationActivities: applicationActivities)
	}
	
	func updateUIViewController(_ uiViewController: UIActivityViewController, context: UIViewControllerRepresentableContext<SaveFilePickerView>) {
	
	}
}
