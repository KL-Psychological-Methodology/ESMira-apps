//
// Created by JodliDev on 12.10.22.
//

import SwiftUI
import sharedCode

struct ListRewardsView: View {
	@EnvironmentObject var appState: AppState
	@State private var studies: [Study] = []
	@State private var currentStudy: Study? = nil
	@State private var showDeleteAlert = false
	
	var body: some View {
		Group {
			if(studies.count == 1 && self.studies[0].state == .joined) {
				RewardView(study: self.studies[0])
			}
			else {
				List(studies, id: \.webId) { study in
					NavigationLink(destination: RewardView(study: study)) {
						Text(study.title)
					}
				}
			}
		}
		.onAppear {
			self.studies = DbLogic().getStudiesWithRewards()
		}
	}
}
