package mock

import at.jodlidev.esmira.sharedCode.NotificationsInterface
import at.jodlidev.esmira.sharedCode.data_structure.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockNotifications: NotificationsInterface {
	val firePostponedList = ArrayList<Alarm>()
	var fireCount = 0
	val fireSchedulesChangedList = ArrayList<Study>()
	val fireQuestionnaireBingList = ArrayList<Questionnaire>()
	val fireStudyNotificationList = ArrayList<Questionnaire>()
	val fireMessageNotificationList = ArrayList<Study>()
	val removeQuestionnaireBingList = ArrayList<Questionnaire>()
	val removeList = ArrayList<Int>()
	
	override fun firePostponed(alarm: Alarm, msg: String, subId: Int) {
		firePostponedList.add(alarm)
	}
	
	override fun fire(title: String, msg: String, id: Int) {
		++fireCount
	}
	
	override fun fireSchedulesChanged(study: Study) {
		fireSchedulesChangedList.add(study)
	}
	
	override fun fireQuestionnaireBing(
		title: String,
		msg: String,
		questionnaire: Questionnaire,
		timeoutMin: Int,
		type: DataSet.EventTypes,
		scheduledToTimestamp: Long
	) {
		fireQuestionnaireBingList.add(questionnaire)
	}
	
	override fun fireStudyNotification(
		title: String,
		msg: String,
		questionnaire: Questionnaire,
		scheduledToTimestamp: Long
	) {
		fireStudyNotificationList.add(questionnaire)
	}
	
	override fun fireMessageNotification(study: Study) {
		fireMessageNotificationList.add(study)
	}
	
	override fun removeQuestionnaireBing(questionnaire: Questionnaire) {
		removeQuestionnaireBingList.add(questionnaire)
	}
	
	override fun remove(id: Int) {
		removeList.add(id)
	}
	
	fun reset() {
		firePostponedList.clear()
		fireCount = 0
		fireSchedulesChangedList.clear()
		fireQuestionnaireBingList.clear()
		fireStudyNotificationList.clear()
		fireMessageNotificationList.clear()
		removeQuestionnaireBingList.clear()
		removeList.clear()
		
	}
}