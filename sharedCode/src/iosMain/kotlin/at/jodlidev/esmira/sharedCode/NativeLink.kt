package at.jodlidev.esmira.sharedCode

import kotlin.native.concurrent.AtomicReference

/**
 * Created by JodliDev on 03.06.2020.
 */
actual object NativeLink {
	fun init(
		sql: SQLiteInterface,
		smartphoneData: SmartphoneDataInterface,
		dialogOpener: DialogOpenerInterface,
		notifications: NotificationsInterface,
		postponedActions: PostponedActionsInterface,
		iosCode: IosCodeInterface
	) {
		println("Initializing iOS NativeLink")
		if(_sql.value == null)
			_sql.value = sql
		_smartphoneData.value = smartphoneData
		_dialogOpener.value = dialogOpener
		_notifications.value = notifications
		_postponedActions.value = postponedActions
		_iosCode.value = iosCode
		
		_isInitialized.value = true
	}
	
	private val _isInitialized = AtomicReference<Boolean>(false)
	actual val isInitialized
		get() = _isInitialized.value
	
	private val _isSynchronizing = AtomicReference<Boolean>(false)
	actual var isSynchronizing: Boolean
		get() = _isSynchronizing.value
		set(value) { _isSynchronizing.value = value }
	
	private val _sql = AtomicReference<SQLiteInterface?>(null)
	actual val sql: SQLiteInterface
		get() = _sql.value!!
	
	private val _smartphoneData = AtomicReference<SmartphoneDataInterface?>(null)
	actual val smartphoneData: SmartphoneDataInterface
		get() = _smartphoneData.value!!
	
	private val _dialogOpener = AtomicReference<DialogOpenerInterface?>(null)
	actual val dialogOpener: DialogOpenerInterface
		get() = _dialogOpener.value!!
	
	private val _notifications = AtomicReference<NotificationsInterface?>(null)
	actual val notifications: NotificationsInterface
		get() = _notifications.value!!
	
	private val _postponedActions = AtomicReference<PostponedActionsInterface?>(null)
	actual val postponedActions: PostponedActionsInterface
		get() = _postponedActions.value!!

	actual fun getExceptionStackTrace(e: Throwable): String {
		val result = StringBuilder("${e.cause}: ${e.message}")
		for(line: String in e.getStackTrace()) {
			result.append("\n\tat ")
			result.append(line)
		}
		
		return result.toString()
	}
	
	fun setSQL(sql: SQLiteInterface) {
		if(_sql.value == null)
			_sql.value = sql
	}
	actual fun resetSql(sql: SQLiteInterface) {
		_sql.value = sql
	}
	
	private val _iosCode = AtomicReference<IosCodeInterface?>(null)
	private val iosCode: IosCodeInterface
		get() = _iosCode.value!!
	
	actual fun getNowMillis(): Long {
		return iosCode.currentTimeMillis()
	}
	
	actual fun getTimezone(): String {
		return iosCode.getTimezone()
	}
	
	actual fun getMidnightMillis(timestamp: Long): Long {
		return iosCode.getMidnightMillis(timestamp)
	}
	
	actual fun formatDate(ms: Long): String {
		return iosCode.formatDate(ms)
	}
	
	actual fun formatTime(ms: Long): String {
		return iosCode.formatTime(ms)
	}
	
	actual fun formatDateTime(ms: Long): String {
		return iosCode.formatDateTime(ms)
	}
}