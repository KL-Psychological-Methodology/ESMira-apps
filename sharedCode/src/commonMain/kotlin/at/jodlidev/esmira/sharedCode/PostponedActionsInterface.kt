package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.Alarm

/**
 * Created by JodliDev on 18.05.2020.
 */
interface PostponedActionsInterface {
	fun scheduleAlarm(alarm: Alarm): Boolean
	fun cancel(alarm: Alarm)
	fun syncDataSets()
	fun updateStudiesRegularly()
	fun cancelUpdateStudiesRegularly()
}