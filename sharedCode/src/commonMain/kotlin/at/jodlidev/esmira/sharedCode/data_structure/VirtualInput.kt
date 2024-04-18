package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

/**
 * Created by SelinaDev
 *
 * A Virtual Input is an input that is part of a questionnaire, but isn't being shown to the user.
 * It functions as a target for calculations within a Merlin Script, i.e., it can be set programmatically.
 * Unlike a study-global variable within a Merlin Script it is saved as part of the same dataset as all other inputs in the questionnaire.
 *
 * Note: This is a stripped down version of the Input.kt class, and duplicates some of its code.
 * At some point it might be necessary/useful to unify both classes, or have one extend the other as base class (as long as this class is a strict subset of the functionality of Input.kt).
 */

@Serializable
class VirtualInput internal constructor( ) {

    var name: String = "virtual_input"

    @Transient private lateinit var _value: String
    @Transient lateinit var  questionnaire: Questionnaire

    internal constructor(name: String, questionnaire: Questionnaire) : this() {
        this.name = name
        this.questionnaire = questionnaire
    }

    fun getValue(): String {
        if (!this::_value.isInitialized) {
            val cache = QuestionnaireCache.loadCacheValue(questionnaire.id, name)
            if (cache != null)
                fromBackupObj(DbLogic.getJsonConfig().decodeFromString(cache))
            else
                _value = ""
        }
        return _value
    }

    fun setValue(value: String) {
        this._value = value
        QuestionnaireCache.saveCacheValue(questionnaire.id, name, getBackupString())
    }

    internal fun fillIntoDataSet(dataSet: DataSet) {
        dataSet.addResponseData(name, getValue())
    }

    private fun getBackupJsonObj(): JsonObject {
        return buildJsonObject {
            put("value", _value)
        }
    }

    private fun getBackupString(): String {
        return getBackupJsonObj().toString()
    }

    private fun fromBackupObj(obj: JsonObject) {
        _value = obj.getValue("value").jsonPrimitive.content
    }
}