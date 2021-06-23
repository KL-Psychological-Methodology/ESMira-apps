//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode
import AVFoundation
//import UIKit
//import AVKit

//struct VideoStruct: View {
//	@State var loadingSuccess: Bool = false
//	@State var loadingError = false
//	@Binding var value: String
//	let input: Input
//	
//	var body: some View {
//		VStack {
//			TextStruct(input: self.input)
//			if(self.loadingError) {
//				Text("error_loading_failed")
//				Button("reload") {
//					self.loadingError = false
//				}
//			}
//			else {
//				ZStack {
////					VideoPlayerView(url: self.input.url, loadingSuccess: self.$loadingSuccess, loadingError: self.$loadingError)
//					VideoPlayerView(url: self.input.url, loadingSuccess: Binding(
//						get: {
//							self.value.isEmpty
//						}, set: { success in
//							self.loadingSuccess = success
//							self.value = success ? "loaded" : ""
//						}
//					), loadingError: self.$loadingError)
//						.frame(height: 250)
//					if(!self.loadingSuccess) {
//						LoadingSpinner(isAnimating: .constant(true), style: .medium).padding()
//					}
//				}
//			}
//		}
//	}
//}

struct VideoStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	@State var loadingSuccess: Bool = false
	@State var loadingError = false
	
	var body: some View {
		VStack {
			TextStruct(viewModel: self.viewModel)
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
