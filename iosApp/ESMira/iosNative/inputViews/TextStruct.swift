//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct TextStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
			HtmlTextView(html: self.viewModel.input.displayText)
	}
}
