package at.jodlidev.esmira

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Message


/**
 * Created by JodliDev on 26.08.2019.
 */
class Fragment_messages : Base_fragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val arguments: Bundle = arguments ?: return null
		val study = DbLogic.getStudy(arguments.getLong(KEY_STUDY_ID)) ?: return null
		
		return ComposeView(requireContext()).apply {
			setContent {
				ESMiraSurface {
					val messages = remember { mutableStateOf(DbLogic.getMessages(study.id)) }
					
					MessageListView(
						messages = messages.value,
						setAsRead = { message ->
							message.markAsRead()
						},
						sendMessage = { content, done ->
							Web.sendMessageAsync(
								content = content,
								study = study,
								onError = { msg ->
									activity?.runOnUiThread {
										//TODO: use Snackbar instead
										Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
										done(false)
									}
								},
								onSuccess = {
									activity?.runOnUiThread {
										Toast.makeText(context, R.string.info_message_sent, Toast.LENGTH_SHORT).show()
										messages.value = DbLogic.getMessages(study.id)
										done(true)
									}
								}
							)
						}
					)
				}
			}
		}
	}
	
	@Composable
	private fun MessageView(message: Message, setAsRead: (Message) -> Unit) {
		val color = if(!message.fromServer)
			MaterialTheme.colors.primary
		else if(message.isNew) {
			setAsRead(message)
			MaterialTheme.colors.secondary
		}
		else
			MaterialTheme.colors.primaryVariant
		
		Row {
			if(!message.fromServer)
				Spacer(modifier = Modifier.weight(0.2f))
			
			Column(
				modifier = Modifier
					.clip(RoundedCornerShape(10.dp))
					.weight(0.8f)
					.background(color)
					.padding(all = 5.dp)
			) {
				Text(
					NativeLink.formatDateTime(message.sent),
					color = MaterialTheme.colors.onPrimary,
					textAlign = TextAlign.End,
					fontSize = 10.sp,
					modifier = Modifier.fillMaxWidth()
				)
				Text(
					message.content,
					color = MaterialTheme.colors.onPrimary,
					textAlign = TextAlign.Start,
					modifier = Modifier.fillMaxWidth()
				)
			}
			
			if(message.fromServer)
				Spacer(modifier = Modifier.weight(0.2f))
		}
	}
	
	@Composable
	private fun NewMessageBox(sendMessage: (String) -> Unit, cancel: () -> Unit) {
		val messageContent = rememberSaveable { mutableStateOf("") }
		
		val focusRequester = remember { FocusRequester() }
		LaunchedEffect(Unit) {
			focusRequester.requestFocus()
		}
		Column {
			OutlinedTextField(
				modifier = Modifier
					.focusRequester(focusRequester)
					.fillMaxWidth()
					.height(100.dp),
				value = messageContent.value,
				onValueChange = {
					messageContent.value = it
				}
			)
			
			Row {
				Spacer(modifier = Modifier.weight(1f))
				
				TextButton(onClick = {
					cancel()
				}) {
					Text(stringResource(R.string.cancel), textAlign = TextAlign.Center)
				}
				
				Spacer(modifier = Modifier.width(20.dp))
				
				TextButton(onClick = {
					sendMessage(messageContent.value)
				}) {
					Text(stringResource(R.string.send), textAlign = TextAlign.Center)
				}
			}
		}
	}
	
	@Composable
	private fun Footer(sendMessage: (String, done: (Boolean) -> Unit) -> Unit, close: () -> Unit) {
		val isSending = rememberSaveable { mutableStateOf(false) }
		if(isSending.value) {
			CircularProgressIndicator()
			Spacer(modifier = Modifier.height(10.dp))
		}
		else {
			NewMessageBox (
				cancel = close,
				sendMessage = { content ->
					isSending.value = true
					sendMessage(content) { success ->
						isSending.value = false
						if(success) {
							close()
						}
					}
				}
			)
		}
	}
	
	@Composable
	private fun MessageListView(messages: List<Message>, setAsRead: (Message) -> Unit, sendMessage: (String, done: (Boolean) -> Unit) -> Unit) {
		val listState = rememberLazyListState()
		val writeMessage = rememberSaveable { mutableStateOf(false) }
		
		LaunchedEffect(messages.size) {
			listState.scrollToItem(messages.size)
		}
		
		Column(horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 5.dp)
		) {
			
			LazyColumn(
				horizontalAlignment = Alignment.CenterHorizontally,
				state = listState,
				modifier = Modifier.fillMaxWidth().weight(1f)
			) {
				items(messages) { message ->
					Spacer(modifier = Modifier.height(10.dp))
					MessageView(message, setAsRead)
				}
				item {
					Spacer(modifier = Modifier.height(20.dp))
				}
				if(!writeMessage.value) {
					item {
						OutlinedButton(onClick = {
							writeMessage.value = true
						}) {
							Text(stringResource(R.string.send_message_to_researcher))
						}
						Spacer(modifier = Modifier.height(20.dp))
					}
				}
			}
			
			if(writeMessage.value) {
				Footer(sendMessage) {
					writeMessage.value = false
				}
			}
		}
	}
	
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewMessageListView() {
		val messageFromServer = Message(-1, "This message is sent from the server",1674207703947)
		messageFromServer.fromServer = true
		val messageFromServerNew = Message(-1, "This message is sent from the server and is new",1674207703947)
		messageFromServerNew.fromServer = true
		messageFromServerNew.isNew = true
		
		val messages = listOf(
			Message(-1, "This message is sent from client",1674207703947),
			messageFromServer,
			messageFromServerNew
		)
		
		ESMiraSurface {
			MessageListView(messages, {_ -> }) {_, _ -> }
		}
	}
	
	@Preview
	@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
	@Composable
	fun PreviewNewMessageBoxView() {
		ESMiraSurface {
			NewMessageBox({_ -> }) {}
		}
	}
	
	
	override fun onPause() {
		super.onPause()
		activity.let { a -> (a as Activity_main).updateNavigationBadges() }
		
	}
	
	companion object {
		const val KEY_STUDY_ID: String = "study_id"
	}
}