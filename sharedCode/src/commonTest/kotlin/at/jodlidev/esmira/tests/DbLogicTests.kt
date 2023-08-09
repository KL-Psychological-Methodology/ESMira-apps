package tests

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.Scheduler
import at.jodlidev.esmira.sharedCode.data_structure.*
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perValue
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_timed
import BaseCommonTest
import kotlin.test.*


/**
 * Created by JodliDev on 20.04.2022.
 */
class DbLogicTests : BaseCommonTest() {
	
	@Test
	fun startupApp() {
		
		//open error report:
		ErrorBox.error("", "")
		DbLogic.startupApp()
		assertNotEquals(0, dialogOpener.errorReportCount) //ErrorBox.error potentially also opens an errorReport
		
		//TODO: DbLogic.startupApp() uses Web directly which creates problems with testing
	}
	
	@Test
	fun setNotificationsToSetup_notificationsAreSetup() {
		DbUser.getUid() //create user
		assertFalse(DbLogic.notificationsAreSetup())
		DbLogic.setNotificationsToSetup()
		assertTrue(DbLogic.notificationsAreSetup())
	}
	
	//reportMissedInvitation(), getMissedInvitations and resetMissedInvitations() are tested in reportMissedInvitation_getMissedInvitations_resetMissedInvitations()
	
	@Test
	fun hasNoStudies() {
		assertTrue(DbLogic.hasNoStudies())
		assertTrue(DbLogic.hasNoJoinedStudies())
		
		createStudy().save()
		assertFalse(DbLogic.hasNoStudies())
	}
	
	@Test
	fun hasNoJoinedStudies() {
		assertTrue(DbLogic.hasNoJoinedStudies())
		
		val study = DbLogic.getStudy(getBaseStudyId())!!
		study.join()
		assertFalse(DbLogic.hasNoJoinedStudies())
	}
	
	@Test
	fun hasStudiesWithStatistics() {
		assertFalse(DbLogic.hasStudiesWithStatistics())
		
		createStudy("""{
				"id":123,
				"personalStatistics": {
					"charts": [{}]
				}
			}""").save()
		assertTrue(DbLogic.hasStudiesWithStatistics())
		
		reset()
		assertFalse(DbLogic.hasStudiesWithStatistics())
		createStudy("""{
				"id":123,
				"publicStatistics": {
					"charts": [{}]
				}
			}""").save()
		assertTrue(DbLogic.hasStudiesWithStatistics())
	}
	
	@Test
	fun hasStudiesForMessages() {
		assertFalse(DbLogic.hasStudiesForMessages())
		
		val study = DbLogic.getStudy(getBaseStudyId())!!
		study.sendMessagesAllowed = true
		study.join()
		assertTrue(DbLogic.hasStudiesForMessages())
	}
	
	@Test
	fun hasStudiesWithRewards() {
		assertFalse(DbLogic.hasStudiesWithRewards())
		
		val study = DbLogic.getStudy(getBaseStudyId())!!
		study.enableRewardSystem = true
		study.join()
		assertTrue(DbLogic.hasStudiesWithRewards())
	}
	
	@Test
	fun getStudy() {
		val study1 = createStudy()
		study1.title = "study1"
		val study2 = createStudy()
		study2.title = "study2"
		val study3 = createStudy()
		study3.title = "study3"
		val study4 = createStudy()
		study4.title = "study4"
		
		study1.save()
		study2.save()
		study3.save()
		study4.save()
		
		assertEquals("study1", DbLogic.getStudy(study1.id)?.title)
		assertEquals("study2", DbLogic.getStudy(study2.id)?.title)
		assertEquals("study3", DbLogic.getStudy(study3.id)?.title)
		assertEquals("study4", DbLogic.getStudy(study4.id)?.title)
	}
	
	@Test
	fun getJoinedStudies() {
		assertEquals(0, DbLogic.getJoinedStudies().size)
		
		createStudy().join()
		assertEquals(1, DbLogic.getJoinedStudies().size)
		
		createStudy().join()
		assertEquals(2, DbLogic.getJoinedStudies().size)
		
		createStudy().join()
		assertEquals(3, DbLogic.getJoinedStudies().size)
	}
	
	@Test
	fun getStudiesWithEditableSchedules() {
		assertEquals(0, DbLogic.getStudiesWithEditableSchedules().size)
		
		createStudy("""{
				"id":123,
				"questionnaires":[{
					"actionTriggers":[{
						"schedules":[{}]
					}]
				}]
			}""").join()
		assertEquals(1, DbLogic.getStudiesWithEditableSchedules().size)
		
		createStudy("""{
				"id":123,
				"questionnaires":[{
					"actionTriggers":[{
						"schedules":[{"userEditable": false}]
					}]
				}]
			}""").join()
		assertEquals(1, DbLogic.getStudiesWithEditableSchedules().size)
		
		createStudy("""{
				"id":123,
				"questionnaires":[{
					"actionTriggers":[{
						"schedules":[{}]
					}]
				}]
			}""").join()
		assertEquals(2, DbLogic.getStudiesWithEditableSchedules().size)
	}
	
	@Test
	fun getStudiesWithStatistics() {
		assertEquals(0, DbLogic.getStudiesWithStatistics().size)
		
		createStudy("""{
				"id":123,
				"personalStatistics": {
					"charts": [{}]
				}
			}""").save()
		assertEquals(1, DbLogic.getStudiesWithStatistics().size)
		
		createStudy("""{
				"id":123,
				"publicStatistics": {
					"charts": [{}]
				}
			}""").save()
		assertEquals(2, DbLogic.getStudiesWithStatistics().size)
	}
	
	@Test
	fun getStudiesForMessages() {
		assertEquals(0, DbLogic.getStudiesForMessages().size)
		
		val study = DbLogic.getStudy(getBaseStudyId())!!
		study.sendMessagesAllowed = true
		study.join()
		assertEquals(1, DbLogic.getStudiesForMessages().size)
	}
	
	@Test
	fun getStudiesWithRewards() {
		assertEquals(0, DbLogic.getStudiesWithRewards().size)
		
		val study = DbLogic.getStudy(getBaseStudyId())!!
		study.enableRewardSystem = true
		study.join()
		assertEquals(1, DbLogic.getStudiesWithRewards().size)
	}
	
