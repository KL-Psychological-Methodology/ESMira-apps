package at.jodlidev.esmira.sharedCode

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


expect fun nativeBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit)
expect fun<T> nativeAsync(block: suspend () -> T)

expect fun isIOS(): Boolean
expect fun isAndroid(): Boolean