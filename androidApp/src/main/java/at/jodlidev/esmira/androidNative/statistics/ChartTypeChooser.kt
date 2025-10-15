package at.jodlidev.esmira.androidNative.statistics

import android.content.Context
import android.service.autofill.Dataset
import android.view.View
import androidx.core.content.ContextCompat
import at.jodlidev.esmira.*
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfo
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection
import at.jodlidev.esmira.sharedCode.statistics.ChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.ChartChooserInterface
import at.jodlidev.esmira.sharedCode.statistics.ChartFormatterInterface
import at.jodlidev.esmira.sharedCode.statistics.ChartDataSetInterface
import at.jodlidev.esmira.sharedCode.statistics.chartBuilder.BarChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.chartBuilder.LineChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.chartBuilder.PieChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.chartBuilder.ScatterChartBuilder
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.*
import java.lang.ref.WeakReference

/**
 * Created by JodliDev on 06.10.2020.
 */
class ChartTypeChooser(private val context: Context) : ChartChooserInterface {
	
	private class AndroidLineChartBuilder(
		private val context: Context,
		chartInfoCollection: ChartInfoCollection,
		chartInfo: ChartInfo
	) : LineChartBuilder(chartInfoCollection, chartInfo) {
		private var lineData: LineData = LineData()
		private var chartViewRef: WeakReference<LineChart>? = null
		
		override fun createDataSet(label: String, color: String): ChartDataSetInterface {
			val dataSet = LineDataSet(ArrayList<Entry>(), label)
			dataSet.valueTextColor = ContextCompat.getColor(context, R.color.textDefault) //make dark mode ready
			
			lineData.addDataSet(dataSet)
			return DataSetWrapper(dataSet)
		}

		override fun applyThreshold() {
			for ((i, dataset) in lineData.dataSets.withIndex()) {
				if(!useThreshold(i)) {
					continue
				}
				val values = (0..<dataset.entryCount).map { j ->
					val entry = dataset.getEntryForIndex(j)
					entry.y.toDouble()
				}
				val colors = getThresholdColors(values, i) ?:continue
				DataSetWrapper(dataset as LineDataSet).setCircleColors(colors)
			}
		}
		
		override fun addValue(xValue: Float, yValue: Float, dataSetIndex: Int) {
			lineData.addEntry(Entry(xValue, yValue), dataSetIndex)
		}
		
		override fun createChart(): View {
			val chartView = LineChart(context)
			
			//make dark mode ready:
			chartView.legend.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.xAxis.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.axisLeft.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.description.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.axisRight.textColor = ContextCompat.getColor(context, R.color.textDefault)
			
			chartViewRef = WeakReference(chartView)
			chartView.data = lineData
			setupChart(ChartViewWrapper(chartView))
			return chartView
		}
		
		override fun postUpdateChart() {
			val chartView = chartViewRef?.get() ?: return
			postUpdateChart(ChartViewWrapper(chartView))
		}
		override fun removeEntries() {
			for(dataSet in lineData.dataSets) {
				dataSet.clear()
			}
		}
	}
	
	private class AndroidBarChartBuilder(
		private val context: Context,
		chartInfoCollection: ChartInfoCollection,
		chartInfo: ChartInfo
	) : BarChartBuilder(chartInfoCollection, chartInfo) {
		private var barData: BarData = BarData()
		private var chartViewRef: WeakReference<BarChart>? = null
		
		override fun groupBars(barWidth: Float, fromX: Float, groupSpace: Float, barSpace: Float) {
			barData.barWidth = barWidth
			barData.groupBars(fromX, groupSpace, barSpace)
		}
		override fun createDataSet(label: String, color: String): ChartDataSetInterface {
			val dataSet = BarDataSet(ArrayList<BarEntry>(), label)
			dataSet.valueTextColor = ContextCompat.getColor(context, R.color.textDefault) //make dark mode ready
			
			barData.addDataSet(dataSet)
			return DataSetWrapper(dataSet)
		}

		override fun applyThreshold() {
			for ((i, dataset) in barData.dataSets.withIndex()) {

				if(!useThreshold(i)) {
					continue
				}
				val values = (0..<dataset.entryCount).map { j ->
					val entry = dataset.getEntryForIndex(j)
					entry.y.toDouble()
				}
				val colors = getThresholdColors(values, i) ?: continue
				DataSetWrapper(dataset as BarDataSet).setColors(colors)
			}
		}
		
		override fun addValue(xValue: Float, yValue: Float, dataSetIndex: Int) {
			barData.addEntry(BarEntry(xValue, yValue), dataSetIndex)
		}
		override fun createChart(): View {
			val chartView = BarChart(context)
			
			//make dark mode ready:
			chartView.legend.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.xAxis.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.axisLeft.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.description.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.axisRight.textColor = ContextCompat.getColor(context, R.color.textDefault)
			
			chartViewRef = WeakReference(chartView)
			chartView.data = barData

			val legendEntries = chartInfo.axisContainer.map {
				LegendEntry(it.label, Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, DataSetWrapper.getIntColor(it.color))
			}
			chartView.legend.setCustom(legendEntries)
			
			setupChart(ChartViewWrapper(chartView))
			return chartView
		}
		override fun postUpdateChart() {
			val chartView = chartViewRef?.get() ?: return
			postUpdateChart(ChartViewWrapper(chartView))
		}
		override fun removeEntries() {
			for(dataSet in barData.dataSets) {
				dataSet.clear()
			}
		}
	}

