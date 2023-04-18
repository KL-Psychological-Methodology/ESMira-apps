//
// Created by JodliDev on 05.08.20.
//

import Foundation
import SwiftUI

struct RadioButtonView<Content> : View where Content: View {
	@Binding var state: String
	let value: String
	var labelEl: Content? = nil
	var label: String? = nil
	var listener: ((String) -> Void)? = nil
	
	init(state: Binding<String>, value: String, labelEl: Content, listener: ((String) -> Void)? = nil) {
		self._state = state
		self.value = value
		self.listener = listener
		
		self.labelEl = labelEl
	}
	
	var body: some View {
		Button(action: {
			self.state = self.value
			self.listener?(self.value)
		}) {
			HStack(alignment: .center, spacing: 10) {
				Image(systemName: self.state == self.value ? "largecircle.fill.circle" : "circle")
					.resizable()
					.aspectRatio(contentMode: .fit)
					.frame(width: 20, height: 20)
					.foregroundColor(Color("PrimaryDark"))
				if(self.label != nil) {
					Text(self.label!)
						.foregroundColor(Color.primary)
						.fixMultiline()
						.font(.system(size: 16))
						.multilineTextAlignment(.leading)
				}
				else if(self.labelEl != nil) {
					self.labelEl
						.foregroundColor(Color.primary)
				}
			}
		}
	}
}

extension RadioButtonView where Content == EmptyView {
	init(state: Binding<String>, label: String, value: String, listener: ((String) -> Void)? = nil) {
		self._state = state
		self.value = value
		self.listener = listener
		
		self.label = label
	}
}
