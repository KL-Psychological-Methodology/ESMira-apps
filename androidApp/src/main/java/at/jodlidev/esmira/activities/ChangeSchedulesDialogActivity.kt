package at.jodlidev.esmira.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.SignalTime
import at.jodlidev.esmira.views.ESMiraDialogContent
import at.jodlidev.esmira.views.elements.TimeButtonView

/**
 * Created by JodliDev on 26.04.2019.
 */
class ChangeSchedulesDialogActivity: ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val studyId = intent.extras?.getLong(EXTRA_STUDY_ID) ?: return
		val resetSchedules = intent.extras?.getBoolean(EXTRA_RESET_SCHEDULES) ?: false
		
		val study = DbLogic.getStudy(studyId) ?: return
		val context = applicationContext
		
		
		setContent {
			ESMiraDialogContent(
				confirmButtonLabel = stringResource(R.string.save),
				onConfirmRequest =  {
					if(study.saveSchedules(resetSchedules)) {
						if(!resetSchedules)
							Toast.makeText(context, context.getString(R.string.info_schedule_changed_after_one_day), Toast.LENGTH_LONG).show()
						finish()
					}
				},
				dismissButtonLabel = stringResource(R.string.cancel),
				onDismissRequest = { finish() },
				title = stringResource(R.string.change_schedules),
				contentPadding = PaddingValues(),
			) {
				SchedulesList(study.editableSignalTimes)
			}
		}
	}
	
	
	@Composable
	fun SchedulesList(
		signalTimes: List<SignalTime>,
		modifier: Modifier = Modifier
	) {
		LazyColumn(modifier = modifier.fillMaxWidth()) {
			itemsIndexed(signalTimes) { i, signalTime ->
				SignalTimeItem(signalTime,
					modifier = Modifier
						.fillMaxWidth()
						.background(color = if(i % 2 != 0) colorLineBackground1 else colorLineBackground2)
				)
			}
		}
	}
	
	@Composable
	fun SignalTimeItem(
		signalTime: SignalTime,
		modifier: Modifier = Modifier
	) {
		val showWarning = remember { mutableStateOf(false) }
		val checkValid = {
			showWarning.value = signalTime.isFaulty()
		}
		Column(
			modifier = modifier.padding(vertical = 5.dp)
		) {
			Text(signalTime.questionnaire.title,
				fontSize = MaterialTheme.typography.titleLarge.fontSize,
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.padding(vertical = 10.dp)
					.align(alignment = Alignment.CenterHorizontally)
			)
			if(signalTime.random) {
				Text(stringResource(R.string.colon_between), modifier = Modifier.padding(start = 20.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.padding(start = 5.dp)
						.align(alignment = Alignment.CenterHorizontally)
				) {
					TimeButtonView(
						get = { signalTime.getStart() },
						save = { timestamp ->
							signalTime.setStart(timestamp)
							checkValid()
						}
					)
					Text(stringResource(R.string.word_and), modifier = Modifier.padding(horizontal = 5.dp))
					TimeButtonView(
						get = { signalTime.getEnd() },
						save = { timestamp ->
							signalTime.setEnd(timestamp)
							checkValid()
						}
					)
				}
			}
			else {
				TimeButtonView(
					get = { signalTime.getStart() },
					save = { timestamp ->
						signalTime.setStart(timestamp)
						checkValid()
					},
					modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
				)
			}
			if(showWarning.value) {
				Text(stringResource(R.string.error_schedule_time_window_too_small), color = MaterialTheme.colorScheme.error)
			}
		}
	}
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewSchedulesList() {
		ESMiraSurface {
			SchedulesList(listOf(
				DbLogic.createJsonObj("""{"label": "Title 1", "random": true, "startTimeOfDay": 3600000, "endTimeOfDay": 7200000}"""),
				DbLogic.createJsonObj("""{"label": "Title 2", "random": true}"""),
				DbLogic.createJsonObj("""{"label": "Title 3", "random": false}"""),
				DbLogic.createJsonObj("""{"label": "Title 4", "random": false}"""),
				DbLogic.createJsonObj("""{"label": "Title 5", "random": true}"""),
				DbLogic.createJsonObj("""{"label": "Title 6", "random": false}""")
			))
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewSignalTimeItemRandom() {
		ESMiraSurface {
			SignalTimeItem(DbLogic.createJsonObj("""{"label": "Title", "random": true}"""))
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewSignalTimeItemSingle() {
		ESMiraSurface {
			SignalTimeItem(DbLogic.createJsonObj("""{"label": "Title", "random": false}"""))
		}
	}
	
	
	
	companion object {
		private const val EXTRA_STUDY_ID = "study_id"
		private const val EXTRA_RESET_SCHEDULES = "reset_schedules"
		
		fun start(context: Context, studyId: Long, resetSchedules: Boolean = false) {
			val intent = Intent(context, ChangeSchedulesDialogActivity::class.java)
			if (context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			intent.putExtra(EXTRA_STUDY_ID, studyId)
			intent.putExtra(EXTRA_RESET_SCHEDULES, resetSchedules)
			context.startActivity(intent)
		}
	}
}