package at.jodlidev.esmira.sharedCode

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun kotlinRunOnUiThread(block: suspend () -> Unit) = uiScope.launch { block() }

expect fun nativeBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit)
expect fun<T> nativeAsync(block: suspend () -> T)

expect fun isIOS(): Boolean
expect fun isAndroid(): Boolean