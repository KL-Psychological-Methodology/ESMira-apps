package at.jodlidev.esmira.sharedCode

import io.ktor.util.date.GMTDate
import io.ktor.util.date.plus

/**
 * Created by JodliDev on 31.08.2020.
 */
class DueDateFormatter(
	private val soonString: String,
	private val todayString: String,
	private val tomorrowString: String,
	private val inXDaysString: String
) {
	private val now: Long = NativeLink.getNowMillis()
	private val soon: Long = now + SOON_MS
	private val endOfToday: Long = NativeLink.getMidnightMillis() + ONE_DAY
	private val endOfTomorrow: Long = endOfToday + ONE_DAY
	
	private var getExact: Boolean = false
	
	constructor(): this("", "", "", "") {
		getExact = true
	}
	
	fun get(timestamp: Long): String {
		return when {
			getExact -> {
				NativeLink.formatDateTime(timestamp)
			}
			timestamp < soon ->
				soonString
			timestamp < endOfToday ->
				todayString
			timestamp < endOfTomorrow ->
				tomorrowString
			else ->
				inXDaysString.replace("%1\$d", ((timestamp - now) / ONE_DAY).toString())
		}
	}
	
	companion object {
		private const val SOON_MS: Long = 1000 * 60
		private const val ONE_DAY: Long = 1000 * 60 * 60 * 24
	}
}