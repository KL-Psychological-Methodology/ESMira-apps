//
// Created by JodliDev on 25.05.20.
//

import Charts
import SwiftUI
import sharedCode


struct StatisticsView: View {
	enum Screen {
		case personalStatistics, publicStatistics
	}
	
	struct ChartView: View {
		let chartInfo: ChartInfo
		let studyStateIsJoined: Bool
		let width: CGFloat
		
		@State private var isReady = false
		
		var body: some View {
			VStack(alignment: .leading) {
				Text(chartInfo.title).bold().fixMultiline()
				HtmlTextView(html: chartInfo.chartDescription, isReady: self.$isReady).frame(width: width)
				if(chartInfo.hideUntilCompletion && self.studyStateIsJoined) {
					Group {
						Text("visible_when_study_finished").bold().padding(.horizontal)
					}.frame(height: 300).background(Color.gray)
				}
				else {
					HStack {
						Text(chartInfo.yAxisLabel).rotationEffect(.degrees(-90)).fixedSize().frame(width: 20)
						SwiftUIChart(chartBuilder: chartInfo.builder).frame(height: 300)
					}
					Text(chartInfo.xAxisLabel).frame(alignment: .center)
				}
				Divider()
			}
				.padding(.bottom)
			.opacity(self.isReady ? 1 : 0)
		}
	}
	
	
	
	@EnvironmentObject var appState: AppState
	
	let study: Study
	private let personalCollection: ChartInfoCollection
	@State private var publicCollection: ChartInfoCollection? = nil
	@State private var showPublicStatistics = false
	@State private var currentScreen = Screen.personalStatistics
	private let studyStateIsJoined: Bool
	
//	@State private var scrollAction = LegacyScrollAction.idle
	
	@State var loadingState: LoadingState = .hidden
	@State var loadingMsg: String = ""
	@State var web: Web? = nil
	
	init(_ study: Study) {
		self.study = study
		
		self.personalCollection = ChartInfoCollection(study: study)
		
		self.studyStateIsJoined = study.state == Study.STATES.joined && study.isActive()
		self.initCharts(personalCollection)
	}
	
	private func initCharts(_ chartInfoCollection: ChartInfoCollection) {
		for chartInfo in chartInfoCollection.charts {
			chartInfo.doInitBuilder(chartInfoCollection: chartInfoCollection, chartChooser: ChartTypeChooser())
		}
	}
	
	
	private func loadPublicStudiesError(msg: String) {
		self.appState.showToast(msg)
		self.showPublicStatistics = false
		print(msg)
		loadingState = .hidden
	}
	private func loadPublicStudiesSuccess(json: String) {
		let publicCollection = ChartInfoCollection(json: json, study: self.study)
		personalCollection.addPublicData(publicChartCollection: publicCollection)
		initCharts(publicCollection)
		self.publicCollection = publicCollection
		
		loadingState = .hidden
		self.showPublicStatistics = true
	}
	
	
	private func drawStatistics(charts: [ChartInfo], width: CGFloat) -> some View {
		return VStack {
			if(charts.isEmpty) {
				Text("info_no_statistics")
			}
			else {
				ForEach(charts, id: \.self) { chartInfo in
					ChartView(chartInfo: chartInfo, studyStateIsJoined: self.studyStateIsJoined, width: width)
				}
			}
		}
	}
	var body: some View {
		VStack {
			
			if(self.currentScreen == .personalStatistics) {
				GeometryReader { geometry in
					ScrollView {
						self.drawStatistics(charts: self.personalCollection.charts, width: geometry.size.width)
					}
				}
				.padding(.horizontal)
			}
			else if(self.currentScreen == .publicStatistics) {
				GeometryReader { geometry in
					ScrollView {
						self.drawStatistics(charts: self.publicCollection!.charts, width: geometry.size.width)
					}
				}
				.padding(.horizontal)

			}
			
			if(self.showPublicStatistics) {
				HStack {
					Spacer()
					Button(action:  {
						self.currentScreen = .personalStatistics
					}) {
						VStack {
							Image(systemName: "person.fill")
								.padding(.top, 3)
							Text("statistics_personal").bold().font(.system(size: 14))
								.padding(5)
						}
					}
						.foregroundColor(self.currentScreen != .personalStatistics ? Color("PrimaryDark") : Color.white)
						.background(self.currentScreen == .personalStatistics ? Color("PrimaryDark") : Color.white.opacity(0))
						.cornerRadius(15)
						.padding(10)
					Spacer()
					Button(action:  {
						self.currentScreen = .publicStatistics
					}) {
						VStack {
							Image(systemName: "person.3.fill")
								.padding(.top, 3)
							Text("statistics_public").bold().font(.system(size: 14))
								.padding(5)
						}
					}
						.foregroundColor(self.currentScreen != .publicStatistics ? Color("PrimaryDark") : Color.white)
						.background(self.currentScreen == .publicStatistics ? Color("PrimaryDark") : Color.white.opacity(0))
						.cornerRadius(15)
						.padding(10)
					Spacer()
				}
					.background(Color("Surface"))
			}
		}
			.customLoader(isShowing: self.$loadingState,
				blocking: false,
				onShowing: {
					self.web = Web.Companion().loadStatistics(study: self.study, onError: self.loadPublicStudiesError, onSuccess: self.loadPublicStudiesSuccess)
				},
				onCancel: {
					if self.web != nil {
						self.web?.cancel()
					}
				}
			)
			.onAppear {
				if(self.study.publicStatisticsNeeded) {
					if (!self.personalCollection.hasPublicData) {
						self.loadingState = .loading
					}
					else {
						self.showPublicStatistics = true
					}
				}
				self.study.statisticWasViewed()
			}
	}
}
