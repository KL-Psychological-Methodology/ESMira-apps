package tests

import BaseCommonTest
import at.jodlidev.esmira.sharedCode.*
import at.jodlidev.esmira.sharedCode.data_structure.DataSet
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.sharedCode.data_structure.StudyToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Created by JodliDev on 26.04.2022.
 */
class WebTest : BaseCommonTest() {
	
	@Test
	fun getStudyInfoMapForUpdates() {
		//these studies are supposed to be ignored:
		Study.newInstance(testUrl, testAccessKey, """{"id":$studyWebId}""").save()
		Study.newInstance(testUrl, testAccessKey, """{"id":$studyWebId}""").save()
		
		
		val entries = mapOf(
			Pair("url1", arrayOf(
				Triple(1L, "accessKey1", 1),
				Triple(2L, "accessKey2", 2),
				Triple(3L, "accessKey3", 3)
			)),
			Pair("url2", arrayOf(
				Triple(4L, "accessKey4", 4),
				Triple(5L, "accessKey5", 5),
			))
		)
		
		for((url, studyDataList) in entries) {
			for(studyData in studyDataList) {
				val (webId, accessKey, version) = studyData
				Study.newInstance(
					url, accessKey,
					"""{"id":$webId, "version": $version, "questionnaires":[{}]}"""
				).join()
			}
		}
		val map = Web().getStudyInfoMapForUpdates(true)
		
		assertEquals(entries.size, map.size)
		for((url, studyDataList) in entries) {
			val studyInfoList = map[url]!!
			assertEquals(studyInfoList.size, studyDataList.size)
			for(studyData in studyDataList) {
				val (webId, accessKey, version) = studyData
				val studyInfo = studyInfoList[webId.toString()]
				assertEquals(accessKey, studyInfo?.accessKey)
				assertEquals(version, studyInfo?.version)
			}
		}
	}
	
	@Test
	fun processStudyUpdateResponse() {
		val url = "url"
		
		val entries = mapOf(
			Pair(1, Triple("""{}""", 0, false)),
			Pair(2, Triple("""{"study": {"id":2}}""", 1, false)),
			Pair(3, Triple("""{"msgs":[{"content": "text", "sent": 0}, {"content": "text", "sent": 0}]}""", 0, true)),
		)
		
		for((webId, data) in entries) {
			val (response, expectedUpdate, expectedMsgs) = data
			Study.newInstance(url, testAccessKey, """{"id":$webId}""").save()
			notifications.reset()
			val updateCount = Web().processStudyUpdateResponse(url, """{"$webId":$response}""")
			assertEquals(expectedUpdate, updateCount)
			assertEquals(if(expectedMsgs) 1 else 0, notifications.fireMessageNotificationList.size)
		}
		
	}
	
	@Test
	fun processSyncData() {
		val study = createStudy()
		DataSet.createShortDataSet(DataSet.TYPE_JOIN, study)
		DataSet.createShortDataSet(DataSet.TYPE_JOIN, study)
		DataSet.createShortDataSet(DataSet.TYPE_JOIN, study)
		val (dataSet1, dataSet2, dataSet3) = DbLogic.getUnSyncedDataSets()[testUrl]!!
		
		val response = """{
			"states": [
				{"dataSetId": ${dataSet1.id}, "success": true, "error": ""},
				{"dataSetId": ${dataSet2.id}, "success": false, "error": ""},
				{"dataSetId": ${dataSet3.id}, "success": true, "error": ""}
			],
			"tokens": {
				5: 1111,
				50: 1111,
				500: 1111,
				5000: 1111
			}
		}"""
		
		Web().processSyncData(testUrl, response)
		
		//datasets;
		assertEquals(DataSet.STATES.SYNCED, DbLogic.getDataSet(dataSet1.id)?.synced)
		assertEquals(DataSet.STATES.NOT_SYNCED_ERROR, DbLogic.getDataSet(dataSet2.id)?.synced)
		assertEquals(DataSet.STATES.SYNCED, DbLogic.getDataSet(dataSet3.id)?.synced)
		
		//token:
		assertTrue(StudyToken.hasToken(5))
		assertTrue(StudyToken.hasToken(50))
		assertTrue(StudyToken.hasToken(500))
		assertTrue(StudyToken.hasToken(5000))
	}
}