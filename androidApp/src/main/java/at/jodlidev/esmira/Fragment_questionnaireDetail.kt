package at.jodlidev.esmira

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Page
import at.jodlidev.esmira.sharedCode.DbLogic

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.androidNative.ChooseInputView
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import java.io.File


/**
 * Created by JodliDev on 11.04.2019.
 */
class Fragment_questionnaireDetail : Base_fragment() {
	private lateinit var questionnaire: Questionnaire
	private var pageNumber: Int = 0
	private var formStarted: Long = 0
	private var originalOrientation: Int = 0
	
//	private class ListAdapter constructor(
//		context: Context,
//		val questionnaire: Questionnaire,
//		val pageIndex: Int,
//		val nextPageListener: View.OnClickListener,
//		val saveQuestionnaireListener: View.OnClickListener
//	) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//		class ViewHolder(view: AndroidInputViewInterface) : RecyclerView.ViewHolder((view as View)) {
//			init {
//				itemView.layoutParams = ViewGroup.LayoutParams(
//					ViewGroup.LayoutParams.MATCH_PARENT,
//					ViewGroup.LayoutParams.WRAP_CONTENT
//				)
//			}
//		}
//		class HeaderViewHolder(parent: ViewGroup, page: Page) : RecyclerView.ViewHolder(
//			LayoutInflater.from(parent.context).inflate(R.layout.item_questionnaire_header, parent, false) as ViewGroup
//		) {
//			init {
//				if(page.header.isNotEmpty()) {
//					val headerEl = itemView.findViewById<TextView>(R.id.headerText)
//					HtmlHandler.setHtml(page.header, headerEl)
//				}
//				else
//					itemView.findViewById<TextView>(R.id.headerText).visibility = View.GONE
//			}
//		}
//		class FooterViewHolder(
//			parent: ViewGroup,
//			isLastPage: Boolean,
//			hasRequired: Boolean,
//			nextPageListener: View.OnClickListener,
//			saveQuestionnaireListener: View.OnClickListener,
//			page: Page
//		) : RecyclerView.ViewHolder(
//			LayoutInflater.from(parent.context).inflate(R.layout.item_questionnaire_footer, parent, false) as ViewGroup
//		) {
//			init {
//				itemView.findViewById<View>(R.id.info_required).visibility = if(hasRequired) View.VISIBLE else View.GONE
//				val btnContinue = itemView.findViewById<Button>(R.id.btn_continue)
//				val btnSave = itemView.findViewById<Button>(R.id.btn_save)
//				btnSave.setOnClickListener(saveQuestionnaireListener)
//
//				if(isLastPage) {
//					btnContinue.visibility = View.GONE
//					btnSave.visibility = View.VISIBLE
//				}
//				else {
//					btnSave.visibility = View.GONE
//					btnContinue.visibility = View.VISIBLE
//					btnContinue.setText(R.string.continue_)
//					btnContinue.setOnClickListener(nextPageListener)
//				}
//
//				itemView.layoutParams = ViewGroup.LayoutParams(
//					ViewGroup.LayoutParams.MATCH_PARENT,
//					ViewGroup.LayoutParams.WRAP_CONTENT
//				)
//
//				if(page.footer.isNotEmpty()) {
//					val footerEl = itemView.findViewById<TextView>(R.id.footerText)
//					HtmlHandler.setHtml(page.footer, footerEl)
//				}
//				else
//					itemView.findViewById<TextView>(R.id.footerText).visibility = View.GONE
//			}
//		}
//
//		val page = questionnaire.pages[pageIndex]
//		val inputs = page.inputs
//		val backColor1: Int = ContextCompat.getColor(context, R.color.questionnaire1)
//		val backColor2: Int = ContextCompat.getColor(context, R.color.questionnaire2)
//
//		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//			return when(viewType) {
//				HEADER -> HeaderViewHolder(parent, page)
//				FOOTER -> FooterViewHolder(
//					parent,
//					pageIndex == questionnaire.pages.size - 1,
//					questionnaire.questionnairePageHasRequired(pageIndex),
//					nextPageListener,
//					saveQuestionnaireListener,
//					page
//				)
//				else -> ViewHolder(InputViewChooser.getView(parent.context, viewType))
//			}
//		}
//
//		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//			val type = getItemViewType(position)
//			if(type != FOOTER && type != HEADER) {
//				val index = position - 1
//				val input: Input = inputs[index]
//				holder.itemView.setBackgroundColor(if(index % 2 != 0) backColor1 else backColor2)
//
//				val inputView = holder.itemView as AndroidInputViewInterface
//				inputView.bindData(input, questionnaire)
//			}
//		}
//
//		override fun getItemViewType(position: Int): Int {
//			return when(position) {
//				0 -> HEADER
//				inputs.size + 1 -> FOOTER
//				else -> inputs[position-1].type.ordinal
//			}
//		}
//
//		override fun getItemCount(): Int {
//			return inputs.size + 2
//		}
//
//		companion object {
//			private const val HEADER = -1
//			private const val FOOTER = -2
//		}
//	}
//	override fun onCreate(savedInstanceState: Bundle?) {
//		super.onCreate(savedInstanceState)
//		val state = savedInstanceState ?: arguments
//
//		if(state == null) {
//			goToAsSub(Activity_main.SITE_LIST_QUESTIONNAIRES, null)
//			return
//		}
//		val questionnaireId: Long = state.getLong(KEY_QUESTIONNAIRE, -1)
//
//		try {
//			questionnaire = DbLogic.getQuestionnaire(questionnaireId) ?: throw Exception()
//			if(!questionnaire.canBeFilledOut())
//				throw Exception()
//		}
//		catch(e: Exception) {
//			goToAsSub(Activity_main.SITE_LIST_QUESTIONNAIRES, null)
//		}
//
//		formStarted = state.getLong(KEY_FORM_STARTED, System.currentTimeMillis())
//		pageIndex = state.getInt(KEY_PAGE, 0)
//
//
//		for(i in 0 .. pageIndex) {
//			for((j, value) in (state.getStringArrayList("$STATE_INPUT_DATA$i") ?: ArrayList()).withIndex()) {
//				questionnaire.pages[i].orderedInputs[j].fromBackupString(value)
//			}
//		}
//
//		originalOrientation = requireActivity().requestedOrientation
//		activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
//	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		originalOrientation = requireActivity().requestedOrientation
		activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
		
