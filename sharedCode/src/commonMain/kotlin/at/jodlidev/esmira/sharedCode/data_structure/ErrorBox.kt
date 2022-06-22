package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor

/**
 * Created by JodliDev on 11.04.2019.
 */
class ErrorBox {
	var id: Long = 0
	var timestamp: Long
	var severity: Int
	var title: String
	var msg: String
	
	internal constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		timestamp = c.getLong(1)
		severity = c.getInt(2)
		title = c.getString(3)
		msg = c.getString(4)
	}
	
	private constructor(title: String, severity: Int, msg: String) {
		timestamp = NativeLink.getNowMillis()
		this.severity = severity
		this.title = if(title.length > MAX_TITLE_SIZE) title.substring(0, MAX_TITLE_SIZE) else title
		this.msg = if(msg.length > MAX_MSG_SIZE) msg.substring(0, MAX_MSG_SIZE) else msg
	}
	
	private fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_TIMESTAMP, timestamp)
		values.putInt(KEY_SEVERITY, severity)
		values.putString(KEY_TITLE, title)
		values.putString(KEY_MSG, msg)
		id = db.insert(TABLE, values)
	}
	
	fun export(): String {
		val severityString: String = when(severity) {
			SEVERITY_ERROR -> "Error"
			SEVERITY_WARN -> "Warning"
			SEVERITY_LOG -> "Log"
			else -> "Unknown"
		}
		return "$severityString: $id.$title - ${getFormattedDateTime()} (Timestamp: $timestamp)\n$msg"
	}
	
	fun getFormattedDateTime(): String {
		return NativeLink.formatDateTime(timestamp)
	}
	
	companion object {
		const val TABLE = "error_box"
		const val KEY_ID = "_id"
		const val KEY_TIMESTAMP = "time_ms"
		const val KEY_SEVERITY = "severity"
		const val KEY_TITLE = "title"
		const val KEY_MSG = "msg"
		const val KEY_REVIEWED = "reviewed"

		val COLUMNS = arrayOf(
				KEY_ID,
				KEY_TIMESTAMP,
				KEY_SEVERITY,
				KEY_TITLE,
				KEY_MSG
		)

		const val SEVERITY_LOG: Int = 1
		const val SEVERITY_WARN: Int = 2
		const val SEVERITY_ERROR: Int = 3
		
		const val MAX_SAVED_ERRORS = 800
		private const val MAX_TITLE_SIZE = 1000
		private const val MAX_MSG_SIZE = 5000
		
		fun getReportHeader(comment: String?): StringBuilder {
			val output = StringBuilder()
			
			//additional data:
			output.append("App: ")
			output.append(NativeLink.smartphoneData.appType)
			output.append(" ")
			output.append(NativeLink.smartphoneData.appVersion)
			output.append("-")
			output.append(DbLogic.getVersion())

			//device info:
			output.append("; ")
			output.append(NativeLink.smartphoneData.model)
			output.append(" (")
			output.append(NativeLink.smartphoneData.manufacturer)
			output.append("); OS-version: ")
			output.append(NativeLink.smartphoneData.osVersion)
			output.append('\n')
			
			
			//studies:
			for(study in DbLogic.getAllStudies()) {
				output.append(study.title)
				output.append("; v")
				output.append(study.version)
				output.append(".")
				output.append(study.subVersion)
				output.append(" (id=")
				output.append(study.webId)
				output.append(", state=")
				output.append(study.state.name)
				output.append(", joined at=")
				output.append(study.joined)
				output.append(", accessKey=")
				output.append(study.accessKey)
				output.append("): ")
				output.append(study.serverUrl)
				output.append('\n')
			}
			
			//User:
			output.append("User: ")
			output.append(DbLogic.getUid())
			
			//Comment:
			if(comment != null) {
				output.append("\nComment: ")
				output.append(comment)
			}
			
			
			return output
		}
		
		private fun exceptionToString(e: Throwable): String {
			return NativeLink.getExceptionStackTrace(e)
		}
		
		fun log(title: String, msg: String) {
			add(title, SEVERITY_LOG, msg)
		}
		
		fun warn(title: String, msg: String) {
			add(title, SEVERITY_WARN, msg)
		}
		
		fun warn(title: String, msg: String, e: Throwable) {
			warn(title, "$msg\n${exceptionToString(e)}")
		}
		
		fun error(title: String, msg: String) {
			add(title, SEVERITY_ERROR, msg)
			NativeLink.dialogOpener.errorReport()
		}
		
		fun error(title: String, msg: String, e: Throwable) {
			error(title, "$msg\n${exceptionToString(e)}")
		}
		
		private fun add(title: String, severity: Int, msg: String) {
			val error = ErrorBox("$title (${NativeLink.smartphoneData.appVersion})", severity, msg)
			println("${error.timestamp}: $msg")
			error.save()
		}
	}
}