	@Test
	fun getAllStudies() {
		assertEquals(0, DbLogic.getAllStudies().size)
		createStudy().save()
		assertEquals(1, DbLogic.getAllStudies().size)
		createStudy().save()
		assertEquals(2, DbLogic.getAllStudies().size)
		createStudy().save()
		assertEquals(3, DbLogic.getAllStudies().size)
	}
	
	@Test
	fun getMessages() {
		assertEquals(0, DbLogic.getMessages(getBaseStudyId()).size)
		
		Message.addMessage(getBaseStudyId(), "My first girlfriend turned into the moon", 10000)
		assertEquals(1, DbLogic.getMessages(getBaseStudyId()).size)
		assertEquals(0, DbLogic.getMessages(123).size)
		
		Message.addMessage(getBaseStudyId(), "My first girlfriend turned into the moon", 10000)
		assertEquals(2, DbLogic.getMessages(getBaseStudyId()).size)
		assertEquals(0, DbLogic.getMessages(456).size)
	}
	
	@Test
	fun countUnreadMessages() {
		val studyId = 5L
		assertEquals(0, DbLogic.countUnreadMessages())
		assertEquals(0, DbLogic.countUnreadMessages(studyId))
		
		Message.addMessage(studyId, "My first girlfriend turned into the moon", 10000, true)
		Message.addMessage(studyId, "My first girlfriend turned into the moon", 10000, true)
		Message.addMessage(-2, "My first girlfriend turned into the moon", 10000, true)
		Message.addMessage(getBaseStudyId(), "My first girlfriend turned into the moon", 10000)
		assertEquals(2, DbLogic.countUnreadMessages(studyId))
		assertEquals(3, DbLogic.countUnreadMessages())
	}
	
	@Test
	fun getQuestionnaire() {
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.title = "q1"
		questionnaire.save(true)
		assertEquals("q1", DbLogic.getQuestionnaire(questionnaire.id)?.title)
		assertNull(DbLogic.getQuestionnaire(-1))
	}
	
	@Test
	fun getLastDynamicTextIndex() {
		val dynamicInputData1 = DynamicInputData(5, "v1", 0)
		dynamicInputData1.createdTime = 1000
		dynamicInputData1.save()
		assertEquals(1000, DbLogic.getLastDynamicTextIndex(5, "v1")?.createdTime)
		
		val dynamicInputData2 = DynamicInputData(5, "v1", 0)
		dynamicInputData2.createdTime = 1001
		dynamicInputData2.save()
		assertEquals(1001, DbLogic.getLastDynamicTextIndex(5, "v1")?.createdTime)
		
		val dynamicInputData3 = DynamicInputData(5, "v1", 0)
		dynamicInputData3.createdTime = 999
		dynamicInputData3.save()
		assertEquals(1001, DbLogic.getLastDynamicTextIndex(5, "v1")?.createdTime)
		
		val dynamicInputData4 = DynamicInputData(5, "v2", 0)
		dynamicInputData4.createdTime = 1002
		dynamicInputData4.save()
		assertEquals(1001, DbLogic.getLastDynamicTextIndex(5, "v1")?.createdTime)
	}
	
	@Test
	fun getAvailableListForDynamicText() {
		val qId = 5L
		val name = "v1"
		val wrongName = "v2"
		for(tryI in 0 until 100) {
			val compare = arrayListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
			val size = compare.size
			
			while(compare.size != 0) {
				val list = DbLogic.getAvailableListForDynamicText(qId, name, size)
				assertEquals(compare.size, list.size)
				for((i, index) in compare.withIndex()) {
					assertEquals(index, list[i], "list[$i] is not $index but ${list[i]}")
				}
				val rand = compare.random()
				compare.remove(rand)
				DynamicInputData(qId, name, rand).save()
				DynamicInputData(qId, wrongName, rand).save()
			}
			//cleanup
			DbLogic.deleteCheckedRandomTexts(qId, name)
			DbLogic.deleteCheckedRandomTexts(qId, wrongName)
		}
	}
	
	@Test
	fun hasUnSyncedDataSets() {
		val db = NativeLink.sql
		val study = DbLogic.getStudy(getBaseStudyId())!!
		
		assertFalse(DbLogic.hasUnSyncedDataSets(getBaseStudyId()))
		DataSet.createShortDataSet(DataSet.EventTypes.joined, study)
		assertTrue(DbLogic.hasUnSyncedDataSets(getBaseStudyId()))
		
		val values = db.getValueBox()
		values.putInt(DataSet.KEY_SYNCED, UploadData.States.SYNCED.ordinal)
		db.update(DataSet.TABLE, values, null, null)
		assertFalse(DbLogic.hasUnSyncedDataSets(getBaseStudyId()))
		
		val fileUpload = FileUpload(study, "path/to/file", FileUpload.DataTypes.Image)
		fileUpload.save()
		fileUpload.setReadyForUpload()
		assertTrue(DbLogic.hasUnSyncedDataSets(getBaseStudyId()))
		
		fileUpload.setTemporary()
		assertFalse(DbLogic.hasUnSyncedDataSets(getBaseStudyId()))
	}
	
	@Test
	fun getUnSyncedDataSetCount() {
		val study = DbLogic.getStudy(getBaseStudyId())!!
		assertEquals(0, DbLogic.getUnSyncedDataSetCount())
		
		DataSet.createShortDataSet(DataSet.EventTypes.joined, study)
		assertEquals(1, DbLogic.getUnSyncedDataSetCount())
		
		DataSet.createShortDataSet(DataSet.EventTypes.joined, study)
		assertEquals(2, DbLogic.getUnSyncedDataSetCount())
		
		val fileUpload1 = FileUpload(study, "path/to/file", FileUpload.DataTypes.Image)
		fileUpload1.save()
		fileUpload1.setReadyForUpload()
		assertEquals(2, DbLogic.getUnSyncedDataSetCount())
		
		val fileUpload2 = FileUpload(study, "path/to/file", FileUpload.DataTypes.Image)
		fileUpload2.save()
		fileUpload2.setReadyForUpload()
		assertEquals(2, DbLogic.getUnSyncedDataSetCount())
		
		val fileUpload3 = FileUpload(study, "path/to/file", FileUpload.DataTypes.Image)
		fileUpload3.save()
		fileUpload3.setReadyForUpload()
		assertEquals(3, DbLogic.getUnSyncedDataSetCount())
		
		val fileUpload4 = FileUpload(study, "path/to/file", FileUpload.DataTypes.Image)
		fileUpload4.save()
		fileUpload4.setReadyForUpload()
		assertEquals(4, DbLogic.getUnSyncedDataSetCount())
		
		DataSet.createShortDataSet(DataSet.EventTypes.joined, study)
		assertEquals(4, DbLogic.getUnSyncedDataSetCount())
	}
	
