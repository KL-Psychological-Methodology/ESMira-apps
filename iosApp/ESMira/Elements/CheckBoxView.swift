//
// Created by JodliDev on 05.08.20.
//

import Foundation
import SwiftUI

struct CheckBoxView : View {
	@Binding var state:Bool
	let label: String
	let callback: ((Bool) -> ())?
	
	init(label: String, state: Binding<Bool>, callback: ((Bool) -> ())? = nil) {
		self.label = label
		self._state = state
		self.callback = callback
	}
	
	var body: some View {
		Button(action: {
			self.state = !self.state
			if(self.callback != nil) {
				self.callback!(self.state)
			}
		}) {
			HStack(alignment: .center, spacing: 10) {
				Image(systemName: self.state ? "checkmark.square" : "square")
					.renderingMode(.original)
					.resizable()
					.aspectRatio(contentMode: .fit)
					.frame(width: 20, height: 20)
				
				Text(self.label)
					.foregroundColor(Color.black)
					.fixMultiline()
			}
		}
	}
}
