package at.jodlidev.esmira

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Created by JodliDev on 24.04.2019.
 */
public class Activity_errorReport : AppCompatActivity(), View.OnClickListener {
	private lateinit var rootView: ViewGroup
	private lateinit var comment: EditText
	
	private class ListAdapter internal constructor(var errors: List<ErrorBox>, var comment: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
		
		class HeaderHolder(textEl: TextView) : RecyclerView.ViewHolder(textEl)
		class MainHolder(var root: ViewGroup) : RecyclerView.ViewHolder(root) {
			var dateTime: TextView = root.findViewById(R.id.dateTime)
			var title: TextView = root.findViewById(R.id.title)
			var msg: TextView = root.findViewById(R.id.msg)
		}
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
			return when(viewType) {
				HEADER -> {
//					val textEl = TextView(parent.context)
//					textEl.setPadding(20, 20, 20, 20)
					val textEl = LayoutInflater.from(parent.context).inflate(R.layout.item_text_small, parent, false) as TextView
					HeaderHolder(textEl)
				}
				else -> {
					val vG = LayoutInflater.from(parent.context).inflate(R.layout.item_error_box, parent, false) as ViewGroup
					MainHolder(vG)
				}
			}
		}
		
		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
			when(getItemViewType(position)) {
				HEADER -> {
					(holder.itemView as TextView).text = ErrorBox.getReportHeader(comment)
				}
				else -> {
					val mainHolder = holder as MainHolder
					val context = mainHolder.title.context
					val error = errors[position]
					mainHolder.root.setBackgroundColor(ContextCompat.getColor(context, if(position % 2 == 0) R.color.questionnaire1 else R.color.questionnaire2))
					mainHolder.dateTime.text = error.getFormattedDateTime()
					mainHolder.title.text = error.title
					mainHolder.msg.text = error.msg
					when(error.severity) {
						ErrorBox.SEVERITY_LOG -> mainHolder.title.setTextColor(ContextCompat.getColor(context, R.color.log))
						ErrorBox.SEVERITY_WARN -> mainHolder.title.setTextColor(ContextCompat.getColor(context, R.color.warn))
						ErrorBox.SEVERITY_ERROR -> mainHolder.title.setTextColor(ContextCompat.getColor(context, R.color.error))
					}
				}
			}
		}
		
		override fun getItemViewType(position: Int): Int {
			return if(position == 0) HEADER else MAIN
		}
		
		override fun getItemCount(): Int {
			return errors.size
		}
		
		companion object {
			private const val MAIN = 1
			private const val HEADER = 2
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		DbLogic.setErrorsAsReviewed()
		setContentView(R.layout.activity_error_report)
		
		rootView = findViewById(R.id.root_view)
		
		comment = findViewById(R.id.comment)

		val btnSendData = findViewById<View>(R.id.btn_send_data)
		btnSendData.setOnClickListener(this)

		findViewById<View>(R.id.btn_send).setOnClickListener(this)
		findViewById<View>(R.id.btn_cancel).setOnClickListener(this)
		
		if(intent.hasExtra(EXTRA_CALLED_MANUALLY) && intent.extras?.getBoolean(EXTRA_CALLED_MANUALLY) == true) {
			findViewById<View>(R.id.header).visibility = View.GONE
			findViewById<View>(R.id.desc).visibility = View.GONE
		}
	}
	
	override fun onClick(v: View) {
		when(v.id) {
			R.id.btn_send_data -> {
				val list = RecyclerView(this)
				list.layoutManager = LinearLayoutManager(this)
				list.adapter = ListAdapter(DbLogic.getErrors(), comment.text.toString())
				MaterialAlertDialogBuilder(this, R.style.AppTheme_ActivityDialog)
						.setView(list)
						.setTitle(R.string.what_is_sent)
						.setPositiveButton(R.string.close, null)
						.show()
			}
			R.id.btn_cancel -> finish()
			R.id.btn_send -> {
				val testLabSetting = Settings.System.getString(applicationContext.contentResolver, "firebase.test.lab")
				if("true" != testLabSetting) {//we don't want to have google bots trigger the error report
					Web.sendErrorReportAsync(
						comment = if(comment.text.isNotEmpty()) comment.text.toString() else null,
						onError = {
								msg -> Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show()
						},
						onSuccess = {
							runOnUiThread {
								Toast.makeText(applicationContext, R.string.info_thank_you, Toast.LENGTH_SHORT).show()
							}
							finish()
						}
					)
				}
			}
		}
	}
	
	companion object {
		private const val EXTRA_CALLED_MANUALLY = "with_error"

		fun start(context: Context, calledManually: Boolean = false) {
			val intent = Intent(context, Activity_errorReport::class.java)
			intent.putExtra(EXTRA_CALLED_MANUALLY, calledManually)
			if(context !is Activity)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			context.startActivity(intent)
		}
	}
}