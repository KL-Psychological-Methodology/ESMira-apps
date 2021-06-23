package at.jodlidev.esmira.sharedCode.statistics.chartBuilder

import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfo
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection
import at.jodlidev.esmira.sharedCode.statistics.ChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.ChartDataSetInterface
import kotlin.math.sqrt

/**
 * Created by JodliDev on 07.10.2020.
 */
abstract class ScatterChartBuilder(
	chartInfoCollection: ChartInfoCollection,
	chartInfo: ChartInfo
) : ChartBuilder(chartInfoCollection, chartInfo) {
	private class StateData(val color: String) {
		var n: Int = 0
		var xSum = 0F
		var ySum = 0F
		var xySum = 0F
		var xxSum = 0F
		var yySum = 0F
		var xMin = Float.MAX_VALUE
		var xMax = Float.MIN_VALUE
	}
	private val stateData: ArrayList<StateData> = ArrayList()
	
	
	override fun fillData() {
		super.fillData()
		for(value in stateData) {
			createLinearRegression(value)
		}
	}
	override fun addPublicData(publicChartCollection: ChartInfoCollection) {
		super.addPublicData(publicChartCollection)
		
		for(i in 0 until chartInfo.publicVariables.size) {
			createLinearRegression(stateData[i])
		}
	}
	
	protected abstract fun addValueIntoSet(xValue: Float, yValue: Float, dataSetIndex: Int)
	override fun addValue(xValue: Float, yValue: Float, dataSetIndex: Int) {
		addValueIntoSet(xValue, yValue, dataSetIndex*2) //There are two dataSets per data (regression line) - so we have to multiply dataSetIndex by 2
		calcValuesForRegression(xValue, yValue, dataSetIndex)
	}
	abstract fun addLegendEntry(label: String, color: String, isPublic: Boolean)
	
	override fun initDataSet(label: String, color: String, isPublic: Boolean) {
		setupDataSet(createDataSet(label, color), color, isPublic)
		createDataSet("", color) //for regression line
		addLegendEntry(label, color, isPublic)
		stateData.add(StateData(color))
//		createLinearRegression(color)
	}
	override fun setupDataSet(dataSet: ChartDataSetInterface, color: String, isPublic: Boolean) {
		dataSet.setColor(color)
		dataSet.setDrawValues(false)
		if(!isPublic) {
			dataSet.setScatterShape(ChartDataSetInterface.Shape.Circle)
			dataSet.setForm(ChartDataSetInterface.Shape.Circle)
			dataSet.setScatterShapeSize(12f)
		}
	}
	
	private fun calcValuesForRegression(xValue: Float, yValue: Float, dataSetIndex: Int) {
		val data = stateData[dataSetIndex]
		++data.n
		data.xSum += xValue
		data.ySum += yValue
		data.xySum += xValue*yValue
		data.xxSum += xValue*xValue
		data.yySum += yValue*yValue
		if(xValue < data.xMin)
			data.xMin = xValue
		if(xValue > data.xMax)
			data.xMax = xValue
	}
	private fun createLinearRegression(data: StateData) {
		if(data.n >= 2) {
			val r2NotSquared = (data.n*data.xySum - data.xSum*data.ySum) / sqrt((data.n*data.xxSum - data.xSum*data.xSum) * (data.n*data.yySum - data.ySum*data.ySum))
			
			if((r2NotSquared*r2NotSquared)*100 > chartInfo.fitToShowLinearProgression) {
				val slope = (data.n * data.xySum - data.xSum * data.ySum) / (data.n * data.xxSum - data.xSum * data.xSum)
				val intercept = (data.ySum - slope * data.xSum) / data.n
				
				setupRegressionLineDataSet(packageLinearRegressionIntoBox(data.xMin, intercept + slope * data.xMin, data.xMax, intercept + slope * data.xMax), data.color)
			}
		}
	}
	
	private fun setupRegressionLineDataSet(dataSet: ChartDataSetInterface, color: String) {
		dataSet.setColor(color)
		dataSet.setDrawCircles(false)
		dataSet.setDrawValues(false)
		dataSet.setHighlightEnabled(false)
	}
	
	protected abstract fun packageLinearRegressionIntoBox(x1: Float, y1: Float, x2: Float, y2: Float): ChartDataSetInterface
}