package at.jodlidev.esmira.input_views

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import at.jodlidev.esmira.*
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import com.google.android.material.button.MaterialButton

/**
 * Created by JodliDev on 16.04.2019.
 */
class PhotoView(context: Context) : View.OnClickListener, TextElView(context, R.layout.view_input_photo) {
	private val cameraBtn: MaterialButton = findViewById(R.id.cameraBtn)
	private val imagePreview: ImageView = findViewById(R.id.imagePreview)
	
	init {
		cameraBtn.setOnClickListener(this)
	}
	
	override fun onClick(v: View?) {
		Activity_photoCamera.start(FragmentManager.findFragment(this), input.name)
		
		
//		val activity = (context as AppCompatActivity) ?: return
//
//		if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//			ActivityCompat.requestPermissions(
//				activity,
//				arrayOf(Manifest.permission.CAMERA),
//				Fragment_questionnaireDetail.REQUEST_CAMERA_PERMISSION
//			)
//			return
//		}
//
//		val fragment: Base_fragment = FragmentManager.findFragment(this)
//		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
////		intent.putExtra(Fragment_questionnaireDetail.CAMERA_INPUT_NAME, input.name)
////		intent.putExtra(Fragment_questionnaireDetail.CAMERA_PREVIEW_ID, id)
//
//		try {
//			fragment.startActivityForResult(intent, Fragment_questionnaireDetail.REQUEST_CAMERA)
//		} catch (e: ActivityNotFoundException) {
//			Toast.makeText(context, context.getString(R.string.error_no_cameraApp), Toast.LENGTH_LONG).show()
//		}
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		imagePreview.tag = input.name
		super.bindData(input, questionnaire)
	}
}