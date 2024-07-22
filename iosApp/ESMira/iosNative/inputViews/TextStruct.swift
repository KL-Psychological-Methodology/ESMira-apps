//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct TextStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		HtmlTextView(html: getText(text: self.viewModel.input.displayText))
	}
	
	func getText(text: String) -> String {
		if(text == MerlinRunner().ERROR_MARKER) {
			return NSLocalizedString("text_script_error", comment: "")
		}
		return text
	}
}
