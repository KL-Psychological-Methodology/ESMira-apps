package at.jodlidev.esmira.androidNative.statistics

import android.graphics.Color
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.statistics.ChartFormatterInterface
import at.jodlidev.esmira.sharedCode.statistics.ChartDataSetInterface
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*

/**
 * Created by JodliDev on 07.10.2020.
 */
//class DataSetWrapper(private val dataSet: BarLineScatterCandleBubbleDataSet<*>) : ChartDataSetInterface {
class DataSetWrapper(private val dataSet: DataSet<*>) : ChartDataSetInterface {
	
	override fun setColor(color: String) {
		dataSet.color = getIntColor(color)
	}
	
	override fun setCircleColor(color: String) {
		(dataSet as LineDataSet).setCircleColor(getIntColor(color))
	}
	
	override fun setDrawCircleHole(x: Boolean) {
		(dataSet as LineDataSet).setDrawCircleHole(x)
	}
	override fun setCircleRadius(x: Float) {
		(dataSet as LineDataSet).circleRadius = x
	}
	
	override fun setDrawCircles(x: Boolean) {
		(dataSet as LineDataSet).setDrawCircles(x)
	}
	
	override fun setDrawFilled(x: Boolean) {
		(dataSet as LineDataSet).setDrawFilled(x)
	}
	
	override fun setDrawValues(x: Boolean) {
		dataSet.setDrawValues(x)
	}
	
	override fun setFillColor(color: String) {
		(dataSet as LineDataSet).fillColor = getIntColor(color)
	}
	
	override fun setHighlightEnabled(x: Boolean) {
		dataSet.isHighlightEnabled = x
	}
	
	override fun setMode(mode: ChartDataSetInterface.Mode) {
		(dataSet as LineDataSet).mode = when(mode) {
			ChartDataSetInterface.Mode.Linear -> LineDataSet.Mode.LINEAR
			ChartDataSetInterface.Mode.CubicBezier -> LineDataSet.Mode.CUBIC_BEZIER
			ChartDataSetInterface.Mode.HorizontalBezier -> LineDataSet.Mode.HORIZONTAL_BEZIER
			ChartDataSetInterface.Mode.Stepped -> LineDataSet.Mode.STEPPED
		}
	}
	
	override fun setSliceSpace(x: Float) {
		(dataSet as PieDataSet).sliceSpace = x
	}
	override fun setScatterShape(shape: ChartDataSetInterface.Shape) {
		(dataSet as ScatterDataSet).setScatterShape(when(shape) {
			ChartDataSetInterface.Shape.Square -> ScatterChart.ScatterShape.SQUARE
			ChartDataSetInterface.Shape.Circle -> ScatterChart.ScatterShape.CIRCLE
			ChartDataSetInterface.Shape.Triangle -> ScatterChart.ScatterShape.TRIANGLE
			ChartDataSetInterface.Shape.Cross -> ScatterChart.ScatterShape.CROSS
			ChartDataSetInterface.Shape.X -> ScatterChart.ScatterShape.X
			ChartDataSetInterface.Shape.ChevronUp -> ScatterChart.ScatterShape.CHEVRON_UP
			ChartDataSetInterface.Shape.ChevronDown -> ScatterChart.ScatterShape.CHEVRON_DOWN
			else -> ScatterChart.ScatterShape.SQUARE
		})
	}
	override fun setScatterShapeSize(x: Float) {
		(dataSet as ScatterDataSet).scatterShapeSize = x
	}
	
	override fun setForm(form: ChartDataSetInterface.Shape) {
		dataSet.form = when(form) {
			ChartDataSetInterface.Shape.Square -> Legend.LegendForm.SQUARE
			ChartDataSetInterface.Shape.Circle -> Legend.LegendForm.CIRCLE
			ChartDataSetInterface.Shape.Line -> Legend.LegendForm.LINE
			ChartDataSetInterface.Shape.Empty -> Legend.LegendForm.EMPTY
			ChartDataSetInterface.Shape.None -> Legend.LegendForm.NONE
			else -> Legend.LegendForm.DEFAULT
		}
	}
	
	override fun setValueFormatter(formatter: ChartFormatterInterface) {
		dataSet.valueFormatter = ChartViewWrapper.AndroidFormatter(formatter)
	}
	
	companion object {
		fun getIntColor(color: String): Int {
			return try {
				if(color.isNotEmpty()) Color.parseColor(color) else Color.BLACK
			}
			catch(e: Throwable) {
				ErrorBox.warn("DataSetWrapper", "Color \"$color\" is not valid! Using black instead", e)
				Color.BLACK
			}
		}
	}
}