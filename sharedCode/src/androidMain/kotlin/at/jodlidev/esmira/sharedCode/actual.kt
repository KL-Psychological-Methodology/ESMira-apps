package at.jodlidev.esmira.sharedCode

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

actual fun nativeBlocking(context: CoroutineContext, block: suspend CoroutineScope.() -> Unit) = runBlocking(context, block)
actual fun<T> nativeAsync(block: suspend () -> T) {
	MainScope().launch(Dispatchers.IO) {
		block()
	}
//	MainScope().launch {
//		withContext(Dispatchers.IO) {
//			block()
//		}
//	}
}

fun onUIThread(block: () -> Unit) {
	MainScope().launch(Dispatchers.Main) {
		block()
	}
}
actual fun isIOS(): Boolean = false
actual fun isAndroid(): Boolean = true



