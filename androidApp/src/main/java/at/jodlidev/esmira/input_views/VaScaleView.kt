package at.jodlidev.esmira.input_views

import android.content.Context
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 16.04.2019.
 */
class VaScaleView(context: Context) : TextElView(context, R.layout.view_input_va_scale) {
	var hasSelection = false
	private val leftEl: TextView = findViewById(R.id.left)
	private val rightEl: TextView = findViewById(R.id.right)
	private val seekBar: SeekBar = findViewById(R.id.seekbar)
	
	init {
		seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onStartTrackingTouch(s: SeekBar) {
				if(isBound) {
					hasSelection = true
					s.thumb.mutate().alpha = 255
				}
			}
			
			override fun onStopTrackingTouch(seekBar: SeekBar) {}
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
				if(isBound)
					input.value = if(hasSelection) seekBar.progress.toString() else ""
			}
		})
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		isBound = false //in case this view was reused
		
		HtmlHandler.setHtml(input.leftSideLabel, leftEl)
		HtmlHandler.setHtml(input.rightSideLabel, rightEl)
		
		
		if(input.value.isNotEmpty()) {
			try {
				hasSelection = true
				seekBar.progress = input.value.toInt()
				seekBar.thumb.mutate().alpha = 255
			}
			catch(e: Exception) {}
		}
		else {
			hasSelection = false
			seekBar.progress = 0
			seekBar.thumb.mutate().alpha = 0
		}
		
		super.bindData(input, questionnaire)
	}
}