	@Test
	fun getUnSyncedDataSets() {
		val db = NativeLink.sql
		val study = DbLogic.getStudy(getBaseStudyId())!!
		
		assertEquals(0, DbLogic.getUnSyncedDataSets().size)
		
		DataSet.createShortDataSet(DataSet.EventTypes.joined, study)
		assertEquals(1, DbLogic.getUnSyncedDataSets()[testUrl]?.size)
		
		DataSet.createShortDataSet(DataSet.EventTypes.joined, study)
		assertEquals(2, DbLogic.getUnSyncedDataSets()[testUrl]?.size)
		
		val values = db.getValueBox()
		values.putInt(DataSet.KEY_SYNCED, UploadData.States.SYNCED.ordinal)
		db.update(DataSet.TABLE, values, null, null)
		assertEquals(0, DbLogic.getUnSyncedDataSets().size)
		
		DataSet.createShortDataSet(DataSet.EventTypes.joined, study)
		assertEquals(1, DbLogic.getUnSyncedDataSets()[testUrl]?.size)
		
		val fileUpload = FileUpload(study, "path/to/file", FileUpload.DataTypes.Image)
		fileUpload.save()
		fileUpload.setReadyForUpload()
		assertEquals(1, DbLogic.getUnSyncedDataSets()[testUrl]?.size)
	}
	
	@Test
	fun getPendingFileUploads_getTemporaryFileUploads_cleanupFiles() {
		//create fileUpload, change state, cleanup
		val fileUpload = FileUpload(createStudy(), "path/to/file", FileUpload.DataTypes.Image)
		fileUpload.save()
		assertEquals(1, DbLogic.getTemporaryFileUploads().size)
		
		fileUpload.setReadyForUpload()
		assertEquals(0, DbLogic.getTemporaryFileUploads().size)
		assertEquals(1, DbLogic.getPendingFileUploads().size)
		
		fileUpload.setTemporary()
		assertEquals(1, DbLogic.getTemporaryFileUploads().size)
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		
		DbLogic.cleanupFiles()
		assertEquals(1, DbLogic.getTemporaryFileUploads().size)
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		
		DbLogic.cleanupFiles(true)
		assertEquals(0, DbLogic.getTemporaryFileUploads().size)
		assertEquals(0, DbLogic.getPendingFileUploads().size)
	}
	
	@Test
	fun getErrorCount() {
		assertEquals(0, DbLogic.getErrorCount())
		
		ErrorBox.error("Error", "Error")
		assertEquals(1, DbLogic.getErrorCount())
		
		ErrorBox.warn("Warning", "Warning")
		assertEquals(1, DbLogic.getErrorCount())
		
		ErrorBox.log("Log", "Log")
		assertEquals(1, DbLogic.getErrorCount())
		
		ErrorBox.error("Error", "Error")
		assertEquals(2, DbLogic.getErrorCount())
	}
	
	@Test
	fun getWarnCount() {
		assertEquals(0, DbLogic.getWarnCount())
		
		ErrorBox.error("Error", "Error")
		assertEquals(0, DbLogic.getWarnCount())
		
		ErrorBox.warn("Warning", "Warning")
		assertEquals(1, DbLogic.getWarnCount())
		
		ErrorBox.log("Log", "Log")
		assertEquals(1, DbLogic.getWarnCount())
		
		ErrorBox.warn("Warning", "Warning")
		assertEquals(2, DbLogic.getWarnCount())
	}
	
	@Test
	fun hasNewErrors_setErrorsAsReviewed() {
		assertFalse(DbLogic.hasNewErrors())
		
		ErrorBox.warn("Warning", "Warning")
		assertFalse(DbLogic.hasNewErrors())
		
		ErrorBox.log("Log", "Log")
		assertFalse(DbLogic.hasNewErrors())
		
		ErrorBox.error("Error", "Error")
		assertTrue(DbLogic.hasNewErrors())
		
		DbLogic.setErrorsAsReviewed()
		assertFalse(DbLogic.hasNewErrors())
	}
	
	@Test
	fun getErrors() {
		assertEquals(0, DbLogic.getErrors().size)
		
		ErrorBox.error("Error", "Error")
		assertEquals(1, DbLogic.getErrors().size)
		
		ErrorBox.warn("Warning", "Warning")
		assertEquals(2, DbLogic.getErrors().size)
		
		ErrorBox.log("Log", "Log")
		assertEquals(3, DbLogic.getErrors().size)
	}
	
	//
	//Alarms
	//
	
	@Test
	fun getLastAlarmBefore() {
		val timestamp = 1114313512000
		val qId = 5L
		val signalTime = createJsonObj<SignalTime>()
		signalTime.bindParent(qId, createJsonObj())
		Alarm.createFromSignalTime(signalTime, -1, timestamp -1)
		Alarm.createFromSignalTime(signalTime, -1, timestamp -2)
		Alarm.createFromSignalTime(signalTime, -1, timestamp -3)
		Alarm.createFromSignalTime(signalTime, -1, timestamp +1)
		Alarm.createFromSignalTime(signalTime, -1, timestamp +2)
		Alarm.createFromSignalTime(signalTime, -1, timestamp +3)
		
		val alarm = DbLogic.getLastAlarmBefore(timestamp, qId)
		assertEquals(timestamp-1, alarm?.timestamp)
	}
	
