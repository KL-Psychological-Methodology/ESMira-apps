package at.jodlidev.esmira

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import at.jodlidev.esmira.sharedCode.QrInterpreter
import at.jodlidev.esmira.sharedCode.Web
import me.dm7.barcodescanner.zxing.ZXingScannerView

/**
 * Created by JodliDev on 22.04.2019.
 */
class Activity_QRscanner : AppCompatActivity(), ZXingScannerView.ResultHandler {
	private val interpreter = QrInterpreter()
	private lateinit var scannerView: ZXingScannerView

	public override fun onCreate(state: Bundle?) {
		super.onCreate(state)
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
		}
		else initScanner()
	}
	
	private fun initScanner() {
		scannerView = ZXingScannerView(this)
		setContentView(scannerView)
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		if(requestCode == REQUEST_CAMERA_PERMISSION) {
			if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				initScanner()
			else {
				finish()
			}
		}
	}
	
	public override fun onResume() {
		super.onResume()
		if(this::scannerView.isInitialized) {
			scannerView.setResultHandler(this)
			scannerView.startCamera()
		}
	}
	
	public override fun onPause() {
		super.onPause()
		if(this::scannerView.isInitialized)
			scannerView.stopCamera()
	}
	
	override fun handleResult(rawResult: com.google.zxing.Result) {
		val data = interpreter.check(rawResult.text)
		if(data != null) {
			Activity_addStudy.start(this, Web.getServerName(data.url), data.url, data.accessKey, data.studyId) {
				scannerView.resumeCameraPreview(this)
			}
			
		}
		else {
			Toast.makeText(this, R.string.qrCodeInvalid, Toast.LENGTH_SHORT).show()
			scannerView.resumeCameraPreview(this)
		}
		
	}
	
	companion object {
		const val REQUEST_QR_RESPONSE = 203
		const val SERVER_URL = "url"
		const val PASS = "pass"
		const val STUDY_ID = "study_id"
		
		private val EXPECTED_JSON_VALUES = arrayOf(SERVER_URL, PASS, STUDY_ID)
		const val KEY_ANSWER = "answer"
		private const val REQUEST_CAMERA_PERMISSION = 101
		
		
		fun start(context: Context) {
			context.startActivity(Intent(context, Activity_QRscanner::class.java))
		}
//		fun start(activity: Activity) {
//			val intent = Intent(activity, Activity_QRscanner::class.java)
//			activity.startActivityForResult(intent, REQUEST_QR_RESPONSE)
//		}
//		fun start(fragment: Fragment) {
//			val intent = Intent(fragment.context, Activity_QRscanner::class.java)
//			fragment.startActivityForResult(intent, REQUEST_QR_RESPONSE)
//		}
	}
}