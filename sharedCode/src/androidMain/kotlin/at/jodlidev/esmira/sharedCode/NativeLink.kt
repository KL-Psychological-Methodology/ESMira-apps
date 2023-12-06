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
	) {
		println("Initializing Android NativeLink")
		if(_sql.get() == null)
			_sql.set(sql)
		_smartphoneData.set(smartphoneData)
		_dialogOpener.set(dialogOpener)
		_notifications.set(notifications)
		_postponedActions.set(postponedActions)
		
		_isInitialized.set(true)
	}
	
	private val _isInitialized = AtomicReference<Boolean>(false)
	actual val isInitialized
		get() = _isInitialized.get()
	
	private val _isSynchronizing = AtomicReference<Boolean>(false)
	actual var isSynchronizing: Boolean
		get() = _isSynchronizing.get()
		set(value) = _isSynchronizing.set(value)
	
	private val _isUpdating = AtomicReference<Boolean>(false)
	actual var isUpdating: Boolean
		get() = _isUpdating.get()
		set(value) = _isUpdating.set(value)
	
	private val _sql = AtomicReference<SQLiteInterface?>(null)
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
	actual fun formatDate(ms: Long): String {
		val date = Date(ms)
		val formatter: DateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT)
		return formatter.format(date)
	}
	actual fun formatTime(ms: Long): String {
		val date = Date(ms)
		val formatter: DateFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
		return formatter.format(date)
	}
	actual fun formatDateTime(ms: Long): String {
		val date = Date(ms)
		val formatter: DateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
//		val formatter: DateFormat = DateFormat.getDateTimeInstance()
		return formatter.format(date)
	}
	actual fun getTimezone(): String {
		val cal = Calendar.getInstance()
		return cal.timeZone.getDisplayName(true, TimeZone.SHORT)
	}
}