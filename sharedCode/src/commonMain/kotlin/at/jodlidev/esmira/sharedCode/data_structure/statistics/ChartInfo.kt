package at.jodlidev.esmira.sharedCode.data_structure.statistics

import at.jodlidev.esmira.sharedCode.data_structure.ObservedVariable
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.sharedCode.statistics.ChartBuilder
import at.jodlidev.esmira.sharedCode.statistics.ChartChooserInterface
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 26.08.2019.
 * not in db
 */
@Serializable
class ChartInfo (
	var chartType: Int = CHART_TYPE_LINE,
	var dataType: Int = DATA_TYPE_DAILY,
	var valueType: Int = VALUE_TYPE_MEAN,
	var displayPublicVariable: Boolean = false,
	var hideUntilCompletion: Boolean = false,
	var storageType: Int = ObservedVariable.STORAGE_TYPE_TIMED,
	var inPercent: Boolean = false,
	var publicVariables: List<AxisContainer> = ArrayList(),
	var axisContainer: List<AxisContainer> = ArrayList(),
	var title: String = "",
	var chartDescription: String = "",
	var xAxisLabel: String = "",
	var yAxisLabel: String = "",
	var maxYValue: Int = 0,
	var fitToShowLinearProgression: Int = 0,
	var xAxisIsNumberRange: Boolean = false,
	var hideOnClient: Boolean = false,
    var showOnlyGroup: Int = 0,
    var showOnlyLang: String = "",
) {
	@Serializable
	class AxisContainer (
		var yAxis: AxisData = AxisData(0, ""),
		var xAxis: AxisData = AxisData(0, ""),
		var label: String = "",
		var color: String = "#00bbff",
		var useThresholdOnClient: Boolean = true,
		var useThreshold: Boolean = false,
		var threshold: Double = 0.0,
		var thresholdColor: String = "#dc4e9d"
	) {
		
		init {
			if(label.isEmpty())
				label = yAxis.variableName
		}
	}
	@Serializable
	class AxisData (
		var observedVariableIndex: Int = -1,
		var variableName: String = ""
	)
	
	@Transient
	lateinit var builder: ChartBuilder
	
	fun initBuilder(chartInfoCollection: ChartInfoCollection, chartChooser: ChartChooserInterface) {
		if(this::builder.isInitialized)
			return
		builder = when(chartType) {
			CHART_TYPE_LINE_FILLED -> chartChooser.getLineChartBuilder(true, chartInfoCollection, this)
			CHART_TYPE_LINE -> chartChooser.getLineChartBuilder(false, chartInfoCollection, this)
			CHART_TYPE_BARS -> chartChooser.getBarChartBuilder(chartInfoCollection, this)
			CHART_TYPE_SCATTER -> chartChooser.getScatterChartBuilder(chartInfoCollection, this)
			CHART_TYPE_PIE -> chartChooser.getPieChartBuilder(chartInfoCollection, this)
			else -> chartChooser.getLineChartBuilder(false, chartInfoCollection, this)
		}
		
		builder.fillData()
	}

    fun isAvailable(study: Study): Boolean {
        val hiddenGeneral = hideOnClient
        val hiddenFromGroup = showOnlyGroup != 0 && study.group != showOnlyGroup
        val hiddenFromLang = showOnlyLang != "" && showOnlyLang != study.lang && study.getAvailableLangs().size > 1

        return !(hiddenGeneral || hiddenFromGroup || hiddenFromLang)
    }
	
	
	companion object {
		const val CHART_TYPE_LINE = 0
		const val CHART_TYPE_LINE_FILLED = 1
		const val CHART_TYPE_BARS = 2
		const val CHART_TYPE_SCATTER = 3
		const val CHART_TYPE_PIE = 4
		
		const val VALUE_TYPE_MEAN = 0
		const val VALUE_TYPE_SUM = 1
		const val VALUE_TYPE_COUNT = 2
		
		const val DATA_TYPE_DAILY = 0
		const val DATA_TYPE_FREQ_DISTR = 1
		const val DATA_TYPE_SUM = 2
		const val DATA_TYPE_XY = 3
	}
}