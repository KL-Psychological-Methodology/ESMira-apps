//
// Created by JodliDev on 12.11.20.
//

import Foundation
import SwiftUI

extension Text {
	func fixMultiline() -> some View {
		//https://stackoverflow.com/questions/56505929/the-text-doesnt-get-wrapped-in-swift-ui
		self.fixedSize(horizontal: false, vertical: true)
	}
}
extension List {
	func fixButtons() -> some View {
		//buttons inside a list behave weirdly. This fixes it
		self.buttonStyle(PlainButtonStyle())
	}
}

extension UIColor {
	//Thanks to: https://stackoverflow.com/questions/26341008/how-to-convert-uicolor-to-hex-and-display-in-nslog
	var hexString: String {
		let cgColorInRGB = cgColor.converted(to: CGColorSpace(name: CGColorSpace.sRGB)!, intent: .defaultIntent, options: nil)!
		let colorRef = cgColorInRGB.components
		let r = colorRef?[0] ?? 0
		let g = colorRef?[1] ?? 0
		let b = ((colorRef?.count ?? 0) > 2 ? colorRef?[2] : g) ?? 0
		let a = cgColor.alpha

		var color = String(
			format: "#%02lX%02lX%02lX",
			lroundf(Float(r * 255)),
			lroundf(Float(g * 255)),
			lroundf(Float(b * 255))
		)

		if a < 1 {
			color += String(format: "%02lX", lroundf(Float(a * 255)))
		}

		return color
	}
}



extension UIApplication {
	func endEditing(_ force: Bool) {
		self.windows
			.filter{$0.isKeyWindow}
			.first?
			.endEditing(force)
	}
}

struct ResignKeyboardOnDragGesture: ViewModifier {
	var gesture = DragGesture().onChanged{_ in
		UIApplication.shared.endEditing(true)
	}
	func body(content: Content) -> some View {
		content.gesture(gesture)
	}
}

extension View {
	func resignKeyboardOnDragGesture() -> some View {
		return modifier(ResignKeyboardOnDragGesture())
	}
}
