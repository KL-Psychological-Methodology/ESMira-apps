//
//  Created by JodliDev on 13.04.23.
//

import SwiftUI
import sharedCode


struct AboutESMira: View {
	
	@EnvironmentObject var appState: AppState
	
	@State var askDevPassword = false
	@State var devPassword = ""
	@State var secretClickCount = 0
	
	func secretClick() {
		self.secretClickCount += 1
		if(self.secretClickCount >= 10) {
			if(DbUser().isDev()) {
				DbUser().setDev(enabled: false, pass: "")
				self.appState.showTranslatedToast("info_dev_inactive")
			}
			else {
				self.askDevPassword = true
			}
			self.secretClickCount = 0
		}
	}
	
	var body: some View {
		ScrollView {
			VStack(alignment: .leading) {
				HStack {
					Spacer()
					Image("roundAppIcon")
						.padding([.trailing], 20)
						.onTapGesture {
							self.secretClick()
						}
					VStack(alignment: .leading) {
						Text("ESMira").font(.largeTitle)
						Text(String(format: NSLocalizedString("ios_version", comment: ""), "\(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Error")", DbLogic().getVersion()))
					}
					Spacer()
				}
				
				Spacer(minLength: 20)
				
				Text("about_ESMira_description")
					.fixMultiline()
				
				Spacer(minLength: 10)
				
				LibraryLine(NSLocalizedString("colon_homepage", comment: ""), "https://esmira.kl.ac.at")
				LibraryLine(NSLocalizedString("colon_github", comment: ""), "https://github.com/KL-Psychological-Methodology/ESMira")
				
				Divider()
					.padding([.vertical], 30)
				
				
				LibrariesView(libraries: [
					LibraryLine("Accompanist", "https://google.github.io/accompanist/"),
					LibraryLine("Android-Debug-Database", "https://github.com/amitshekhariitbhu/Android-Debug-Database"),
					LibraryLine("Charts", "https://github.com/danielgindi/Charts"),
					LibraryLine("CodeScanner", "https://github.com/twostraws/CodeScanner"),
					LibraryLine("Kotlin Multiplatform", "https://kotlinlang.org/lp/mobile/"),
					LibraryLine("Kotlinx.serialization", "https://github.com/Kotlin/kotlinx.serialization"),
					LibraryLine("Ktor", "https://ktor.io/"),
					LibraryLine("Markwon", "https://noties.io/Markwon/"),
					LibraryLine("MPAndroidChart", "https://github.com/PhilJay/MPAndroidChart"),
					LibraryLine("URLImage", "https://github.com/dmytro-anokhin/url-image"),
					LibraryLine("ZXing Android Embedded", "https://github.com/journeyapps/zxing-android-embedded")
				])
			}
				.padding(10)
				.textFieldAlert(isPresented: self.$askDevPassword, text: self.$devPassword, title: "password") {
					if (DbUser().setDev(enabled: true, pass: self.devPassword)) {
						self.appState.showTranslatedToast("info_dev_active")
					}
				}
		}
	}
}


struct LibrariesView: View {
	let libraries: [LibraryLine]
	var body: some View {
		Text("colon_used_libraries")
			.bold()
			.font(.title)
			.padding([.bottom], 10)
		ForEach(self.libraries, id: \.name) { line in
			line
		}
	}
}

struct LibraryLine: View {
	let name: String
	let url: String
	
	init(_ name: String, _ url: String) {
		self.name = name
		self.url = url
	}
	
	var body: some View {
		VStack(alignment: .leading) {
			Text(self.name).bold()
			Text(self.url)
				.font(.caption)
				.padding([.leading], 10)
		}
		.padding([.bottom], 5)
		.onTapGesture {
				  UIApplication.shared.open(URL(string: self.url)!, options: [:])
			  }
	}
}



