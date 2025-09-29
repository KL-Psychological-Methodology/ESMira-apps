package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.*
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 29.05.2019.
 */
@Serializable
class EventTrigger {
	var cueCode: DataSet.EventTypes = DataSet.EventTypes.joined
	var randomDelay = false
	var delaySec = 0
	var delayMinimumSec = 0
	var skipThisQuestionnaire = false
	
	@Transient var exists = false
	@Transient var id: Long = 0
	@Transient var studyId: Long = -1
	@Transient var questionnaireId: Long = -1
	@Transient var actionTriggerId: Long = -1
	@Transient lateinit var actionString : String //from ActionTrigger

	internal var specificQuestionnaireInternalId: Long = -1
	
	internal constructor(actionTrigger: ActionTrigger, c: SQLiteCursor) {
		getMinimalCursor(c)
		actionTriggerId = actionTrigger.id
		studyId = actionTrigger.studyId
		questionnaireId = actionTrigger.questionnaire.id
		actionString = actionTrigger.actionsString
	}
	
	internal constructor(c: SQLiteCursor) {
		getMinimalCursor(c)
		actionTriggerId = c.getLong(7)
		studyId = c.getLong(8)
		questionnaireId = c.getLong(9)
		actionString = c.getString(10)
	}
	fun bindParent(questionnaire: Questionnaire, actionTrigger: ActionTrigger) {
		actionTriggerId = actionTrigger.id
		studyId = questionnaire.studyId
		questionnaireId = questionnaire.id
		actionString = actionTrigger.actionsString
	}
	
	private fun getMinimalCursor(c: SQLiteCursor) {
		id = c.getLong(0)
		cueCode = DataSet.EventTypes.valueOf(c.getString(1))
		randomDelay = c.getBoolean(2)
		delaySec = c.getInt(3)
		delayMinimumSec = c.getInt(4)
		skipThisQuestionnaire = c.getBoolean(5)
		specificQuestionnaireInternalId = c.getLong(6)
	}
	
	fun triggerCheck(questionnaire: Questionnaire?) {
		val noCondition = !skipThisQuestionnaire && specificQuestionnaireInternalId == -1L
		val skipThisQuestionnaire = skipThisQuestionnaire && questionnaireId == questionnaire?.id
		val specificQuestionnaire = specificQuestionnaireInternalId != -1L && questionnaire?.internalId == specificQuestionnaireInternalId && studyId == questionnaire.studyId


		if(noCondition || skipThisQuestionnaire || specificQuestionnaire) {
			if(delaySec == 0) {
				if(questionnaire?.isActive() == true) {
					exec(NativeLink.getNowMillis())
				}
			} else
				Scheduler.scheduleEventTrigger(this)
		}
	}
	
	fun exec(timestamp: Long, fireNotifications: Boolean = true) {
		exec(DbLogic.getActionTrigger(actionTriggerId)!!, timestamp, fireNotifications)
	}
	
	fun exec(alarm: Alarm, fireNotifications: Boolean = true) {
		exec(alarm.actionTrigger, alarm.timestamp, fireNotifications)
	}
	
	private fun exec(actionTrigger: ActionTrigger, timestamp: Long, fireNotifications: Boolean = true) {
		actionTrigger.execActions(timestamp, fireNotifications)
	}
	
	fun save(db: SQLiteInterface = NativeLink.sql) {
		val values = db.getValueBox()
		values.putString(KEY_CUE, cueCode.toString())
		values.putBoolean(KEY_RANDOM_DELAY, randomDelay)
		values.putInt(KEY_DELAY, delaySec)
		values.putInt(KEY_DELAY_MIN, delayMinimumSec)
		values.putBoolean(KEY_SKIP_THIS_QUESTIONNAIRE, skipThisQuestionnaire)
		values.putLong(KEY_SPECIFIC_QUESTIONNAIRE, specificQuestionnaireInternalId)
		values.putLong(KEY_ACTION_TRIGGER_ID, actionTriggerId)
		values.putLong(KEY_STUDY_ID, studyId)
		values.putLong(KEY_QUESTIONNAIRE_ID, questionnaireId)

		if(exists)
			db.update(TABLE, values, "$KEY_ID = ?", arrayOf(id.toString()))
		else
			id = db.insert(TABLE, values)
	}
	
	fun delete(db: SQLiteInterface = NativeLink.sql) {
		Scheduler.remove(this)
		db.delete(TABLE, "$KEY_ID = ?", arrayOf(id.toString()))
	}


	companion object {
		const val TABLE = "eventTriggers"
		const val KEY_ID = "_id"
		const val KEY_ACTION_TRIGGER_ID = "action_trigger"
		const val KEY_STUDY_ID = "study_id"
		const val KEY_QUESTIONNAIRE_ID = "questionnaire_id"
		const val KEY_LABEL = "label"
		const val KEY_CUE = "cue_code"
		const val KEY_RANDOM_DELAY = "random_delay"
		const val KEY_DELAY = "trigger_delay"
		const val KEY_DELAY_MIN = "trigger_delay_min"
		const val KEY_SKIP_THIS_QUESTIONNAIRE = "skip_this_questionnaire"
		const val KEY_SPECIFIC_QUESTIONNAIRE = "specific_questionnaire_internalId"
		const val EXT_KEY_ID = "$TABLE.$KEY_ID"
		const val EXT_KEY_CUE = "$TABLE.$KEY_CUE"
		const val EXT_KEY_STUDY_ID = "$TABLE.$KEY_STUDY_ID"

		val COLUMNS = arrayOf(
			KEY_ID,
			KEY_CUE,
			KEY_RANDOM_DELAY,
			KEY_DELAY,
			KEY_DELAY_MIN,
			KEY_SKIP_THIS_QUESTIONNAIRE,
			KEY_SPECIFIC_QUESTIONNAIRE
		)
		val COLUMNS_JOINED = arrayOf(
			EXT_KEY_ID,
			"$TABLE.$KEY_CUE",
			"$TABLE.$KEY_RANDOM_DELAY",
			"$TABLE.$KEY_DELAY",
			"$TABLE.$KEY_DELAY_MIN",
			"$TABLE.$KEY_SKIP_THIS_QUESTIONNAIRE",
			"$TABLE.$KEY_SPECIFIC_QUESTIONNAIRE",
			"$TABLE.$KEY_ACTION_TRIGGER_ID",
			"$TABLE.$KEY_STUDY_ID",
			"$TABLE.$KEY_QUESTIONNAIRE_ID",
			ActionTrigger.TABLE + "." + ActionTrigger.KEY_ACTIONS
		)
		const val TABLE_JOINED = "$TABLE LEFT JOIN ${ActionTrigger.TABLE} ON $TABLE.$KEY_ACTION_TRIGGER_ID=${ActionTrigger.TABLE}.${ActionTrigger.KEY_ID}"
	}
}