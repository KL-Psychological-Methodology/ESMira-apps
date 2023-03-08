package at.jodlidev.esmira.views.main

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.DialogButton
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Message
import at.jodlidev.esmira.sharedCode.data_structure.Study

/**
 * Created by JodliDev on 21.02.2023.
 */
@Composable
fun MessagesView(
	getStudy: () ->Study,
	goBack: () -> Unit
) {
	val context = LocalContext.current
	val messages = remember {
		mutableStateOf(DbLogic.getMessages(getStudy().id))
	}
	
	LaunchedEffect(true) {
		Web.updateStudiesAsync {
			messages.value = DbLogic.getMessages(getStudy().id)
		}
	}
	DefaultScaffoldView(title = stringResource(R.string.messages), goBack = goBack) {
		MessageListView(
			messages = messages.value,
			setAsRead = { message ->
				message.markAsRead()
			},
			sendMessage = { content, done ->
				Web.sendMessageAsync(
					content = content,
					study = getStudy(),
					onError = { msg ->
						//TODO: use Snackbar instead
						Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
						done(false)
					},
					onSuccess = {
						messages.value = DbLogic.getMessages(getStudy().id)
						done(true)
					}
				)
			}
		)
	}
}



@Composable
private fun MessageView(message: Message, setAsRead: (Message) -> Unit) {
	DisposableEffect(message) {
		onDispose {
			setAsRead(message)
		}
	}
	val color = if(!message.fromServer)
		MaterialTheme.colorScheme.primary
	else if(message.isNew) {
		MaterialTheme.colorScheme.tertiary
	}
	else
		MaterialTheme.colorScheme.secondary
	
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
				color = MaterialTheme.colorScheme.onPrimary,
				textAlign = TextAlign.End,
				fontSize = 10.sp,
				modifier = Modifier.fillMaxWidth()
			)
			Text(
				message.content,
				color = MaterialTheme.colorScheme.onPrimary,
				textAlign = TextAlign.Start,
				modifier = Modifier.fillMaxWidth()
			)
		}
		
		if(message.fromServer)
			Spacer(modifier = Modifier.weight(0.2f))
	}
}

@Composable
private fun MessageListView(messages: List<Message>, setAsRead: (Message) -> Unit, sendMessage: (String, done: (Boolean) -> Unit) -> Unit) {
	val listState = rememberLazyListState()
	
//	LaunchedEffect(messages.size) {
//		listState.scrollToItem(messages.size)
//	}
	
	Column(horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 5.dp)
	) {
		if(messages.isEmpty()) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
			) {
				Text(stringResource(R.string.info_no_messages))
			}
		}
		else {
			LazyColumn(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Bottom,
				reverseLayout = true,
				state = listState,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			) {
				items(messages.reversed()) { message ->
					Spacer(modifier = Modifier.height(10.dp))
					MessageView(message, setAsRead)
				}
				item {
					Spacer(modifier = Modifier.height(20.dp))
				}
			}
		}
		
		Footer(sendMessage)
	}
}

@Composable
private fun Footer(sendMessage: (String, done: (Boolean) -> Unit) -> Unit) {
	val isSending = rememberSaveable { mutableStateOf(false) }
	val writeMessage = rememberSaveable { mutableStateOf(false) }
	val close = {
		writeMessage.value = false
	}
	
	if(!writeMessage.value) {
		DefaultButton(
			stringResource(R.string.send_message_to_researcher),
			onClick = {
				writeMessage.value = true
			}
		)
		Spacer(modifier = Modifier.height(20.dp))
	}
	else {
		if(isSending.value) {
			CircularProgressIndicator()
			Spacer(modifier = Modifier.height(10.dp))
		}
		else {
			NewMessageBox(
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewMessageBox(sendMessage: (String) -> Unit, cancel: () -> Unit) {
	val messageContent = rememberSaveable { mutableStateOf("") }
	
	val focusRequester = remember { FocusRequester() }
	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}
	Column {
		Row {
			
			DialogButton(
				stringResource(R.string.cancel),
				onClick = {
					cancel()
				})
			
			Spacer(modifier = Modifier.weight(1f))
			
			DialogButton(
				stringResource(R.string.send),
				onClick = {
					sendMessage(messageContent.value)
				})
		}
		OutlinedTextField(
			modifier = Modifier
				.focusRequester(focusRequester)
				.fillMaxWidth()
				.height(100.dp)
				.padding(bottom = 3.dp),
			value = messageContent.value,
			onValueChange = {
				messageContent.value = it
			}
		)
		
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
fun PreviewMessageListViewNoMessages() {
	val messageFromServer = Message(-1, "This message is sent from the server",1674207703947)
	messageFromServer.fromServer = true
	val messageFromServerNew = Message(-1, "This message is sent from the server and is new",1674207703947)
	messageFromServerNew.fromServer = true
	messageFromServerNew.isNew = true
	
	ESMiraSurface {
		MessageListView(ArrayList(), {}) {_, _ -> }
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