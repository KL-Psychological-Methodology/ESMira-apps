package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.SQLiteInterface

/**
 * Created by SelinaDev on 31.10.2020 🎃
 */


class QuestionnaireMetadata (var studyId: Long = -1, var questionnaireInternalId: Long = -1) {

    var id: Long = 0
    var timesCompleted: Int = 0

    internal constructor(c: SQLiteCursor) : this() {
        id = c.getLong(0)
        studyId = c.getLong(1)
        questionnaireInternalId = c.getLong(2)
        timesCompleted = c.getInt(3)
    }

    fun save(db: SQLiteInterface = NativeLink.sql) {
        val values = db.getValueBox()
        values.putLong(KEY_STUDY_ID, studyId)
        values.putLong(KEY_QUESTIONNAIRE_ID, questionnaireInternalId)
        values.putInt(KEY_TIMES_COMPLETED, timesCompleted)

        if(!questionnaireMetadataExists()) {
            db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
        } else {
            id = db.insert(TABLE, values)
        }
    }

    private fun questionnaireMetadataExists(): Boolean {
        return Companion.questionnaireMetadataExists(studyId, questionnaireInternalId)
    }

    companion object {
        const val TABLE = "questionnaire_metadata"
        const val KEY_ID = "_id"
        const val KEY_STUDY_ID = "study_id"
        const val KEY_QUESTIONNAIRE_ID = "questionnaire_id"
        const val KEY_TIMES_COMPLETED = "times_completed"

        val COLUMNS = arrayOf(
            KEY_ID,
            KEY_STUDY_ID,
            KEY_QUESTIONNAIRE_ID,
            KEY_TIMES_COMPLETED,
        )

        private fun questionnaireMetadataExists(
            studyId: Long,
            questionnaireInternalId: Long
        ): Boolean {
            val c = NativeLink.sql.select(
                TABLE,
                arrayOf(),
                "$KEY_STUDY_ID = ? AND $KEY_QUESTIONNAIRE_ID = ?",
                arrayOf(studyId.toString(), questionnaireInternalId.toString()),
                null,
                null,
                null,
                "1"
            )
            return c.moveToFirst()
        }
    }
}