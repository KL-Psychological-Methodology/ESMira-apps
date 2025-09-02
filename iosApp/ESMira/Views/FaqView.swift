//
//  Created by Selina on 01.09.25.
//

import SwiftUI
import sharedCode

struct FaqView: View {
	let study: Study
	
	var body: some View {
		ScrollView {
			HtmlTextView(html: study.faq).padding()
		}
	}
}
