//
// Created by JodliDev on 05.08.20.
//

import Foundation
import SwiftUI

struct MultilineTextField: UIViewRepresentable {
	@Binding var text: String
	func makeCoordinator() -> Coordinator {
		Coordinator(self)
	}
	
	func makeUIView(context: Context) -> UITextView {
		let view = UITextView()
		view.isScrollEnabled = true
		view.isEditable = true
		view.isUserInteractionEnabled = true
		view.font = UIFont.systemFont(ofSize: 17.0)
		view.delegate = context.coordinator
		return view
	}
	func updateUIView(_ uiView: UITextView, context: Context) {
		uiView.text = self.text
	}
	
	class Coordinator : NSObject, UITextViewDelegate {
		var parent: MultilineTextField
		
		init(_ uiTextView: MultilineTextField) {
			self.parent = uiTextView
		}
		
		func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
			return true
		}
		
		func textViewDidChange(_ textView: UITextView) {
			self.parent.text = textView.text
		}
	}
}