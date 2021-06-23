package at.jodlidev.esmira

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import at.jodlidev.esmira.androidNative.InputViewChooser
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Page
import at.jodlidev.esmira.input_views.AndroidInputViewInterface
import at.jodlidev.esmira.sharedCode.DbLogic


/**
 * Created by JodliDev on 11.04.2019.
 */
class Fragment_questionnaireDetail : Base_fragment() {
	private lateinit var questionnaire: Questionnaire
	private var pageIndex: Int = 0
	private var formStarted: Long = 0
	
	private class ListAdapter constructor(
		context: Context,
		val questionnaire: Questionnaire,
		val pageIndex: Int,
		val nextPageListener: View.OnClickListener,
		val saveQuestionnaireListener: View.OnClickListener
	) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
		class ViewHolder(view: AndroidInputViewInterface) : RecyclerView.ViewHolder((view as View)) {
			init {
				itemView.layoutParams = ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT
				)
			}
		}
		class HeaderViewHolder(parent: ViewGroup, page: Page) : RecyclerView.ViewHolder(
			LayoutInflater.from(parent.context).inflate(R.layout.item_questionnaire_header, parent, false) as ViewGroup
		) {
			init {
				if(page.header.isNotEmpty()) {
					val headerEl = itemView.findViewById<TextView>(R.id.headerText)
					headerEl.text = HtmlCompat.fromHtml(page.header, HtmlCompat.FROM_HTML_MODE_LEGACY)
					headerEl.movementMethod = LinkMovementMethod.getInstance()
				}
				else
					itemView.findViewById<TextView>(R.id.headerText).visibility = View.GONE
			}
		}
		class FooterViewHolder(
			parent: ViewGroup,
			isLastPage: Boolean,
			hasRequired: Boolean,
			nextPageListener: View.OnClickListener,
			saveQuestionnaireListener: View.OnClickListener,
			page: Page
		) : RecyclerView.ViewHolder(
			LayoutInflater.from(parent.context).inflate(R.layout.item_questionnaire_footer, parent, false) as ViewGroup
		) {
			init {
				itemView.findViewById<View>(R.id.info_required).visibility = if(hasRequired) View.VISIBLE else View.GONE
				val btnContinue = itemView.findViewById<Button>(R.id.btn_continue)
				val btnSave = itemView.findViewById<Button>(R.id.btn_save)
				btnSave.setOnClickListener(saveQuestionnaireListener)
				
				if(isLastPage) {
					btnContinue.visibility = View.GONE
					btnSave.visibility = View.VISIBLE
				}
				else {
					btnSave.visibility = View.GONE
					btnContinue.visibility = View.VISIBLE
					btnContinue.setText(R.string.continue_)
					btnContinue.setOnClickListener(nextPageListener)
				}
				
				itemView.layoutParams = ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT
				)
				
				if(page.footer.isNotEmpty()) {
					val footerEl = itemView.findViewById<TextView>(R.id.footerText)
					footerEl.text = HtmlCompat.fromHtml(page.footer, HtmlCompat.FROM_HTML_MODE_LEGACY)
					footerEl.movementMethod = LinkMovementMethod.getInstance()
				}
				else
					itemView.findViewById<TextView>(R.id.footerText).visibility = View.GONE
			}
		}
		
		val page = questionnaire.pages[pageIndex]
		val inputs = page.inputs
		val backColor1: Int = ContextCompat.getColor(context, R.color.questionnaire1)
		val backColor2: Int = ContextCompat.getColor(context, R.color.questionnaire2)
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
			return when(viewType) {
				HEADER -> HeaderViewHolder(parent, page)
				FOOTER -> FooterViewHolder(
					parent,
					pageIndex == questionnaire.pages.size - 1,
					questionnaire.questionnairePageHasRequired(pageIndex),
					nextPageListener,
					saveQuestionnaireListener,
					page
				)
				else -> ViewHolder(InputViewChooser.getView(parent.context, viewType))
			}
		}
		
		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
			val type = getItemViewType(position)
			if(type != FOOTER && type != HEADER) {
				val index = position - 1
				val input: Input = inputs[index]
				holder.itemView.setBackgroundColor(if(index % 2 != 0) backColor1 else backColor2)
				
				val inputView = holder.itemView as AndroidInputViewInterface
				inputView.bindData(input, questionnaire)
			}
		}
		
		override fun getItemViewType(position: Int): Int {
			return when(position) {
				0 -> HEADER
				inputs.size + 1 -> FOOTER
				else -> inputs[position-1].type.ordinal
			}
		}
		
		override fun getItemCount(): Int {
			return inputs.size + 2
		}
		
		companion object {
			private const val HEADER = -1
			private const val FOOTER = -2
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val state = savedInstanceState ?: arguments
		
		if(state == null) {
			goToAsSub(Activity_main.SITE_LIST_QUESTIONNAIRES, null)
			return
		}
		val questionnaireId: Long = state.getLong(KEY_QUESTIONNAIRE, -1)
		
		try {
			questionnaire = DbLogic.getQuestionnaire(questionnaireId) ?: throw Exception()
			if(!questionnaire.canBeFilledOut())
				throw Exception()
		}
		catch(e: Exception) {
			goToAsSub(Activity_main.SITE_LIST_QUESTIONNAIRES, null)
		}
		
		formStarted = state.getLong(KEY_FORM_STARTED, System.currentTimeMillis())
		pageIndex = state.getInt(KEY_PAGE, 0)
		
		
		for(i in 0 .. pageIndex) {
			for((j, value) in (state.getStringArrayList("$STATE_INPUT_DATA$i") ?: ArrayList()).withIndex()) {
				questionnaire.pages[i].orderedInputs[j].value = value
			}
		}
		
		activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_questionnaire_detail, container, false)
	}
	
	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		if(!this::questionnaire.isInitialized)
			return
		val contentBox = rootView.findViewById<RecyclerView>(R.id.content_box)
		contentBox.adapter = ListAdapter(
			requireContext(),
			questionnaire,
			pageIndex,
			{
				val errorIndex = this.questionnaire.checkQuestionnaire(pageIndex)
				if(errorIndex == -1) {
					val state = createState(pageIndex)
					arguments = state
					
					val gotoState = state.clone() as Bundle
					gotoState.putInt(KEY_PAGE, pageIndex + 1)
					
					goToAsSub(Activity_main.SITE_QUESTIONNAIRE_DETAIL, gotoState)
				}
				else
					messageMissing(contentBox, errorIndex)
			},
			{
				val errorIndex = questionnaire.checkQuestionnaire(pageIndex)
				
				if(errorIndex == -1) {
					questionnaire.saveQuestionnaire(formStarted)
					message(R.string.info_questionnaire_success)
					goToAsSub(Activity_main.SITE_LIST_QUESTIONNAIRES, null)
				}
				else
					messageMissing(contentBox, errorIndex)
			})
		
		setTitle(questionnaire.getQuestionnaireTitle(pageIndex), false)
	}
	
	private fun messageMissing(contentBox: RecyclerView, errorIndex: Int) {
		message(R.string.error_missing_fields)
		
		val layoutManager: RecyclerView.LayoutManager = contentBox.layoutManager ?: return
		
		val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
			override fun getVerticalSnapPreference(): Int {
				return SNAP_TO_START
			}
			
			override fun onStop() {
				super.onStop()
				
				val view: View = layoutManager.findViewByPosition(errorIndex + 1) ?: return
				view.postDelayed({
					view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shaking))
				}, 300)
			}
		}
		smoothScroller.targetPosition = errorIndex+1
		layoutManager.startSmoothScroll(smoothScroller)
	}
	
	
	private fun createState(pageIndex: Int, bundle: Bundle = Bundle()): Bundle {
		for((i, page) in questionnaire.pages.withIndex()) {
			val pageCache = ArrayList<String>()
			for(input in page.orderedInputs) {
				pageCache.add(input.value)
			}
			bundle.putStringArrayList("$STATE_INPUT_DATA$i", pageCache)
		}
		bundle.putLong(KEY_QUESTIONNAIRE, questionnaire.id)
		bundle.putInt(KEY_PAGE, pageIndex)
		bundle.putLong(KEY_FORM_STARTED, formStarted)
		
		return bundle
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		createState(pageIndex, outState)
		
		super.onSaveInstanceState(outState)
	}
	
	override fun onDestroy() {
		activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
		super.onDestroy()
	}
	
	companion object {
		const val KEY_QUESTIONNAIRE: String = "questionnaire_id"
		private const val KEY_PAGE: String = "pageIndex"
		private const val KEY_FORM_STARTED: String = "form_started"
		private const val KEY_RESPONSES: String = "responses"
		private const val STATE_INPUT_DATA: String = "input_data"
	}
}