package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor

/**
 * Created by JodliDev on 23.02.2021.
 */
class Message {
	
	var id: Long = -1
	var studyId: Long = -1
	var content: String
	var sent: Long
	var isNew = false
	var fromServer = false
	
	internal constructor(studyId: Long, content: String, sent: Long) {
		this.studyId = studyId
		this.content = content
		this.sent = sent
	}
	internal constructor(c: SQLiteCursor) {
		id = c.getLong(0)
		studyId = c.getLong(1)
		content = c.getString(2)
		sent = c.getLong(3)
		isNew = c.getBoolean(4)
		fromServer = c.getBoolean(5)
	}
	
	fun save() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putLong(KEY_STUDY_ID, studyId)
		values.putString(KEY_CONTENT, content)
		values.putLong(KEY_SENT, sent)
		values.putBoolean(KEY_IS_NEW, isNew)
		values.putBoolean(KEY_FROM_SERVER, fromServer)
		
		id = db.insert(TABLE, values)
	}
	
	fun markAsRead() {
		val db = NativeLink.sql
		val values = db.getValueBox()
		values.putBoolean(KEY_IS_NEW, false)
		isNew = false
		db.update(TABLE, values, "${KEY_ID} = ?", arrayOf(id.toString()))
	}
	
	companion object {
		const val TABLE = "messages"
		const val KEY_ID = "_id"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_CONTENT = "content"
		const val KEY_SENT = "sentTimestamp"
		const val KEY_IS_NEW = "is_new"
		const val KEY_FROM_SERVER = "fromServer"
		
		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_STUDY_ID,
			KEY_CONTENT,
			KEY_SENT,
			KEY_IS_NEW,
			KEY_FROM_SERVER
		)
		
		fun addMessage(studyId: Long, content: String, sent: Long, fromServer: Boolean = false) {
			val msg = Message(studyId, content, sent)
			if(fromServer) {
				msg.fromServer = true
				msg.isNew = true
			}
			msg.save()
		}
	}
}