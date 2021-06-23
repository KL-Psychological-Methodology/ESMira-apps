package at.jodlidev.esmira.sharedCode.statistics

/**
 * Created by JodliDev on 06.10.2020.
 */
interface ChartViewInterface {
	interface Legend {
		enum class VerticalAlignment {
			Top, Center, Bottom
		}
		enum class HorizontalAlignment {
			Left, Center, Right
		}
		enum class Orientation {
			Vertical, Horizontal
		}
		fun setVerticalAlignment(x: VerticalAlignment)
		fun setHorizontalAlignment(x: HorizontalAlignment)
		fun setOrientation(x: Orientation)
	}
	interface Axis {
		enum class Position {
			Top, TopInside, Bottom, BottomInside, BothSided
		}
		fun setSpaceMin(x: Float)
		fun setSpaceMax(x: Float)
		fun setLabelRotationAngle(x: Float)
		fun setLabelCount(x: Int)
		fun setAxisMinimum(x: Float)
		fun setAxisMaximum(x: Float)
		fun setDrawAxisLine(x: Boolean)
		fun setDrawGridLines(x: Boolean)
		fun setDrawLabels(x: Boolean)
		fun setPosition(x: Position)
		fun setValueFormatter(x: ChartFormatterInterface)
		fun setGranularity(x: Float)
	}
	
	fun getDataSetCount(): Int
	
	fun getLegend(): Legend
	fun getXAxis(): Axis
	fun getLeftAxis(): Axis
	fun getRightAxis(): Axis
	
	fun setDoubleTapToZoomEnabled(x: Boolean)
	fun setScaleYEnabled(x: Boolean)
	fun setMinOffset(x: Float)
	fun setVisibleXRangeMinimum(x: Float)
	fun setDescriptionEnabled(x: Boolean)
	fun setHighlightPerTapEnabled(x: Boolean)
	fun setHighlightPerDragEnabled(x: Boolean)
	
	fun setDrawHoleEnabled(x: Boolean)
	fun setEntryLabelColor(color: String)
	
	fun notifyCurrentDataChanged()
	fun notifyDataSetChanged()
	fun fitScreen()
	fun zoom(scaleX: Float, scaleY: Float, x: Float, y: Float)
	fun moveViewToX(x: Float)
	
	fun getView(): Any
}