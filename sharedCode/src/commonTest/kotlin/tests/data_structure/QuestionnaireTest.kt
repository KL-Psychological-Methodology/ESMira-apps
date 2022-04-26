package tests.data_structure

import at.jodlidev.esmira.sharedCode.*
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class QuestionnaireTest : BaseDataStructureTest() {
	
	@Test
	fun actionTriggers() {
		val questionnaire = createObj<Questionnaire>()
		assertEquals(0, questionnaire.actionTriggers.size)
		
		val questionnaireDb = createObj<Questionnaire>()
		questionnaireDb.fromJson = false
		assertEquals(0, questionnaireDb.actionTriggers.size)
		mockTools.assertSqlWasSelected(ActionTrigger.TABLE, 0, questionnaire.id.toString())
	}
	
	@Test
	fun pages() {
		val questionnaire = createObj<Questionnaire>("""{"pages":[{},{},{},{}]}""")
		assertEquals(4, questionnaire.pages.size)
	}
	
	@Test
	fun sumScores() {
		val questionnaire = createObj<Questionnaire>("""{"sumScores":[{},{}]}""")
		assertEquals(2, questionnaire.sumScores.size)
	}
	
	@Test
	fun getQuestionnaireTitle() {
		val title = "Katara"
		val questionnaire = createObj<Questionnaire>()
		questionnaire.title = title
		assertEquals(title, questionnaire.getQuestionnaireTitle(0))
		
		val questionnaireWithPages = createObj<Questionnaire>("""{"pages":[{},{},{}]}""")
		questionnaireWithPages.title = title
		val pagesNum = questionnaireWithPages.pages.size
		assertEquals("${title} 1/$pagesNum", questionnaireWithPages.getQuestionnaireTitle(0))
		assertEquals("${title} 2/$pagesNum", questionnaireWithPages.getQuestionnaireTitle(1))
		assertEquals("${title} 3/$pagesNum", questionnaireWithPages.getQuestionnaireTitle(2))
	}
	
	//save() ist tested in DatabaseSharedTestLogic.questionnaire_save()
	
	@Test
	fun saveQuestionnaire() {
		val notifications = mockTools.getNotifications()
		val testValue = "You can't handle the truth!"
		val questionnaire = createObj<Questionnaire>("""{"pages": [{"inputs": [{}]}]}""")
		questionnaire.studyId = getBaseStudyId()
		val input = questionnaire.pages[0].inputs[0]
		input.value = testValue
		
		val lastCompleted = questionnaire.lastCompleted
		questionnaire.save(true) // so updateLastCompleted() is run completely
		questionnaire.saveQuestionnaire(0)
		
		//check if inputs are saved:
		val value = mockTools.getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf(testValue))
		
		//check if lastCompleted was updated:
		assertNotEquals(lastCompleted, questionnaire.lastCompleted)
		mockTools.assertSqlWasUpdated(Questionnaire.TABLE, Questionnaire.KEY_LAST_COMPLETED, questionnaire.lastCompleted)
		
		//check if notifications were removed:
		assertEquals(1, notifications.removeQuestionnaireBingList.size)
	}
	
	@Test
	fun checkQuestionnaire() {
		var questionnaire = createObj<Questionnaire>(
			"""{"pages": [{"inputs": [{}, {}, {}]}, {}, {"inputs": [{}, {}]}]}"""
		)
		
		for((i, _) in questionnaire.pages.withIndex()) {
			assertEquals(-1, questionnaire.checkQuestionnaire(i), "Failed on page ${i+1}")
		}
		
		questionnaire = createObj<Questionnaire>(
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
		val questionnaire = createObj<Questionnaire>()
		questionnaire.exists = true
		
		questionnaire.updateLastNotification(timestamp)
		assertEquals(timestamp, questionnaire.lastNotification)
		mockTools.assertSqlWasUpdated(Questionnaire.TABLE, Questionnaire.KEY_LAST_NOTIFICATION, timestamp)
	}
	
	@Test
	fun updateLastCompleted() {
		val timestamp = 1001L
		val questionnaire = createObj<Questionnaire>()
		questionnaire.lastCompleted = timestamp
		questionnaire.exists = true
		
		questionnaire.updateLastCompleted(false)
		assertNotEquals(timestamp, questionnaire.lastCompleted)
		mockTools.assertSqlWasUpdated(Questionnaire.TABLE, Questionnaire.KEY_LAST_COMPLETED, questionnaire.lastCompleted)
		
		
		questionnaire.updateLastCompleted(true)
		assertEquals(0, questionnaire.lastNotification)
		mockTools.assertSqlWasUpdated(Questionnaire.TABLE, Questionnaire.KEY_LAST_NOTIFICATION, 0)
	}
	
	@Test
	fun delete() {
		val notifications = mockTools.getNotifications()
		val questionnaire = createObj<Questionnaire>()
		questionnaire.delete()
		mockTools.assertSqlWasDeleted(Questionnaire.TABLE, 0, questionnaire.id.toString())
		assertEquals(1, notifications.removeQuestionnaireBingList.size)
	}
	
	@Test
	fun hasSchedules() {
		assertFalse(createObj<Questionnaire>().hasSchedules())
		assertTrue(createObj<Questionnaire>("""{"actionTriggers":[{},{"schedules":[{}]}]}""").hasSchedules())
		assertFalse(createObj<Questionnaire>("""{"actionTriggers":[{},{}]}""").hasSchedules())
	}
	
	@Test
	fun hasEvents() {
		assertFalse(createObj<Questionnaire>().hasEvents())
		assertFalse(createObj<Questionnaire>("""{"actionTriggers":[{},{}]}""").hasEvents())
		assertTrue(createObj<Questionnaire>("""{"actionTriggers":[{},{"eventTriggers":[{}]}]}""").hasEvents())
	}
	
	@Test
	fun hasDelayedEvents() {
		assertFalse(createObj<Questionnaire>().hasDelayedEvents())
		assertFalse(createObj<Questionnaire>("""{"actionTriggers":[{},{"eventTriggers":[{}]}]}""").hasDelayedEvents())
		assertTrue(createObj<Questionnaire>("""{"actionTriggers":[{},{"eventTriggers": [{"delaySec":10}]}]}""").hasDelayedEvents())
	}
	
	@Test
	fun hasNotifications() {
		assertFalse(createObj<Questionnaire>().hasNotifications())
		assertTrue(createObj<Questionnaire>("""{"actionTriggers":[{},{"actions": [{"type": 3}]}]}""").hasNotifications())
	}
	
	@Test
	fun usesPostponedActions() {
		assertFalse(createObj<Questionnaire>().usesPostponedActions())
		assertTrue(createObj<Questionnaire>("""{"actionTriggers":[{},{"eventTriggers":[{"delaySec": 10}]}]}""").usesPostponedActions())
		assertTrue(createObj<Questionnaire>("""{"actionTriggers":[{},{"schedules":[{}]}]}""").usesPostponedActions())
	}
	
	@Test
	fun hasScreenTracking() {
		assertFalse(createObj<Questionnaire>().hasScreenTracking())
		assertTrue(createObj<Questionnaire>(
			"""{"pages": [{"inputs": [{}, {}, {}]}, {}, {"inputs": [{}, {"responseType": "app_usage"}]}]}"""
		).hasScreenTracking())
	}
	
	@Test
	fun hasEditableSchedules() {
		assertFalse(createObj<Questionnaire>().hasEditableSchedules())
		assertTrue(createObj<Questionnaire>("""{"actionTriggers":[{},{"schedules":[{"userEditable": true}]}]}""").hasEditableSchedules())
		assertFalse(createObj<Questionnaire>("""{"actionTriggers":[{},{"schedules":[{"userEditable": false}]}]}""").hasEditableSchedules())
	}
	
	//isActive is tested in DatabaseSharedTestLogic.questionnaire_isActive()
	
	@Test
	fun willBeActiveIn() {
		//Note: we have to account for script running times - now is not exact because a different one is used in willBeActiveIn()
		val oneDay = 1000*60*60*24
		val variance = 1000*60
		val now = NativeLink.getNowMillis()
		val study = DbLogic.getStudy(getBaseStudyId())!!
		
		var questionnaire = createObj<Questionnaire>(
			"{\"durationStart\": ${now + oneDay*2}, \"durationStartingAfterDays\": 3}"
		)
		
		study.joined = now
		var willBeActiveIn = questionnaire.willBeActiveIn(study)
		assertTrue(willBeActiveIn > oneDay*2 - variance && willBeActiveIn <= oneDay*2)
		
		study.joined = now - oneDay*2
		willBeActiveIn = questionnaire.willBeActiveIn(study)
		assertTrue(willBeActiveIn > oneDay - variance && willBeActiveIn <= oneDay)
	}
	
	@Test
	fun canBeFilledOut() {
		val now = NativeLink.getNowMillis()
		val fromMidnight = now - NativeLink.getMidnightMillis(now)
		val oneHour = 1000*60*60
		val oneDay = oneHour*24
		
		//test completableOncePerNotification:
		
		var questionnaire = createObj<Questionnaire>(
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
		
		questionnaire = createObj<Questionnaire>(
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
		questionnaire = createObj<Questionnaire>(
			"{\"completableAtSpecificTime\": true, \"completableAtSpecificTimeStart\": ${oneHour*18}, \"completableAtSpecificTimeEnd\": ${oneHour*3}, \"pages\":[{\"items\":[{}]}]}"
		)
		assertTrue(questionnaire.canBeFilledOut(626637180000)) // 1989-11-9 18:53:00
		assertFalse(questionnaire.canBeFilledOut(1114306312000)) // 2005-04-24 03:31:52
		
		//timeframe from 03:00 - 17:00
		questionnaire = createObj<Questionnaire>(
			"{\"completableAtSpecificTime\": true, \"completableAtSpecificTimeStart\": ${oneHour*3}, \"completableAtSpecificTimeEnd\": ${oneHour*17}, \"pages\":[{\"items\":[{}]}]}"
		)
		assertFalse(questionnaire.canBeFilledOut(626637180000)) // 1989-11-9 18:53:00
		assertTrue(questionnaire.canBeFilledOut(1114306312000)) // 2005-04-24 03:31:52
		
		//timeframe after 04:00
		questionnaire = createObj<Questionnaire>(
			"{\"completableAtSpecificTime\": true, \"completableAtSpecificTimeStart\": ${oneHour*4}, \"pages\":[{\"items\":[{}]}]}"
		)
		assertTrue(questionnaire.canBeFilledOut(626637180000)) // 1989-11-9 18:53:00
		assertFalse(questionnaire.canBeFilledOut(1114306312000)) // 2005-04-24 03:31:52
		
		//timeframe before 18:00
		questionnaire = createObj<Questionnaire>(
			"{\"completableAtSpecificTime\": true, \"completableAtSpecificTimeEnd\": ${oneHour*18}, \"pages\":[{\"items\":[{}]}]}"
		)
		assertFalse(questionnaire.canBeFilledOut(626637180000)) // 1989-11-9 18:53:00
		assertTrue(questionnaire.canBeFilledOut(1114306312000)) // 2005-04-24 03:31:52
		
		//
		//test limitCompletionFrequency
		//
		
		questionnaire = createObj<Questionnaire>(
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
		
		questionnaire = createObj<Questionnaire>(
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
		val questionnaire = createObj<Questionnaire>("""
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