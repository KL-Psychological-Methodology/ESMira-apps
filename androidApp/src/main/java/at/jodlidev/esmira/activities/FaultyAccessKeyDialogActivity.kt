package at.jodlidev.esmira.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.ESMiraDialogContent

/**
 * Created by JodliDev on 24.04.2019.
 */
class FaultyAccessKeyDialogActivity : ComponentActivity() {
	private var newAccessKey = mutableStateOf("")
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		if(!intent.hasExtra(EXTRA_STUDY_ID))
			return
		val studyId = intent.getLongExtra(EXTRA_STUDY_ID, -1)
		val study = DbLogic.getStudy(studyId) ?: return
		this.newAccessKey.value = study.accessKey
		
		setContent {
			ESMiraSurface {
				MainDialog(study)
			}
		}
		DbLogic.setErrorsAsReviewed()
	}
	
	private fun saveNewAccessKey(study: Study) {
		study.saveFaultyAccessKeyState(false, this.newAccessKey.value)
		Web.updateStudiesAsync {
			val faultyStudy = DbLogic.getFirstStudyWithFaultyAccessKey()
			if(faultyStudy != null)
				NativeLink.dialogOpener.faultyAccessKey(faultyStudy)
		}
		finish()
	}
	
	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun MainDialog(study: Study) {
		ESMiraDialogContent(
			confirmButtonLabel = stringResource(R.string.save),
			onConfirmRequest = { saveNewAccessKey(study) },
			title = study.title,
			dismissButtonLabel = stringResource(R.string.cancel),
			onDismissRequest = { finish() }
		) {
			Column(
				modifier = Modifier
					.verticalScroll(rememberScrollState())
			) {
				Text(study.title, fontWeight = FontWeight.Bold)
				Text(stringResource(R.string.info_wrong_access_key))
				OutlinedTextField(
					value = newAccessKey.value,
					onValueChange = {
						newAccessKey.value = it
					}
				)
			}
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun Preview() {
		ESMiraSurface {
			MainDialog(DbLogic.createJsonObj("""{"id": "1234", "title": "Test study"}"""))
		}
	}
	
	companion object {
		private const val EXTRA_STUDY_ID = "studyId"
		
		fun start(context: Context, study: Study) {
			val intent = Intent(context, FaultyAccessKeyDialogActivity::class.java)
			intent.putExtra(EXTRA_STUDY_ID, study.id)
			if(context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			context.startActivity(intent)
		}
	}
}