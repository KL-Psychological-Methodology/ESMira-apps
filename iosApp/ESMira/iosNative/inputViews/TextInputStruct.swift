//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode

struct TextInputStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			TextField("", text: self.$viewModel.value)
				.padding()
				.border(Color("Outline"))
		}
		
	}
}
