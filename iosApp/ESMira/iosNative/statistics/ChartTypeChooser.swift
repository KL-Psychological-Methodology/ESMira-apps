//
// Created by JodliDev on 07.10.20.
//

import Foundation
import sharedCode
import Charts
import UIKit

class ChartTypeChooser : ChartChooserInterface {
    private class IOsLineChartBuilder : LineChartBuilder {
		private var lineData = LineChartData()
		weak var chartViewRef: LineChartView?
		
		override init(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo) {
			super.init(chartInfoCollection: chartInfoCollection, chartInfo: chartInfo)
		}
		
		override func createDataSet(label: String, color: String) -> ChartDataSetInterface {
			let dataSet = LineChartDataSet(entries: [], label: label)
			
			lineData.addDataSet(dataSet)
			
			return DataSetWrapper(dataSet)
		}
		
		override func addValue(xValue: Float, yValue: Float, dataSetIndex: Int32) {
			lineData.addEntry(ChartDataEntry(x: Double(xValue), y: Double(yValue)), dataSetIndex: Int(dataSetIndex))
		}
		
		override func createChart() -> Any {
			let chartView = LineChartView()
			chartViewRef = chartView
			chartView.data = lineData
			setupChart(chartView: ChartViewWrapper(chartView))
			
			return chartView
		}
		
		override func postUpdateChart() {
			if(chartViewRef != nil) {
				postUpdateChart(chartView: ChartViewWrapper(chartViewRef!))
			}
		}
		
		override func removeEntries() {
			for dataSet in lineData.dataSets {
				dataSet.clear()
			}
		}
	}
	
	private class IOsBarChartBuilder : BarChartBuilder {
		private var barData = BarChartData()
		weak var chartViewRef: BarChartView?
		
		override init(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo) {
			super.init(chartInfoCollection: chartInfoCollection, chartInfo: chartInfo)
		}
		
		override func groupBars(barWidth: Float, fromX: Float, groupSpace: Float, barSpace: Float) {
			barData.barWidth = Double(barWidth)
			barData.groupBars(fromX: Double(fromX), groupSpace: Double(groupSpace), barSpace: Double(barSpace))
		}
		
		override func createDataSet(label: String, color: String) -> ChartDataSetInterface {
			let dataSet = BarChartDataSet(entries: [], label: label)
			
			barData.addDataSet(dataSet)
			return DataSetWrapper(dataSet)
		}
		
		override func addValue(xValue: Float, yValue: Float, dataSetIndex: Int32) {
			barData.addEntry(BarChartDataEntry(x: Double(xValue), y: Double(yValue)), dataSetIndex: Int(dataSetIndex))
		}
		
		override func createChart() -> Any {
			let chartView = BarChartView()
			chartViewRef = chartView
			chartView.data = barData
			setupChart(chartView: ChartViewWrapper(chartView))
			
			return chartView
		}
		
		override func postUpdateChart() {
			if(chartViewRef != nil) {
				postUpdateChart(chartView: ChartViewWrapper(chartViewRef!))
			}
		}
		
		override func removeEntries() {
			for dataSet in barData.dataSets {
				dataSet.clear()
			}
		}
	}
	
	private class IOsScatterChartBuilder : ScatterChartBuilder {
		private var scatterData = ScatterChartData()
		private var regressionData = LineChartData()
		private var legendEntries: [LegendEntry] = []
		weak var chartViewRef: CombinedChartView?
		
		override init(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo) {
			super.init(chartInfoCollection: chartInfoCollection, chartInfo: chartInfo)
		}
		
		override func createDataSet(label: String, color: String) -> ChartDataSetInterface {
			let dataSet = ScatterChartDataSet(entries: [], label: label)
			scatterData.addDataSet(dataSet)
			
			return DataSetWrapper(dataSet)
		}
		
