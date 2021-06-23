package at.jodlidev.esmira

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import at.jodlidev.esmira.sharedCode.Web
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Created by JodliDev on 15.09.2020.
 */
class Activity_WelcomeScreen : AppCompatActivity() {
	class ServerListAdapter internal constructor(
		private val context: Context,
		private val serverList: List<Pair<String, String>>,
		private var selectedUrl: String = "",
		private val listener: (title: String, url: String) -> Unit
	) : RecyclerView.Adapter<ServerListAdapter.ServerViewHolder>() {
		private lateinit var currentSelected: ServerViewHolder
		
		class ServerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
			LayoutInflater.from(parent.context).inflate(R.layout.item_radio_header_sub, parent, false)
		) {
			val radioEl: RadioButton = itemView.findViewById(R.id.radio1)
			val headerEl: TextView = itemView.findViewById(R.id.header)
			val subEl: TextView = itemView.findViewById(R.id.sub)
		}
		
		init {
			listener(serverList[0].first, serverList[0].second)
		}
		private fun selectHolder(holder: ServerViewHolder, title: String, url: String) {
			if(this::currentSelected.isInitialized)
				currentSelected.radioEl.isChecked = false
			holder.radioEl.isChecked = true
			currentSelected = holder
			
			selectedUrl = url
			listener(title, url)
		}
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
			return ServerViewHolder(parent)
		}
		
		override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
			when(getItemViewType(position)) {
				ENTER_MANUALLY -> {
					holder.headerEl.text = context.getString(R.string.enter_manually)
					
					holder.itemView.setOnClickListener {
						val dialogView = View.inflate(context, R.layout.dialog_manual_server, null)
						val serverText = dialogView.findViewById<EditText>(R.id.server_text)
						serverText.setText(holder.subEl.text)
						
						MaterialAlertDialogBuilder(context, R.style.AppTheme_ActivityDialog)
							.setView(dialogView)
							.setPositiveButton(android.R.string.ok) { _, _ ->
								val url = serverText.text.toString()
								holder.subEl.text = url
								holder.subEl.visibility = View.VISIBLE
								selectHolder(holder, Web.getServerName(url), url)
							}
							.setNegativeButton(android.R.string.cancel, null).show()
					}
					
					if(!this::currentSelected.isInitialized) {
						selectHolder(holder, selectedUrl, selectedUrl)
						holder.subEl.text = selectedUrl
					}
					else
						holder.subEl.visibility = View.GONE
				}
				else -> {
					val entry = serverList[position]
					holder.headerEl.text = entry.first
					holder.subEl.text = entry.second
					
					holder.itemView.setOnClickListener {
						selectHolder(holder, entry.first, entry.second)
					}
					
					if(selectedUrl == entry.second)
						selectHolder(holder, entry.first, entry.second)
					else if(position == 0 && selectedUrl.isEmpty())
						selectHolder(holder, entry.first, entry.second)
					
				}
			}
			
		}
		
		override fun getItemViewType(position: Int): Int {
			return when(position) {
				serverList.size -> ENTER_MANUALLY
				else -> SERVER_STRING
			}
		}
		
		override fun getItemCount(): Int {
			return serverList.size + 1
		}
		
		
		companion object {
			private val SERVER_STRING = 0
			private const val ENTER_MANUALLY = 1
		}
	}
	
	enum class ProgressPos {
		Welcome,
		
		QrQuestion,
		QrScanning,
		
		ServerQuestion,
		AccessKeyQuestion,
	}
	
	private var urlTitle = ""
	private var url = ""
	private var accessKey = ""
	
	private var progressPos = ProgressPos.QrQuestion
	private lateinit var container: ViewGroup
	private var skipWelcome = false
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		setContentView(R.layout.activity_welcome_screen)
		container = findViewById(R.id.container)
		
		val extras = intent.extras
		skipWelcome = extras != null && extras.getBoolean(KEY_SKIP_WELCOME)
		
		var startingPos = if(skipWelcome) ProgressPos.QrQuestion else ProgressPos.Welcome
		if(savedInstanceState != null) {
			url = savedInstanceState.getString(Activity_addStudy.KEY_SERVER_URL, url)
			accessKey = savedInstanceState.getString(Activity_addStudy.KEY_ACCESS_KEY, accessKey)
			startingPos = ProgressPos.valueOf(savedInstanceState.getString(KEY_PROGRESS, startingPos.name))
		}
		
		
		doProgress(startingPos)
	}
	
	private fun createView(res: Int): View {
		val view = View.inflate(this, res, null)
		container.addView(view)
		return view
	}
	
	private fun drawProgress(newAnimRes: Int = -1) {
//		container.removeAllViews()
		val view: View
		when(progressPos) {
			ProgressPos.Welcome -> {
				view = createView(R.layout.view_welcome)
				setupButton(view, R.id.btn_next, ProgressPos.QrQuestion)
			}
			ProgressPos.QrQuestion -> {
				view = createView(R.layout.view_welcome_qr_exists)
				setupButton(view, R.id.btn_no, ProgressPos.ServerQuestion)
				setupButton(view, R.id.btn_yes, ProgressPos.QrScanning)
			}
			ProgressPos.QrScanning -> {
				view = createView(R.layout.view_welcome_qr_scanning)
				view.findViewById<Button>(R.id.btn_continue).setOnClickListener {
					Activity_QRscanner.start(this@Activity_WelcomeScreen)
				}
			}
			ProgressPos.ServerQuestion -> {
				view = createView(R.layout.view_welcome_server_question)
				view.findViewById<RecyclerView>(R.id.list_box).adapter = ServerListAdapter(this, Web.serverList, url) { title, newUrl ->
					urlTitle = title
					url = newUrl
				}
				setupButton(view, R.id.btn_yes, ProgressPos.AccessKeyQuestion)
			}
			ProgressPos.AccessKeyQuestion -> {
				view = createView(R.layout.view_welcome_accesskey_question)
				view.findViewById<Button>(R.id.btn_yes).setOnClickListener {
					val dialogView = View.inflate(this, R.layout.dialog_search_manually, null)
					val accessKeyText = dialogView.findViewById<EditText>(R.id.accessCode_text)
					accessKeyText.setText(accessKey)
					
					MaterialAlertDialogBuilder(this, R.style.AppTheme_ActivityDialog)
						.setView(dialogView)
						.setPositiveButton(android.R.string.ok) { _, _ ->
							accessKey = accessKeyText.text.toString()
							Activity_addStudy.start(this, urlTitle, url, accessKey, 0)
						}
						.setNegativeButton(android.R.string.cancel, null).show()
				}
				view.findViewById<Button>(R.id.btn_no).setOnClickListener {
					Activity_addStudy.start(this, urlTitle, url, "", 0)
				}
			}
		}
		if(newAnimRes != -1)
			view.startAnimation(AnimationUtils.loadAnimation(applicationContext, newAnimRes))
		
		view.findViewById<Button>(R.id.btn_back)?.setOnClickListener {
			onBackPressed()
		}
	}
	
	private fun doProgress(pos: ProgressPos) {
		val oldAnimRes: Int
		val newAnimRes: Int
		if(progressPos.ordinal >= pos.ordinal) {
			oldAnimRes = R.anim.slide_to_right
			newAnimRes = R.anim.slide_from_left
		}
		else {
			oldAnimRes = R.anim.slide_to_left
			newAnimRes = R.anim.slide_from_right
		}
		
		progressPos = pos
		
		if(container.childCount == 0) {
			drawProgress()
		}
		else {
			while(container.childCount > 1) {
				container.removeView(container.getChildAt(1))
			}
			
			val view = container.getChildAt(0)
			val anim = AnimationUtils.loadAnimation(applicationContext, oldAnimRes)
			anim.setAnimationListener(object : Animation.AnimationListener {
				override fun onAnimationStart(animation: Animation?) {}
				override fun onAnimationEnd(animation: Animation?) {
					view.post {
						container.removeView(view)
					}
				}

				override fun onAnimationRepeat(animation: Animation?) {}
			})
			view.startAnimation(anim)
			drawProgress(newAnimRes)
		}
	}
	
	private fun setupButton(view: View, resId: Int, target: ProgressPos) {
		view.findViewById<Button>(resId).setOnClickListener {
			doProgress(target)
		}
	}
	
	override fun onSaveInstanceState(savedInstanceState: Bundle) {
		super.onSaveInstanceState(savedInstanceState)
		
		savedInstanceState.putString(Activity_addStudy.KEY_SERVER_URL, url)
		savedInstanceState.putString(Activity_addStudy.KEY_ACCESS_KEY, accessKey)
		savedInstanceState.putString(KEY_PROGRESS, progressPos.name)
	}
	
	
	override fun onBackPressed() {
		when(progressPos) {
			ProgressPos.Welcome ->
				MaterialAlertDialogBuilder(this, R.style.AppTheme_ActivityDialog)
					.setTitle(R.string.welcome_exit_questionTitle)
					.setMessage(R.string.welcome_exit_questionDesc)
					.setPositiveButton(android.R.string.ok) { _, _ ->
						super.onBackPressed()
					}
					.setNegativeButton(android.R.string.cancel, null).show()
				
			ProgressPos.ServerQuestion ->
				doProgress(ProgressPos.QrQuestion)
			ProgressPos.QrQuestion ->
				if(skipWelcome)
					super.onBackPressed()
				else
					doProgress(ProgressPos.Welcome)
			else ->
				doProgress(ProgressPos.values()[progressPos.ordinal-1])
		}
	}
	
	companion object {
		private const val KEY_SKIP_WELCOME = "skip_welcome"
		private const val KEY_PROGRESS = "progress"
		fun start(context: Context, notFirstTime: Boolean = false) {
			val intent = Intent(context, Activity_WelcomeScreen::class.java)
			if(notFirstTime)
				intent.putExtra(KEY_SKIP_WELCOME, true)
			context.startActivity(intent)
		}
	}
}