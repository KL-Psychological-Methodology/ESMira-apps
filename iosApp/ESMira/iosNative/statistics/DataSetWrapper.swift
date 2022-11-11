//
// Created by JodliDev on 07.10.20.
//

import Foundation
import sharedCode
import Charts
import UIKit

class DataSetWrapper : ChartDataSetInterface {
    class LibValueFormatter: ValueFormatter {
        let statisticFormatter: ChartFormatterInterface
        
        init(_ statisticFormatter: ChartFormatterInterface) {
            self.statisticFormatter = statisticFormatter
        }
        
        func stringForValue(_ value: Double, entry: ChartDataEntry, dataSetIndex: Int, viewPortHandler: ViewPortHandler?) -> String {
            return statisticFormatter.getString(value: Float(value))
        }
    }
    let dataSet: ChartBaseDataSet
    init(_ dataSet: ChartBaseDataSet) {
        self.dataSet = dataSet
    }
    
	func setCircleRadius(x: Float) {
        (dataSet as! LineChartDataSet).circleRadius = CGFloat(x)
	}
	
	func setForm(form: ChartDataSetInterfaceShape) {
        switch(form) {
            case .square:
                dataSet.form = .square
            case .circle:
                dataSet.form = .circle
            case .line:
                dataSet.form = .line
            case .empty:
                dataSet.form = .empty
            case .none:
                dataSet.form = .none
            default:
                print("form is not implemented")
            
        }
	}
	
	func setScatterShape(shape: ChartDataSetInterfaceShape) {
        switch(shape) {
            case .square:
                (dataSet as! ScatterChartDataSet).setScatterShape(.square)
            case .circle:
                (dataSet as! ScatterChartDataSet).setScatterShape(.circle)
            case .triangle:
                (dataSet as! ScatterChartDataSet).setScatterShape(.triangle)
            case .cross:
                (dataSet as! ScatterChartDataSet).setScatterShape(.cross)
            case .x:
                (dataSet as! ScatterChartDataSet).setScatterShape(.x)
            case .chevronup:
                (dataSet as! ScatterChartDataSet).setScatterShape(.chevronUp)
            case .chevrondown:
                (dataSet as! ScatterChartDataSet).setScatterShape(.chevronDown)
            default:
                print("setScatterShape is not implemented")
            
        }
	}
	
	func setScatterShapeSize(x: Float) {
        (dataSet as! ScatterChartDataSet).scatterShapeSize = CGFloat(x)
	}
	
	
	
	func setCircleColor(color: String) {
        (dataSet as! LineChartDataSet).setCircleColor(DataSetWrapper.getNSUIColor(color))
	}
	
	func setColor(color: String) {
        dataSet.setColor(DataSetWrapper.getNSUIColor(color))
	}
	
	func setDrawCircleHole(x: Bool) {
        (dataSet as! LineChartDataSet).drawCircleHoleEnabled = x
	}
	
	func setDrawCircles(x: Bool) {
        (dataSet as! LineChartDataSet).drawCirclesEnabled = x
	}
	
	func setDrawFilled(x: Bool) {
        (dataSet as! LineChartDataSet).drawFilledEnabled = x
	}
	
	func setDrawValues(x: Bool) {
        dataSet.drawValuesEnabled = x
	}
	
	func setFillColor(color: String) {
        (dataSet as! LineChartDataSet).fillColor = DataSetWrapper.getNSUIColor(color)
	}
	
	func setHighlightEnabled(x: Bool) {
        dataSet.highlightEnabled = x
	}
	
	func setMode(mode: ChartDataSetInterfaceMode) {
        switch(mode) {
            case .linear:
                (dataSet as! LineChartDataSet).mode = .linear
            case .cubicbezier:
                (dataSet as! LineChartDataSet).mode = .cubicBezier
            case .horizontalbezier:
                (dataSet as! LineChartDataSet).mode = .horizontalBezier
            case .stepped:
                (dataSet as! LineChartDataSet).mode = .stepped
			default:
				print("mode is not implemented")
		}
	}
	
	func setSliceSpace(x: Float) {
        (dataSet as! PieChartDataSet).sliceSpace = CGFloat(x)
	}
	
	func setValueFormatter(formatter: ChartFormatterInterface) {
        dataSet.valueFormatter = LibValueFormatter(formatter)
	}
    
    static func getNSUIColor(_ hex: String) -> NSUIColor {
        //thanks to https://www.hackingwithswift.com/example-code/uicolor/how-to-convert-a-hex-color-to-a-uicolor
        if(!hex.hasPrefix("#")) {
            return NSUIColor.black
        }
        let r, g, b, a: CGFloat
        let start = hex.index(hex.startIndex, offsetBy: 1)
        let hexColor = String(hex[start...])
        
        let scanner = Scanner(string: (hexColor.count == 6) ? "FF\(hexColor)" : hexColor)
        
        var hexNumber: UInt64 = 0
        
        if(!scanner.scanHexInt64(&hexNumber)) {
            return NSUIColor.black
        }
        
        a = CGFloat((hexNumber & 0xff000000) >> 24) / 255
        r = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
        g = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
        b = CGFloat(hexNumber & 0x000000ff) / 255
        
        return NSUIColor(red: r, green: g, blue: b, alpha: a)
    }
}
