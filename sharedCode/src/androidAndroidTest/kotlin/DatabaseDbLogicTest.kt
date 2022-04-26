import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLite
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tests.database.DbLogicSharedTests

/**
 * Created by JodliDev on 05.04.2022.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseDbLogicTest : DbLogicSharedTests() {
	override fun initDb() {
		val sql = SQLite(ApplicationProvider.getApplicationContext(), null)
		NativeLink.init(sql, smartphoneData, dialogOpener, notifications, postponedActions)
		NativeLink.resetSql(sql)
	}
	
	@Before
	fun setup() {
		reset()
	}
	
	@Test
	override fun getUid() {
		super.getUid()
	}
	
	@Test
	override fun setDev_isDev_getAdminAppType() {
		super.setDev_isDev_getAdminAppType()
	}
	
	@Test
	override fun setLang_getLang() {
		super.setLang_getLang()
	}
	
	@Test
	override fun startupApp() {
		super.startupApp()
	}
	
	@Test
	override fun getPendingFileUploads_getTemporaryFileUploads_cleanupFiles() {
		super.getPendingFileUploads_getTemporaryFileUploads_cleanupFiles()
	}
	
	@Test
	override fun setNotificationsToSetup_notificationsAreSetup() {
		super.setNotificationsToSetup_notificationsAreSetup()
	}
	
	@Test
	override fun reportMissedInvitation_getMissedInvitations_resetMissedInvitations() {
		super.reportMissedInvitation_getMissedInvitations_resetMissedInvitations()
	}
	
	@Test
	override fun hasNoStudies() {
		super.hasNoStudies()
	}
	
	@Test
	override fun hasStudiesWithStatistics() {
		super.hasStudiesWithStatistics()
	}
	
	@Test
	override fun hasStudiesForMessages() {
		super.hasStudiesForMessages()
	}
	
	@Test
	override fun getStudy() {
		super.getStudy()
	}
	
	@Test
	override fun getJoinedStudies() {
		super.getJoinedStudies()
	}
	
	@Test
	override fun getStudiesWithEditableSchedules() {
		super.getStudiesWithEditableSchedules()
	}
	
	@Test
	override fun getStudiesWithStatistics() {
		super.getStudiesWithStatistics()
	}
	
	@Test
	override fun getStudiesForMessages() {
		super.getStudiesForMessages()
	}
	
	@Test
	override fun getAllStudies() {
		super.getAllStudies()
	}
	
	@Test
	override fun getMessages() {
		super.getMessages()
	}
	
	@Test
	override fun countUnreadMessages() {
		super.getMessages()
	}
	
	@Test
	override fun getQuestionnaire() {
		super.getQuestionnaire()
	}
	
	@Test
	override fun getLastDynamicTextIndex() {
		super.getLastDynamicTextIndex()
	}
	
	@Test
	override fun getAvailableListForDynamicText() {
		super.getAvailableListForDynamicText()
	}
	
	@Test
	override fun hasUnsyncedDataSetsAfterQuit() {
		super.hasUnsyncedDataSetsAfterQuit()
	}
	
	@Test
	override fun getUnSyncedDataSetCount() {
		super.getUnSyncedDataSetCount()
	}
	
	@Test
	override fun getUnSyncedDataSets() {
		super.getUnSyncedDataSets()
	}
	
	@Test
	override fun getErrorCount() {
		super.getErrorCount()
	}
	
	@Test
	override fun getWarnCount() {
		super.getWarnCount()
	}
	
	@Test
	override fun hasNewErrors_setErrorsAsReviewed() {
		super.hasNewErrors_setErrorsAsReviewed()
	}
	
	@Test
	override fun getErrors() {
		super.getErrors()
	}
	
	@Test
	override fun getAlarms_getAlarm() {
		super.getAlarms_getAlarm()
	}
	
	@Test
	override fun getAlarmsBefore() {
		super.getAlarmsBefore()
	}
	
	@Test
	override fun getAlarmsAfterToday() {
		super.getAlarmsAfterToday()
	}
	
	@Test
	override fun getLastSignalTimeAlarm() {
		super.getLastSignalTimeAlarm()
	}
	
	@Test
	override fun getLastAlarmsPerSignalTime() {
		super.getLastAlarmsPerSignalTime()
	}
	
	@Test
	override fun getNextAlarms() {
		super.getNextAlarms()
	}
	
	@Test
	override fun getNextAlarm() {
		super.getNextAlarm()
	}
	
	@Test
	override fun getReminderAlarmsFrom() {
		super.getReminderAlarmsFrom()
	}
	
	@Test
	override fun getAlarmsFrom() {
		super.getAlarmsFrom()
	}
	
	@Test
	override fun getActionTrigger() {
		super.getActionTrigger()
	}
	
	@Test
	override fun getActionTriggers() {
		super.getActionTriggers()
	}
	
	@Test
	override fun getEventTrigger() {
		super.getEventTrigger()
	}
	
	@Test
	override fun getEventTriggers() {
		super.getEventTriggers()
	}
	
	@Test
	override fun getLatestEventTrigger() {
		super.getLatestEventTrigger()
	}
	
	@Test
	override fun triggerEventTrigger() {
		super.triggerEventTrigger()
	}
	
	@Test
	override fun getSchedule() {
		super.getSchedule()
	}
	
	@Test
	override fun hasEditableSchedules() {
		super.hasEditableSchedules()
	}
	
	@Test
	override fun getSignalTime() {
		super.getSignalTime()
	}
	
	@Test
	override fun getSignalTimes() {
		super.getSignalTimes()
	}
	
	@Test
	override fun signalTimeHasAlarms() {
		super.signalTimeHasAlarms()
	}
	
	@Test
	override fun getObservedVariables() {
		super.getObservedVariables()
	}
	
	@Test
	override fun getPersonalStatisticsTimed() {
		super.getPersonalStatisticsTimed()
	}
	
	@Test
	override fun getPersonalStatisticsPerValue() {
		super.getPersonalStatisticsPerValue()
	}
	
	@Test
	override fun getStudyServerUrls() {
		super.getStudyServerUrls()
	}
}