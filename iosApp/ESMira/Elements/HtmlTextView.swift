//
// Created by JodliDev on 09.06.20.
// Thanks to https://stackoverflow.com/questions/56892691/how-to-show-html-or-markdown-in-a-swiftui-text
// And thanks to https://stackoverflow.com/a/70401810/10423612
// And thanks to https://medium.com/@thomsmed/rendering-html-in-swiftui-65e883a63571
// And thanks to https://stackoverflow.com/questions/59731236/swiftui-attributed-string-from-html-crashes-the-app
// And "thanks" to https://developer.apple.com/forums/thread/115405
//

import SwiftUI
import ZMarkupParser


protocol StringFormatter {
	func format(string: String) -> NSAttributedString
}

class ZMarkupParserFormatter: StringFormatter {
	func format(string: String) -> NSAttributedString {
		let parser = ZHTMLParserBuilder
			.initWithDefault()
			.set(spacingPolicy: .paragraphSpacing)
			.add(ExtendTagName("mark"), withCustomStyle: MarkupStyle(backgroundColor: MarkupStyleColor(name: .yellow)))
//			.add(ExtendTagName("div"), withCustomStyle: ParagraphMarkup())
			.set(rootStyle: MarkupStyle(
				font: MarkupStyleFont(size: UILabel().font.pointSize),
				foregroundColor: MarkupStyleColor(color: UIColor.label))
			)
			.build()
		
		return parser.render(string)
	}
}

//class HTMLFormatter: StringFormatter {
//	func format(string: String) -> NSAttributedString {
//		let htmlBody = "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"/></head><body style=\"font-size: \(UILabel().font.pointSize)px; color: \(colorToHex(UIColor.label)); font-family: system-ui, -apple-system, sans-serif\">\(string)</body></html>"
//
//		guard let data = htmlBody.data(using: .utf8),
//			  let attributedText = try? NSAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding: String.Encoding.utf8.rawValue], documentAttributes: nil)
//		else { return NSAttributedString(string: string) }
//
//		return attributedText
//	}
//	private func colorToHex(_ color: UIColor) -> String {
//		return color.hexString
//	}
//}


//struct AttributedText: UIViewRepresentable {
//	typealias UIViewType = UITextView
//
//	private var htmlText: String
//	private var isScrollable: Bool
//	init(htmlText: String, isScrollable: Bool = false) {
//		self.isScrollable = isScrollable
//		self.htmlText = htmlText
//	}
//
//	func makeUIView(context: Context) -> UIViewType {
//		let view = ContentTextView()
//		view.text = htmlText
//
//		view.setContentHuggingPriority(.defaultLow, for: .vertical)
//  		view.setContentHuggingPriority(.defaultLow, for: .horizontal)
//  		view.setContentCompressionResistancePriority(.required, for: .vertical)
//  		view.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
////  		view.contentInset = .zero
////  		view.textContainer.lineFragmentPadding = 0
//  		view.backgroundColor = .blue
//  		view.isScrollEnabled = self.isScrollable
//		return view
//	}
//
//	func updateUIView(_ uiView: UITextView, context: Context) {
//		DispatchQueue.main.async {
////			uiView.sizeToFit()
//			uiView.invalidateIntrinsicContentSize()
//		}
//	}
//
//
//	/// ContentTextView
// 	/// subclass of UITextView returning contentSize as intrinsicContentSize
// 	private class ContentTextView: UITextView {
// 		override var canBecomeFirstResponder: Bool { false }
//
// 		override var intrinsicContentSize: CGSize {
// 			print("Frame:\(frame.width), super: \(super.intrinsicContentSize.width), contentSize:\(contentSize.width), height: \(attributedText.size().height)")
//			return sizeThatFits(CGSize(width: bounds.width, height: CGFloat.greatestFiniteMagnitude))
//// 			let rect = self.attributedText.boundingRect(with: CGSize.init(width: super.intrinsicContentSize.width, height: CGFloat.greatestFiniteMagnitude),
//// 														options: [.usesLineFragmentOrigin, .usesFontLeading],
//// 												 context: nil)
//// 			return CGSize(width: super.intrinsicContentSize.width, height: ceil(rect.size.height))
// //			return CGSize(width: super.intrinsicContentSize.width, height: super.intrinsicContentSize.height + 10)
// //			frame.height > 0 ? contentSize : super.intrinsicContentSize
// //			return sizeThatFits(CGSize(width: frame.width, height: CGFloat.greatestFiniteMagnitude))
// //			print(contentSize)
// //			print(super.intrinsicContentSize)
//// 			return frame.height > 0 ? contentSize : sizeThatFits(CGSize(width: frame.width, height: CGFloat.greatestFiniteMagnitude))
// 		}
// 	}
//}

