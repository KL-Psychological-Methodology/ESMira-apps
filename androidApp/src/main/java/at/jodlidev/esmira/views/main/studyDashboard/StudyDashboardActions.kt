package at.jodlidev.esmira.views.main.studyDashboard

import at.jodlidev.esmira.sharedCode.data_structure.Alarm
import at.jodlidev.esmira.sharedCode.data_structure.Study

/**
 * Created by JodliDev on 20.02.2023.
 */
data class StudyDashboardActions (
	val getStudy: () -> Study,
	val getStudyList: () -> List<Study> = { ArrayList() },
	val reloadStudy: () -> Unit = {},
	val switchStudy: (Long) -> Unit = {},
	val getCompletedQuestionnaireCount: () -> Int = { 0 },
	val hasPinnedQuestionnaires: () -> Boolean = { false },
	val countActivePinnedQuestionnaires: () -> Int = { 0 },
	val hasRepeatingQuestionnaires: () -> Boolean = { false },
	val countActiveRepeatingQuestionnaires: () -> Int = { 0 },
	val hasOneTimeQuestionnaires: () -> Boolean = { false },
	val countActiveOneTimeQuestionnaires: () -> Int = { 0 },
	val hasUnSyncedDataSets: () -> Boolean = { false },
	val countUnreadMessages: () -> Int = { 0 },
	val getNextAlarm: () -> Alarm? = { null }
)