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
		input.value = testValue1
		assertEquals(testValue1, input.value)
		
		
		val dynamicInput = createJsonObj<Input>(
			"""{"responseType": "dynamic_input", "subInputs":[{}]}"""
		)
		dynamicInput.value = testValue1
		
		assertEquals(testValue1, dynamicInput.value) //dynamicValue has not been initialized yet. So input.value is used
		dynamicInput.getDynamicInput(createJsonObj<Questionnaire>())
		dynamicInput.subInputs[0].value = testValue2
		assertEquals(testValue2, dynamicInput.value) //now that dynamicValue been initialized, value projects its current dynamicInputs value
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
		input.addImage("path/to/file", getBaseStudyId())
		assertEquals(testUrl, input.addedFiles[0].serverUrl)
	}
	
	private fun testDynamicInput(random: Boolean) {
		val name = "dynamic"
		val json = """{
				"name": "$name",
				"responseType": "dynamic_input",
				"random": $random,
				"subInputs":[
					{"name": "dyn1"},
					{"name": "dyn2"},
					{"name": "dyn3"},
					{"name": "dyn4"},
					{"name": "dyn5"},
					{"name": "dyn6"},
					{"name": "dyn7"},
					{"name": "dyn8"},
					{"name": "dyn9"}
				]
			}"""
		//we need new inputs every time because dynamicInput is cached
		val questionnaire = createJsonObj<Questionnaire>()
		val defaultInput = createJsonObj<Input>(json)
		
		for(tryI in 0 until 100) {
			val previousNames = ArrayList<String>()
			var subInputName: String
			// get next input; make sure that it was not selected yet (previousNames); "fill out" questionnaire; repeat
			for(i in 0 until defaultInput.subInputs.size) {
				questionnaire.lastCompleted = NativeLink.getNowMillis() + 1000 //fake filled out questionnaire
				subInputName = createJsonObj<Input>(json).getDynamicInput(questionnaire).name
				assertEquals(
					-1,
					previousNames.indexOf(subInputName),
					"$subInputName was used twice. Previous inputs: $previousNames"
				)
				previousNames.add(subInputName)
			}
			
			subInputName = createJsonObj<Input>(json).getDynamicInput(questionnaire).name
			assertNotEquals(-1, previousNames.indexOf(subInputName)) //all subInputs have been used. So now we get one we already had
			DbLogic.deleteCheckedRandomTexts(questionnaire.id, name) //clean up
		}
	}
	@Test
	fun getDynamicInput() {
		testDynamicInput(false)
		testDynamicInput(true)
	}
	
	@Test
	fun needsValue() {
		val input = createJsonObj<Input>()
		
		input.value = ""
		assertFalse(input.needsValue())
		
		input.required = true
		assertTrue(input.needsValue())
		
		input.value = testValue1
		assertFalse(input.needsValue())
	}
	
	@Test
	fun getBackupString_fromBackupString() {
		val input = createJsonObj<Input>()
		input.value = testValue1
		input.additionalValues["val1"] = testValue2
		input.additionalValues["val2"] = testValue3
		
		val backup = input.getBackupString()
		val newInput = createJsonObj<Input>()
		newInput.fromBackupString(backup)
		
		assertEquals(testValue1, newInput.value)
		assertEquals(testValue2, newInput.additionalValues["val1"])
		assertEquals(testValue3, newInput.additionalValues["val2"])
	}
	
	@Test
	fun hasScreenTracking() {
		assertFalse(createJsonObj<Input>().hasScreenTracking())
		assertTrue(createJsonObj<Input>("""{"responseType": "app_usage"}""").hasScreenTracking())
	}
	
	@Test
	fun fillIntoDataSet() {
		val questionnaire = createJsonObj<Questionnaire>()
		val dataSet = createDataSet()
		val input = createJsonObj<Input>()
		
		input.additionalValues["val1"] = testValue2
		input.additionalValues["val2"] = testValue3
		input.addImage("path", getBaseStudyId())
		input.addImage("path2", getBaseStudyId())
		input.value = testValue1 //needs to be after addImage() because sets value to the current timestamp - and we dont want to test that here
		
		input.fillIntoDataSet(dataSet)
		
		assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, 0, 0)
		assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, 0, 1)
		
		dataSet.saveQuestionnaire(questionnaire, NativeLink.getNowMillis())
		val value = getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf(testValue1))
		assertNotEquals(-1, value.indexOf(testValue2))
		assertNotEquals(-1, value.indexOf(testValue3))
	}
}