package tests.data_structure

import at.jodlidev.esmira.sharedCode.*
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class QuestionnaireTest : BaseCommonTest() {
	
	@Test
	fun actionTriggers() {
		val questionnaire = createJsonObj<Questionnaire>()
		assertEquals(0, questionnaire.actionTriggers.size)
		
		val questionnaireDb = createJsonObj<Questionnaire>()
		questionnaireDb.fromJsonOrUpdated = false
		assertEquals(0, questionnaireDb.actionTriggers.size)
		assertSqlWasSelected(ActionTrigger.TABLE, 0, questionnaire.id.toString())
	}
	
	@Test
	fun pages() {
		val questionnaire = createJsonObj<Questionnaire>("""{"pages":[{},{},{},{}]}""")
		assertEquals(4, questionnaire.pages.size)
	}
	
	@Test
	fun sumScores() {
		val questionnaire = createJsonObj<Questionnaire>("""{"sumScores":[{},{}]}""")
		assertEquals(2, questionnaire.sumScores.size)
	}
	
	@Test
	fun getQuestionnaireTitle() {
		val title = "Katara"
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.title = title
		assertEquals(title, questionnaire.getQuestionnaireTitle(0))
		
		val questionnaireWithPages = createJsonObj<Questionnaire>("""{"pages":[{},{},{}]}""")
		questionnaireWithPages.title = title
		val pagesNum = questionnaireWithPages.pages.size
		assertEquals("${title} 1/$pagesNum", questionnaireWithPages.getQuestionnaireTitle(0))
		assertEquals("${title} 2/$pagesNum", questionnaireWithPages.getQuestionnaireTitle(1))
		assertEquals("${title} 3/$pagesNum", questionnaireWithPages.getQuestionnaireTitle(2))
	}
	
	@Test
	fun save() {
		val questionnaire = createJsonObj<Questionnaire>("""{"title": "Azula", "actionTriggers":[{},{}]}""")
		questionnaire.save(true)
		assertEquals(questionnaire.title, DbLogic.getQuestionnaire(questionnaire.id)?.title)
		assertEquals(2, DbLogic.getActionTriggers(questionnaire.studyId).size)
		assertEquals(0, notifications.fireSchedulesChangedList.size)
		
		questionnaire.title = "new"
		questionnaire.save(true)
		assertEquals("new", DbLogic.getQuestionnaire(questionnaire.id)?.title)
		assertEquals(0, notifications.fireSchedulesChangedList.size)
		
		val questionnaireDifferent = createJsonObj<Questionnaire>("""{"actionTriggers":[{},{},{}]}""")
		questionnaireDifferent.studyId = getBaseStudyId()
		questionnaireDifferent.fromJsonOrUpdated = true
		questionnaireDifferent.exists = true
		questionnaireDifferent.id = questionnaire.id
		questionnaireDifferent.save(true)
		assertEquals(1, notifications.fireSchedulesChangedList.size)
		assertEquals(3, DbLogic.getActionTriggers(questionnaireDifferent.studyId).size)
	}
	
	@Test
	fun saveQuestionnaire() {
		val testValue = "You can't handle the truth!"
		val questionnaire = createJsonObj<Questionnaire>("""{"pages": [{"inputs": [{}]}]}""")
		questionnaire.studyId = getBaseStudyId()
		val input = questionnaire.pages[0].inputs[0]
		input.value = testValue
		
		val lastCompleted = questionnaire.lastCompleted
		questionnaire.save(true) // so updateLastCompleted() is run completely
		questionnaire.saveQuestionnaire(0)
		
		//check if inputs are saved:
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf(testValue))
		
		//check if lastCompleted was updated:
		assertNotEquals(lastCompleted, questionnaire.lastCompleted)
		assertSqlWasUpdated(Questionnaire.TABLE, Questionnaire.KEY_LAST_COMPLETED, questionnaire.lastCompleted)
		
		//check if notifications were removed:
		assertEquals(1, notifications.removeQuestionnaireBingList.size)
	}
	
	@Test
	fun checkQuestionnaire() {
		var questionnaire = createJsonObj<Questionnaire>(
			"""{"pages": [{"inputs": [{}, {}, {}]}, {}, {"inputs": [{}, {}]}]}"""
		)
		
		for((i, _) in questionnaire.pages.withIndex()) {
			assertEquals(-1, questionnaire.checkQuestionnaire(i), "Failed on page ${i+1}")
		}
		
		questionnaire = createJsonObj<Questionnaire>(
			"""{"pages": [
				{"inputs": [{}, {"required": true}, {}]},
				{"inputs": [{"required": true}, {}, {"required": true}]},
				{"inputs": [{"required": true}]}
			]}"""
		)
		
		assertEquals(1, questionnaire.checkQuestionnaire(0))
		assertEquals(0, questionnaire.checkQuestionnaire(1))
		assertEquals(0, questionnaire.checkQuestionnaire(2))
		
		questionnaire.pages[0].inputs[0].value = "It's alive! It's alive!"
		assertEquals(1, questionnaire.checkQuestionnaire(0))
		questionnaire.pages[0].inputs[2].value = "It's alive! It's alive!"
		assertEquals(1, questionnaire.checkQuestionnaire(0))
		questionnaire.pages[0].inputs[1].value = "It's alive! It's alive!"
		assertEquals(-1, questionnaire.checkQuestionnaire(0))
		
		questionnaire.pages[1].inputs[0].value = "It's alive! It's alive!"
		assertEquals(2, questionnaire.checkQuestionnaire(1))
		questionnaire.pages[1].inputs[1].value = "It's alive! It's alive!"
		assertEquals(2, questionnaire.checkQuestionnaire(1))
		questionnaire.pages[1].inputs[2].value = "It's alive! It's alive!"
		assertEquals(-1, questionnaire.checkQuestionnaire(1))
		
		questionnaire.pages[2].inputs[0].value = "It's alive! It's alive!"
		assertEquals(-1, questionnaire.checkQuestionnaire(2))
	}
	
	@Test
	fun updateLastNotification() {
		val timestamp = 1001L
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.exists = true
		
		questionnaire.updateLastNotification(timestamp)
		assertEquals(timestamp, questionnaire.lastNotification)
		assertSqlWasUpdated(Questionnaire.TABLE, Questionnaire.KEY_LAST_NOTIFICATION, timestamp)
	}
	
	@Test
	fun updateLastCompleted() {
		val timestamp = 1001L
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.lastCompleted = timestamp
		questionnaire.exists = true
		
		questionnaire.updateLastCompleted(false)
		assertNotEquals(timestamp, questionnaire.lastCompleted)
		assertSqlWasUpdated(Questionnaire.TABLE, Questionnaire.KEY_LAST_COMPLETED, questionnaire.lastCompleted)
		
		
		questionnaire.updateLastCompleted(true)
		assertEquals(0, questionnaire.lastNotification)
		assertSqlWasUpdated(Questionnaire.TABLE, Questionnaire.KEY_LAST_NOTIFICATION, 0)
	}
	
	@Test
	fun delete() {
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.delete()
		assertSqlWasDeleted(Questionnaire.TABLE, 0, questionnaire.id.toString())
		assertEquals(1, notifications.removeQuestionnaireBingList.size)
	}
	
	@Test
	fun hasSchedules() {
		assertFalse(createJsonObj<Questionnaire>().hasSchedules())
		assertTrue(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"schedules":[{}]}]}""").hasSchedules())
		assertFalse(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{}]}""").hasSchedules())
	}
	
	@Test
	fun hasEvents() {
		assertFalse(createJsonObj<Questionnaire>().hasEvents())
		assertFalse(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{}]}""").hasEvents())
		assertTrue(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"eventTriggers":[{}]}]}""").hasEvents())
	}
	
	@Test
	fun hasDelayedEvents() {
		assertFalse(createJsonObj<Questionnaire>().hasDelayedEvents())
		assertFalse(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"eventTriggers":[{}]}]}""").hasDelayedEvents())
		assertTrue(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"eventTriggers": [{"delaySec":10}]}]}""").hasDelayedEvents())
	}
	
	@Test
	fun hasNotifications() {
		assertFalse(createJsonObj<Questionnaire>().hasNotifications())
		assertTrue(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"actions": [{"type": 3}]}]}""").hasNotifications())
	}
	
	@Test
	fun usesPostponedActions() {
		assertFalse(createJsonObj<Questionnaire>().usesPostponedActions())
		assertTrue(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"eventTriggers":[{"delaySec": 10}]}]}""").usesPostponedActions())
		assertTrue(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"schedules":[{}]}]}""").usesPostponedActions())
	}
	
	@Test
	fun hasScreenTracking() {
		assertFalse(createJsonObj<Questionnaire>().hasScreenOrAppTracking())
		assertTrue(createJsonObj<Questionnaire>(
			"""{"pages": [{"inputs": [{}, {}, {}]}, {}, {"inputs": [{}, {"responseType": "app_usage"}]}]}"""
		).hasScreenOrAppTracking())
	}
	
	@Test
	fun hasEditableSchedules() {
		assertFalse(createJsonObj<Questionnaire>().hasEditableSchedules())
		assertTrue(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"schedules":[{"userEditable": true}]}]}""").hasEditableSchedules())
		assertFalse(createJsonObj<Questionnaire>("""{"actionTriggers":[{},{"schedules":[{"userEditable": false}]}]}""").hasEditableSchedules())
	}
	
	@Test
	fun isActive() {
		val now = NativeLink.getNowMillis()
		val study = createStudy()
		study.group = 2
		study.joinedTimestamp = now
		study.save()
		
		//test durationPeriodDays:
		var questionnaire = createJsonObj<Questionnaire>("{\"durationPeriodDays\": 2}") {it.studyId = study.id}
		assertTrue(questionnaire.isActive())
		
		study.joinedTimestamp = now - (1000*60*60*24*2 + 1)
		study.save()
		assertFalse(questionnaire.isActive())
		
		
		//test durationStartingAfterDays:
		questionnaire = createJsonObj<Questionnaire>("{\"durationStartingAfterDays\": 2}") {it.studyId = study.id}
		study.joinedTimestamp = now - 1000*60*60*24*1
		study.save()
		assertFalse(questionnaire.isActive())
		
		study.joinedTimestamp = now - (1000*60*60*24*2 + 1)
		study.save()
		assertTrue(questionnaire.isActive())
		
		
		//test limitToGroup
		assertTrue(
			createJsonObj<Questionnaire>("{\"limitToGroup\": 0}") {it.studyId = study.id}.isActive()
		)
		assertFalse(
			createJsonObj<Questionnaire>("{\"limitToGroup\": 1}") {it.studyId = study.id}.isActive()
		)
		assertTrue(
			createJsonObj<Questionnaire>("{\"limitToGroup\": 2}") {it.studyId = study.id}.isActive()
		)
		
		
		//test durationStart
		assertTrue(createJsonObj<Questionnaire>("{\"durationStart\": $now}").isActive())
		assertFalse(createJsonObj<Questionnaire>("{\"durationStart\": ${now + 1000 * 60}}").isActive())
		
		
		//test durationEnd
		assertTrue(createJsonObj<Questionnaire>("{\"durationEnd\": ${now + 1000 * 60}}").isActive())
		assertFalse(createJsonObj<Questionnaire>("{\"durationEnd\": ${now - 1}}").isActive())
		
		
		//test completableOnce
		questionnaire = createJsonObj<Questionnaire>("{\"completableOnce\": true}")
		assertTrue(questionnaire.isActive())
		questionnaire.lastCompleted = now
		assertFalse(questionnaire.isActive())
	}
	
	
	@Test
	fun willBeActiveIn() {
		//Note: we have to account for script running times - now is not exact because a different one is used in willBeActiveIn()
		val oneDay = 1000*60*60*24
		val variance = 1000*60
		val now = NativeLink.getNowMillis()
		val study = DbLogic.getStudy(getBaseStudyId())!!
		
		val questionnaire = createJsonObj<Questionnaire>(
			"{\"durationStart\": ${now + oneDay*2}, \"durationStartingAfterDays\": 3}"
		)
		
		study.joinedTimestamp = now
		var willBeActiveIn = questionnaire.willBeActiveIn(study)
		assertTrue(willBeActiveIn > oneDay*2 - variance && willBeActiveIn <= oneDay*2)
		
		study.joinedTimestamp = now - oneDay*2
		willBeActiveIn = questionnaire.willBeActiveIn(study)
		assertTrue(willBeActiveIn > oneDay - variance && willBeActiveIn <= oneDay)
	}
	
	@Test
	fun canBeFilledOut() {
		//we have to be careful because of the users timezone:
//		val targetDate = NativeLink.getMidnightMillis(1114313512000) + 12712000 // 2005-04-24 03:31:52
		val targetDate1 = NativeLink.getMidnightMillis(1114313512000) + 12712000 // 2005-04-24 03:31:52
		val targetDate2 = NativeLink.getMidnightMillis(626637180000) + 67980000 // 1989-11-9 18:53:00
		val now = NativeLink.getNowMillis()
		val fromMidnight = now - NativeLink.getMidnightMillis(now)
		val oneHour = 1000*60*60
		val oneDay = oneHour*24
		
		//test completableOncePerNotification:
		
		var questionnaire = createJsonObj<Questionnaire>(
			"{\"completableOncePerNotification\": true, \"pages\":[{\"items\":[{}]}]}"
		)
		assertFalse(questionnaire.canBeFilledOut(now)) //no notification
		questionnaire.lastNotification = now
		questionnaire.lastCompleted = now-1 //completed before notification
		assertTrue(questionnaire.canBeFilledOut(now))
		questionnaire.lastCompleted = now+1
		assertFalse(questionnaire.canBeFilledOut(now)) // was completed after notification
		
		//
		//test completableOncePerNotification and completableMinutesAfterNotification:
		//
		
		questionnaire = createJsonObj<Questionnaire>(
			"{\"completableOncePerNotification\": true, \"completableMinutesAfterNotification\": 2, \"pages\":[{\"items\":[{}]}]}"
		)
		assertFalse(questionnaire.canBeFilledOut(now)) //no notification
		questionnaire.lastNotification = now
		questionnaire.lastCompleted = now - 1000*60*2 - 2 //completed before notification
		assertTrue(questionnaire.canBeFilledOut(now))
		questionnaire.lastNotification = now - 1000*60*2 - 1
		assertFalse(questionnaire.canBeFilledOut(now)) //notification was longer than completableMinutesAfterNotification
		
		//
		//test completableAtSpecificTime and completableAtSpecificTimeStart and completableAtSpecificTimeEnd:
		//
		
		//timeframe from 18:00 - 03:00
		questionnaire = createJsonObj<Questionnaire>(
			"{\"completableAtSpecificTime\": true, \"completableAtSpecificTimeStart\": ${oneHour*18}, \"completableAtSpecificTimeEnd\": ${oneHour*3}, \"pages\":[{\"items\":[{}]}]}"
		)
		assertTrue(questionnaire.canBeFilledOut(targetDate2)) // 1989-11-9 18:53:00
		assertFalse(questionnaire.canBeFilledOut(targetDate1)) // 2005-04-24 03:31:52
		
		//timeframe from 03:00 - 17:00
		questionnaire = createJsonObj<Questionnaire>(
			"{\"completableAtSpecificTime\": true, \"completableAtSpecificTimeStart\": ${oneHour*3}, \"completableAtSpecificTimeEnd\": ${oneHour*17}, \"pages\":[{\"items\":[{}]}]}"
		)
		assertFalse(questionnaire.canBeFilledOut(targetDate2)) // 1989-11-9 18:53:00
		assertTrue(questionnaire.canBeFilledOut(targetDate1)) // 2005-04-24 03:31:52
		
		//timeframe after 04:00
		questionnaire = createJsonObj<Questionnaire>(
			"{\"completableAtSpecificTime\": true, \"completableAtSpecificTimeStart\": ${oneHour*4}, \"pages\":[{\"items\":[{}]}]}"
		)
		assertTrue(questionnaire.canBeFilledOut(targetDate2)) // 1989-11-9 18:53:00
		assertFalse(questionnaire.canBeFilledOut(targetDate1)) // 2005-04-24 03:31:52
		
		//timeframe before 18:00
		questionnaire = createJsonObj<Questionnaire>(
			"{\"completableAtSpecificTime\": true, \"completableAtSpecificTimeEnd\": ${oneHour*18}, \"pages\":[{\"items\":[{}]}]}"
		)
		assertFalse(questionnaire.canBeFilledOut(targetDate2)) // 1989-11-9 18:53:00
		assertTrue(questionnaire.canBeFilledOut(targetDate1)) // 2005-04-24 03:31:52
		
		//
		//test limitCompletionFrequency
		//
		
		questionnaire = createJsonObj<Questionnaire>(
			"{\"limitCompletionFrequency\": true, \"completionFrequencyMinutes\": 60, \"pages\":[{\"items\":[{}]}]}"
		)
		assertTrue(questionnaire.canBeFilledOut(now))
		questionnaire.lastCompleted = now - (oneHour-1) //completed less than an hour ago
		assertFalse(questionnaire.canBeFilledOut(now))
		questionnaire.lastCompleted = now - (oneHour+1) //completed more than an hour ago
		assertTrue(questionnaire.canBeFilledOut(now))
		
		//
		//test publishedAndroid or publishedIOS
		//
		
		questionnaire = createJsonObj<Questionnaire>(
			"{\"pages\":[{\"items\":[{}]}]}"
		)
		if(NativeLink.smartphoneData.phoneType == PhoneType.Android) {
			questionnaire.publishedIOS = false
			assertTrue(questionnaire.canBeFilledOut(now))
			questionnaire.publishedAndroid = false
		}
		else if(NativeLink.smartphoneData.phoneType == PhoneType.IOS) {
			questionnaire.publishedAndroid = false
			assertTrue(questionnaire.canBeFilledOut(now))
			questionnaire.publishedIOS = false
		}
		assertFalse(questionnaire.canBeFilledOut(now))
	}
	
	@Test
	fun questionnairePageHasRequired() {
		val questionnaire = createJsonObj<Questionnaire>("""
			{"pages":[
				{"inputs":[{},{"required":true}]},
				{"inputs":[{},{}]},
				{"inputs":[{"required":true}]}
			]}
		""")
		
		assertTrue(questionnaire.questionnairePageHasRequired(0))
		assertFalse(questionnaire.questionnairePageHasRequired(1))
		assertTrue(questionnaire.questionnairePageHasRequired(2))
	}
}