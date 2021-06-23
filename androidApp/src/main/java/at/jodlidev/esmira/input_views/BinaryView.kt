package at.jodlidev.esmira.input_views

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input
import com.google.android.material.button.MaterialButton

/**
 * Created by JodliDev on 16.04.2019.
 */
class BinaryView(context: Context) : TextElView(context, R.layout.view_input_binary) {
	private val leftEl: MaterialButton = findViewById(R.id.left)
	private val rightEl: MaterialButton = findViewById(R.id.right)
//	private val buttonGroup: MaterialButtonToggleGroup = findViewById(R.id.button_group)
	
	init {
//		buttonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
//			if(isChecked) {
//				if(checkedId == R.id.left)
//					input.value = "0"
//				else(checkedId == R.id.right)
//				input.value = "0"
//			}
//			else
//				input.value = ""
//		}
		
		leftEl.setOnClickListener {
			setValue("0")
		}
		rightEl.setOnClickListener {
			setValue("1")
		}
	}
	
	fun setValue(s: String) {
		when(s) {
			"0" -> {
				leftEl.icon = ContextCompat.getDrawable(context, R.drawable.ic_success_green_24dp)
				rightEl.icon = ContextCompat.getDrawable(context, R.drawable.ic_unchecked_24dp)
				input.value = "0"
			}
			"1" -> {
				leftEl.icon = ContextCompat.getDrawable(context, R.drawable.ic_unchecked_24dp)
				rightEl.icon = ContextCompat.getDrawable(context, R.drawable.ic_success_green_24dp)
				input.value = "1"
			}
			else -> {
				leftEl.icon = ContextCompat.getDrawable(context, R.drawable.ic_unchecked_24dp)
				rightEl.icon = ContextCompat.getDrawable(context, R.drawable.ic_unchecked_24dp)
				input.value = ""
			}
		}
	}
	
	override fun bindData(input: Input, questionnaire: Questionnaire) {
		super.bindData(input, questionnaire)
		
//		when(input.value) {
//			"0" ->
//				buttonGroup.check(R.id.left)
//			"1" ->
//				buttonGroup.check(R.id.right)
//		}
		
		leftEl.text = HtmlCompat.fromHtml(input.leftSideLabel, HtmlCompat.FROM_HTML_MODE_LEGACY)
		rightEl.text = HtmlCompat.fromHtml(input.rightSideLabel, HtmlCompat.FROM_HTML_MODE_LEGACY)
		setValue(input.value)
	}
}