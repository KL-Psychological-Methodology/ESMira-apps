package tests.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class InputTest : BaseCommonTest() {
	val testValue1 = "I'll be back!"
	val testValue2 = "Get to the choppa!"
	val testValue3 = "Hasta la vista baby"
	
	@Test
	fun value() {
		val input = Input()
		input.questionnaire = DbLogic.createJsonObj()
		input.setValue(testValue1)
		assertEquals(testValue1, input.getValue())
		
		
		val dynamicInput = createJsonObj<Input>(
			"""{"responseType": "dynamic_input", "subInputs":[{}]}"""
		)
		dynamicInput.questionnaire = DbLogic.createJsonObj()
		dynamicInput.setValue(testValue1)
		
		assertEquals(testValue1, dynamicInput.getValue()) //dynamicValue has not been initialized yet. So input.value is used
		dynamicInput.questionnaire = createJsonObj()
		dynamicInput.getDynamicInput()
		dynamicInput.subInputs[0].setValue(testValue2)
		assertEquals(testValue2, dynamicInput.getValue()) //now that dynamicValue been initialized, value projects its current dynamicInputs value
	}
	
	@Test
	fun desc() {
		val input = createJsonObj<Input>("""{"text":  "$testValue1"}""")
		input.required = false
		assertEquals(testValue1, input.desc)
		input.required = true
		assertEquals("$testValue1*", input.desc)
	}
	
	@Test
	fun addImage() {
		val input = Input()
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.studyId = getBaseStudyId()
		input.questionnaire = questionnaire
		input.setFile("path/to/file")
		assertEquals("path/to/file", input.getFileName())
	}
	
	private fun testDynamicInput(random: Boolean) {
		val name = "dynamic"
		
		val study = createDbStudy(1111, """[{"pages": [{"inputs": [{
				"name": "$name",
				"responseType": "dynamic_input",
				"random": $random,
				"subInputs":[
					{"defaultValue": "dyn1"},
					{"defaultValue": "dyn2"},
					{"defaultValue": "dyn3"},
					{"defaultValue": "dyn4"},
					{"defaultValue": "dyn5"},
					{"defaultValue": "dyn6"},
					{"defaultValue": "dyn7"},
					{"defaultValue": "dyn8"},
					{"defaultValue": "dyn9"}
				]
			}]}]}]""")
		
		val questionnaire = study.questionnaires[0]
		val input = questionnaire.pages[0].inputs[0]
		
		for(tryI in 0 until 100) {
			val previousValues = ArrayList<String>()
			var subInputValue: String
			// get next input; make sure that it was not selected yet (previousNames); "fill out" questionnaire; repeat
			for(i in 0 until input.subInputs.size) {
				val loadedQuestionnaire = DbLogic.getQuestionnaire(questionnaire.id) ?: throw Exception()
				loadedQuestionnaire.saveQuestionnaire()
				loadedQuestionnaire.metadata.lastCompleted += 1000 // getDynamicInput() needs happen after last completion or it will return the same value. And sometimes kotlin is too fast
				val currentInput = loadedQuestionnaire.pages[0].inputs[0]
				subInputValue = currentInput.getDynamicInput().defaultValue
				assertEquals(
					-1,
					previousValues.indexOf(subInputValue),
					"$subInputValue was used twice. Previous inputs: $previousValues"
				)
				previousValues.add(subInputValue)
			}
			val loadedQuestionnaire = DbLogic.getQuestionnaire(questionnaire.id) ?: throw Exception()
			loadedQuestionnaire.saveQuestionnaire()
			loadedQuestionnaire.metadata.lastCompleted += 1000 // getDynamicInput() needs happen after last completion or it will return the same value. And sometimes kotlin is too fast
			val currentInput = loadedQuestionnaire.pages[0].inputs[0]
			subInputValue = currentInput.getDynamicInput().defaultValue
			assertNotEquals(-1, previousValues.indexOf(subInputValue)) //all subInputs have been used. So now we get one we already had
			DbLogic.deleteCheckedRandomTexts(questionnaire.id, name) //clean up
		}
	}
	@Test
	fun getDynamicInput() {
//		testDynamicInput(false)
		testDynamicInput(true)
	}
	
	@Test
	fun needsValue() {
		val input = createJsonObj<Input>()
		input.questionnaire = DbLogic.createJsonObj()
		
		input.setValue("")
		assertFalse(input.needsValue())
		
		input.required = true
		assertTrue(input.needsValue())
		
		input.setValue(testValue1)
		assertFalse(input.needsValue())
	}
	
	@Test
	fun getBackupString_fromBackupString() {
		val input = createJsonObj<Input>()
		input.questionnaire = DbLogic.createJsonObj()
		input.setValue(testValue1, mapOf(Pair("val1", testValue2), Pair("val2", testValue3)))
		
		val backup = input.getBackupString()
		val newInput = createJsonObj<Input>()
		newInput.fromBackupString(backup)
		
		assertEquals(testValue1, newInput.getValue())
		assertEquals(testValue2, newInput.getAdditional("val1"))
		assertEquals(testValue3, newInput.getAdditional("val2"))
	}
	
	@Test
	fun hasScreenTracking() {
		assertFalse(createJsonObj<Input>().hasScreenOrAppTracking())
		assertTrue(createJsonObj<Input>("""{"responseType": "app_usage"}""").hasScreenOrAppTracking())
	}
	
	@Test
	fun fillIntoDataSet() {
		val study = DbLogic.createJsonObj<Study>(
			"""{"id": 2222, "questionnaires": [{"durationPeriodDays": 2, "pages": [{"inputs": [{}]}] }]}"""
		)
		study.finishJSON("exampleUrl", "")
		study.join()
		val input = study.questionnaires[0].pages[0].inputs[0]
		
		val questionnaire = createJsonObj<Questionnaire>()
		val dataSet = createDataSet()
//		val input = createJsonObj<Input>()
		
//		input.questionnaire = questionnaire
		input.setValue("", mapOf(Pair("val1", testValue1), Pair("val2", testValue2)))
		input.setFile("path")
		input.setValue(testValue1) //needs to be after setFile() because sets value to the current timestamp - and we dont want to test that here
		
		input.fillIntoDataSet(dataSet)
		
		assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, 0, 0)
		
		dataSet.saveQuestionnaire(questionnaire)
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf(testValue1))
		assertNotEquals(-1, value.indexOf(testValue2))
	}
}