		override func packageLinearRegressionIntoBox(x1: Float, y1: Float, x2: Float, y2: Float) -> ChartDataSetInterface {
			var regressionList: [ChartDataEntry] = []
			regressionList.append(ChartDataEntry(x: Double(x1), y: Double(y1)))
			regressionList.append(ChartDataEntry(x: Double(x2), y: Double(y2)))
			
			let regressionDataSet = LineChartDataSet(entries: regressionList, label: "")
			regressionData.addDataSet(regressionDataSet)
			
			return DataSetWrapper(regressionDataSet)
		}
		
		override func addValueIntoSet(xValue: Float, yValue: Float, dataSetIndex: Int32) {
			scatterData.addEntry(ChartDataEntry(x: Double(xValue), y: Double(yValue)), dataSetIndex: Int(dataSetIndex))
		}
		
		override func addLegendEntry(label: String, color: String, isPublic: Bool) {
			let entry = LegendEntry(
				label: label,
				form: .default,
				formSize: CGFloat.nan,
				formLineWidth: CGFloat.nan,
				formLineDashPhase: CGFloat.nan,
				formLineDashLengths: nil,
				formColor: DataSetWrapper.getNSUIColor(color)
			)
			
			if(!isPublic) {
				entry.form = .circle
			}
			legendEntries.append(entry)
		}
		
		override func createChart() -> Any {
			let chartView = CombinedChartView()
			chartViewRef = chartView
			let data = CombinedChartData()
			data.scatterData = scatterData
			data.lineData = regressionData
			chartView.data = data
			
			if(!legendEntries.isEmpty) {
				chartView.legend.setCustom(entries: legendEntries)
			}
			setupChart(chartView: ChartViewWrapper(chartView))
			return chartView
		}
		
		override func postUpdateChart() {
			let chartView = chartViewRef
			if(chartView != nil) {
				if(!legendEntries.isEmpty) {
					chartView!.legend.setCustom(entries: legendEntries)
				}
				postUpdateChart(chartView: ChartViewWrapper(chartView!))
			}
		}
		
		override func removeEntries() {
			for dataSet in scatterData.dataSets {
				dataSet.clear()
			}
		}
	}
	private class IOsPieChartBuilder : PieChartBuilder {
		var colors: [NSUIColor] = []
		var entries: [PieChartDataEntry] = []
		
		override init(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo) {
			super.init(chartInfoCollection: chartInfoCollection, chartInfo: chartInfo)
		}
		
		override func addColor(color: String) {
			colors.append(DataSetWrapper.getNSUIColor(color))
		}
		override func addEntry(value: Float, label: String) {
			entries.append(PieChartDataEntry(value: Double(value), label: label))
		}
		
		
		override func createChart() -> Any {
			let pieDataSet = PieChartDataSet(entries: entries, label: "")
			if(entries.count == 0) {
				entries.append(PieChartDataEntry(value: 1, label: NSLocalizedString("no_chart_data", comment: "")))
                pieDataSet.drawValuesEnabled = false
			}
			let chartView = PieChartView()
			setupChart(chartView: ChartViewWrapper(chartView))

			if(!colors.isEmpty) {
				pieDataSet.colors = colors
			}
			setupDataSet(dataSet: DataSetWrapper(pieDataSet))
			chartView.data = PieChartData(dataSet: pieDataSet)
			
			return chartView
		}
		
		override func removeEntries() {
			colors = []
			entries = []
		}
	}
	
	
	
	func getBarChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo) -> ChartBuilder {
		IOsBarChartBuilder(chartInfoCollection: chartInfoCollection, chartInfo: chartInfo)
	}
	
	func getLineChartBuilder(isLineFilled: Bool, chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo) -> ChartBuilder {
		IOsLineChartBuilder(chartInfoCollection: chartInfoCollection, chartInfo: chartInfo)
	}
	
	func getPieChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo) -> ChartBuilder {
		IOsPieChartBuilder(chartInfoCollection: chartInfoCollection, chartInfo: chartInfo)
	}
	
	func getScatterChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo) -> ChartBuilder {
		IOsScatterChartBuilder(chartInfoCollection: chartInfoCollection, chartInfo: chartInfo)
	}
}
