import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLite
import tests.SharedInstrumentedTests
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
class AndroidInstrumentedTests : SharedInstrumentedTests() {
	override fun initDb() {
		val sql = SQLite(ApplicationProvider.getApplicationContext(), null)
		NativeLink.init(sql, smartphoneData, dialogOpener, notifications, postponedActions)
		NativeLink.resetSql(sql)
	}
	
	@Before
	fun setup() {
		reset()
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
	override fun reportMissedInvitation_getMissedInvitations_resetMissedInvitations() {
		super.reportMissedInvitation_getMissedInvitations_resetMissedInvitations()
	}
}