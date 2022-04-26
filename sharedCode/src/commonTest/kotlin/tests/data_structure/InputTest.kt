package tests.data_structure

import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class InputTest : BaseDataStructureTest() {
	val testValue1 = "I'll be back!"
	val testValue2 = "Get to the choppa!"
	val testValue3 = "Hasta la vista baby"
	
	@Test
	fun value() {
		val input = Input()
		input.value = testValue1
		assertEquals(testValue1, input.value)
		
		
		val dynamicInput = createObj<Input>(
			"""{"responseType": "dynamic_input", "subInputs":[{}]}"""
		)
		dynamicInput.value = testValue1
		
		assertEquals(testValue1, dynamicInput.value) //dynamicValue has not been initialized yet. So input.value is used
		dynamicInput.getDynamicInput(createObj<Questionnaire>())
		dynamicInput.subInputs[0].value = testValue2
		assertEquals(testValue2, dynamicInput.value) //now that dynamicValue been initialized, value projects its current dynamicInputs value
	}
	
	@Test
	fun desc() {
		val input = createObj<Input>("""{"text":  "$testValue1"}""")
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
	
	// getDynamicInput() is tested in DatabaseSharedTestLogic.input_testDynamicInput()
	
	@Test
	fun needsValue() {
		val input = createObj<Input>()
		
		input.value = ""
		assertFalse(input.needsValue())
		
		input.required = true
		assertTrue(input.needsValue())
		
		input.value = testValue1
		assertFalse(input.needsValue())
	}
	
	@Test
	fun getBackupString_fromBackupString() {
		val input = createObj<Input>()
		input.value = testValue1
		input.additionalValues["val1"] = testValue2
		input.additionalValues["val2"] = testValue3
		
		val backup = input.getBackupString()
		val newInput = createObj<Input>()
		newInput.fromBackupString(backup)
		
		assertEquals(testValue1, newInput.value)
		assertEquals(testValue2, newInput.additionalValues["val1"])
		assertEquals(testValue3, newInput.additionalValues["val2"])
	}
	
	@Test
	fun hasScreenTracking() {
		assertFalse(createObj<Input>().hasScreenTracking())
		assertTrue(createObj<Input>("""{"responseType": "app_usage"}""").hasScreenTracking())
	}
	
	@Test
	fun fillIntoDataSet() {
		val questionnaire = createObj<Questionnaire>()
		val dataSet = createDataSet()
		val input = createObj<Input>()
		
		input.additionalValues["val1"] = testValue2
		input.additionalValues["val2"] = testValue3
		input.addImage("path", getBaseStudyId())
		input.addImage("path2", getBaseStudyId())
		input.value = testValue1 //needs to be after addImage() because sets value to the current timestamp - and we dont want to test that here
		
		input.fillIntoDataSet(dataSet)
		
		mockTools.assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, false, 0)
		mockTools.assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, false, 1)
		
		dataSet.saveQuestionnaire(questionnaire, NativeLink.getNowMillis())
		val value = mockTools.getSqlSavedValue(DataSet.TABLE, DataSet.KEY_RESPONSES) as String
		assertNotEquals(-1, value.indexOf(testValue1))
		assertNotEquals(-1, value.indexOf(testValue2))
		assertNotEquals(-1, value.indexOf(testValue3))
	}
}