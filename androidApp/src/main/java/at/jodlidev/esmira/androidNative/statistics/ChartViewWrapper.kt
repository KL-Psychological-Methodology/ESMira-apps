package at.jodlidev.esmira.androidNative.statistics

import at.jodlidev.esmira.sharedCode.statistics.ChartViewInterface
import at.jodlidev.esmira.sharedCode.statistics.ChartFormatterInterface
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IAxisValueFormatter

/**
 * Created by JodliDev on 06.10.2020.
 */
class ChartViewWrapper(val chartView: Chart<*>): ChartViewInterface {
	
	class AndroidFormatter(private val formatter: ChartFormatterInterface): IAxisValueFormatter {
		override fun getFormattedValue(value: Float, axis: AxisBase?): String {
			return formatter.getString(value)
		}
	}
	class AndroidLegend(private val legend: Legend): ChartViewInterface.Legend {
		override fun setVerticalAlignment(x: ChartViewInterface.Legend.VerticalAlignment) {
			legend.verticalAlignment = when(x) {
				ChartViewInterface.Legend.VerticalAlignment.Top -> Legend.LegendVerticalAlignment.TOP
				ChartViewInterface.Legend.VerticalAlignment.Center -> Legend.LegendVerticalAlignment.CENTER
				ChartViewInterface.Legend.VerticalAlignment.Bottom -> Legend.LegendVerticalAlignment.BOTTOM
			}
		}
		
		override fun setHorizontalAlignment(x: ChartViewInterface.Legend.HorizontalAlignment) {
			legend.horizontalAlignment = when(x) {
				ChartViewInterface.Legend.HorizontalAlignment.Left -> Legend.LegendHorizontalAlignment.LEFT
				ChartViewInterface.Legend.HorizontalAlignment.Center -> Legend.LegendHorizontalAlignment.CENTER
				ChartViewInterface.Legend.HorizontalAlignment.Right -> Legend.LegendHorizontalAlignment.RIGHT
			}
		}
		
		override fun setOrientation(x: ChartViewInterface.Legend.Orientation) {
			legend.orientation = when(x) {
				ChartViewInterface.Legend.Orientation.Vertical -> Legend.LegendOrientation.VERTICAL
				ChartViewInterface.Legend.Orientation.Horizontal -> Legend.LegendOrientation.HORIZONTAL
			}
		}
	}
	class AndroidAxis(private val axis: AxisBase): ChartViewInterface.Axis {
		override fun setAxisMaximum(x: Float) {
			axis.axisMaximum = x
		}
		
		override fun setAxisMinimum(x: Float) {
			axis.axisMinimum = x
		}
		
		override fun setDrawAxisLine(x: Boolean) = axis.setDrawAxisLine(x)
		
		
		override fun setDrawGridLines(x: Boolean) = axis.setDrawGridLines(x)
		
		
		override fun setDrawLabels(x: Boolean) = axis.setDrawLabels(x)
		
		
		override fun setLabelCount(x: Int) {
			axis.labelCount = x
		}
		
		override fun setLabelRotationAngle(x: Float) {
			(axis as XAxis).labelRotationAngle = x
		}
		
		override fun setPosition(x: ChartViewInterface.Axis.Position) {
			(axis as XAxis).position = when(x) {
				ChartViewInterface.Axis.Position.Top -> XAxis.XAxisPosition.TOP
				ChartViewInterface.Axis.Position.TopInside -> XAxis.XAxisPosition.TOP_INSIDE
				ChartViewInterface.Axis.Position.Bottom -> XAxis.XAxisPosition.BOTTOM
				ChartViewInterface.Axis.Position.BottomInside -> XAxis.XAxisPosition.BOTTOM_INSIDE
				ChartViewInterface.Axis.Position.BothSided -> XAxis.XAxisPosition.BOTH_SIDED
			}
		}
		
		override fun setSpaceMax(x: Float) {
			axis.spaceMax = x
		}
		
		override fun setSpaceMin(x: Float) {
			axis.spaceMin
		}
		
		override fun setValueFormatter(x: ChartFormatterInterface) {
			axis.valueFormatter = AndroidFormatter(x)
		}

		override fun setGranularity(x: Float) {
			axis.granularity = x
		}

	}
	
	override fun getDataSetCount(): Int {
		return chartView.data.dataSetCount
	}
	
	override fun getLegend(): ChartViewInterface.Legend {
		return AndroidLegend(chartView.legend)
	}
	
	override fun getXAxis(): ChartViewInterface.Axis {
		return AndroidAxis(chartView.xAxis)
	}
	override fun getLeftAxis(): ChartViewInterface.Axis {
		return AndroidAxis((chartView as BarLineChartBase<*>).axisLeft)
	}
	override fun getRightAxis(): ChartViewInterface.Axis {
		return AndroidAxis((chartView as BarLineChartBase<*>).axisRight)
	}
	
	
	
	override fun setDoubleTapToZoomEnabled(x: Boolean) {
		(chartView as BarLineChartBase<*>).isDoubleTapToZoomEnabled = x
	}
	
	override fun setDrawHoleEnabled(x: Boolean) {
		(chartView as PieChart).isDrawHoleEnabled = x
	}
	
	override fun setEntryLabelColor(color: String) {
		(chartView as PieChart).setEntryLabelColor(DataSetWrapper.getIntColor(color))
	}
	
	override fun setScaleYEnabled(x: Boolean) {
		(chartView as BarLineChartBase<*>).isScaleYEnabled = x
	}
	
	override fun setMinOffset(x: Float) {
		(chartView as BarLineChartBase<*>).minOffset = x
	}
	
	override fun setVisibleXRangeMinimum(x: Float) = (chartView as BarLineChartBase<*>).setVisibleXRangeMinimum(x)
	
	override fun setDescriptionEnabled(x: Boolean) {
		chartView.description.isEnabled = x
	}
	override fun setHighlightPerTapEnabled(x: Boolean) {
		chartView.isHighlightPerTapEnabled = x
	}
	override fun setHighlightPerDragEnabled(x: Boolean) {
		(chartView as BarLineChartBase<*>).isHighlightPerDragEnabled = x
	}
	
	
	override fun notifyDataSetChanged() {
		chartView.notifyDataSetChanged()
		chartView.invalidate()
	}
	
	override fun notifyCurrentDataChanged() {
		chartView.data.notifyDataChanged()
	}
	
	override fun fitScreen() = (chartView as BarLineChartBase<*>).fitScreen()
	override fun zoom(scaleX: Float, scaleY: Float, x: Float, y: Float) = (chartView as BarLineChartBase<*>).zoom(scaleX, scaleY, x, y)
	override fun moveViewToX(x: Float) {
		chartView.post {
			(chartView as BarLineChartBase<*>).moveViewToX(x)
			
		}
	}
	
	override fun getView() : BarLineChartBase<*> {
		return chartView as BarLineChartBase<*>
	}
}