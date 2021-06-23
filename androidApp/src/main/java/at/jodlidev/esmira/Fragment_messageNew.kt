package at.jodlidev.esmira

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Study
import com.google.android.material.snackbar.Snackbar


/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_messageNew : Base_fragment() {
	
	private lateinit var study: Study

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val arguments: Bundle = arguments ?: return null
		study = DbLogic.getStudy(arguments.getLong(Fragment_messages.KEY_STUDY_ID))
			?: throw Exception("Study is null (id: ${arguments.getLong(Fragment_messages.KEY_STUDY_ID)})!")
		return inflater.inflate(R.layout.fragment_message_new, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val btn = rootView.findViewById<Button>(R.id.btn_send)
		val content = rootView.findViewById<TextView>(R.id.content)
		btn.setOnClickListener {
			Web.sendMessageAsync(
				content = content.text.toString(),
				study = study,
				onError = {
						msg -> Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show()
				},
				onSuccess = {
					activity?.runOnUiThread {
						Toast.makeText(context, R.string.info_message_sent, Toast.LENGTH_SHORT).show()
						activity?.onBackPressed()
					}
				}
			)
			
		}
		
		setTitle(R.string.new_message)
	}
	
	companion object {
		const val KEY_STUDY_ID: String = "study_id"
	}
}