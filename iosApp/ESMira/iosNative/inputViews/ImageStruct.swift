//
// Created by JodliDev on 19.08.20.
//

import Foundation
import SwiftUI
import sharedCode
import URLImage

struct ImageStruct: View {
	@ObservedObject var viewModel: InputViewModel
	
	private func getStrippedUrl(_ url: String) -> String {
		do {
			let regex = try NSRegularExpression(pattern: "data:image/.+;base64,(.+)")
			let result = regex.firstMatch(in: url, options: [], range: NSRange(url.startIndex..., in: url))
			let nsString = url as NSString
			print(nsString.substring(with: result!.range(at: 1)))
			return nsString.substring(with: result!.range(at: 1))
		}
		catch {
			ErrorBox.Companion().warn(title: "Image", msg: "base64 image had errors!")
			return ""
		}
	}
	
	var body: some View {
		if(self.viewModel.input.url.starts(with: "data")) {
			let data = Data(base64Encoded: self.getStrippedUrl(self.viewModel.input.url), options: .ignoreUnknownCharacters)
			let uiImage = UIImage(data: data!)
			Image(uiImage: uiImage!)
				.resizable()
				.scaledToFit()
				.frame(maxWidth: UIScreen.main.bounds.width)
		}
		else {
			URLImage(URL(string: self.viewModel.input.url)!) { image in
				image
					.resizable()
					.aspectRatio(contentMode: .fit)
			}
		}
	}
}
