//
// Created by JodliDev on 25.05.20.
//

import Charts
import SwiftUI
import sharedCode


struct StatisticsView: View {
	
//	open class BalloonMarker: MarkerImage {
//		//thanks to: https://github.com/danielgindi/Charts/blob/master/ChartsDemo-iOS/Objective-C/Components/BalloonMarker.swift
//		let chartBuilder: IOsChartBuilder
//
//		private var arrowSize = CGSize(width: 15, height: 11)
//		private var backColor = UIColor(named: "Accent")!.cgColor
//		private var textColor = UIColor.white
//		private var insets: UIEdgeInsets = UIEdgeInsets(top: 3.0, left: 3.0, bottom: 3.0, right: 3.0)
//		private var minimumSize = CGSize(width: 75.0, height: 60.0)
//
//		private var label: String = ""
//		private var _drawAttributes = [NSAttributedString.Key : Any]()
//
//		init(chartBuilder: IOsChartBuilder) {
//			self.chartBuilder = chartBuilder
//			super.init()
//		}
//		open override func offsetForDrawing(atPoint point: CGPoint) -> CGPoint {
//			var offset = self.offset
//			var size = self.size
//
//			if(size.width == 0.0 && image != nil) {
//				size.width = image!.size.width
//			}
//			if(size.height == 0.0 && image != nil) {
//				size.height = image!.size.height
//			}
//
//			let width = size.width
//			let height = size.height
//			let padding: CGFloat = 8.0
//
//			var origin = point
//			origin.x -= width / 2
//			origin.y -= height
//
//			if(origin.x + offset.x < 0.0) {
//				offset.x = -origin.x + padding
//			}
//			else if let chart = chartView, origin.x + width + offset.x > chart.bounds.size.width {
//				offset.x = chart.bounds.size.width - origin.x - width - padding
//			}
//
//			if(origin.y + offset.y < 0) {
//				offset.y = height + padding;
//			}
//			else if let chart = chartView, origin.y + height + offset.y > chart.bounds.size.height {
//				offset.y = chart.bounds.size.height - origin.y - height - padding
//			}
//
//			return offset
//		}
//
//		open override func draw(context: CGContext, point: CGPoint) {
//			let offset = self.offsetForDrawing(atPoint: point)
//			let size = self.size
//
//			var rect = CGRect(
//				origin: CGPoint(
//					x: point.x + offset.x,
//					y: point.y + offset.y),
//				size: size)
//			rect.origin.x -= size.width / 2.0
//			rect.origin.y -= size.height
//
//			context.saveGState()
//
//			context.setFillColor(backColor)
//
//			if(offset.y > 0) {
//				context.beginPath()
//				context.move(to: CGPoint(
//					x: rect.origin.x,
//					y: rect.origin.y + arrowSize.height))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x + (rect.size.width - arrowSize.width) / 2.0,
//					y: rect.origin.y + arrowSize.height))
//				//arrow vertex
//				context.addLine(to: CGPoint(
//					x: point.x,
//					y: point.y))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x + (rect.size.width + arrowSize.width) / 2.0,
//					y: rect.origin.y + arrowSize.height))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x + rect.size.width,
//					y: rect.origin.y + arrowSize.height))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x + rect.size.width,
//					y: rect.origin.y + rect.size.height))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x,
//					y: rect.origin.y + rect.size.height))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x,
//					y: rect.origin.y + arrowSize.height))
//				context.fillPath()
//			}
//			else {
//				context.beginPath()
//				context.move(to: CGPoint(
//					x: rect.origin.x,
//					y: rect.origin.y))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x + rect.size.width,
//					y: rect.origin.y))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x + rect.size.width,
//					y: rect.origin.y + rect.size.height - arrowSize.height))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x + (rect.size.width + arrowSize.width) / 2.0,
//					y: rect.origin.y + rect.size.height - arrowSize.height))
//				//arrow vertex
//				context.addLine(to: CGPoint(
//					x: point.x,
//					y: point.y))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x + (rect.size.width - arrowSize.width) / 2.0,
//					y: rect.origin.y + rect.size.height - arrowSize.height))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x,
//					y: rect.origin.y + rect.size.height - arrowSize.height))
//				context.addLine(to: CGPoint(
//					x: rect.origin.x,
//					y: rect.origin.y))
//				context.fillPath()
//			}
//
//			if(offset.y > 0) {
//				rect.origin.y += self.insets.top + arrowSize.height
//			}
//			else {
//				rect.origin.y += self.insets.top
//			}
//
//			rect.size.height -= self.insets.top + self.insets.bottom
//
//			UIGraphicsPushContext(context)
//
//
//
//
//			label.draw(in: rect, withAttributes: _drawAttributes)
//
//			UIGraphicsPopContext()
//
//			context.restoreGState()
//		}
//
//		open override func refreshContent(entry: ChartDataEntry, highlight: Highlight) {
////			label = String(entry.y)
//
//			let xLabel = self.chartBuilder.statisticChart.xAxisLabel.isEmpty ? NSLocalizedString("axis_x_name", comment: "") : self.chartBuilder.statisticChart.xAxisLabel
//			let yLabel = self.chartBuilder.statisticChart.yAxisLabel.isEmpty ? NSLocalizedString("axis_y_name", comment: "") : self.chartBuilder.statisticChart.yAxisLabel
//
//			let xValue = self.chartBuilder.xAxisFormatter.getString(value: Float(entry.x))
//			let yValue = String(format: "%.2f", entry.y)
//
//			let lineX = xValue.isEmpty ? "" : "\(xLabel): \(xValue)"
//			let lineY = yValue.isEmpty ? "" : "\(yLabel): \(yValue)"
//
//			label = "\(lineX)\n\(lineY)"
//
//
//
//			let paragraphStyle = NSParagraphStyle.default.mutableCopy() as? NSMutableParagraphStyle
//			paragraphStyle?.alignment = .center
//
//			_drawAttributes.removeAll()
//			_drawAttributes[.paragraphStyle] = paragraphStyle
//			_drawAttributes[.foregroundColor] = UIColor.white
//
//			let _labelSize = label.size(withAttributes: _drawAttributes)
//
//			var size = CGSize()
//			size.width = _labelSize.width + self.insets.left + self.insets.right
//			size.height = _labelSize.height + self.insets.top + self.insets.bottom
//			size.width = max(minimumSize.width, size.width)
//			size.height = max(minimumSize.height, size.height)
//			self.size = size
//		}
//	}
	
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
			if(self.showPublicStatistics) {
                HStack {
					Button(action:  {
						self.currentScreen = .personalStatistics
					}) {
						Text("statistics_personal").bold().font(.system(size: 14))
					}
						.foregroundColor(self.currentScreen == .personalStatistics ? Color("Accent") : Color.white)
						.padding()
					Spacer()
					Button(action:  {
						self.currentScreen = .publicStatistics
					}) {
						Text("statistics_public").bold().font(.system(size: 14))
					}
						.foregroundColor(self.currentScreen == .publicStatistics ? Color("Accent") : Color.white)
						.padding()
				}
					.background(Color("PrimaryLight"))
			}
			
			
//			GeometryReader { geometry in
//				ScrollView {
//					self.drawStatistics(charts: self.currentScreen == .personalStatistics ? self.personalCollection.charts : self.publicCollection!.charts, width: geometry.size.width)
//				}
//			}
			
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
			}
	}
}
