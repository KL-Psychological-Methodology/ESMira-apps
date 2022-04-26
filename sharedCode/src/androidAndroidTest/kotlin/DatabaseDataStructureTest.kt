import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLite
import tests.database.DataStructureSharedTests
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Created by JodliDev on 05.04.2022.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseDataStructureTest : DataStructureSharedTests() {
	override fun initDb() {
		NativeLink.init(SQLite(ApplicationProvider.getApplicationContext(), null), smartphoneData, dialogOpener, notifications, postponedActions)
	}
	
	@Before
	fun setup() {
		reset()
	}
	
	@Test
	override fun actionTrigger_trigger_scheduleChanged_notification() {
		super.actionTrigger_trigger_scheduleChanged_notification()
	}
	
	@Test
	override fun alarm_do_scheduleAhead() {
		super.alarm_do_scheduleAhead()
	}
	
	@Test
	override fun alarm_do_exec() {
		super.alarm_do_exec()
	}
	
	override fun createEmptyFile(content: String): String {
		val context: Context = ApplicationProvider.getApplicationContext()
		val file = File(context.filesDir, "test.txt")
		
		val stream: OutputStream = BufferedOutputStream(FileOutputStream(file))
		stream.write(content.toByteArray())
		stream.flush()
		
		return file.path
	}
	@Test
	override fun fileUpload_createAndDeleteFile() {
		super.fileUpload_createAndDeleteFile()
	}
	
	@Test
	override fun input_testDynamicInput() {
		super.fileUpload_createAndDeleteFile()
	}
	
	@Test
	override fun observedVariable_finishJSON() {
		super.observedVariable_finishJSON()
	}
	
	@Test
	override fun questionnaire_save() {
		super.questionnaire_save()
	}
	
	@Test
	override fun questionnaire_isActive() {
		super.questionnaire_isActive()
	}
	
	@Test
	override fun study_create_and_delete() {
		super.study_create_and_delete()
	}
	
	@Test
	override fun study_do_editableSchedules() {
		super.study_do_editableSchedules()
	}
	
	@Test
	override fun study_do_editableSignalTimes() {
		super.study_do_editableSignalTimes()
	}
	
	@Test
	override fun study_do_alreadyExists() {
		super.study_do_alreadyExists()
	}
	
	@Test
	override fun study_do_getOldLeftStudy() {
		super.study_do_getOldLeftStudy()
	}
}