//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode
import AVFoundation

struct VideoStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	@State var loadingSuccess: Bool = false
	@State var loadingError = false
	
	var body: some View {
		VStack {
			if(self.loadingError) {
				Text("error_loading_failed")
				Button("reload") {
					self.loadingError = false
				}
			}
			else {
				ZStack {
					VideoPlayerView(url: self.viewModel.input.url, loadingSuccess: Binding(
						get: {
							self.viewModel.value.isEmpty
						}, set: { success in
							self.loadingSuccess = success
							self.viewModel.value = success ? "loaded" : ""
						}
					), loadingError: self.$loadingError)
					.frame(height: 250)
					if(!self.loadingSuccess) {
						LoadingSpinner(isAnimating: .constant(true), style: .medium).padding()
					}
				}
			}
		}
	}
}
