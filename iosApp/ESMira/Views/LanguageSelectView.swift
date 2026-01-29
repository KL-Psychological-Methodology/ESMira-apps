import SwiftUI
import sharedCode

struct LanguageSelect: View {
	let study: Study
	let languages: [String: String]
	
	@State private var selectedLanguage = ""
	@EnvironmentObject var navigationState: NavigationState
	
	init(study: Study) {
		self.study = study
		self.selectedLanguage = study.lang
		let languageCodes = study.getAvailableLangs()
		let currentLocale = Locale.current
		var languageMap: [String: String] = [:]
		for code in languageCodes {
			if let languageName = currentLocale.localizedString(forLanguageCode: code) {
				languageMap.updateValue(code, forKey: languageName)
			}
		}
		self.languages = languageMap
	}
	
	
	var body: some View {
		VStack {
			ScrollView {
				VStack {
					Text("select_language_instruction").padding(.vertical)
					VStack(alignment: .leading) {
						ForEach(self.languages.sorted(by: {$0.key < $1.key}), id: \.key) { lang, code in
							RadioButtonView(state: self.$selectedLanguage, label: lang, value: code) { value in
								self.study.saveLanguage(newLang: code)
								Web.Companion().updateStudiesAsync(forceStudyUpdate: true, filterStudies: KotlinArray<KotlinLong>(size: 1, init: {_ in KotlinLong(value: self.study.id)})) {_ in
									navigationState.reloadStudy()
								}
							}
						}
					}
				}.padding()
			}
		}
	}
}
