package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 03.06.2020.
 */
expect object NativeLink {
	fun getExceptionStackTrace(e: Throwable): String
	
	fun resetSql(sql: SQLiteInterface)
	
	val isInitialized: Boolean
	
	val sql: SQLiteInterface
	val smartphoneData: SmartphoneDataInterface
	val dialogOpener: DialogOpenerInterface
	val notifications: NotificationsInterface
	val postponedActions: PostponedActionsInterface
	
	fun getNowMillis(): Long
	fun getTimezone(): String
	fun getMidnightMillis(timestamp: Long = -1L): Long
	fun formatDate(ms: Long): String
	fun formatTime(ms: Long): String
	fun formatDateTime(ms: Long): String
}