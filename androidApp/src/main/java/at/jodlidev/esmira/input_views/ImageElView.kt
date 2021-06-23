package at.jodlidev.esmira.input_views

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.nativeAsync
import at.jodlidev.esmira.sharedCode.onUIThread
import java.net.URL

/**
 * Created by JodliDev on 16.04.2019.
 */
class ImageElView(context: Context) : TextElView(context, R.layout.view_input_image) {
	private val imageView = findViewById<ImageView>(R.id.image)
	private val loadingEl = findViewById<View>(R.id.loading_el)
	private val btnReload = findViewById<View>(R.id.btn_reload)
	private val errorMsg = findViewById<View>(R.id.error_msg)
	
	init {
		btnReload.setOnClickListener {
			loadImage(input.url)
		}
	}
	
	private fun loadImage(urlString: String) {
		nativeAsync {
			try {
				val url = URL(urlString)
				val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
				
				onUIThread {
					loadingEl.visibility = GONE
					errorMsg.visibility = GONE
					btnReload.visibility = GONE
					imageView.setImageBitmap(bitmap)
				}
			}
			catch(e: Exception) {
				onUIThread {
					println(e)
					loadingEl.visibility = GONE
					errorMsg.visibility = VISIBLE
					btnReload.visibility = VISIBLE
				}
			}
		}
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		super.bindData(input, questionnaire)
		loadImage(input.url)
	}
}