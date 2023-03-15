package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.*

/**
 * Created by JodliDev on 18.05.2020.
 */
interface NotificationsInterface {
	fun firePostponed(alarm: Alarm, msg: String, subId: Int = -1) //only used by IOS
	
	fun fire(title: String, msg: String, id: Int)
	fun fireSchedulesChanged(study: Study)
	fun fireQuestionnaireBing(title: String, msg: String, questionnaire: Questionnaire, timeoutMin: Int, type: DataSet.EventTypes, scheduledToTimestamp: Long)
	fun fireStudyNotification(title: String, msg: String, questionnaire: Questionnaire, scheduledToTimestamp: Long)
	
	fun fireMessageNotification(study: Study)
	
	fun removeQuestionnaireBing(questionnaire: Questionnaire)
	fun remove(id: Int)
}