	@Test
	fun getAlarms_getAlarm() {
		val qId = 5L
		val timestamp = 1114313512000
		
		val schedule = createJsonObj<Schedule>()
		schedule.id = 7
		val signalTime = createJsonObj<SignalTime>()
		signalTime.bindParent(qId, schedule)
		
		assertEquals(0, DbLogic.getAlarms().size)
		
		//
		//getAlarms(schedule: Schedule)
		//
		Alarm.createFromSignalTime(signalTime, -1, timestamp)
		assertEquals(0, DbLogic.getAlarms(createJsonObj()).size)
		assertEquals(1, DbLogic.getAlarms(schedule).size)
		
		//
		//getAlarms()
		//
		createAlarmFromSignalTime("""{"label": "test2"}""").save()
		val alarms = DbLogic.getAlarms()
		assertEquals(2, alarms.size)
		assertEquals(alarms[0].timestamp, DbLogic.getAlarm(alarms[0].id)?.timestamp)
		assertEquals(alarms[1].timestamp, DbLogic.getAlarm(alarms[1].id)?.timestamp)
	}
	
	@Test
	fun getAlarmsBefore() {
		val timestamp = 1114313512000
		
		assertEquals(0, DbLogic.getAlarmsBefore(timestamp).size)
		
		Alarm.createFromSignalTime(createJsonObj(), -1, timestamp)
		assertEquals(0, DbLogic.getAlarmsBefore(timestamp-1).size)
		assertEquals(1, DbLogic.getAlarmsBefore(timestamp).size)
		
		Alarm.createFromSignalTime(createJsonObj(), -1, timestamp-1)
		assertEquals(0, DbLogic.getAlarmsBefore(timestamp-2).size)
		assertEquals(1, DbLogic.getAlarmsBefore(timestamp-1).size)
		assertEquals(2, DbLogic.getAlarmsBefore(timestamp).size)
	
	}
	
	@Test
	fun getAlarmsAfterToday() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.id = 54
		val todayMidnight = NativeLink.getMidnightMillis()
		
		assertEquals(0, DbLogic.getAlarmsAfterToday(signalTime).size)
		
		Alarm.createFromSignalTime(signalTime, -1, todayMidnight)
		assertEquals(0, DbLogic.getAlarmsAfterToday(signalTime).size)
		
		Alarm.createFromSignalTime(signalTime, -1, todayMidnight + Scheduler.ONE_DAY_MS-1)
		assertEquals(0, DbLogic.getAlarmsAfterToday(signalTime).size)
		
		Alarm.createFromSignalTime(signalTime, -1, todayMidnight + Scheduler.ONE_DAY_MS)
		assertEquals(1, DbLogic.getAlarmsAfterToday(signalTime).size)
	}
	
	@Test
	fun getLastSignalTimeAlarm() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.id = 54
		val timestamp = NativeLink.getNowMillis()
		
		assertNull(DbLogic.getLastSignalTimeAlarm(signalTime))
		
		val alarm = Alarm.createFromSignalTime(signalTime, -1, timestamp)
		assertEquals(alarm.id, DbLogic.getLastSignalTimeAlarm(signalTime)?.id)
		
		val alarm2 = Alarm.createFromSignalTime(signalTime, -1, timestamp-1)
		assertEquals(alarm.id, DbLogic.getLastSignalTimeAlarm(signalTime)?.id)
		
		val alarm3 = Alarm.createFromSignalTime(signalTime, -1, timestamp+1)
		assertEquals(alarm3.id, DbLogic.getLastSignalTimeAlarm(signalTime)?.id)
		
		val alarm4 = Alarm.createFromSignalTime(createJsonObj(), -1, timestamp+2)
		assertEquals(alarm3.id, DbLogic.getLastSignalTimeAlarm(signalTime)?.id)
	}
	
	@Test
	fun getLastAlarmsPerSignalTime() {
		val timestamp = 1114313512000
		val signalTime = createJsonObj<SignalTime>()
		signalTime.id = 54
		
		assertEquals(0, DbLogic.getLastAlarmPerSignalTime().size)
		
		Alarm.createFromSignalTime(signalTime, -1, timestamp)
		assertEquals(1, DbLogic.getLastAlarmPerSignalTime().size)
		assertEquals(timestamp, DbLogic.getLastAlarmPerSignalTime()[0].timestamp)
		
		Alarm.createFromSignalTime(signalTime, -1, timestamp-1)
		assertEquals(1, DbLogic.getLastAlarmPerSignalTime().size)
		assertEquals(timestamp, DbLogic.getLastAlarmPerSignalTime()[0].timestamp)
		
		Alarm.createFromSignalTime(signalTime, -1, timestamp+1)
		assertEquals(1, DbLogic.getLastAlarmPerSignalTime().size)
		assertEquals(timestamp+1, DbLogic.getLastAlarmPerSignalTime()[0].timestamp)
		
		Alarm.createFromSignalTime(createJsonObj(), -1, timestamp+2)
		assertEquals(2, DbLogic.getLastAlarmPerSignalTime().size)
	
	}
	
