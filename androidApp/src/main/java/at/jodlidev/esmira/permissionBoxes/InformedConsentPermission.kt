package at.jodlidev.esmira.permissionBoxes

import android.content.Context
import android.view.View
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Created by JodliDev on 20.12.2021.
 */
class InformedConsentPermission(context: Context, val study: Study, num: Int) :
	APermissionBox(context, num, R.layout.view_informed_consent, R.string.informed_consent) {
	init {
		setPadding(0, 0, 0, 20)
	}
	
	override fun enable(continueCallback : () -> Unit) {
		super.enable(continueCallback)
		
		val btnConsent = findViewById<View>(R.id.btn_consent)
		
		btnConsent.setOnClickListener {
			context?.let { it1 ->
				MaterialAlertDialogBuilder(it1, R.style.AppTheme_ActivityDialog)
					.setTitle(resources.getString(R.string.informed_consent))
					.setMessage(study.informedConsentForm)
					.setPositiveButton(R.string.i_agree) { _, _ ->
						btnConsent.visibility = View.GONE
						rootView.findViewById<View>(R.id.desc_informed_consent).visibility = View.GONE
						setFinished()
					}
					.setNegativeButton(android.R.string.cancel, null).show()
			}
		}
	}
	
}