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
		//response will be processed in Fragment_questionnaireDetail.onActivityResult()
		
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		imagePreview.tag = input.name
		super.bindData(input, questionnaire)
	}
}