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