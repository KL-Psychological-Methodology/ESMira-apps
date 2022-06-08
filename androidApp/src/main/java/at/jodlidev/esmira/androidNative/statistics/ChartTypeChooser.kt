package at.jodlidev.esmira.androidNative.statistics

import android.content.Context
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
import com.github.mikephil.charting.formatter.ValueFormatter
import java.lang.ref.WeakReference

/**
 * Created by JodliDev on 06.10.2020.
 */
class ChartTypeChooser(private val context: Context) : ChartChooserInterface {

//	private class CustomMarkerView constructor(context: Context?, layoutResource: Int, private val androidChartBuilder: AndroidChartBuilder) : MarkerView(context, layoutResource) {
//		private val tvContent: TextView = findViewById(R.id.tvContent)
//		private val outerLayer: ViewGroup = findViewById(R.id.outerLayer)
//		private lateinit var calcOffset: MPPointF
//		private var rightSided = false
//		private var xPos = 0F
//
//		private val intFormatter: ValueFormatter = object : ValueFormatter() {
//			private val float_format: DecimalFormat = DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.getDefault()))
//			override fun getFormattedValue(value: Float): String {
//				return float_format.format(value.toDouble())
//			}
//		}
//
//		override fun refreshContent(entry: Entry, highlight: Highlight) {
//			val xLabel = if(androidChartBuilder.statisticChart.xAxisLabel.isEmpty()) context.getString(R.string.axis_x_name) else androidChartBuilder.statisticChart.xAxisLabel
//			val yLabel = if(androidChartBuilder.statisticChart.yAxisLabel.isEmpty()) context.getString(R.string.axis_y_name) else androidChartBuilder.statisticChart.yAxisLabel
//
////			val xValue = androidChartBuilder.xAxisFormatter.getString(e.x.roundToInt().toFloat())
//			val xValue = androidChartBuilder.xAxisFormatter.getString(entry.x)
//			val yValue = intFormatter.getFormattedValue(entry.y)
//
//			val lineX = if(xValue.isEmpty()) "" else "<b>$xLabel:</b> $xValue"
//			val lineY = if(yValue.isEmpty()) "" else "<b>$yLabel:</b> $yValue"
//
//			tvContent.text = HtmlCompat.fromHtml("$lineX<br/>$lineY", HtmlCompat.FROM_HTML_MODE_LEGACY)
//
//			xPos = highlight.drawX
//
//			super.refreshContent(entry, highlight)
//		}
//
//		override fun getOffset(): MPPointF {
//			val rightSided = xPos + width > androidChartBuilder.chartView.width
//			if(!this::calcOffset.isInitialized || this.rightSided != rightSided) {
//				if(rightSided) {
//					calcOffset = MPPointF(-width.toFloat(), (-height).toFloat())
//					outerLayer.background = ContextCompat.getDrawable(context, R.drawable.shape_speech_bubble_right)
//				}
//				else {
//					calcOffset = MPPointF(0F, (-height).toFloat())
//					outerLayer.background = ContextCompat.getDrawable(context, R.drawable.shape_speech_bubble_left)
//				}
//			}
//
//			this.rightSided = rightSided
//			return calcOffset
//		}
//	}
	
	
	private class AndroidFormatter(private val formatter: ChartFormatterInterface): ValueFormatter() {
		override fun getFormattedValue(value: Float): String {
			return formatter.getString(value)
		}
	}
	
	private class AndroidLineChartBuilder(
		private val context: Context,
		chartInfoCollection: ChartInfoCollection,
		chartInfo: ChartInfo
	) : LineChartBuilder(chartInfoCollection, chartInfo) {
		private var lineData: LineData = LineData()
		private lateinit var chartViewRef: WeakReference<LineChart>
		
		override fun createDataSet(label: String, color: String): ChartDataSetInterface {
			val dataSet = LineDataSet(ArrayList<Entry>(), label)
			dataSet.valueTextColor = ContextCompat.getColor(context, R.color.textDefault) //make dark mode ready
			
			lineData.addDataSet(dataSet)
			return DataSetWrapper(dataSet)
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
			val chartView = chartViewRef.get() ?: return
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
		private lateinit var chartViewRef: WeakReference<BarChart>
		
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
			
			setupChart(ChartViewWrapper(chartView))
			return chartView
		}
		override fun postUpdateChart() {
			val chartView = chartViewRef.get() ?: return
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
		private lateinit var chartViewRef: WeakReference<CombinedChart>


		override fun createDataSet(label: String, color: String): ChartDataSetInterface {
			val dataSet = ScatterDataSet(ArrayList<Entry>(), label)
			dataSet.valueTextColor = ContextCompat.getColor(context, R.color.textDefault) //make dark mode ready
			
			scatterData.addDataSet(dataSet)
			return DataSetWrapper(dataSet)
		}

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
			val chartView = chartViewRef.get() ?: return

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
				pieDataSet.colors = colors
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