	private class AndroidScatterChartBuilder(
		private val context: Context,
		chartInfoCollection: ChartInfoCollection,
		chartInfo: ChartInfo
	) : ScatterChartBuilder(chartInfoCollection, chartInfo) {
		private var scatterData = ScatterData()
		private var regressionData = LineData()
		private var legendEntries = ArrayList<LegendEntry>()
		private var chartViewRef: WeakReference<CombinedChart>? = null


		override fun createDataSet(label: String, color: String): ChartDataSetInterface {
			val dataSet = ScatterDataSet(ArrayList<Entry>(), label)
			dataSet.valueTextColor = ContextCompat.getColor(context, R.color.textDefault) //make dark mode ready
			
			scatterData.addDataSet(dataSet)
			return DataSetWrapper(dataSet)
		}

		override fun applyThreshold() {}

		override fun packageLinearRegressionIntoBox(x1: Float, y1: Float, x2: Float, y2: Float): ChartDataSetInterface {
			val regressionList = ArrayList<Entry>()
			regressionList.add(Entry(x1, y1))
			regressionList.add(Entry(x2, y2))

			val regressionDataSet = LineDataSet(regressionList, "")

			regressionData.addDataSet(regressionDataSet)
			return DataSetWrapper(regressionDataSet)
		}

		override fun addValueIntoSet(xValue: Float, yValue: Float, dataSetIndex: Int) {
			scatterData.addEntry(Entry(xValue, yValue), dataSetIndex)
		}

		override fun addLegendEntry(label: String, color: String, isPublic: Boolean) {
			val entry = LegendEntry(label, Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, DataSetWrapper.getIntColor(color))
			if(!isPublic)
				entry.form = Legend.LegendForm.CIRCLE
			legendEntries.add(entry)
		}

		override fun postUpdateChart() {
			val chartView = chartViewRef?.get() ?: return

			if(legendEntries.isNotEmpty())
				chartView.legend.setCustom(legendEntries)

			postUpdateChart(ChartViewWrapper(chartView))
		}

		override fun createChart(): View {
			val chartView = CombinedChart(context)
			
			//make dark mode ready:
			chartView.legend.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.xAxis.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.axisLeft.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.description.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.axisRight.textColor = ContextCompat.getColor(context, R.color.textDefault)
			
			chartViewRef = WeakReference(chartView)
			val data = CombinedData()
			data.setData(scatterData)
			data.setData(regressionData)
			chartView.data = data

			if(legendEntries.isNotEmpty())
				chartView.legend.setCustom(legendEntries)
			setupChart(ChartViewWrapper(chartView))

			return chartView
		}
		override fun removeEntries() {
			for(dataSet in scatterData.dataSets) {
				dataSet.clear()
			}
		}
	}

	private class AndroidPieChartBuilder(
		private val context: Context,
		chartInfoCollection: ChartInfoCollection,
		chartInfo: ChartInfo
	) : PieChartBuilder(chartInfoCollection, chartInfo) {
		var colors = ArrayList<Int>()
		var entries = ArrayList<PieEntry>()

		override fun addColor(color: String) {
			colors.add(DataSetWrapper.getIntColor(color))
		}

		override fun addEntry(value: Float, label: String) {
			entries.add(PieEntry(value, label))
		}

		override fun applyThreshold() {}

		override fun createChart(): View {
			val pieDataSet = PieDataSet(entries, "")
			pieDataSet.valueTextColor = ContextCompat.getColor(context, R.color.textDefault) //make dark mode ready
			
			if(entries.size == 0) {
				entries.add(PieEntry(1f, context.getString(R.string.no_data)))
				pieDataSet.setDrawValues(false)
			}

			val chartView = PieChart(context)
			
			//make dark mode ready:
			chartView.legend.textColor = ContextCompat.getColor(context, R.color.textDefault)
			chartView.description.textColor = ContextCompat.getColor(context, R.color.textDefault)
			
			setupChart(ChartViewWrapper(chartView))

			if(colors.isNotEmpty())
				pieDataSet.setColors(colors)
			setupDataSet(DataSetWrapper(pieDataSet))

			chartView.data = PieData(pieDataSet)

			return chartView
		}
		
		override fun removeEntries() {
			colors = ArrayList()
			entries = ArrayList()
		}
	}
	
	
	
	override fun getBarChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo): ChartBuilder {
		return AndroidBarChartBuilder(context, chartInfoCollection, chartInfo)
	}
	
	override fun getLineChartBuilder(isLineFilled: Boolean, chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo): ChartBuilder {
		return AndroidLineChartBuilder(context, chartInfoCollection, chartInfo)
	}
	
	override fun getPieChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo): ChartBuilder {
		return AndroidPieChartBuilder(context, chartInfoCollection, chartInfo)
	}
	
	override fun getScatterChartBuilder(chartInfoCollection: ChartInfoCollection, chartInfo: ChartInfo): ChartBuilder {
		return AndroidScatterChartBuilder(context, chartInfoCollection, chartInfo)
	}
}