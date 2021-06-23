package at.jodlidev.esmira.sharedCode.statistics

import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfo
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection

/**
 * Created by JodliDev on 06.10.2020.
 */
interface ChartChooserInterface {
	interface Chart {
		fun createChart()
		fun addValue(xValue: Float, yValue: Float)
		fun packageIntoBox(label: String, color: String)
		fun currentValueCount(): Int
		fun reset()
	}
	fun getLineChartBuilder(isLineFilled: Boolean, chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo): ChartBuilder
	fun getBarChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo): ChartBuilder
	fun getScatterChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo): ChartBuilder
	fun getPieChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo): ChartBuilder
}