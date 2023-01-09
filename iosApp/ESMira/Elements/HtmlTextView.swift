//
// Created by JodliDev on 09.06.20.
// Thanks to https://stackoverflow.com/questions/56892691/how-to-show-html-or-markdown-in-a-swiftui-text
//

//import WebKit
import SwiftUI


struct HtmlTextRepresentable: UIViewRepresentable {
	let html: String
	@Binding var dynamicHeight: CGFloat
	@Binding var isReady: Bool
	@State var isScrollable = false
	
	func updateUIView(_ label: UITextView, context: Context) {
		updateHtml(label)
	}
	
	func makeUIView(context: UIViewRepresentableContext<Self>) -> UITextView {
		let label = UITextView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width - 10, height: 0))
		
		
		label.isEditable = false
		label.isScrollEnabled = self.isScrollable
		label.translatesAutoresizingMaskIntoConstraints = !self.isScrollable
		
		label.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
		
		label.font = UIFont.systemFont(ofSize: 16)
		label.text = html
		label.backgroundColor = .clear
		
		updateHtml(label)
		
		return label
	}
	
	private func updateHtml(_ label: UITextView) {
		label.text = html
		self.isReady = false
		
		DispatchQueue.main.async {
			let data = NSString(string: "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"/></head><body style=\"font-size: \(UILabel().font.pointSize)px; font-family: system-ui, -apple-system, sans-serif\">\(self.html)</body></html>")
				.data(using: String.Encoding.unicode.rawValue)
			
			if let attributedString = try? NSAttributedString(
				data: data!,
				options: [.documentType: NSAttributedString.DocumentType.html],
				documentAttributes: nil
			) {
				label.attributedText = attributedString
				self.dynamicHeight = label.sizeThatFits(CGSize(width: label.frame.width, height: CGFloat.greatestFiniteMagnitude)).height
			}
			
			self.isReady = true
		}
	}
}


struct HtmlTextView: View {
	let html: String
	@State private var height: CGFloat = .zero
	@Binding var isReady: Bool
	
	var body: some View {
//		GeometryReader { geometry in
//			HtmlTextRepresentable(html: self.html, maxWidth: geometry.size.width, dynamicHeight: self.$height, isReady: self.$isReady).frame(minHeight: self.height)
//        }.frame(minHeight: self.height)
		HtmlTextRepresentable(html: self.html, dynamicHeight: self.$height, isReady: self.$isReady)
			.frame(minHeight: self.height)
	}
}


struct ScrollableHtmlTextView: View {
	let html: String
	@State private var height: CGFloat = .zero
	@State var isReady: Bool = false

	var body: some View {
		HtmlTextRepresentable(html: self.html, dynamicHeight: self.$height, isReady: self.$isReady, isScrollable: true).disabled(!self.isReady)
		if(!self.isReady) {
			LoadingSpinner(isAnimating: .constant(true), style: .medium)
		}
	}
}



struct HtmlTextView_Previews: PreviewProvider {
	static var previews: some View {
		HtmlTextView(html: "qweqweqweqwfe<b>adsd</b> sdfsf sdfsdf fdgdfg dfgdfg fdgf fgfgf fdgdfg", isReady: .constant(true))
	}
}
