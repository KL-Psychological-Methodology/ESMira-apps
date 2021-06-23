package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 03.06.2020.
 */
interface IosCodeInterface {
	fun currentTimeMillis(): Long
	fun getTimezone(): String
	fun getMidnightMillis(timestamp: Long): Long
	fun formatShortDate(ms: Long): String
	fun formatTime(ms: Long): String
	fun formatDateTime(ms: Long): String
}