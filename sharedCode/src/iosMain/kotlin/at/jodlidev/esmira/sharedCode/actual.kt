package at.jodlidev.esmira.sharedCode

import kotlinx.coroutines.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t
//import platform.UIKit.UIDevice
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.freeze

//private class MainDispatcher: CoroutineDispatcher() {
//	override fun dispatch(context: CoroutineContext, block: Runnable) {
//		dispatch_async(dispatch_get_main_queue()) {
//			block.run()
//		}
//	}
//}
//internal class MainScope: CoroutineScope {
//	private val dispatcher = MainDispatcher()
//	private val job = Job()
//
//	override val coroutineContext: CoroutineContext
//		get() = dispatcher + job
//}
//actual fun<T> nativeAsync(block: suspend () -> T) {
//	MainScope().launch {
//		withContext(Dispatchers.Default) {
//			block()
//		}
//	}
//}


//object MainLoopDispatcher: CoroutineDispatcher() {
//	override fun dispatch(context: CoroutineContext, block: Runnable) {
//		NSRunLoop.mainRunLoop().performBlock {
//			block.run()
//		}
//	}
//}



internal class MainDispatcher(private val dispatchQueue: dispatch_queue_t) : CoroutineDispatcher() {
	override fun dispatch(context: CoroutineContext, block: Runnable) {
		dispatch_async(dispatchQueue.freeze()) {
			block.run()
		}
//		dispatch_async(dispatch_get_main_queue()) {
//			block.run()
//		}
	}
}

actual fun nativeBlocking(context: CoroutineContext, block: suspend CoroutineScope.() -> Unit) = runBlocking(context, block)
actual fun<T> nativeAsync(block: suspend () -> T) {
	val context = MainDispatcher(dispatch_get_main_queue())
	CoroutineScope(context).launch {
		block()
	}
}



actual fun isIOS(): Boolean = true
actual fun isAndroid(): Boolean = false


//actual fun platformName(): String {
//	return UIDevice.currentDevice.systemName() +
//			" " +
//			UIDevice.currentDevice.systemVersion
//}


//func getIosDate() -> String{
//	let formatted = DateFormatter()
//	formatted.dateFormat = DateUtilities().dateFormat
//	return formatted.string(from: Date())
//}

