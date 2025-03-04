package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 03.06.2020.
 */
expect object NativeLink {
	fun getExceptionStackTrace(e: Throwable): String
	
	fun resetSql(sql: SQLiteInterface)
	
	val isInitialized: Boolean
	var isSynchronizing: Boolean
	var isUpdating: Boolean
	
	val sql: SQLiteInterface
	val smartphoneData: SmartphoneDataInterface
	val dialogOpener: DialogOpenerInterface
	val notifications: NotificationsInterface
	val postponedActions: PostponedActionsInterface
	
	fun getNowMillis(): Long
	fun getTimezone(): String
	fun getTimezoneOffsetMillis(): Int
	fun getMidnightMillis(timestamp: Long = -1L): Long
	fun formatDate(ms: Long): String
	fun formatTime(ms: Long): String
	fun formatDateTime(ms: Long): String
	fun getDatesDiff(ms1: Long, ms2: Long): Long
}