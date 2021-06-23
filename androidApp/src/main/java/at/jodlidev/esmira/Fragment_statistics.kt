package at.jodlidev.esmira

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import at.jodlidev.esmira.androidNative.statistics.ChartTypeChooser
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection


/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_statistics : Base_fragment() {
	private lateinit var study: Study
	private var statisticType: Int = 0
	private lateinit var noStatisticsEl: View
	private lateinit var chartViewContainer: ViewGroup
	
	private lateinit var chartCollection: ChartInfoCollection

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		//load arguments:
		val arguments: Bundle = arguments ?: return null

		study = DbLogic.getStudy(arguments.getLong(KEY_STUDY_ID)) ?: throw Exception("Study is null (id: ${arguments.getLong(KEY_STUDY_ID)})!")
		statisticType = arguments.getInt(KEY_STATISTIC_TYPE)
		return inflater.inflate(R.layout.fragment_statistics, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		noStatisticsEl = rootView.findViewById(R.id.no_statistics_el)
		chartViewContainer = rootView.findViewById(R.id.chart_container)
		if(statisticType == Fragment_statisticsRoot.PAGE_PERSONAL) {
			chartCollection = ChartInfoCollection(study)
			initCharts()
		}
	}

	private fun initCharts() {
		val chartsArray = if(statisticType == Fragment_statisticsRoot.PAGE_PERSONAL) study.personalCharts else study.publicCharts
		
		if(chartsArray.isEmpty())
			return
		else
			noStatisticsEl.visibility = View.GONE
		
		val studyStateIsJoined = study.state == Study.STATES.Joined && study.isActive()
		
		for(chartInfo in chartCollection.charts) {
			val parent = FrameLayout(requireContext())
			chartViewContainer.addView(parent)
			val box = View.inflate(context, R.layout.item_statistic_chart, parent)
			box.findViewById<TextView>(R.id.header).text = chartInfo.title
			box.findViewById<TextView>(R.id.desc).text = HtmlCompat.fromHtml(chartInfo.chartDescription, HtmlCompat.FROM_HTML_MODE_LEGACY)
			
			if(studyStateIsJoined && chartInfo.hideUntilCompletion) {
				val noChartDesc = TextView(context)
				noChartDesc.setText(R.string.visible_when_study_finished)
				noChartDesc.setTypeface(null, Typeface.BOLD)
				noChartDesc.setBackgroundColor(Color.LTGRAY)
				noChartDesc.gravity = Gravity.CENTER
				noChartDesc.setPadding(15, 0, 15, 0)
				
				box.findViewById<FrameLayout>(R.id.chart_box).addView(noChartDesc)
			}
			else {
				if(chartInfo.xAxisLabel.isNotEmpty())
					box.findViewById<TextView>(R.id.xAxisLabel).text = chartInfo.xAxisLabel
				else
					box.findViewById<TextView>(R.id.xAxisLabel).visibility = View.GONE
				
				if(chartInfo.yAxisLabel.isNotEmpty())
					box.findViewById<TextView>(R.id.yAxisLabel).text = chartInfo.yAxisLabel
				else
					box.findViewById<TextView>(R.id.yAxisLabel).visibility = View.GONE
				
				val chooser = ChartTypeChooser(requireContext())
				chartInfo.initBuilder(chartCollection, chooser)
				val chart = chartInfo.builder.createChart() as View
				
				box.findViewById<FrameLayout>(R.id.chart_box).addView(chart)
			}
		}
	}
	
	fun initPublicData() {
		if(parentFragment !is Fragment_statisticsRoot)
			return
		val fragment: Fragment_statisticsRoot = parentFragment as Fragment_statisticsRoot
		val publicChartCollection = fragment.publicChartCollection

		if(statisticType == Fragment_statisticsRoot.PAGE_PUBLIC) {
			chartCollection = publicChartCollection
			initCharts()
		}
		else if(this::chartCollection.isInitialized)
			chartCollection.addPublicData(publicChartCollection)
	}
	
	companion object {
		const val KEY_STUDY_ID: String = "study_id"
		const val KEY_STATISTIC_TYPE: String = "statistic_type"
	}
}