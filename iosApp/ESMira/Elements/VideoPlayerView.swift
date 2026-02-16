//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode
import WebKit


struct VideoPlayerView: UIViewRepresentable {
	let url: String
	@Binding var loadingSuccess: Bool
	@Binding var loadingError: Bool
	
	func makeUIView(context: Context) -> WKWebView {
		let view = WKWebView(frame: .zero)
		view.navigationDelegate = context.coordinator
		let url = URL(string: self.url)
		if(url != nil) {
			var urlRequest = URLRequest(url: url!)
			urlRequest.addValue("https://github.com/KL-Psychological-Methodology/ESMira", forHTTPHeaderField: "Referer")
			view.load(urlRequest)
		}
		return view
	}
	
	func updateUIView(_ view: WKWebView, context: UIViewRepresentableContext<VideoPlayerView>) {
	}
	
	class Coordinator: NSObject, WKNavigationDelegate {
		@Binding var loadingSuccess: Bool
		@Binding var loadingError: Bool
		
		init(_ loadingSuccess: Binding<Bool>, _ loadingError: Binding<Bool>) {
			self._loadingSuccess = loadingSuccess
			self._loadingError = loadingError
		}
		
		func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
			loadingSuccess = true
			loadingError = false
		}
		
		func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
			loadingSuccess = false
			loadingError = true
		}
		func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
			loadingSuccess = false
			loadingError = true
		}
	}
	
	func makeCoordinator() -> Coordinator {
		Coordinator(self.$loadingSuccess, self.$loadingError)
	}
}
