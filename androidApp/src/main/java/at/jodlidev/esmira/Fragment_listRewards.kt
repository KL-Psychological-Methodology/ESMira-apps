package at.jodlidev.esmira

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.Study


/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_listRewards : Base_fragment() {
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val studies = DbLogic.getStudiesWithRewards()
		if(studies.size == 1) {
			val b = Bundle()
			b.putLong(Fragment_reward.KEY_STUDY_ID, studies[0].id)
			goToAsRoot(Activity_main.SITE_REWARD, b)
			return View(context)
		}
		
		setTitle(R.string.rewards)
		
		return ComposeView(requireContext()).apply {
			setContent {
				ESMiraSurface {
					RewardList(
						studies
					) { study ->
						val b = Bundle()
						b.putLong(Fragment_reward.KEY_STUDY_ID, study.id)
						goToAsSub(Activity_main.SITE_REWARD, b)
					}
				}
			}
		}
	}
	
	@Composable
	fun RewardList(studies: List<Study>, gotoReward: (Study) -> Unit) {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
		) {
			studies.forEach { study ->
				StudyRow(study, gotoReward)
			}
		}
	}
	@Composable
	fun StudyRow(study: Study, gotoReward: (Study) -> Unit) {
		Row (
			modifier = Modifier
				.clickable { gotoReward(study) }
				.padding(horizontal = 10.dp, vertical = 10.dp)
				.fillMaxWidth()
		) {
			Text(study.title, fontSize = 18.sp)
		}
	}
	
	@Preview
	@Composable
	fun PreviewRewardList() {
		ESMiraSurface() {
			RewardList(
				listOf(
					Study.newInstance("", "", """{"id":1, "title": "Study1"}"""),
					Study.newInstance("", "", """{"id":1, "title": "Study2"}"""),
					Study.newInstance("", "", """{"id":1, "title": "Study3"}""")
				)
			) {}
		}
	}
}

