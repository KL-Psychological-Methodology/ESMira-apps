package at.jodlidev.esmira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import at.jodlidev.esmira.sharedCode.QrInterpreter
import at.jodlidev.esmira.sharedCode.Web

/**
 * Created by JodliDev on 21.09.2020.
 */
class Activity_LinkInterpreter : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val data = intent?.data ?: finish()
		
		val urlData = QrInterpreter().check(data.toString())
		if(urlData == null) {
			finish()
			return
		}
		Activity_addStudy.start(this, Web.getServerName(urlData.url), urlData.url, urlData.accessKey, urlData.studyId, urlData.qId) {
			finish()
		}
	}
}