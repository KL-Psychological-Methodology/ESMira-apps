import SwiftUI
import sharedCode

struct LangQuestionView: View {
	@ObservedObject var addStudyState: AddStudyState
	var study: Study
	let languages: [String: String]
	
	@State private var selectedLanguage = ""
	@State var gotoStudyLoader = false
	
	init(study: Study) {
		self.study = study
		self.selectedLanguage = study.lang
		self.addStudyState = AddStudyState(
			serverUrl: study.serverUrl,
			accessKey: study.accessKey,
			lang: study.lang,
			studyWebId: study.webId
		)
		let languageCodes = study.getAvailableLangs()
		let currentLocale = Locale.current
		var languageMap: [String:String] = [:]
		for code in languageCodes {
			if let languageName = currentLocale.localizedString(forLanguageCode: code) {
				languageMap.updateValue(code, forKey: languageName)
			}
		}
		languages = languageMap
	}
	
	var body: some View {
		if(self.gotoStudyLoader) {
			StudyLoaderView(addStudyState: self.addStudyState)
		} else {
			VStack {
				Text("select_language_instruction").padding(.vertical)
				ScrollView {
					VStack(alignment: .leading) {
						ForEach(self.languages.sorted(by: {$0.key < $1.key}), id: \.key) { lang, code in
							HStack {
								RadioButtonView(state: self.$selectedLanguage, label: lang, value: code) { value in
									self.addStudyState.lang = code
									self.gotoStudyLoader = true
								}
							}
						}
					}
				}
				Spacer()
				
				Divider()
				HStack {
					Spacer()
					NavigationLink(destination: StudyDetailView(study: study)) {
						Text("continue_")
						Image(systemName: "chevron.compact.right")
					}
				}
				
				
			}.padding()
		}
	}
}
