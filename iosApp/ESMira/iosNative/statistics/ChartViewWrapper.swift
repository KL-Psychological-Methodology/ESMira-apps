//
// Created by JodliDev on 06.10.20.
//

import Foundation
import Charts
import sharedCode
import UIKit

class ChartViewWrapper : ChartViewInterface {
	class AxisFormatter: IAxisValueFormatter {
		let statisticFormatter: ChartFormatterInterface
		
		init(_ statisticFormatter: ChartFormatterInterface) {
			self.statisticFormatter = statisticFormatter
		}
		
		func stringForValue(_ value: Double, axis: AxisBase?) -> String {
			return statisticFormatter.getString(value: Float(value))
		}
	}
	
	class IOsLegend : ChartViewInterfaceLegend {
		let legend: Legend
		
		init(_ legend: Legend) {
			self.legend = legend
		}
		
		
		func setVerticalAlignment(x: ChartViewInterfaceLegendVerticalAlignment) {
			switch(x) {
				case .top:
					legend.verticalAlignment = Legend.VerticalAlignment.top
				case .center:
					legend.verticalAlignment = Legend.VerticalAlignment.center
				case .bottom:
					legend.verticalAlignment = Legend.VerticalAlignment.bottom
				default:
					print("setPosition not implemented")
					return
			}
		}
		func setHorizontalAlignment(x: ChartViewInterfaceLegendHorizontalAlignment) {
			switch(x) {
				case .left:
					legend.horizontalAlignment = Legend.HorizontalAlignment.left
				case .center:
					legend.horizontalAlignment = Legend.HorizontalAlignment.center
				case .right:
					legend.horizontalAlignment = Legend.HorizontalAlignment.right
				default:
					print("setPosition not implemented")
					return
			}
		}
		
		func setOrientation(x: ChartViewInterfaceLegendOrientation) {
			switch(x) {
				case .vertical:
					legend.orientation = Legend.Orientation.vertical
				case .horizontal:
					legend.orientation = Legend.Orientation.horizontal
				default:
					print("setPosition not implemented")
					return
			}
		}
		
		
	}
	class IOsAxis : ChartViewInterfaceAxis {
		let axis: AxisBase
		
		init(_ axis: AxisBase) {
			self.axis = axis
		}
		func setAxisMaximum(x: Float) {
			axis.axisMaximum = Double(x)
		}
		
		func setAxisMinimum(x: Float) {
			axis.axisMinimum = Double(x)
		}
		
		func setDrawAxisLine(x: Bool) {
			axis.drawAxisLineEnabled = x
		}
		
		func setDrawGridLines(x: Bool) {
			axis.drawGridLinesEnabled = x
		}
		
		func setDrawLabels(x: Bool) {
			axis.drawLabelsEnabled = x
		}
		
		func setLabelCount(x: Int32) {
			axis.labelCount = Int(x)
		}
		
		func setLabelRotationAngle(x: Float) {
			(axis as! XAxis).labelRotationAngle = CGFloat(x)
		}
		
		func setPosition(x: ChartViewInterfaceAxisPosition) {
			switch(x) {
				case .top:
					(axis as! XAxis).labelPosition = .top
				case .topinside:
					(axis as! XAxis).labelPosition = .topInside
				case .bottom:
					(axis as! XAxis).labelPosition = .bottom
				case .bottominside:
					(axis as! XAxis).labelPosition = .bottomInside
				case .bothsided:
					(axis as! XAxis).labelPosition = .bothSided
				default:
					print("setPosition not implemented")
					return
			}
			
		}
		
		func setSpaceMax(x: Float) {
			axis.spaceMax = Double(x)
		}
		
		func setSpaceMin(x: Float) {
			axis.spaceMin = Double(x)
		}
		
		func setValueFormatter(x: ChartFormatterInterface) {
			axis.valueFormatter = AxisFormatter(x)
		}
		
		func setGranularity(x: Float) {
			axis.granularity = Double(x)
		}
		
	}
	
	let chartView: ChartViewBase
	init(_ chartView: ChartViewBase) {
		self.chartView = chartView
	}
	
	func getDataSetCount() -> Int32 {
		Int32(chartView.data?.dataSetCount ?? 0)
	}
	
	func getLeftAxis() -> ChartViewInterfaceAxis {
		return IOsAxis((chartView as! BarLineChartViewBase).leftAxis)
	}
	
	func getLegend() -> ChartViewInterfaceLegend {
		return IOsLegend(chartView.legend)
	}
	
	func getRightAxis() -> ChartViewInterfaceAxis {
		return IOsAxis((chartView as! BarLineChartViewBase).rightAxis)
	}
	
	
	func getXAxis() -> ChartViewInterfaceAxis {
		return IOsAxis(chartView.xAxis)
	}
	
	
	func setDescriptionEnabled(x: Bool) {
		//missing
	}
	
	func setDoubleTapToZoomEnabled(x: Bool) {
		(chartView as! BarLineChartViewBase).doubleTapToZoomEnabled = x
	}
	
	func setHighlightPerDragEnabled(x: Bool) {
		(chartView as! BarLineChartViewBase).highlightPerDragEnabled = x
	}
	
	func setHighlightPerTapEnabled(x: Bool) {
		chartView.highlightPerTapEnabled = x
	}
	
	
	func setDrawHoleEnabled(x: Bool) {
		(chartView as! PieChartView).drawHoleEnabled = x
	}
	
	func setEntryLabelColor(color: String) {
		(chartView as! PieChartView).entryLabelColor = DataSetWrapper.getNSUIColor(color)
	}
	
	func setMinOffset(x: Float) {
		(chartView as! BarLineChartViewBase).minOffset = CGFloat(x)
	}
	
	func setMinimHeight(x: Int32) {
		//TODO
	}
	
	func setScaleYEnabled(x: Bool) {
		(chartView as! BarLineChartViewBase).scaleYEnabled = x
	}
	
	func setVisibleXRangeMinimum(x: Float) {
		(chartView as! BarLineChartViewBase).setVisibleXRangeMinimum(Double(x))
	}
	
	func notifyDataSetChanged() {
		chartView.notifyDataSetChanged()
	}
	func notifyCurrentDataChanged() {
		chartView.data?.notifyDataChanged()
	}
	func fitScreen() {
		(chartView as! BarLineChartViewBase).fitScreen()
	}
	func zoom(scaleX: Float, scaleY: Float, x: Float, y: Float) {
		(chartView as! BarLineChartViewBase).zoom(scaleX: CGFloat(scaleX), scaleY: CGFloat(scaleY), x: CGFloat(x), y: CGFloat(y))
	}
	func moveViewToX(x: Float) {
		(chartView as! BarLineChartViewBase).moveViewToX(Double(x))
	}
	
	func getView() -> Any {
		chartView
	}
	}
