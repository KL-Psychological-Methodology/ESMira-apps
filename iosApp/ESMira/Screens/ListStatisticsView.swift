//
// Created by JodliDev on 25.05.20.
//

import SwiftUI
import sharedCode

struct ListStatisticsView: View {
	@EnvironmentObject var appState: AppState
	@State private var studies: [Study] = []
	@State private var currentStudy: Study? = nil
	@State private var showDeleteAlert = false
	
	var body: some View {
		Group {
			if(studies.count == 1 && self.studies[0].state == .joined) {
				StatisticsView(self.studies[0])
			}
			else {
				List(studies, id: \.webId) { study in
					HStack {
                        if(study.state != Study.STATES.joined) {
							Button(action: {
								self.currentStudy = study
								self.showDeleteAlert = true
							}) {
								Image(systemName: "trash.circle")
							}
						}
						NavigationLink(destination: StatisticsView(study).navigationBarTitle(study.title)) {
							Text(study.title)
						}
					}
				}.fixButtons()
			}
		}
		.alert(isPresented: self.$showDeleteAlert) {
			Alert(title: Text("dialogTitle_delete_statistics"), message: Text("dialogDesc_delete_statistics"),
				primaryButton: .destructive(Text("delete_")) {
					self.currentStudy!.delete()
					self.studies = DbLogic().getStudiesWithStatistics()
				}, secondaryButton: .cancel())
		}
		.onAppear {
			self.studies = DbLogic().getStudiesWithStatistics()
		}
	}
}
