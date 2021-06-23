package at.jodlidev.esmira

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.tabs.TabLayout
import java.util.*

/**
 * Created by JodliDev on 24.04.2019.
 */
class Activity_editSchedules : AppCompatActivity() {
	private inner class ScreenSlidePagerAdapter internal constructor(
		fm: FragmentManager?,
		private val studies: List<Study>
	) : FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
		internal val fragments = HashMap<Int, Fragment_editSchedules?>()
		
		override fun getItem(position: Int): Fragment {
			return if (!fragments.containsKey(position)) {
				val f = Fragment_editSchedules()
				val bundle = Bundle()
				bundle.putInt(Fragment_editSchedules.KEY_POSITION, position)
				f.arguments = bundle
				fragments[position] = f
				f
			}
			else fragments[position]!!
		}
		
		override fun getCount(): Int {
			return studies.size
		}
		
		override fun getPageTitle(position: Int): CharSequence {
			val study = studies[position]
			return study.title
		}
	}
	
	lateinit var studies: List<Study>
	
	private var resetSchedules = false
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_edit_schedules)
		setTitle(R.string.change_schedules)
		
		studies = DbLogic.getStudiesWithSchedules()
		val viewPager = findViewById<ViewPager>(R.id.container)
		val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
		
		tabLayout.setupWithViewPager(viewPager)
		val adapter = ScreenSlidePagerAdapter(supportFragmentManager, studies)
		viewPager.adapter = adapter
		
		val b = intent.extras
		if (b != null && b.containsKey(EXTRA_STUDY_ID)) {
			val studyId = b.getLong(EXTRA_STUDY_ID)
			for ((i, study) in studies.withIndex()) {
				if (study.id == studyId)
					viewPager.currentItem = i
			}
			resetSchedules = true
		}
		if (studies.isEmpty())
			findViewById<View>(R.id.no_schedules).visibility = View.VISIBLE
		
		findViewById<View>(R.id.btn_cancel).setOnClickListener {
			finish()
		}
		findViewById<View>(R.id.btn_save).setOnClickListener(View.OnClickListener {
			var resetScheduleMsg = false
			for(fragment in adapter.fragments.entries) {
				val currentReturn = fragment.value?.save(resetSchedules) ?: continue
				
				if(currentReturn == Fragment_editSchedules.SAVE_STATE_POSTPONED)
					resetScheduleMsg = true
				if(currentReturn == Fragment_editSchedules.SAVE_STATE_ERROR)
					return@OnClickListener
			}
			
			if(resetScheduleMsg)
				Toast.makeText(applicationContext, R.string.info_schedule_changed_after_one_day, Toast.LENGTH_LONG).show()
			finish()
		})
	}
	
	companion object {
		const val EXTRA_STUDY_ID = "study_id"
		fun start(context: Context) {
			val intent = Intent(context, Activity_editSchedules::class.java)
			if (context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			context.startActivity(intent)
		}
		
		fun start(context: Context, study_id: Long) {
			val intent = Intent(context, Activity_editSchedules::class.java)
			if (context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			intent.putExtra(EXTRA_STUDY_ID, study_id)
			context.startActivity(intent)
		}
	}
}