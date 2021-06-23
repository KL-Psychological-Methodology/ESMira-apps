//
// Created by JodliDev on 15.10.20.
// Thanks to:
// https://stackoverflow.com/questions/56726663/how-to-add-a-textfield-to-alert-in-swiftui
//

import SwiftUI



struct TextFieldAlert: UIViewControllerRepresentable {
	class TextFieldAlertViewController: UIViewController {
		init(title: String, text: Binding<String>, isPresented: Binding<Bool>, listener: (() -> Void)? = nil) {
			self.alertTitle = title
			self.text = text
			self.isPresented = isPresented
			self.listener = listener
			super.init(nibName: nil, bundle: nil)
		}
		
		required init?(coder: NSCoder) {
			fatalError("init(coder:) has not been implemented")
		}
		
		private let alertTitle: String
		private let message: String = ""
		private var text: Binding<String>
		private var isPresented: Binding<Bool>
		private var listener: (() -> Void)? = nil
		
		
		override func viewDidAppear(_ animated: Bool) {
			super.viewDidAppear(animated)
			presentAlertController()
		}
		
		private func presentAlertController() {
			let alert = UIAlertController(title: NSLocalizedString(alertTitle, comment: ""), message: message, preferredStyle: .alert)
			
			alert.addTextField { [weak self] textField in
				textField.text = self?.text.wrappedValue
			}
			
			alert.addAction(UIAlertAction(title: NSLocalizedString("cancel", comment: ""), style: .cancel) { [weak self] _ in
				self?.isPresented.wrappedValue = false
			})
			alert.addAction(UIAlertAction(title: NSLocalizedString("ok_", comment: ""), style: .default) { [weak self] _ in
				self?.isPresented.wrappedValue = false
				self?.text.wrappedValue = alert.textFields?.first?.text ?? ""
				self?.listener?()
			})
			
			present(alert, animated: true, completion: nil)
		}
	}
	@Binding var isPresented: Bool
	@Binding var text: String

	let title: String
	var listener: (() -> Void)? = nil

	init(isPresented: Binding<Bool>, text: Binding<String>, title: String, listener: (() -> Void)? = nil) {
		self._isPresented = isPresented
		self._text = text
		self.title = title
		self.listener = listener
	}
	
	
	typealias UIViewControllerType = TextFieldAlertViewController
	
	func makeUIViewController(context: UIViewControllerRepresentableContext<TextFieldAlert>) -> UIViewControllerType {
		TextFieldAlertViewController(title: title, text: $text, isPresented: $isPresented, listener: listener)
	}
	
	func updateUIViewController(_ uiViewController: UIViewControllerType, context: UIViewControllerRepresentableContext<TextFieldAlert>) {
		// no update needed
	}
}

extension View {
	func textFieldAlert(isPresented: Binding<Bool>, text: Binding<String>, title: String, listener: (() -> Void)? = nil) -> some View {
		ZStack {
			if (isPresented.wrappedValue) {
				TextFieldAlert(isPresented: isPresented, text: text, title: title, listener: listener)
			}
			self
		}
	}
}