		val state = savedInstanceState ?: arguments ?: return null
		val questionnaireId: Long = state.getLong(KEY_QUESTIONNAIRE, -1)
		
		questionnaire = DbLogic.getQuestionnaire(questionnaireId) ?: throw Exception()
		if(!questionnaire.canBeFilledOut())
			return null
		
		pageNumber = state.getInt(KEY_PAGE, 0)
		val isLastPage = pageNumber == questionnaire.pages.size - 1
		
		setTitle(questionnaire.getQuestionnaireTitle(pageNumber), false)
		
		for(i in 0 .. pageNumber) {
			for((j, value) in (state.getStringArrayList("$STATE_INPUT_DATA$i") ?: ArrayList()).withIndex()) {
				questionnaire.pages[i].orderedInputs[j].fromBackupString(value)
			}
		}
		
		return ComposeView(requireContext()).apply {
			setContent {
				val formStarted = rememberSaveable { state.getLong(KEY_FORM_STARTED, System.currentTimeMillis()) }
				val listState = rememberLazyListState()
				val coroutineScope = rememberCoroutineScope()
				
				ESMiraSurface {
					MainView(listState, questionnaire, questionnaire.pages[pageNumber], isLastPage) {
//						for(page in questionnaire.pages) {
//							for(input in page.inputs) {
//								println("${input.type}: ${input.value}, ${input.additionalValues}")
//							}
//						}
						
						val errorIndex = questionnaire.checkQuestionnaire(pageNumber)
						if(errorIndex != -1) {
							message(R.string.error_missing_fields)
							coroutineScope.launch {
								listState.animateScrollToItem(errorIndex)
							}

							return@MainView
						}

						if(isLastPage) {
							questionnaire.saveQuestionnaire(formStarted)
							goToAsRoot(Activity_main.SITE_QUESTIONNAIRE_SUCCESS, null)
						}
						else {
							val currentState = createState(pageNumber)
							arguments = currentState

							val gotoState = currentState.clone() as Bundle
							gotoState.putInt(KEY_PAGE, pageNumber + 1)

							goToAsSub(Activity_main.SITE_QUESTIONNAIRE_DETAIL, gotoState)
						}
					}
				}
			}
		}
	}
	
	@Composable
	fun MainView(listState: LazyListState, questionnaire: Questionnaire, page: Page, isLastPage: Boolean, clickBtn: () -> Unit) {
		LazyColumn(state = listState) {
			if(page.header.isNotEmpty()) {
				item {
					HtmlHandler.HtmlText(html = page.header, modifier = Modifier
						.fillMaxWidth()
						.padding(all = 5.dp)
					)
				}
			}
			
			itemsIndexed(page.inputs, { i, _ -> i }) { i, input ->
				ChooseInputView(
					questionnaire,
					input,
					Modifier
						.fillMaxWidth()
						.background(color = if(i % 2 == 0) colorLineBackground1 else colorLineBackground2)
						.padding(all = 5.dp)
				)
			}
			
			
			if(page.footer.isNotEmpty()) {
				item {
					HtmlHandler.HtmlText(html = page.footer, modifier = Modifier
						.fillMaxWidth()
						.background(color = if(page.inputs.size % 2 == 0) colorLineBackground1 else colorLineBackground2)
						.padding(all = 5.dp)
					)
				}
			}
			
			item {
				Row(
					horizontalArrangement = Arrangement.Center,
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 10.dp)
				) {
					if(isLastPage) {
						TextButton(
							onClick = clickBtn,
						) {
							Icon(
								Icons.Default.Save,
								contentDescription = "",
								modifier = Modifier.size(ButtonDefaults.IconSize)
							)
							Spacer(Modifier.size(ButtonDefaults.IconSpacing))
							Text(stringResource(R.string.save))
						}
					}
					else {
						Spacer(modifier = Modifier.weight(1f))
						TextButton(
							onClick = clickBtn,
						) {
							Text(stringResource(R.string.continue_))
							Spacer(Modifier.size(ButtonDefaults.IconSpacing))
							Icon(
								Icons.Default.KeyboardArrowRight,
								contentDescription = "",
								modifier = Modifier.size(ButtonDefaults.IconSize)
							)
						}
					}
				}
			}
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMainView() {
		val questionnaire = DbLogic.createJsonObj<Questionnaire>("""{}""")
		val page = DbLogic.createJsonObj<Page>("""{
			"header": "This is the header",
			"footer": "This is the footer",
			"inputs":[
				{"responseType": "text", "text": "A meaningful description"},
				{"responseType": "va_scale", "text": "A meaningful description"}
			]
		}""")
		
		ESMiraSurface {
			MainView(listState = rememberLazyListState(), questionnaire = questionnaire, page = page, isLastPage = false) {}
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMainViewLastPage() {
		val questionnaire = DbLogic.createJsonObj<Questionnaire>("""{}""")
		val page = DbLogic.createJsonObj<Page>("""{
			"header": "This is the header",
			"footer": "This is the footer",
			"inputs":[
				{"responseType": "text", "text": "A meaningful description"},
				{"responseType": "va_scale", "text": "A meaningful description"}
			]
		}""")

		ESMiraSurface {
			MainView(listState = rememberLazyListState(), questionnaire = questionnaire, page = page, isLastPage = true) {}
		}
	}
	
	
	private fun createState(pageNumber: Int, bundle: Bundle = Bundle()): Bundle {
		bundle.putInt(KEY_PAGE, pageNumber)
		bundle.putLong(KEY_FORM_STARTED, formStarted)
		if(!this::questionnaire.isInitialized)
			return bundle
		
		for((i, page) in questionnaire.pages.withIndex()) {
			val pageCache = ArrayList<String>()
			for(input in page.orderedInputs) {
				pageCache.add(input.getBackupString())
			}
			bundle.putStringArrayList("$STATE_INPUT_DATA$i", pageCache)
		}
		bundle.putLong(KEY_QUESTIONNAIRE, questionnaire.id)
		
		return bundle
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		createState(pageNumber, outState)

		super.onSaveInstanceState(outState)
	}
	
	override fun onDestroy() {
		activity?.requestedOrientation = originalOrientation
		super.onDestroy()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if(data == null)
			return
		val extras = data.extras ?: return
		if(requestCode == Activity_photoCamera.REQUEST_PHOTO_RESPONSE && resultCode == RESULT_OK) {
			val inputName = extras.getString(Activity_photoCamera.INPUT_NAME) ?: return
			val imagePreview: ImageView = requireView().findViewWithTag(inputName)
			val file = extras.getSerializable(Activity_photoCamera.PHOTO_FILE) as File ?: return
			val filePath: String = file.path
			val imageBitmap = BitmapFactory.decodeFile(filePath)
			imagePreview.setImageBitmap(imageBitmap)
			
			var input: Input? = null
			for(loopInput: Input in questionnaire.pages[pageNumber].inputs) {
				if(loopInput.name == inputName) {
					input = loopInput
					break
				}
			}
			if(input == null)
				return
			
			input.addImage(filePath, questionnaire.studyId)
		}
	}
	
	
	companion object {
		const val KEY_QUESTIONNAIRE: String = "questionnaire_id"
		private const val KEY_PAGE: String = "pageIndex"
		private const val KEY_FORM_STARTED: String = "form_started"
		private const val STATE_INPUT_DATA: String = "input_data"
	}
}