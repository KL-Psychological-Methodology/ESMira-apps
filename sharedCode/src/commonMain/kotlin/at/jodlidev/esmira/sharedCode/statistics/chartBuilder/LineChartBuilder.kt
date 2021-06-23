package at.jodlidev.esmira.sharedCode.statistics.chartBuilder

import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfo
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection
import at.jodlidev.esmira.sharedCode.statistics.ChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.ChartDataSetInterface

/**
 * Created by JodliDev on 07.10.2020.
 */
abstract class LineChartBuilder(
	chartInfoCollection: ChartInfoCollection,
	chartInfo: ChartInfo
) : ChartBuilder(chartInfoCollection, chartInfo) {
	override fun setupDataSet(dataSet: ChartDataSetInterface, color: String, isPublic: Boolean) {
		dataSet.setColor(color)
		dataSet.setCircleColor(color)
		dataSet.setCircleRadius(3f)
		dataSet.setDrawCircleHole(false)
		dataSet.setValueFormatter(valueFormatter)
		if(chartInfo.chartType == ChartInfo.CHART_TYPE_LINE_FILLED) {
			dataSet.setMode(ChartDataSetInterface.Mode.CubicBezier)
			dataSet.setDrawFilled(true)
			dataSet.setFillColor("#55${color.substring(1)}")
		}
	}
}