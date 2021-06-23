package at.jodlidev.esmira.sharedCode.statistics.chartBuilder

import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfo
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection
import at.jodlidev.esmira.sharedCode.statistics.ChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.ChartDataSetInterface
import at.jodlidev.esmira.sharedCode.statistics.ChartViewInterface

/**
 * Created by JodliDev on 07.10.2020.
 */
abstract class PieChartBuilder(
	chartInfoCollection: ChartInfoCollection,
	chartInfo: ChartInfo
) : ChartBuilder(chartInfoCollection, chartInfo) {
	//Note: Pie does not have public data
	
	
	class StateData(
		val color: String,
		val label: String
	) {
		var n: Int = 0
	}
	
	val stateData: ArrayList<StateData> = ArrayList()
	
	
	override fun addValue(xValue: Float, yValue: Float, dataSetIndex: Int) {
		val label = xAxisFormatter.getString(xValue)
		addEntry(yValue, if(label.isEmpty()) stateData[dataSetIndex].label else label)
		++stateData[dataSetIndex].n
	}
	
	override fun initDataSet(label: String, color: String, isPublic: Boolean) {
		stateData.add(StateData(color, label))
	}
	
	override fun createDataSet(label: String, color: String): ChartDataSetInterface {
		throw Exception("Not used in PieChart")
	}
	override fun setupDataSet(dataSet: ChartDataSetInterface, color: String, isPublic: Boolean) {
		throw Exception("Not used in PieChart")
	}
	fun setupDataSet(dataSet: ChartDataSetInterface) {
		dataSet.setHighlightEnabled(false)
		dataSet.setSliceSpace(5f)
	}
	
	override fun fillData() {
		super.fillData()
		completeData()
	}
	
	
	private fun completeData() {
		for(data in stateData) {
			if(data.n == 0)
				continue
			else if(data.n == 1)
				addColor(data.color)
			else {
				val n = data.n
				
				var r = data.color.substring(1, 3).toInt(16)
				var g = data.color.substring(3, 5).toInt(16)
				var b = data.color.substring(5, 7).toInt(16)
				
				val rStep = (255 - r) / (n+1)
				val gStep = (255 - g) / (n+1)
				val bStep = (255 - b) / (n+1)
				
				for(i in 0 until n) {
					r += rStep
					g += gStep
					b += bStep
					addColor("#${r.toString(16)}${g.toString(16)}${b.toString(16)}")
				}
			}
		}
	}
	
	protected abstract fun addEntry(value: Float, label: String)
	protected abstract fun addColor(color: String)
	
	override fun setupChart(chartView: ChartViewInterface) {
		chartView.setDescriptionEnabled(false)
		chartView.setEntryLabelColor("#000000FF")
		chartView.setHighlightPerTapEnabled(false)
		chartView.setDrawHoleEnabled(false)
	}
	
	override fun postUpdateChart() {}
}