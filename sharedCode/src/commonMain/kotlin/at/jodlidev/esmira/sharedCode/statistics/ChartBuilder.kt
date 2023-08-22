package at.jodlidev.esmira.sharedCode.statistics

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.statistics.*
import kotlin.math.roundToInt

/**
 * Created by JodliDev on 30.06.2020.
 */
abstract class ChartBuilder(
	var chartInfoCollection: ChartInfoCollection,
	var chartInfo: ChartInfo
) {
	private class PerValueFormatter(val keys: List<String>) : ChartFormatterInterface {
		override fun getString(value: Float): String {
			val index: Int = value.toInt()
			return if(index.toFloat() == value && index < keys.size) {
				val rValue = keys[index]
				if(rValue.length > 15) "${rValue.substring(0,12)}..." else rValue
			}
			else ""
		}
	}
	private class AddValueFormatter(val addValue: Float) : ChartFormatterInterface {
		override fun getString(value: Float): String {
			return (value + addValue).roundToInt().toString()
		}
	}
	private class DateFormatter(private val firstDay: Long): ChartFormatterInterface {
		override fun getString(value: Float): String {
			return NativeLink.formatDate((firstDay + StatisticData_timed.ONE_DAY * value.toInt()) * 1000)
		}
	}
	private class PercentFormatter: ChartFormatterInterface {
		override fun getString(value: Float): String {
			
			return if(value == 0F) "" else "${value.roundToInt()}%"
		}
	}
	private class BlankFormatter: ChartFormatterInterface {
		override fun getString(value: Float): String {
			return ""
		}
	}
	private class FloatFormatter: ChartFormatterInterface {
		override fun getString(value: Float): String {
//			return value.roundToInt().toString()
//			return if(value == 0F) "" else value.roundToInt().toString()
			return if(value == 0F) ""
			else if(value.roundToInt().toFloat() == value) value.roundToInt().toString()
			else ((value*10).roundToInt()/10F).toString()
		}
	}
	
	
	private lateinit var sortedValueIndex: List<String> //will be used when storageType is CHART_TYPE_FREQ_DISTR
	private var valueSum = 0 //will be used when storageType is CHART_TYPE_FREQ_DISTR
	
	var valueFormatter: ChartFormatterInterface = if(chartInfo.inPercent) PercentFormatter() else FloatFormatter()
	lateinit var xAxisFormatter: ChartFormatterInterface
	protected var entriesCount: Int = 0
	
	internal var xMin = Float.MAX_VALUE
	internal var xMax = Float.MIN_VALUE
	
	abstract fun createChart(): Any
	protected abstract fun removeEntries()
	
	private fun getKey(axis: ChartInfo.AxisData) : String {
		return axis.variableName + axis.observedVariableIndex
	}
	
	protected open fun setupChart(chartView: ChartViewInterface) {
		chartView.setDescriptionEnabled(false)
		
		val legend = chartView.getLegend()
		legend.setVerticalAlignment(ChartViewInterface.Legend.VerticalAlignment.Top)
		legend.setHorizontalAlignment(ChartViewInterface.Legend.HorizontalAlignment.Right)
		legend.setOrientation(
			if(chartView.getDataSetCount() > 2)
				ChartViewInterface.Legend.Orientation.Vertical
			else
				ChartViewInterface.Legend.Orientation.Horizontal
		)
		
		val xAxis = chartView.getXAxis()
		xAxis.setSpaceMin(0.5f)
		xAxis.setSpaceMax(0.5f)
		xAxis.setDrawGridLines(false)
		xAxis.setPosition(ChartViewInterface.Axis.Position.Bottom)
		xAxis.setValueFormatter(xAxisFormatter)
		
		val axisLeft = chartView.getLeftAxis()
		axisLeft.setDrawAxisLine(true)
		axisLeft.setDrawGridLines(true)
		axisLeft.setAxisMinimum(0f)
		if(chartInfo.maxYValue != 0)
			axisLeft.setAxisMaximum(chartInfo.maxYValue.toFloat())
		
		val axisRight = chartView.getRightAxis()
		axisRight.setDrawAxisLine(true)
		axisRight.setDrawGridLines(false)
		axisRight.setDrawLabels(false)
		
		val datapointsNum: Int = ZOOM_MIN_VISIBLE_DATAPOINTS.coerceAtMost(entriesCount)
		xAxis.setLabelCount(datapointsNum)
		
//		chartView.marker = CustomMarkerView(context, R.layout.item_statistics_marker, this) //TODO
		chartView.setDoubleTapToZoomEnabled(false)
		chartView.setScaleYEnabled(false)
		chartView.setMinOffset(30f)
		chartView.setHighlightPerTapEnabled(false)
		chartView.setHighlightPerDragEnabled(false)
		chartView.notifyDataSetChanged()
		chartView.setVisibleXRangeMinimum(datapointsNum.toFloat())
		chartView.fitScreen()
		
		when(chartInfo.dataType) {
			ChartInfo.DATA_TYPE_DAILY -> {
				xAxis.setGranularity(1f)
				xAxis.setLabelRotationAngle(-45f)
				if(entriesCount > ZOOM_USUAL_BASE_DATAPOINTS) {
					chartView.zoom(entriesCount.toFloat() / ZOOM_USUAL_BASE_DATAPOINTS, 1f, 0f, 0f)
					chartView.moveViewToX(entriesCount.toFloat())
				}
			}
			ChartInfo.DATA_TYPE_FREQ_DISTR -> {
				if(!chartInfo.xAxisIsNumberRange)
					xAxis.setLabelRotationAngle(-45f)
			}
		}
	}
	
	protected abstract fun postUpdateChart()
	protected open fun postUpdateChart(chartView: ChartViewInterface) {// PieCharts dont have public data - we can ignore them
		chartView.notifyCurrentDataChanged()
		
		val xAxis = chartView.getXAxis()
		xAxis.setLabelCount(ZOOM_MIN_VISIBLE_DATAPOINTS.coerceAtMost(entriesCount))
		xAxis.setValueFormatter(xAxisFormatter)
		
		if(chartView.getDataSetCount() > 2) {
			val legend = chartView.getLegend()
			legend.setOrientation(ChartViewInterface.Legend.Orientation.Vertical)
		}
		chartView.notifyDataSetChanged()
		val dataPointsNum: Int = ZOOM_MIN_VISIBLE_DATAPOINTS.coerceAtMost(entriesCount)
		chartView.setVisibleXRangeMinimum(dataPointsNum.toFloat())
		if(dataPointsNum != 0) {
			chartView.fitScreen()
			if(entriesCount > ZOOM_USUAL_BASE_DATAPOINTS && chartInfo.dataType == ChartInfo.DATA_TYPE_DAILY) {
				chartView.zoom(entriesCount.toFloat() / ZOOM_USUAL_BASE_DATAPOINTS, 1f, 0f, 0f)
				chartView.moveViewToX(entriesCount.toFloat())
			}
		}
	}
	
	
	open fun initDataSet(label: String, color: String, isPublic: Boolean = false) {
		setupDataSet(createDataSet(label, color), color, isPublic)
	}
	abstract fun createDataSet(label: String, color: String): ChartDataSetInterface
	protected abstract fun setupDataSet(dataSet: ChartDataSetInterface, color: String, isPublic: Boolean = false)
	
	protected abstract fun addValue(xValue: Float, yValue: Float, dataSetIndex: Int)
	
	
	internal open fun fillData() {
		val dataListContainer = chartInfoCollection.dataListContainer
		
		if(chartInfo.displayPublicVariable && chartInfo.publicVariables.isNotEmpty()) {
			for(axisContainer in chartInfo.publicVariables) {
				initDataSet(axisContainer.label, axisContainer.color, true)
			}
		}
		if(chartInfo.axisContainer.isEmpty()) {
			xAxisFormatter = BlankFormatter()
			return
		}
		for(axisContainer in chartInfo.axisContainer) {
			initDataSet(axisContainer.label, axisContainer.color)
		}
		val publicSum = if(chartInfo.displayPublicVariable) chartInfo.publicVariables.size else 0
		
		
		when(chartInfo.dataType) {
			ChartInfo.DATA_TYPE_DAILY -> {
				xAxisFormatter = DateFormatter(chartInfoCollection.firstDay)
				
				for((i, axisContainer) in chartInfo.axisContainer.withIndex()) {
					val statistics = dataListContainer[getKey(axisContainer.yAxis)] ?: continue //it is possible that key does ot exist when no data was saved for a variable yet
					fillTimed(statistics, publicSum+i)
				}
			}
			ChartInfo.DATA_TYPE_FREQ_DISTR -> {
				//create an index first:
				if(chartInfo.xAxisIsNumberRange) {
					for(axisContainer in chartInfo.axisContainer) {
						val statistics = dataListContainer[getKey(axisContainer.yAxis)] ?: continue //it is possible that a key does ot exist when no data was saved for a variable yet
						setMinMaxForFreqDistr(statistics)
					}
					xAxisFormatter = AddValueFormatter(xMin)
				}
				else {
					val unsortedValueIndex = HashSet<String>()
					for(axisContainer in chartInfo.axisContainer) {
						val statistics = dataListContainer[getKey(axisContainer.yAxis)] ?: continue //it is possible that a key does ot exist when no data was saved for a variable yet
						indexFreqDistrValues(statistics, unsortedValueIndex)
					}
					this.sortedValueIndex = unsortedValueIndex.sortedWith(this.getFreqDistrComparator())
					xAxisFormatter = PerValueFormatter(sortedValueIndex)
				}
				
				for((i, axisContainer) in chartInfo.axisContainer.withIndex()) {
					val statistics = dataListContainer[getKey(axisContainer.yAxis)] ?: continue //it is possible that a key does ot exist when no data was saved for a variable yet
					fillFreqDistr(statistics, publicSum+i)
				}
			}
			ChartInfo.DATA_TYPE_SUM -> {
				xAxisFormatter = BlankFormatter()
				
				for((i, axisContainer) in chartInfo.axisContainer.withIndex()) {
					val statistics = dataListContainer[getKey(axisContainer.yAxis)]
					if(statistics == null) {//is the case when no data was saved for a variable yet
						addValue(0f, 0f, publicSum+i)
					}
					else
						fillSum(statistics, publicSum+i)
				}
			}
			ChartInfo.DATA_TYPE_XY -> {
				xAxisFormatter = FloatFormatter()
				
				for((i, axisContainer) in chartInfo.axisContainer.withIndex()) {
					fillXY(
						dataListContainer[getKey(axisContainer.xAxis)] ?: continue,
						dataListContainer[getKey(axisContainer.yAxis)] ?: HashMap(),
						publicSum+i
					)
				}
			}
		}
		
		
		
		
//		when(chartInfo.dataType) {
//			ChartInfo.DATA_TYPE_DAILY -> {
//				xAxisFormatter = DateFormatter(chartInfoCollection.firstDay)
//
//				for(axisContainer in chartInfo.axisContainer) {
//					val statistics = dataListContainer[getKey(axisContainer.yAxis)] ?: continue //it is possible that key does ot exist when no data was saved for a variable yet
//					fillTimed(axisContainer, statistics)
//				}
//			}
//			ChartInfo.DATA_TYPE_FREQ_DISTR -> {
//				//create an index first:
//				if(chartInfo.xAxisIsNumberRange) {
//					for(axisContainer in chartInfo.axisContainer) {
//						val statistics = dataListContainer[getKey(axisContainer.yAxis)] ?: continue //it is possible that a key does ot exist when no data was saved for a variable yet
//
//						setMinMaxForFreqDistr(statistics)
//					}
//
//					xAxisFormatter = AddValueFormatter(xMin)
//				}
//				else {
//					val unsortedValueIndex = HashSet<String>()
//					for(axisContainer in chartInfo.axisContainer) {
//						val statistics = dataListContainer[getKey(axisContainer.yAxis)] ?: continue //it is possible that a key does ot exist when no data was saved for a variable yet
//
//						indexFreqDistrValues(statistics, unsortedValueIndex)
//					}
//					this.sortedValueIndex = unsortedValueIndex.sortedWith(this.getFreqDistrComparator())
//					xAxisFormatter = PerValueFormatter(sortedValueIndex)
//				}
//				for(axisContainer in chartInfo.axisContainer) {
//					val statistics = dataListContainer[getKey(axisContainer.yAxis)] ?: continue //it is possible that a key does ot exist when no data was saved for a variable yet
//					fillFreqDistr(axisContainer, statistics)
//				}
//			}
//
//			ChartInfo.DATA_TYPE_SUM -> {
//				xAxisFormatter = BlankFormatter()
//
//				for(axisContainer in chartInfo.axisContainer) {
//					val statistics = dataListContainer[getKey(axisContainer.yAxis)]
//					if(statistics == null) {//is the case when no data was saved for a variable yet
//						addValue(0f)
//						packageIntoBox(axisContainer.label, axisContainer.color)
//					}
//					else
//						fillSum(axisContainer, statistics)
//
//					packageIntoBox(axisContainer.label, axisContainer.color)
//				}
//			}
//			ChartInfo.DATA_TYPE_XY -> {
//				xAxisFormatter = FloatFormatter()
//
//				for(axisContainer in chartInfo.axisContainer) {
//					fillXY(
//						axisContainer,
//						dataListContainer[getKey(axisContainer.xAxis)] ?: continue,
//						dataListContainer[getKey(axisContainer.yAxis)] ?: HashMap()
//					)
//				}
//			}
//		}
	}
	
	private fun fillSum(statistics: Map<String, StatisticData>, dataSetIndex: Int) {
		val valueType: Int = chartInfo.valueType
		var count = 0
		var num = 0f
		when(valueType) {
			ChartInfo.VALUE_TYPE_MEAN -> {
				for((_, statisticData) in statistics) {
					num += statisticData.sum.toFloat()
					count += statisticData.count
				}
				if(count > 0)
					num /= count
			}
			ChartInfo.VALUE_TYPE_SUM ->
				for((_, statisticData) in statistics) {
					num += statisticData.sum.toFloat()
				}
			ChartInfo.VALUE_TYPE_COUNT ->
				for((_, statisticData) in statistics) {
					num += statisticData.count.toFloat()
				}
		}
		addValue(0f, num, dataSetIndex)
		entriesCount = 1
	}
	private fun fillTimed(statistics: Map<String, StatisticData>, dataSetIndex: Int) {
		val range = chartInfoCollection.firstDay .. chartInfoCollection.lastDay step StatisticData_timed.ONE_DAY
		val valueType: Int = chartInfo.valueType
		for((i, num) in range.withIndex()) {
			val numStr = num.toString()
			val data = statistics[numStr]
			if(data == null) {
				addValue(i.toFloat(), 0F, dataSetIndex)
				continue
			}
			when(valueType) {
				ChartInfo.VALUE_TYPE_MEAN ->
					addValue(i.toFloat(), (if(data.count != 0) data.sum.toFloat() / data.count.toFloat() else 0f), dataSetIndex)
				ChartInfo.VALUE_TYPE_SUM ->
					addValue(i.toFloat(), data.sum.toFloat(), dataSetIndex)
				ChartInfo.VALUE_TYPE_COUNT ->
					addValue(i.toFloat(), data.count.toFloat(), dataSetIndex)
			}
		}
		entriesCount = range.count()
	}
	private fun fillXY(statisticsX: Map<String, StatisticData>, statisticsY: Map<String, StatisticData>, dataSetIndex: Int) {
		val unsortedList = ArrayList<Pair<Float, Float>>()
		
		for((dayStr, statX) in statisticsX) {
			val xValue = statX.sum.toFloat()
			val yValue = statisticsY[dayStr]?.sum?.toFloat() ?: 0f
			
			unsortedList.add(Pair(xValue, yValue))
		}
		
		//val sortedList = unsortedList.sortedWith(Comparator {pair1: Pair<Float, Float>, pair2: Pair<Float, Float> -> (pair1.first - pair2.first).toInt() })
		val sortedList = unsortedList.sortedWith {pair1: Pair<Float, Float>, pair2: Pair<Float, Float> ->
			when {
				pair1.first > pair2.first -> 1
				pair1.first < pair2.first -> -1
				else -> 0
			}
		}
		if(sortedList.isNotEmpty()) {
			val xMinValue = sortedList.first().first
			val xMaxValue = sortedList.last().first
			
			for(pair in sortedList) {
				addValue(pair.first, pair.second, dataSetIndex)
			}
			
			entriesCount = if(xMinValue != Float.MAX_VALUE) (xMaxValue - xMinValue).toInt() + 1 else 0
		}
	}
	private fun fillFreqDistr(statistics: Map<String, StatisticData>, dataSetIndex: Int) {
		val sum = valueSum
		val inPercent = chartInfo.inPercent
		
		if(chartInfo.xAxisIsNumberRange) {
			if(xMin != Float.MAX_VALUE) {
				val xMin = xMin.toInt()
				val xMax: Int = xMax.toInt()
				
				for((i, valueName) in (xMin..xMax).withIndex()) {
					val statisticData = statistics[valueName.toString()]
					if(statisticData == null)
						addValue(i.toFloat(), 0f, dataSetIndex)
					else {
						if(inPercent)
							addValue(i.toFloat(), 100 / ((sum.toFloat() / statisticData.count.toFloat())), dataSetIndex)
						else
							addValue(i.toFloat(), statisticData.count.toFloat(), dataSetIndex)
					}
				}
				entriesCount = xMax - xMin + 1
			}
			else
				entriesCount = 0
		}
		else {
			for((i, valueName) in sortedValueIndex.withIndex()) {
				val statisticData = statistics[valueName]
				if(statisticData == null)
					addValue(i.toFloat(), 0f, dataSetIndex)
				else {
					if(inPercent)
						addValue(i.toFloat(), 100 / ((sum.toFloat() / statisticData.count.toFloat())), dataSetIndex)
					else
						addValue(i.toFloat(), statisticData.count.toFloat(), dataSetIndex)
				}
			}
			entriesCount = sortedValueIndex.size
		}
	}
	
	private fun setMinMaxForFreqDistr(statistics: Map<String, StatisticData>) {
		var sum = 0
		for((_, statisticData) in statistics) {
			if(statisticData !is StatisticData_perValue)
				continue
			
			if(statisticData.value.isEmpty()) //we want to skip empty values
				continue
			val value: Float
			try {
				value = statisticData.value.toFloat()
			}
			catch(e: Exception) {
				continue
			}
			sum += statisticData.count
			if(value < xMin)
				xMin = value
			if(value > xMax)
				xMax = value
		}
		
		valueSum += sum
	}
	private fun getFreqDistrComparator(): Comparator<String> {
		return Comparator { a, b ->
			try {
				a.toInt() - b.toInt()
			}
			catch(e: Exception) {
				when {
					a > b -> 1
					a < b -> -1
					else -> 0
				}
			}
		}
	}
	private fun indexFreqDistrValues(statistics: Map<String, StatisticData>, unsortedValueIndex: MutableSet<String>) {
		var sum = 0
		for((_, statisticData) in statistics) {
			if(statisticData !is StatisticData_perValue)
				continue
			
			val value = statisticData.value
			
			if(value.isEmpty()) //we want to skip empty values
				continue
			
			sum += statisticData.count
			if(!unsortedValueIndex.contains(value))
				unsortedValueIndex.add(value)
		}
		
		valueSum += sum
	}
	
	
	open fun addPublicData(publicChartCollection: ChartInfoCollection) {
		if(!chartInfo.displayPublicVariable || chartInfo.chartType == ChartInfo.CHART_TYPE_PIE) {
			return
		}
		
		val publicDataListContainer = publicChartCollection.dataListContainer
		
		loop@ for((i, publicVariable) in chartInfo.publicVariables.withIndex()) {
			val yVariableKey: String = publicVariable.yAxis.variableName + publicVariable.yAxis.observedVariableIndex
			val publicStatisticsY: Map<String, StatisticData> = publicDataListContainer[yVariableKey] ?: continue
			if(publicStatisticsY.isEmpty())
				continue
			
			try {
				when(chartInfo.dataType) {
					ChartInfo.DATA_TYPE_DAILY -> {
						fillTimed(publicStatisticsY, i)
					}
					ChartInfo.DATA_TYPE_SUM -> {
						fillSum(publicStatisticsY, i)
					}
					ChartInfo.DATA_TYPE_FREQ_DISTR -> {
						//add new values to existing index:
						if(chartInfo.xAxisIsNumberRange) {
							setMinMaxForFreqDistr(publicStatisticsY)
							xAxisFormatter = AddValueFormatter(xMin) //xMin may have changed, so formatter must be updated as well
						}
						else {
							val unsortedValueIndex = if(this::sortedValueIndex.isInitialized) sortedValueIndex.toMutableSet() else HashSet()
							indexFreqDistrValues(publicStatisticsY, unsortedValueIndex)
							sortedValueIndex = unsortedValueIndex.sortedWith(this.getFreqDistrComparator())
							xAxisFormatter = PerValueFormatter(sortedValueIndex) //index may have changed, so formatter must be updated as well
						}
						
						//recreate existing data:
						removeEntries()
						val publicSetsCount = chartInfo.publicVariables.size
						for((i2, axisContainer) in chartInfo.axisContainer.withIndex()) {
							val key: String = axisContainer.yAxis.variableName + axisContainer.yAxis.observedVariableIndex
							val personalStatisticsY = chartInfoCollection.dataListContainer[key] ?: continue
							fillFreqDistr(personalStatisticsY, publicSetsCount+i2)
						}
						
						//create public entries:
						fillFreqDistr(publicStatisticsY, i)
					}
					ChartInfo.DATA_TYPE_XY -> {
						val statisticsX = publicDataListContainer[publicVariable.xAxis.variableName + publicVariable.xAxis.observedVariableIndex] ?: continue@loop
						
						fillXY(statisticsX, publicStatisticsY, i)
					}
				}
			}
			catch(e: Exception) {
				ErrorBox.error("statistics", "Adding data to a chart caused an error", e)
			}
			postUpdateChart()
		}
	}
	
	
	companion object {
		private const val ZOOM_MIN_VISIBLE_DATAPOINTS: Int = 5
		private const val ZOOM_USUAL_BASE_DATAPOINTS: Int = 10
	}
}