package at.jodlidev.esmira.sharedCode.statistics

/**
 * Created by JodliDev on 07.10.2020.
 */
interface ChartDataSetInterface {
	enum class Mode {
		Linear, Stepped, CubicBezier, HorizontalBezier
	}
	enum class Shape {
		Square, Circle,
		Line, Empty, None,
		Triangle, Cross, X, ChevronUp, ChevronDown
	}
	fun setMode(mode: Mode)
	
	fun setColor(color: String)
	fun setCircleColor(color: String)
	fun setFillColor(color: String)
	
	fun setCircleRadius(x: Float)
	fun setDrawCircleHole(x: Boolean)
	fun setDrawCircles(x: Boolean)
	fun setDrawFilled(x: Boolean)
	fun setDrawValues(x: Boolean)
	fun setHighlightEnabled(x: Boolean)
	fun setSliceSpace(x: Float)
	fun setForm(form: Shape)
	fun setScatterShape(shape: Shape)
	fun setScatterShapeSize(x: Float)
	
	fun setValueFormatter(formatter: ChartFormatterInterface)
}