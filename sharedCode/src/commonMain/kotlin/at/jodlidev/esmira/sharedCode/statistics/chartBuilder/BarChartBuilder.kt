package at.jodlidev.esmira.sharedCode.statistics.chartBuilder

import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfo
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection
import at.jodlidev.esmira.sharedCode.statistics.ChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.ChartDataSetInterface
import at.jodlidev.esmira.sharedCode.statistics.ChartViewInterface

/**
 * Created by JodliDev on 07.10.2020.
 */
abstract class BarChartBuilder(
	chartInfoCollection: ChartInfoCollection,
	chartInfo: ChartInfo
) : ChartBuilder(chartInfoCollection, chartInfo) {
	override fun setupDataSet(dataSet: ChartDataSetInterface, color: String, isPublic: Boolean) {
		dataSet.setColor(color)
		dataSet.setValueFormatter(valueFormatter)
	}
	
	override fun fillData() {
		super.fillData()
		groupBars()
	}
	
	override fun postUpdateChart(chartView: ChartViewInterface) {
		groupBars()
		super.postUpdateChart(chartView)
	}
	
	fun groupBars() {
		val size = chartInfo.axisContainer.size + chartInfo.publicVariables.size
		if(size > 1) {
			val barSpace = 0.01f
			val barWidth = (0.8f - size * barSpace) / size // groupSpace + 0.8f = 1
			groupBars(barWidth, -0.5f, 0.2f, barSpace)
		}
	}
	
	abstract fun groupBars(barWidth: Float, fromX: Float, groupSpace: Float, barSpace: Float)
}