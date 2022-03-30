package at.jodlidev.esmira.sharedCode

import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by JodliDev on 03.06.2020.
 */
actual object NativeLink {
	@Synchronized
	fun init(
		sql: SQLiteInterface,
		smartphoneData: SmartphoneDataInterface,
		dialogOpener: DialogOpenerInterface,
		notifications: NotificationsInterface,
		postponedActions: PostponedActionsInterface
//		fileOpener: FileOpener
	) {
		println("Initializing Android NativeLink")
		if(_sql.get() == null)
			_sql.set(sql)
		_smartphoneData.set(smartphoneData)
		_dialogOpener.set(dialogOpener)
		_notifications.set(notifications)
		_postponedActions.set(postponedActions)
//		_fileOpener.set(fileOpener)
		
		_isInitialized.set(true)
	}
	
	private val _isInitialized = AtomicReference<Boolean>(false)
	actual val isInitialized
		get() = _isInitialized.get()
	
	private val _sql = AtomicReference<SQLiteInterface?>(null)
	//	@ThreadLocal
//	private lateinit var __sql: SQLiteInterface
	actual val sql: SQLiteInterface
		get() = _sql.get()!!
	
	private val _smartphoneData = AtomicReference<SmartphoneDataInterface>(null)
	actual val smartphoneData: SmartphoneDataInterface
		get() = _smartphoneData.get()!!
	
	private val _dialogOpener = AtomicReference<DialogOpenerInterface>(null)
	actual val dialogOpener: DialogOpenerInterface
		get() = _dialogOpener.get()!!
	
	private val _notifications = AtomicReference<NotificationsInterface>(null)
	actual val notifications: NotificationsInterface
		get() = _notifications.get()!!
	
	private val _postponedActions = AtomicReference<PostponedActionsInterface>(null)
	actual val postponedActions: PostponedActionsInterface
		get() = _postponedActions.get()!!
	
//	private val _fileOpener = AtomicReference<FileOpenerInterface>(null)
//	actual val fileOpener: FileOpenerInterface
//		get() = _fileOpener.get()!!

	actual val fileOpener = FileOpener()
	
	
	actual fun resetSql(sql: SQLiteInterface) {
		_sql.set(sql)
	}
	
	actual fun getExceptionStackTrace(e: Throwable): String  {
		val result: Writer = StringWriter()
		val printWriter = PrintWriter(result)
		e.printStackTrace(printWriter)
		printWriter.close()

		return result.toString()
	}
	
	actual fun getNowMillis(): Long = System.currentTimeMillis()
	actual fun getMidnightMillis(timestamp: Long): Long {
		val cal = Calendar.getInstance()
		if(timestamp != -1L)
			cal.timeInMillis = timestamp
		cal[Calendar.HOUR_OF_DAY] = 0
		cal[Calendar.MINUTE] = 0
		cal[Calendar.SECOND] = 0
		cal[Calendar.MILLISECOND] = 0
		return cal.timeInMillis
	}
	actual fun formatShortDate(ms: Long): String {
		val date = Date(ms)
		val formatter: DateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT)
		return formatter.format(date)
	}
	actual fun formatTime(ms: Long): String {
		val date = Date(ms)
		val formatter: DateFormat = SimpleDateFormat.getTimeInstance()
		return formatter.format(date)
	}
	actual fun formatDateTime(ms: Long): String {
		val date = Date(ms)
		val formatter: DateFormat = DateFormat.getDateTimeInstance()
		return formatter.format(date)
	}
	actual fun getTimezone(): String {
		val cal = Calendar.getInstance()
		return cal.timeZone.getDisplayName(true, TimeZone.SHORT)
	}
}