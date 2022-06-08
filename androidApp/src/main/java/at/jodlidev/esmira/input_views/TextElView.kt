package at.jodlidev.esmira.input_views

import android.content.Context
import android.view.InflateException
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input

/**
 * Created by JodliDev on 16.04.2019.
 */
open class TextElView : ConstraintLayout, AndroidInputViewInterface {
	protected var descEl: TextView
	protected lateinit var input: Input
	protected var isBound = false
	
	constructor(context: Context) : super(context) {
		View.inflate(context, R.layout.view_input_text, this)
		descEl = findViewById(R.id.desc)
	}
	
	constructor(context: Context, res: Int) : super(context) {
		try {
			View.inflate(context, res, this)
		}
		catch(e: InflateException) {
			View.inflate(context, R.layout.view_input_text_template, this)
		}
		descEl = findViewById(R.id.desc)
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		this.input = input
		this.isBound = true
		descEl.visibility = if(input.desc.isEmpty()) GONE else VISIBLE
		HtmlHandler.setHtml(input.desc, descEl)
	}
}