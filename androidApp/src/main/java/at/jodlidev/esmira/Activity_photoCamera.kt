package at.jodlidev.esmira

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.otaliastudios.cameraview.CameraListener

import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream


/**
 * Created by JodliDev on 22.04.2019.
 */
class Activity_photoCamera : AppCompatActivity() {

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
				REQUEST_CAMERA_PERMISSION
			)
		}
		else
			init()
	}
	
	private fun init() {
		setContentView(R.layout.activity_photo_camera)
		val cameraView: CameraView = findViewById(R.id.cameraView)
		cameraView.setLifecycleOwner(this)
		
		val extras = intent.extras
		val inputName = extras?.getString(INPUT_NAME)
		
		cameraView.addCameraListener(object: CameraListener() {
			override fun onPictureTaken(result: PictureResult) {
				result.toBitmap(800, 800) { bitmap ->
					Thread {
						val byteStream = ByteArrayOutputStream()
						bitmap?.compress(Bitmap.CompressFormat.PNG, 50, byteStream)
						
						val intent = Intent()
						intent.putExtra(PHOTO_DATA, byteStream.toByteArray())
						intent.putExtra(INPUT_NAME, inputName)
						setResult(RESULT_OK, intent)
						cameraView.close();
						finish()
					}.run()
					
					
//					val intent = Intent()
//					intent.putExtra(PHOTO_DATA, bitmap)
//					intent.putExtra(INPUT_NAME, inputName)
//					setResult(RESULT_OK, intent)
//					cameraView.close();
//					finish()
				}
			}
		})
		
		findViewById<FloatingActionButton>(R.id.cameraBtn).setOnClickListener {
			cameraView.takePicture()
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		if(requestCode == REQUEST_CAMERA_PERMISSION) {
			if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				init()
			else {
				finish()
			}
		}
	}
	
	companion object {
		const val REQUEST_PHOTO_RESPONSE = 203
		const val PHOTO_DATA = "photoData"
		const val INPUT_NAME = "inputName"
		private const val REQUEST_CAMERA_PERMISSION = 101
		
		
		fun start(fragment: Base_fragment, inputName: String) {
			val intent = Intent(fragment.requireContext(), Activity_photoCamera::class.java)
			intent.putExtra(INPUT_NAME, inputName)
			fragment.startActivityForResult(intent, REQUEST_PHOTO_RESPONSE)
		}
	}
}