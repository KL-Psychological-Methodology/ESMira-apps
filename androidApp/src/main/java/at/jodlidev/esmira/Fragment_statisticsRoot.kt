package at.jodlidev.esmira

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_statisticsRoot : Base_fragment() {
	private lateinit var study: Study
	private lateinit var loadingPublicStatisticsView: WeakReference<View>
	private lateinit var statisticsJson: String
	lateinit var publicChartCollection: ChartInfoCollection
	
	private inner class ScreenSlidePagerAdapter constructor(
		fm: FragmentManager,
		private var resources: Resources,
		private var study: Study
	) : FragmentPagerAdapter((fm), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
		override fun getItem(position: Int): Fragment {
			val f: Base_fragment = Fragment_statistics()
			val bundle = Bundle()
			bundle.putLong(Fragment_statistics.KEY_STUDY_ID, study.id)
			bundle.putInt(Fragment_statistics.KEY_STATISTIC_TYPE, position)
			f.arguments = bundle
			return f
		}
		
		override fun getCount(): Int {
			return if(study.publicStatisticsNeeded) 2 else 1
		}
		
		override fun getPageTitle(position: Int): CharSequence {
			return when(position) {
				PAGE_PERSONAL ->
					resources.getString(R.string.statistics_personal)
				PAGE_PUBLIC ->
					resources.getString(R.string.statistics_public)
				else ->
					"ERROR"
			}
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_statistics_root, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val arguments: Bundle = arguments ?: return
		study = DbLogic.getStudy(arguments.getLong(KEY_STUDY_ID)) ?: return
		
		loadingPublicStatisticsView = WeakReference(rootView.findViewById(R.id.state_loading_public_statistics))
		val viewPager = rootView.findViewById<ViewPager>(R.id.container)
		viewPager.adapter = ScreenSlidePagerAdapter(childFragmentManager, resources, study)
		
		
		if(study.publicStatisticsNeeded) {
			rootView.findViewById<View>(R.id.tab_layout).visibility = View.VISIBLE
			if(savedInstanceState?.getString(STATE_DATA_JSON) != null) {
				viewPager.post {
					initDataJson(savedInstanceState.getString(STATE_DATA_JSON)!!)
					loadingPublicStatisticsView.get()?.visibility = View.GONE
				}
			}
			else if(this::statisticsJson.isInitialized)
				initDataJson(statisticsJson)
			else {
				Web.loadStatistics(
					study,
					onError = { msg ->
						activity?.runOnUiThread {
							loadingPublicStatisticsView.get()?.visibility = View.GONE
							Snackbar.make((rootView), msg, Snackbar.LENGTH_LONG).show()
						}
					},
					onSuccess = { json ->
						activity?.runOnUiThread {
							loadingPublicStatisticsView.get()?.visibility = View.GONE
							initDataJson(json)
						}
					})
			}
		}
		else
			loadingPublicStatisticsView.get()?.visibility = View.GONE
	}
	
	private fun initDataJson(statisticsJson: String) {
		publicChartCollection = ChartInfoCollection(statisticsJson, study)
		this.statisticsJson = statisticsJson
		
		if(isAdded) {
			for(f: Fragment in childFragmentManager.fragments) {
				if(f.isAdded && f is Fragment_statistics) {
					f.initPublicData()
				}
			}
		}
	}
	
	override fun onPause() {
		super.onPause()
		loadingPublicStatisticsView.get()?.visibility = View.GONE
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		if(this::statisticsJson.isInitialized)
			outState.putString(STATE_DATA_JSON, statisticsJson)
	}
	
	companion object {
		const val KEY_STUDY_ID: String = "study_id"
		const val PAGE_PERSONAL: Int = 0
		const val PAGE_PUBLIC: Int = 1
		const val JSON_DATA: String = "data"
		const val JSON_STORAGE_TYPE: String = "storageType"
		const val STATE_DATA_JSON: String = "data_json"
		const val REQUEST_LOAD_PUBLIC_STATISTICS: Int = 333
	}
}