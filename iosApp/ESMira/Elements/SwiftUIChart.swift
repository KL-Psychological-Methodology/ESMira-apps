//
// Created by JodliDev on 29.09.20.
//

import Foundation
import Charts
import SwiftUI
import sharedCode

struct SwiftUIChart : UIViewRepresentable {
	typealias UIViewType = ChartViewBase
//	let chartInfo: ChartInfo
	let chartBuilder: ChartBuilder
//	@Binding var infoCollection: ChartInfoCollection
	
	
//	init(chartInfo: inout ChartInfo, infoCollection: Binding<ChartInfoCollection>) {
//		self.chartInfo = chartInfo
//		self._infoCollection = infoCollection
//	}
	
	func makeUIView(context: Context) -> UIViewType {
		return (chartBuilder.createChart() as! ChartViewBase)
	}
	
	func updateUIView(_ chartView: UIViewType, context: Context) {
		chartView.notifyDataSetChanged()
	}
}
