package at.jodlidev.esmira

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import at.jodlidev.esmira.androidNative.PostponedActions
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox

/**
 * Created by JodliDev on 08.05.2019.
 */
class AlarmBox : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		ErrorBox.log("AlarmBox", "onReceive started...")

		val data = intent.extras
		if(data == null) {
			ErrorBox.error("AlarmBox", "empty intent... This should never happen!")
			return
		}
		val alarmId = data.getLong(PostponedActions.DATA_ALARM_ID)
		val alarm = DbLogic.getAlarm(alarmId)
		if(alarm == null) {
			ErrorBox.warn("AlarmBox", "Alarm is null! (Alarm: $alarmId)")
			return
		}
		alarm.exec()
	}
}