//	@Test
//	fun getNextAlarmsWithNotifications() {
//		val timestamp = 1114313512000
//
//		assertEquals(0, DbLogic.getQuestionnaireAlarmsWithNotifications(-1).size)
//
//		val actionTrigger = DbLogic.createJsonObj<ActionTrigger>()
//		actionTrigger.save(true)
//
//		//first entry:
//		val questionnaire1 = createJsonObj<Questionnaire>()
//		questionnaire1.studyId = getBaseStudyId()
//		questionnaire1.save(true)
//		val signalTime1 = createJsonObj<SignalTime>()
//		signalTime1.questionnaireId = questionnaire1.id
//
//		Alarm.createFromSignalTime(signalTime1, actionTrigger.id, timestamp)
//		val nextAlarmsPerQuestionnaire1 = DbLogic.getQuestionnaireAlarmsWithNotifications(getBaseStudyId())
//		assertEquals(1, nextAlarmsPerQuestionnaire1.size)
//		assertEquals(timestamp, nextAlarmsPerQuestionnaire1[0].timestamp)
//
//		//later:
//		Alarm.createFromSignalTime(signalTime1, actionTrigger.id, timestamp+1)
//		val nextAlarmsPerQuestionnaire2 = DbLogic.getQuestionnaireAlarmsWithNotifications(getBaseStudyId())
//		assertEquals(1, nextAlarmsPerQuestionnaire2.size)
//		assertEquals(timestamp, nextAlarmsPerQuestionnaire2[0].timestamp)
//
//		//sooner:
//		Alarm.createFromSignalTime(signalTime1, -1, timestamp-1)
//		val nextAlarmsPerQuestionnaire3 = DbLogic.getQuestionnaireAlarmsWithNotifications(getBaseStudyId())
//		assertEquals(1, nextAlarmsPerQuestionnaire3.size)
//		assertEquals(timestamp-1, nextAlarmsPerQuestionnaire3[0].timestamp)
//
//		//sooner but different questionnaire:
//		val questionnaire2 = createJsonObj<Questionnaire>()
//		questionnaire2.studyId = getBaseStudyId()
//		questionnaire2.save(true)
//		val signalTime2 = createJsonObj<SignalTime>()
//		signalTime2.questionnaireId = questionnaire2.id
//
//		Alarm.createFromSignalTime(signalTime2, -1, timestamp-2)
//		val nextAlarmsPerQuestionnaire4 = DbLogic.getQuestionnaireAlarmsWithNotifications(getBaseStudyId())
//		assertEquals(2, nextAlarmsPerQuestionnaire4.size)
//		assertEquals(timestamp-2, nextAlarmsPerQuestionnaire4[0].timestamp)
//		assertEquals(timestamp-1, nextAlarmsPerQuestionnaire4[1].timestamp)
//
//		//sooner but different study:
//		val questionnaire3 = createJsonObj<Questionnaire>()
//		questionnaire3.studyId = 5654
//		questionnaire3.save(true)
//		val signalTime3 = createJsonObj<SignalTime>()
//		signalTime3.questionnaireId = questionnaire3.id
//
//		Alarm.createFromSignalTime(signalTime3, -1, timestamp-3)
//		val nextAlarmsPerQuestionnaire5 = DbLogic.getQuestionnaireAlarmsWithNotifications(getBaseStudyId())
//		assertEquals(2, nextAlarmsPerQuestionnaire5.size)
//		assertEquals(timestamp-2, nextAlarmsPerQuestionnaire5[0].timestamp)
//		assertEquals(timestamp-1, nextAlarmsPerQuestionnaire5[1].timestamp)
//	}
	
	@Test
	fun getNextAlarm() {
		val timestamp = 1114313512000
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.id = 5
		val schedule = createJsonObj<Schedule>()
		schedule.id = 6
		val signalTimeSchedule = createJsonObj<SignalTime>()
		signalTimeSchedule.bindParent(7, schedule)
		
		val signalTimeQuestionnaire = createJsonObj<SignalTime>()
		signalTimeQuestionnaire.bindParent(questionnaire.id, createJsonObj())
		
		assertNull(DbLogic.getNextAlarm(questionnaire))
		assertNull(DbLogic.getNextAlarm(schedule))
		
		//first questionnaire entry
		Alarm.createFromSignalTime(signalTimeQuestionnaire, -1, timestamp)
		assertEquals(timestamp, DbLogic.getNextAlarm(questionnaire)?.timestamp)
		assertNull(DbLogic.getNextAlarm(schedule))
		
		//first schedule entry
		Alarm.createFromSignalTime(signalTimeSchedule, -1, timestamp-1)
		assertEquals(timestamp, DbLogic.getNextAlarm(questionnaire)?.timestamp)
		assertEquals(timestamp-1, DbLogic.getNextAlarm(schedule)?.timestamp)
		
		//lower questionnaire entry
		Alarm.createFromSignalTime(signalTimeQuestionnaire, -1, timestamp-2)
		assertEquals(timestamp-2, DbLogic.getNextAlarm(questionnaire)?.timestamp)
		assertEquals(timestamp-1, DbLogic.getNextAlarm(schedule)?.timestamp)
		
		//lower schedule entry
		Alarm.createFromSignalTime(signalTimeSchedule, -1, timestamp-3)
		assertEquals(timestamp-2, DbLogic.getNextAlarm(questionnaire)?.timestamp)
		assertEquals(timestamp-3, DbLogic.getNextAlarm(schedule)?.timestamp)
		
		//higher questionnaire entry
		Alarm.createFromSignalTime(signalTimeQuestionnaire, -1, timestamp+1)
		assertEquals(timestamp-2, DbLogic.getNextAlarm(questionnaire)?.timestamp)
		assertEquals(timestamp-3, DbLogic.getNextAlarm(schedule)?.timestamp)
		
		//higher schedule entry
		Alarm.createFromSignalTime(signalTimeSchedule, -1, timestamp+1)
		assertEquals(timestamp-2, DbLogic.getNextAlarm(questionnaire)?.timestamp)
		assertEquals(timestamp-3, DbLogic.getNextAlarm(schedule)?.timestamp)
	}
	
	@Test
	fun getReminderAlarmsFrom() {
		val timestamp = 1114313512000
		val qId = 5L
		val signalTime = createJsonObj<SignalTime>()
		signalTime.questionnaireId = qId
		
		assertEquals(0, DbLogic.getReminderAlarmsFrom(qId).size)
		
		Alarm.createAsReminder(timestamp, qId, -1, 0, 2)
		assertEquals(1, DbLogic.getReminderAlarmsFrom(qId).size)
		
		Alarm.createAsReminder(timestamp, -1, -1, 0, 2)
		assertEquals(1, DbLogic.getReminderAlarmsFrom(qId).size)
		
		Alarm.createFromSignalTime(createJsonObj(), -1, timestamp+1)
		assertEquals(1, DbLogic.getReminderAlarmsFrom(qId).size)
		
		Alarm.createAsReminder(timestamp, qId, -1, 0, 2)
		assertEquals(2, DbLogic.getReminderAlarmsFrom(qId).size)
		
	}
	
	@Test
	fun getAlarmsFrom() {
		val timestamp = 1114313512000
		
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.id = 5
		
		val actionTrigger = createActionTrigger()
		actionTrigger.id = 6
		
		val eventTrigger = createJsonObj<EventTrigger>()
		eventTrigger.id = 7
		
		val schedule = createJsonObj<Schedule>()
		schedule.id = 8
		
		val signalTimeSingle = createJsonObj<SignalTime>()
		signalTimeSingle.id = 9
		
		val signalTimeQuestionnaire = createJsonObj<SignalTime>()
		signalTimeQuestionnaire.bindParent(questionnaire.id, createJsonObj())
		
		val signalTimeSchedule = createJsonObj<SignalTime>()
		signalTimeSchedule.bindParent(-1, schedule)
		
		
		assertEquals(0, DbLogic.getAlarmsFrom(questionnaire).size)
		
		Alarm.createFromSignalTime(createJsonObj(), -1, timestamp)
		assertEquals(0, DbLogic.getAlarmsFrom(questionnaire).size)
		assertEquals(0, DbLogic.getAlarmsFrom(actionTrigger).size)
		assertEquals(0, DbLogic.getAlarmsFrom(eventTrigger).size)
		assertEquals(0, DbLogic.getAlarmsFrom(schedule).size)
		assertEquals(0, DbLogic.getAlarmsFrom(signalTimeSingle).size)
		
		Alarm.createFromSignalTime(signalTimeQuestionnaire, -1, timestamp)
		assertEquals(1, DbLogic.getAlarmsFrom(questionnaire).size)
		assertEquals(0, DbLogic.getAlarmsFrom(actionTrigger).size)
		assertEquals(0, DbLogic.getAlarmsFrom(eventTrigger).size)
		assertEquals(0, DbLogic.getAlarmsFrom(schedule).size)
		assertEquals(0, DbLogic.getAlarmsFrom(signalTimeSingle).size)
		
		Alarm.createFromSignalTime(createJsonObj(), actionTrigger.id, timestamp)
		assertEquals(1, DbLogic.getAlarmsFrom(questionnaire).size)
		assertEquals(1, DbLogic.getAlarmsFrom(actionTrigger).size)
		assertEquals(0, DbLogic.getAlarmsFrom(eventTrigger).size)
		assertEquals(0, DbLogic.getAlarmsFrom(schedule).size)
		assertEquals(0, DbLogic.getAlarmsFrom(signalTimeSingle).size)
		
		Alarm.createFromEventTrigger(eventTrigger, timestamp)
		assertEquals(1, DbLogic.getAlarmsFrom(questionnaire).size)
		assertEquals(1, DbLogic.getAlarmsFrom(actionTrigger).size)
		assertEquals(1, DbLogic.getAlarmsFrom(eventTrigger).size)
		assertEquals(0, DbLogic.getAlarmsFrom(schedule).size)
		assertEquals(0, DbLogic.getAlarmsFrom(signalTimeSingle).size)
		
		Alarm.createFromSignalTime(signalTimeSchedule, -1, timestamp)
		assertEquals(1, DbLogic.getAlarmsFrom(questionnaire).size)
		assertEquals(1, DbLogic.getAlarmsFrom(actionTrigger).size)
		assertEquals(1, DbLogic.getAlarmsFrom(eventTrigger).size)
		assertEquals(1, DbLogic.getAlarmsFrom(schedule).size)
		assertEquals(0, DbLogic.getAlarmsFrom(signalTimeSingle).size)
		
		Alarm.createFromSignalTime(signalTimeSingle, -1, timestamp)
		assertEquals(1, DbLogic.getAlarmsFrom(questionnaire).size)
		assertEquals(1, DbLogic.getAlarmsFrom(actionTrigger).size)
		assertEquals(1, DbLogic.getAlarmsFrom(eventTrigger).size)
		assertEquals(1, DbLogic.getAlarmsFrom(schedule).size)
		assertEquals(1, DbLogic.getAlarmsFrom(signalTimeSingle).size)
	}
	
	@Test
	fun countAlarmsFrom() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.id = 5
		
		assertEquals(0, DbLogic.countAlarmsFrom(signalTime.id))
		Alarm.createFromSignalTime(signalTime, -1, 1000)
		assertEquals(1, DbLogic.countAlarmsFrom(signalTime.id))
		Alarm.createFromSignalTime(signalTime, -1, 2000)
		assertEquals(2, DbLogic.countAlarmsFrom(signalTime.id))
		Alarm.createFromSignalTime(signalTime, -1, 3000)
		assertEquals(3, DbLogic.countAlarmsFrom(signalTime.id))
		assertEquals(0, DbLogic.countAlarmsFrom(6))
	}
	
	//
	//ActionTrigger
	//
	
	@Test
	fun getActionTrigger() {
		val json = """[{"test":123}]"""
		val actionTrigger = createActionTrigger("""{"actions": $json}""")
		actionTrigger.save(true)
		
		assertEquals(json, DbLogic.getActionTrigger(actionTrigger.id)?.actionsString)
	}
	
	@Test
	fun getActionTriggers() {
		assertEquals(0, DbLogic.getActionTriggers(getBaseStudyId()).size)
		
		createActionTrigger().save(true)
		assertEquals(1, DbLogic.getActionTriggers(getBaseStudyId()).size)
		
		val actionTrigger = createActionTrigger()
		actionTrigger.studyId = 5
		actionTrigger.save(true)
		assertEquals(1, DbLogic.getActionTriggers(getBaseStudyId()).size)
		
		
		createActionTrigger().save(true)
		assertEquals(2, DbLogic.getActionTriggers(getBaseStudyId()).size)
	}
	
	//
	//EventTrigger
	//
	
	@Test
	fun getEventTrigger() {
		val eventTrigger = createJsonObj<EventTrigger>("""{"cueCode": "questionnaire"}""")
		eventTrigger.save()
		
		assertNull(DbLogic.getEventTrigger(6))
		assertEquals(DataSet.EventTypes.questionnaire, DbLogic.getEventTrigger(eventTrigger.id)?.cueCode)
	}
	
	@Test
	fun getEventTriggers() {
		val cue = DataSet.EventTypes.quit
		val other = DataSet.EventTypes.joined
		
		assertEquals(0, DbLogic.getEventTriggers(getBaseStudyId(), cue).size)
		
		val eventTrigger1 = createJsonObj<EventTrigger>()
		eventTrigger1.cueCode = cue
		eventTrigger1.studyId = getBaseStudyId()
		eventTrigger1.save()
		assertEquals(1, DbLogic.getEventTriggers(getBaseStudyId(), cue).size)
		
		val eventTrigger2 = createJsonObj<EventTrigger>()
		eventTrigger2.cueCode = other
		eventTrigger1.studyId = getBaseStudyId()
		eventTrigger2.save()
		assertEquals(1, DbLogic.getEventTriggers(getBaseStudyId(), cue).size)
		
		val eventTrigger3 = createJsonObj<EventTrigger>()
		eventTrigger3.cueCode = cue
		eventTrigger1.studyId = 5
		eventTrigger3.save()
		assertEquals(1, DbLogic.getEventTriggers(getBaseStudyId(), cue).size)
	}
	
	@Test
	fun triggerEventTrigger() {
		val actionTrigger = createActionTrigger("""{"actions": [{"type": ${ActionTrigger.JSON_ACTION_TYPE_MSG}}]}""")
		actionTrigger.save(true) //eventTrigger.exec() called in eventTrigger.triggerCheck() needs ac actionTrigger
		
		val eventTrigger1 = createJsonObj<EventTrigger>("""{"cueCode": "quit"}""")
		eventTrigger1.actionTriggerId = actionTrigger.id
		eventTrigger1.studyId = 5
		eventTrigger1.save()
		val eventTrigger2 = createJsonObj<EventTrigger>("""{"cueCode": "quit"}""")
		eventTrigger2.actionTriggerId = actionTrigger.id
		eventTrigger2.studyId = 6
		eventTrigger2.save()
		
		
		DbLogic.triggerEventTrigger(5, DataSet.EventTypes.quit, -1)
		assertEquals(1, notifications.fireMessageNotificationList.size)
		
		//cue code with wrong study:
		DbLogic.triggerEventTrigger(5, DataSet.EventTypes.joined, -1)
		assertEquals(1, notifications.fireMessageNotificationList.size)
	}
	
	//
	//Schedule
	//
	
	@Test
	fun getSchedule() {
		val schedule = createScheduleForSaving()
		schedule.dailyRepeatRate = 7
		schedule.saveAndScheduleIfExists()
		
		createScheduleForSaving().saveAndScheduleIfExists()
		
		assertNull(DbLogic.getSchedule(-2))
		assertEquals(7, DbLogic.getSchedule(schedule.id)?.dailyRepeatRate)
	
	}
	
	@Test
	fun getAllSchedules() {
		assertEquals(0, DbLogic.getAllSchedules().size)
		
		createScheduleForSaving().saveAndScheduleIfExists()
		assertEquals(1, DbLogic.getAllSchedules().size)
		
		createScheduleForSaving().saveAndScheduleIfExists()
		assertEquals(2, DbLogic.getAllSchedules().size)
		
		createScheduleForSaving().saveAndScheduleIfExists()
		assertEquals(3, DbLogic.getAllSchedules().size)
	}
	
	@Test
	fun hasEditableSchedules() {
		assertFalse(DbLogic.hasEditableSchedules())
		
		val schedule1 = createScheduleForSaving()
		schedule1.userEditable = false
		schedule1.saveAndScheduleIfExists()
		assertFalse(DbLogic.hasEditableSchedules())
		
		val schedule2 = createScheduleForSaving()
		schedule2.userEditable = true
		schedule2.saveAndScheduleIfExists()
		assertTrue(DbLogic.hasEditableSchedules())
	}
	
	@Test
	fun getSignalTime() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.questionnaireId = -5
		signalTime.save()
		
		createJsonObj<SignalTime>().save()
		
		assertNull(DbLogic.getSignalTime(6))
		assertEquals(-5, DbLogic.getSignalTime(signalTime.id)?.questionnaireId)
	}
	
	@Test
	fun getSignalTimes() {
		val schedule = createJsonObj<Schedule>()
		schedule.id = 5
		
		assertEquals(0, DbLogic.getSignalTimes(schedule).size)
		
		createJsonObj<SignalTime>().save()
		assertEquals(0, DbLogic.getSignalTimes(schedule).size)
		
		val signalTime = createJsonObj<SignalTime>()
		signalTime.scheduleId = schedule.id
		signalTime.save()
		assertEquals(1, DbLogic.getSignalTimes(schedule).size)
	}
	
	@Test
	fun signalTimeHasAlarms() {
		val signalTime = createJsonObj<SignalTime>()
		signalTime.id = 5
		
		assertFalse(DbLogic.signalTimeHasAlarms(signalTime.id))
		
		Alarm.createFromSignalTime(signalTime, -1, NativeLink.getNowMillis())
		assertTrue(DbLogic.signalTimeHasAlarms(signalTime.id))
	}
	
	@Test
	fun getObservedVariables() {
		val variableName = "test"
		val variableNameOther = "other"
		val variableNameOtherOther = "otherOtter"
		val studyId = getBaseStudyId()
		
		assertEquals(0, DbLogic.getObservedVariables(studyId, variableName).size)
		
		val o1 = createObservedVariable(variableName)
		o1.studyId = studyId
		o1.save()
		assertEquals(1, DbLogic.getObservedVariables(studyId, variableName).size)
		
		val o2 = createObservedVariable(variableName)
		o2.studyId = 5L
		o2.save()
		assertEquals(1, DbLogic.getObservedVariables(studyId, variableName).size)
		
		val o3 = createObservedVariable(variableNameOther)
		o3.studyId = studyId
		o3.save()
		assertEquals(1, DbLogic.getObservedVariables(studyId, variableName).size)
		
		assertEquals(0, DbLogic.getObservedVariables(studyId, variableNameOtherOther).size)
		assertEquals(0, DbLogic.getObservedVariables(6L, variableName).size)
	}
	
	@Test
	fun getPersonalStatisticsTimed() {
		val timestamp = 1114306312L
		val firstDay = timestamp - StatisticData_timed.ONE_DAY
		val key1 = "test1"
		val key2 = "test2"
		
		val obs1 = createObservedVariable(key1)
		obs1.index = 0
		obs1.save()
		
		val obs2 = createObservedVariable(key1)
		obs2.index = 1
		obs2.save()
		
		val obs3 = createObservedVariable(key2)
		obs3.index = 0
		obs3.save()
		
		//first entry
		val statisticsDataTimed1 = StatisticData_timed(firstDay, key1, 0)
		statisticsDataTimed1.studyId = getBaseStudyId()
		statisticsDataTimed1.observedId = obs1.id
		statisticsDataTimed1.sum = 1.0
		statisticsDataTimed1.save()
		
		//entry after
		val statisticsDataTimed3 = StatisticData_timed(timestamp + 2, key1, 0)
		statisticsDataTimed3.studyId = getBaseStudyId()
		statisticsDataTimed3.observedId = obs1.id
		statisticsDataTimed3.sum = 4.0
		statisticsDataTimed3.save()
		
		//entry in between
		val statisticsDataTimed2 = StatisticData_timed(timestamp + 1, key1, 0)
		statisticsDataTimed2.studyId = getBaseStudyId()
		statisticsDataTimed2.observedId = obs1.id
		statisticsDataTimed2.sum = 3.0
		statisticsDataTimed2.save()
		
		//entry after
		val statisticsDataTimed4 = StatisticData_timed(timestamp + 3, key1, 0)
		statisticsDataTimed4.studyId = getBaseStudyId()
		statisticsDataTimed4.observedId = obs1.id
		statisticsDataTimed4.sum = 5.0
		statisticsDataTimed4.save()
		
		//entry with other index
		val statisticsDataTimed5 = StatisticData_timed(timestamp, key1, 1)
		statisticsDataTimed5.studyId = getBaseStudyId()
		statisticsDataTimed5.observedId = obs2.id
		statisticsDataTimed5.sum = 6.0
		statisticsDataTimed5.save()
		
		//entry with other key
		val statisticsDataTimed6 = StatisticData_timed(timestamp, key2, 0)
		statisticsDataTimed6.studyId = getBaseStudyId()
		statisticsDataTimed6.observedId = obs3.id
		statisticsDataTimed6.sum = 7.0
		statisticsDataTimed6.save()
		
		val keyIndex1 = key1 + 0
		val keyIndex2 = key1 + 1
		val keyIndex3 = key2 + 0
		val statistics = DbLogic.getPersonalStatistics(getBaseStudyId())
		assertEquals(firstDay, statistics.first)
		assertEquals(5, statistics.second[keyIndex1]?.size)
		assertEquals(1.0, statistics.second[keyIndex1]?.get(0)?.sum)
		assertEquals(0.0, statistics.second[keyIndex1]?.get(1)?.sum) //day in between
		assertEquals(3.0, statistics.second[keyIndex1]?.get(2)?.sum)
		assertEquals(4.0, statistics.second[keyIndex1]?.get(3)?.sum)
		assertEquals(5.0, statistics.second[keyIndex1]?.get(4)?.sum)
		
		assertEquals(2, statistics.second[keyIndex2]?.size) // actual entry plus first day
		assertEquals(6.0, statistics.second[keyIndex2]?.get(1)?.sum)
		
		assertEquals(2, statistics.second[keyIndex3]?.size) // actual entry plus first day
		assertEquals(7.0, statistics.second[keyIndex3]?.get(1)?.sum)
	}
	
	@Test
	fun getPersonalStatisticsPerValue() {
		val timestamp = 1114306312L
		val firstDay = timestamp - StatisticData_timed.ONE_DAY
		val key1 = "test1"
		val key2 = "test2"
		
		val obs1 = createObservedVariable(key1)
		obs1.index = 0
		obs1.save()
		
		val obs2 = createObservedVariable(key1)
		obs2.index = 1
		obs2.save()
		
		val obs3 = createObservedVariable(key2)
		obs3.index = 0
		obs3.save()
		
		//first entry
		val statisticsDataTimed1 = StatisticData_perValue("1", key1, 0, 1, getBaseStudyId())
		statisticsDataTimed1.observedId = obs1.id
		statisticsDataTimed1.save()
		
		//second entry
		val statisticsDataTimed2 = StatisticData_perValue("2", key1, 0, 1, getBaseStudyId())
		statisticsDataTimed2.observedId = obs1.id
		statisticsDataTimed2.save()
		
		//entry with different index
		val statisticsDataTimed3 = StatisticData_perValue("3", key1, 1, 1, getBaseStudyId())
		statisticsDataTimed3.observedId = obs2.id
		statisticsDataTimed3.save()
		
		//entry with different key
		val statisticsDataTimed4 = StatisticData_perValue("4", key2, 1, 1, getBaseStudyId())
		statisticsDataTimed4.observedId = obs3.id
		statisticsDataTimed4.save()
		
		
		
		
		val keyIndex1 = key1 + 0
		val keyIndex2 = key1 + 1
		val keyIndex3 = key2 + 0
		val statistics = DbLogic.getPersonalStatistics(getBaseStudyId())
		assertEquals(2, statistics.second[keyIndex1]?.size)
		assertEquals(1, statistics.second[keyIndex2]?.size)
		assertEquals(1, statistics.second[keyIndex3]?.size)
		assertEquals("1", (statistics.second[keyIndex1]?.get(0) as StatisticData_perValue).value)
		assertEquals("2", (statistics.second[keyIndex1]?.get(1) as StatisticData_perValue).value)
		assertEquals("3", (statistics.second[keyIndex2]?.get(0) as StatisticData_perValue).value)
		assertEquals("4", (statistics.second[keyIndex3]?.get(0) as StatisticData_perValue).value)
	}
	
	@Test
	fun getStudyServerUrls() {
		val title1 = "title1"
		val title2 = "title2"
		
		val url1 = testUrl
		val url2 = "https://other.url"
		
		//one study is added per default
		val defaultStudy = DbLogic.getStudy(getBaseStudyId())!!
		defaultStudy.title = title1
		defaultStudy.save()
		assertEquals(1, DbLogic.getStudyServerUrls().size)
		
		val study = createStudy()
		study.title = title2
		study.serverUrl = url2
		study.save()
		val urls = DbLogic.getStudyServerUrls()
		assertEquals(2, urls.size)
		assertEquals(title1, urls[0].first)
		assertEquals(url1, urls[0].second)
		assertEquals(title2, urls[1].first)
		assertEquals(url2, urls[1].second)
		
	}
}