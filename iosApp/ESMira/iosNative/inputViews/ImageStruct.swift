//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode
import URLImage

//struct ImageStruct: View {
//	@Binding var value: String
//	let input: Input
//	
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			URLImage(url: URL(string: self.input.url)!) { image in
//				image
//					.aspectRatio(contentMode: .fit)
//			}
//		}
//	}
//}

struct ImageStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
			URLImage(URL(string: self.viewModel.input.url)!) { image in
				image.aspectRatio(contentMode: .fit)
			}
		}
	}
}
