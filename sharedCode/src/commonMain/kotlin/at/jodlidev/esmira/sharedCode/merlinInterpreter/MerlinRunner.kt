package at.jodlidev.esmira.sharedCode.merlinInterpreter

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.Updater
import at.jodlidev.esmira.sharedCode.data_structure.MerlinLog
import at.jodlidev.esmira.sharedCode.data_structure.Questionnaire
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*
 * Created by SelinaDev
 *
 * This class encapsulates the MerlinInterpreter and everything it depends on.
 * Interpretation works like this: SourceCode -> Scanner -> Tokens -> Parser -> AST -> Interpreter
 * The Runner abstracts these intermediate steps and is able to manage the errors that can occur in each step.
 */

object MerlinRunner {
    const val MERLIN_VERSION = Updater.MERLIN_VERSION

    const val TABLE = "merlinCache"
    const val KEY_ID = "_id"
    const val KEY_STUDY_ID = "studyId"
    const val KEY_GLOBALS_STRING = "globalsString"

    private var cachedGlobals: Pair<Long, MerlinObject>? = null
    private val interpreter = MerlinInterpreter()

    fun run(source: String, questionnaire: Questionnaire?): MerlinType? {
        // Scanning
        val scanner = MerlinScanner(source)
        val tokens: List<MerlinToken>
        try {
            tokens = scanner.scanTokens()
        } catch (e: MerlinScanningError) {
            MerlinLog.logScanningError(questionnaire, e)
            return null
        }

        // Parsing
        val parser = MerlinParser(tokens)
        val statements: List<MerlinStmt>
        try {
            statements = parser.parse()
        } catch (e: MerlinParseError) {
            MerlinLog.logParseError(questionnaire,e)
            return null
        }

        // Get globals
        val studyId = questionnaire?.studyId ?: -1L
        val globals: MerlinObject
        if (studyId == -1L)
            globals = MerlinObject()
        else if (cachedGlobals != null && studyId == cachedGlobals!!.first)
            globals = cachedGlobals!!.second
        else {
            globals = retrieveGlobals(questionnaire)
            cachedGlobals = Pair(studyId, globals)
        }

        // Interpreting
        interpreter.initialize(questionnaire, globals)
        var returnedValue: MerlinType? = null
        try {
            returnedValue = interpreter.interpret(statements)
        } catch (e: MerlinRuntimeError) {
            MerlinLog.logRuntimeError(questionnaire, e, interpreter.getEnvironmentString())
        } finally {
            val storedGlobals = interpreter.getGlobalsObject()?: MerlinObject()
            cachedGlobals = Pair(studyId, storedGlobals)
            saveGlobals(storedGlobals, questionnaire)
            interpreter.cleanup()
        }
        return returnedValue
    }

    fun runForBool(source: String, questionnaire: Questionnaire?, default: Boolean): Boolean {
        return run(source, questionnaire)?.isTruthy() ?: default
    }

    fun runForString(source: String, questionnaire: Questionnaire?): String {
        return run(source, questionnaire)?.stringify() ?: ""
    }

    private fun saveGlobals(obj: MerlinObject, questionnaire: Questionnaire?) {
        val studyId = questionnaire?.studyId ?: return
        val db = NativeLink.sql
        val values = db.getValueBox()
        values.putLong(KEY_STUDY_ID, studyId)
        values.putString(KEY_GLOBALS_STRING, Json.encodeToString(obj))
        db.update(TABLE, values, "$KEY_STUDY_ID=?", arrayOf(studyId.toString()))
    }
    private fun retrieveGlobals(questionnaire: Questionnaire?): MerlinObject {
        val db = NativeLink.sql
        val studyId = questionnaire?.studyId ?: return MerlinObject()
        val c = db.select(
            TABLE,
            arrayOf(KEY_GLOBALS_STRING),
            "$KEY_STUDY_ID = ?", arrayOf(studyId.toString()),
            null,
            null,
            null,
            "1"
        )

        var r: String? = null
        if (c.moveToFirst()) {
            r = c.getString(0)
        } else {
            initializeGlobals(studyId)
        }
        c.close()
        return r?.let { Json.decodeFromString<MerlinObject>(it) } ?: MerlinObject()
    }

    private fun initializeGlobals(studyId: Long) {
        val db = NativeLink.sql
        val values = db.getValueBox()
        values.putLong(KEY_STUDY_ID, studyId)
        values.putString(KEY_GLOBALS_STRING, Json.encodeToString(MerlinObject()))
        db.insert(TABLE, values)
    }

    fun clearGlobals(studyId: Long) {
        val db = NativeLink.sql
        db.delete(TABLE, "${KEY_STUDY_ID} = ?", arrayOf(studyId.toString()))
        cachedGlobals = null
    }
}