package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinParseError
import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinRunner
import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinRuntimeError
import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinScanningError

class MerlinLog: UploadData {
    enum class LogType(val value: Int) {
        UserLog(1),
        ScanningError(2),
        ParseError(3),
        RuntimeError(4),
        None(-1);

        companion object {
            fun valueOf(value: Int) = LogType.values().find { it.value == value }
        }
    }

    override var id: Long = 0
    override var studyWebId: Long = 0
    override val studyId: Long
    override val timestamp: Long
    override val questionnaireName: String
    override val serverUrl: String
    override val serverVersion: Int

    internal val logType: LogType

    override val type: String
        get() = logType.toString()

    var msg: String
    var context: String

    private var _synced = States.NOT_SYNCED
    override var synced: States
        get() {
            return _synced
        }
        set(value) {
            _synced = value
            if(id != 0L) {
                val db = NativeLink.sql
                val values = db.getValueBox()
                values.putInt(KEY_SYNCED, _synced.ordinal)
                db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
            }
        }

    internal constructor(c: SQLiteCursor) {
        id = c.getLong(0)
        studyId = c.getLong(1)
        studyWebId = c.getLong(2)
        serverUrl = c.getString(3)
        serverVersion = c.getInt(4)
        questionnaireName = c.getString(5)
        timestamp = c.getLong(6)
        logType = LogType.valueOf(c.getInt(7)) ?: LogType.None
        msg = c.getString(8)
        context = c.getString(9)
        _synced = States.values()[c.getInt(10)]
    }
    constructor(
        type: LogType,
        study: Study,
        questionnaireName: String,
        msg: String,
        context: String
    ) {
        this.studyId = study.id
        this.studyWebId = study.webId
        this.serverUrl = study.serverUrl
        this.serverVersion = study.serverVersion
        this.questionnaireName = questionnaireName
        this.timestamp = NativeLink.getNowMillis()
        this.logType = type
        this.msg = msg
        this.context = context
    }

    fun getLogString(): String {
        val output = StringBuilder()

        output.appendLine(type)
        output.appendLine()

        //Timestamp:
        output.appendLine(NativeLink.formatDateTime(timestamp))

        //additional data:
        output.append("App: ")
        output.append(NativeLink.smartphoneData.appType)
        output.append(" ")
        output.append(NativeLink.smartphoneData.appVersion)
        output.append("-")
        output.append(DbLogic.getVersion())
        output.append("; Merlin-version: ")
        output.append(MerlinRunner.MERLIN_VERSION)

        //device info:
        output.append("; ")
        output.append(NativeLink.smartphoneData.model)
        output.append(" (")
        output.append(NativeLink.smartphoneData.manufacturer)
        output.append("); OS-version: ")
        output.append(NativeLink.smartphoneData.osVersion)
        output.append('\n')

        //User:
        output.append("User: ")
        output.appendLine(DbUser.getUid())

        //questionnaire:
        output.append("Questionnaire: ")
        output.appendLine(questionnaireName)

        //context:
        output.append("Context: ")
        output.appendLine(context)

        output.appendLine()


        //message:
        output.append(msg)

        return output.toString()
    }

    private fun save() {
        if(id != 0L)
            throw RuntimeException("Trying to save an already created MerlinLog")

        val db = NativeLink.sql
        val values = db.getValueBox()
        values.putLong(KEY_STUDY_ID, studyId)
        values.putLong(KEY_STUDY_WEB_ID, studyWebId)
        values.putString(KEY_SERVER_URL, serverUrl)
        values.putInt(KEY_SERVER_VERSION, serverVersion)
        values.putString(KEY_QUESTIONNAIRE_NAME, questionnaireName)
        values.putLong(KEY_TIMESTAMP, timestamp)
        values.putInt(KEY_TYPE, logType.value)
        values.putString(KEY_MSG, msg)
        values.putString(KEY_CONTEXT, context)
        values.putInt(KEY_SYNCED, _synced.ordinal)
        id = db.insert(TABLE, values)
    }

    override fun delete() {
        NativeLink.sql.delete(TABLE, "$KEY_ID = ?", arrayOf(id.toString()))
    }

    companion object {
        const val TABLE = "merlinLogs"

        const val KEY_ID = UploadData.KEY_ID
        const val KEY_STUDY_ID = UploadData.KEY_STUDY_ID
        const val KEY_STUDY_WEB_ID = UploadData.KEY_STUDY_WEB_ID
        const val KEY_SERVER_URL = UploadData.KEY_SERVER_URL
        const val KEY_SERVER_VERSION = UploadData.KEY_SERVER_VERSION
        const val KEY_QUESTIONNAIRE_NAME = "questionnaire_name"
        const val KEY_TIMESTAMP = "time_ms"
        const val KEY_TYPE = "log_type"
        const val KEY_MSG = "msg"
        const val KEY_CONTEXT = "context"
        const val KEY_SYNCED = UploadData.KEY_SYNCED

        val COLUMNS = arrayOf(
            "$TABLE.$KEY_ID",
            "$TABLE.$KEY_STUDY_ID",
            "$TABLE.$KEY_STUDY_WEB_ID",
            "$TABLE.$KEY_SERVER_URL",
            "$TABLE.$KEY_SERVER_VERSION",
            "$TABLE.$KEY_QUESTIONNAIRE_NAME",
            "$TABLE.$KEY_TIMESTAMP",
            "$TABLE.$KEY_TYPE",
            "$TABLE.$KEY_MSG",
            "$TABLE.$KEY_CONTEXT",
            "$TABLE.$KEY_SYNCED"
        )

        private fun makeLog(type: LogType, questionnaire: Questionnaire?, context: String, msg: String) {
            if (questionnaire == null) {
                ErrorBox.error("MerlinLogger", "Questionnaire is null.")
                return
            }
            val studyId = questionnaire.studyId
            val study = DbLogic.getStudy(studyId)
            if (study == null) {
                ErrorBox.error("MerlinLogger", "Study could not be retrieved.")
                return
            }
            MerlinLog(
                type,
                study,
                questionnaire.title,
                msg,
                context
            ).save()

        }

        fun logScanningError(questionnaire: Questionnaire?, context: String, error: MerlinScanningError) {
            makeLog(LogType.ScanningError, questionnaire, context, error.getFormattedError())
        }

        fun logParseError(questionnaire: Questionnaire?, context: String, error: MerlinParseError) {
            makeLog(LogType.ParseError, questionnaire, context, error.getFormattedError())
        }

        fun logRuntimeError(questionnaire: Questionnaire?, context: String, error: MerlinRuntimeError, environmentString: String) {
            makeLog(LogType.RuntimeError, questionnaire, context, error.getFormattedError() + "\n" + environmentString)
        }

        fun logUserLog(questionnaire: Questionnaire?, context: String, logMsg: String) {
            makeLog(LogType.UserLog, questionnaire, context, logMsg)
        }
    }
}

