package mock

import at.jodlidev.esmira.sharedCode.PostponedActionsInterface
import at.jodlidev.esmira.sharedCode.data_structure.Alarm

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockPostponedActions: PostponedActionsInterface {
	val scheduleAlarmList = ArrayList<Alarm>()
	val cancelList = ArrayList<Alarm>()
	
	var syncDataSetsCount = 0
	var updateStudiesRegularlyCount = 0
	var cancelUpdateStudiesRegularlyCount = 0
	
	override fun scheduleAlarm(alarm: Alarm): Boolean {
		scheduleAlarmList.add(alarm)
		
		return true
	}
	
	override fun cancel(alarm: Alarm) {
		cancelList.add(alarm)
	}
	
	override fun syncDataSets() {
		++syncDataSetsCount
	}
	
	override fun updateStudiesRegularly() {
		++updateStudiesRegularlyCount
	}
	
	override fun cancelUpdateStudiesRegularly() {
		++cancelUpdateStudiesRegularlyCount
	}
}