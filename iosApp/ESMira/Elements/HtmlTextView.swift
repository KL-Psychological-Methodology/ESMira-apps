//
// Created by JodliDev on 09.06.20.
// Thanks to https://stackoverflow.com/questions/56892691/how-to-show-html-or-markdown-in-a-swiftui-text
// And thanks to https://stackoverflow.com/a/70401810/10423612
//

import SwiftUI


protocol StringFormatter {
	func format(string: String) -> NSAttributedString?
}
class HTMLFormatter: StringFormatter {
	func format(string: String) -> NSAttributedString? {
		guard let data = string.data(using: .utf8),
			  let attributedText = try? NSAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding: String.Encoding.utf8.rawValue], documentAttributes: nil)
		else { return nil }
		
		return attributedText
	}
}

struct AttributedText: UIViewRepresentable {
	typealias UIViewType = UITextView
	
	let text: String
	@Binding var isReady: Bool
	private let formatter: StringFormatter = HTMLFormatter()
	@State private var attributedText: NSAttributedString? = nil
	
	
	
	private func colorToHex(_ color: UIColor) -> String {
		return color.hexString
	}
	
	func makeUIView(context: Context) -> UIViewType {
		let view = ContentTextView()
		view.setContentHuggingPriority(.required, for: .vertical)
		view.setContentHuggingPriority(.required, for: .horizontal)
		view.contentInset = .zero
		view.textContainer.lineFragmentPadding = 0
		view.backgroundColor = .clear
		return view
	}
	
	func updateUIView(_ uiView: UITextView, context: Context) {
		guard let attributedText = attributedText else {
			generateAttributedText(uiView)
			return
		}
		
		uiView.attributedText = attributedText
		uiView.invalidateIntrinsicContentSize()
	}
	
	private func generateAttributedText(_ uiView: UITextView) {
		guard attributedText == nil else { return }
		// create attributedText on main thread since HTML formatter will crash SwiftUI
		DispatchQueue.main.async {
			self.attributedText = self.formatter.format(string: "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"/></head><body style=\"font-size: \(UILabel().font.pointSize)px; color: \(colorToHex(UITextView().textColor!)); font-family: system-ui, -apple-system, sans-serif\">\(self.text)</body></html>")
			if(!self.isReady) {
				 self.isReady = true
			 }
		}
	}
	
	/// ContentTextView
	/// subclass of UITextView returning contentSize as intrinsicContentSize
	private class ContentTextView: UITextView {
		override var canBecomeFirstResponder: Bool { false }
		
		override var intrinsicContentSize: CGSize {
			frame.height > 0 ? contentSize : super.intrinsicContentSize
		}
	}
}


struct HtmlTextView: View {
	let html: String
	@Binding var isReady: Bool

	var body: some View {
		AttributedText(text: html, isReady: self.$isReady)
			.frame(minHeight: 1)
	}
}


struct ScrollableHtmlTextView: View {
	let html: String
	@State var isReady: Bool = false

	var body: some View {
		ZStack(alignment: .top) {
			ScrollView {
				AttributedText(text: html, isReady: self.$isReady)
					.frame(minHeight: 1)
					.disabled(!self.isReady)
			}
			if(!self.isReady) {
				LoadingSpinner(isAnimating: .constant(true), style: .medium)
			}
		}
	}
}



struct HtmlTextView_Previews: PreviewProvider {
	static var previews: some View {
		HtmlTextView(html: "qweqweqweqwfe<b>adsd</b> sdfsf sdfsdf fdgdfg dfgdfg fdgf fgfgf fdgdfg", isReady: .constant(true))
	}
}
