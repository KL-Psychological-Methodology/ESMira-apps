package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.SQLiteInterface

// Only used for ScreenTrackingReceiver on Android
// I.e., for AppUsageItem without package id, for devices before Android 10

class ScreenTrackingSession {
    var id: Long = 0L
    var sessionStart = 0L
    var sessionEnd = 0L

    constructor(start: Long, end: Long) {
        sessionStart = start
        sessionEnd = end
    }

    internal constructor(c: SQLiteCursor) {
        id = c.getLong(0)
        sessionStart = c.getLong(1)
        sessionEnd = c.getLong(2)
    }

    fun save(db: SQLiteInterface = NativeLink.sql) {
        val values = db.getValueBox()

        values.putLong(KEY_SESSION_START, sessionStart)
        values.putLong(KEY_SESSION_END, sessionEnd)

        id = db.insert(TABLE, values)
    }

    companion object {
        const val TABLE = "screen_tracking_sessions"
        const val KEY_ID = "_id"
        const val KEY_SESSION_START = "session_start"
        const val KEY_SESSION_END = "session_end"
        private const val ONE_DAY = 1000 * 60 * 60 * 24

        val COLUMNS = arrayOf(
            KEY_ID,
            KEY_SESSION_START,
            KEY_SESSION_END
        )

        fun store(start: Long, end: Long){
            ScreenTrackingSession(start, end).save()
        }

        fun removeBefore(timestamp: Long, db: SQLiteInterface = NativeLink.sql) {
            db.delete(TABLE, "$KEY_SESSION_END < ?", arrayOf(timestamp.toString()))
        }

        private fun getSessions(from: Long, to: Long, db: SQLiteInterface = NativeLink.sql): List<Pair<Long, Long>> {
            val c = db.select(
                TABLE,
                arrayOf(KEY_SESSION_START, KEY_SESSION_END),
                "$KEY_SESSION_START <= ? AND $KEY_SESSION_END >= ?",
                arrayOf(to.toString(), from.toString()),
                null,
                null,
                null,
                null
            )
            val sessions = ArrayList<Pair<Long, Long>>()
            while(c.moveToNext()) {
                sessions.add(Pair(c.getLong(0), c.getLong(1)))
            }
            return sessions
        }

        fun getToday(): List<Pair<Long, Long>> {
            val midnightMillis = NativeLink.getMidnightMillis()
            return getSessions(midnightMillis, NativeLink.getNowMillis())
        }

        fun getYesterday(): List<Pair<Long, Long>> {
            val midnightMillis = NativeLink.getMidnightMillis()
            return getSessions(midnightMillis - ONE_DAY, midnightMillis)
        }
    }
}