struct AttributedText: UIViewRepresentable {
	typealias UIViewType = UITextView

	private let formatter: StringFormatter = ZMarkupParserFormatter()
	private var attributedText: NSAttributedString
//	@State private var attributedText: NSAttributedString = NSAttributedString(string: "")
//	private var htmlText: String
	private var isScrollable: Bool

//	@State public var isReady: Bool = false

	init(htmlText: String, isScrollable: Bool = false) {
		self.attributedText = self.formatter.format(string: htmlText)
		self.isScrollable = isScrollable
//		self.htmlText = htmlText
	}

	private func colorToHex(_ color: UIColor) -> String {
		return color.hexString
	}

	func makeUIView(context: Context) -> UIViewType {
//		DispatchQueue.main.async {
//			self.attributedText = self.formatter.format(string: self.htmlText)
//			self.isReady = true
//		}
		let view = ContentTextView()
		view.setContentHuggingPriority(.defaultLow, for: .vertical)
		view.setContentHuggingPriority(.defaultLow, for: .horizontal)
		view.setContentCompressionResistancePriority(.required, for: .vertical)
		view.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
		view.contentInset = .zero
		view.textContainer.lineFragmentPadding = 0
		view.backgroundColor = .clear
		view.isScrollEnabled = self.isScrollable
		view.attributedText = attributedText
		return view
	}

	func updateUIView(_ uiView: UITextView, context: Context) {
		if(!self.isScrollable) {
			DispatchQueue.main.async {
				uiView.invalidateIntrinsicContentSize()
			}
		}
	}
	

	/// ContentTextView
	/// subclass of UITextView returning contentSize as intrinsicContentSize
	private class ContentTextView: UITextView {
		override var canBecomeFirstResponder: Bool { false }

		override var intrinsicContentSize: CGSize {
			print("bounds:\(bounds.width), size:\(sizeThatFits(CGSize(width: bounds.width, height: CGFloat.greatestFiniteMagnitude)))")
//			return CGSize(width: 360, height: 1530)
			return sizeThatFits(CGSize(width: bounds.width > 0 ? bounds.width : UIScreen.main.bounds.width, height: CGFloat.greatestFiniteMagnitude))
			
//			let rect = self.attributedText.boundingRect(with: CGSize.init(width: super.intrinsicContentSize.width, height: CGFloat.greatestFiniteMagnitude),
//														options: [.usesLineFragmentOrigin, .usesFontLeading],
//												 context: nil)
//			return CGSize(width: super.intrinsicContentSize.width, height: ceil(rect.size.height))
//			return CGSize(width: super.intrinsicContentSize.width, height: super.intrinsicContentSize.height + 10)
//			frame.height > 0 ? contentSize : super.intrinsicContentSize
//			return sizeThatFits(CGSize(width: frame.width, height: CGFloat.greatestFiniteMagnitude))
//			print(contentSize)
//			print(super.intrinsicContentSize)
//			return frame.height > 0 ? contentSize : sizeThatFits(CGSize(width: frame.width, height: CGFloat.greatestFiniteMagnitude))
		}
	}
}


struct HtmlTextView: View {
	let html: String

	var body: some View {
//		Text(html)
		AttributedText(htmlText: html)
			.fixedSize(horizontal: false, vertical: true) //prevents other Elements to be cropped (sometimes happened in Likert Scale with labels that do not fit in one line)
	}
}


struct ScrollableHtmlTextView: View {
	let html: String

	var body: some View {
		//When using a lot of divs for new lines, the scroll position gets messed up when using native scrolling from UITextView
		//As a workaround we just use the SwiftUI ScrollView
//		AttributedText(htmlText: html, isScrollable: true)
		ScrollView {
			AttributedText(htmlText: html)
		}
	}
}



struct HtmlTextView_Previews: PreviewProvider {
	static var previews: some View {
		HtmlTextView(html: "qweqweqweqwfe<b>adsd</b> sdfsf sdfsdf fdgdfg dfgdfg fdgf fgfgf fdgdfg")
	}
}
