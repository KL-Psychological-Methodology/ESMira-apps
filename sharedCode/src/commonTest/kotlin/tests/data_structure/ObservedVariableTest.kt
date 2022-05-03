package tests.data_structure

import at.jodlidev.esmira.sharedCode.data_structure.ObservedVariable
import at.jodlidev.esmira.sharedCode.data_structure.statistics.Condition
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perValue
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_timed
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class ObservedVariableTest : BaseCommonTest() {
	@Test
	fun finishJSON() {
		val variableName = "Katara"
		
		val study = createStudy()
		val observedVariable1 = createObservedVariable(variableName)
		val firstId = observedVariable1.id
		
		observedVariable1.finishJSON(study, 0, variableName)
		assertEquals(firstId, observedVariable1.id)
		observedVariable1.save()
		assertNotEquals(firstId, observedVariable1.id)
		
		val observedVariable2 = createObservedVariable(variableName)
		observedVariable2.finishJSON(study, 0, variableName)
		assertEquals(observedVariable1.id, observedVariable2.id)
		
		val observedVariable3 = createObservedVariable(variableName)
		observedVariable3.finishJSON(study, 0, "Zuko")
		assertEquals(firstId, observedVariable3.id)
	}
	
	@Test
	fun save() {
		val variableName = "Katara"
		
		val observedVariable = createObservedVariable(variableName)
		observedVariable.save()
		assertSqlWasSaved(ObservedVariable.TABLE, ObservedVariable.KEY_VARIABLE_NAME, variableName)
		
		observedVariable.save()
		assertSqlWasUpdated(ObservedVariable.TABLE, ObservedVariable.KEY_VARIABLE_NAME, variableName)
		
	}
	
	@Test
	fun checkCondition() {
		val equalValidValue = 10.0
		val equalInvalidValue = 123123
		val unequalValidValue = 123123
		val unequalInvalidValue = "unequal"
		val greaterValidValue = 13.0
		val greaterInvalidValue = 11.0
		val lesserValidValue = 12.0
		val lesserInvalidValue = 14.0
		
		val observedVariable = createObservedVariable("test")
		observedVariable.conditionsJson = """
		[
			{
				"key": "equal",
				"value": "10"
			},
			{
				"key": "unequal",
				"value": "unequal",
				"operator": 1
			},
			{
				"key": "greater",
				"value": "12.0",
				"operator": 2
			},
			{
				"key": "lesser",
				"value": "13",
				"operator": 3
			}
		]
		"""
		val responses = HashMap<String, JsonElement>()
		
		responses["irrelevant"] = JsonPrimitive(123)
		responses["equal"] = JsonPrimitive(equalValidValue)
		responses["unequal"] = JsonPrimitive(unequalValidValue)
		responses["greater"] = JsonPrimitive(greaterValidValue)
		responses["lesser"] = JsonPrimitive(lesserValidValue)
		
		//
		//TYPE_AND
		//
		
		observedVariable.conditionType = Condition.TYPE_AND
		
		//all conditions are met
		assertTrue(observedVariable.checkCondition(responses))
		
		//equal is not met
		responses["equal"] = JsonPrimitive(equalInvalidValue)
		assertFalse(observedVariable.checkCondition(responses))
		responses["equal"] = JsonPrimitive(equalValidValue)
		
		//unequal is not met
		assertTrue(observedVariable.checkCondition(responses))
		responses["unequal"] = JsonPrimitive(unequalInvalidValue)
		assertFalse(observedVariable.checkCondition(responses))
		responses["unequal"] = JsonPrimitive(unequalValidValue)
		
		//greater is not met
		assertTrue(observedVariable.checkCondition(responses))
		responses["greater"] = JsonPrimitive(greaterInvalidValue)
		assertFalse(observedVariable.checkCondition(responses))
		responses["greater"] = JsonPrimitive(greaterValidValue)
		
		//lesser is not met
		assertTrue(observedVariable.checkCondition(responses))
		responses["lesser"] = JsonPrimitive(lesserInvalidValue)
		assertFalse(observedVariable.checkCondition(responses))
		responses["lesser"] = JsonPrimitive(lesserValidValue)
		
		
		//
		//TYPE_OR
		//
		
		observedVariable.conditionType = Condition.TYPE_OR
		
		//all conditions are met
		assertTrue(observedVariable.checkCondition(responses))
		
		//equal is not met
		responses["equal"] = JsonPrimitive(equalInvalidValue)
		assertTrue(observedVariable.checkCondition(responses))
		
		//unequal is not met
		responses["unequal"] = JsonPrimitive(unequalInvalidValue)
		assertTrue(observedVariable.checkCondition(responses))
		
		//greater is not met
		responses["greater"] = JsonPrimitive(greaterInvalidValue)
		assertTrue(observedVariable.checkCondition(responses))
		
		//lesser is not met (nothing is met now)
		responses["lesser"] = JsonPrimitive(lesserInvalidValue)
		assertFalse(observedVariable.checkCondition(responses))
	}
	
	@Test
	fun createStatistic() {
		val variableName = "Katara"
		val testValue = 10.0
		
		val responses = HashMap<String, JsonElement>()
		
		//test STORAGE_TYPE_TIMED
		responses[variableName] = JsonPrimitive(testValue)
		createObservedVariable(variableName,
			"""{"storageType": ${ObservedVariable.STORAGE_TYPE_TIMED}}"""
		).createStatistic(responses)
		assertSqlWasSaved(StatisticData_timed.TABLE, StatisticData_timed.KEY_SUM, testValue)
		
		//test STORAGE_TYPE_FREQ_DISTR
		responses[variableName] = JsonPrimitive(testValue)
		createObservedVariable(variableName,
			"""{"storageType": ${ObservedVariable.STORAGE_TYPE_FREQ_DISTR}}"""
		).createStatistic(responses)
		assertSqlWasSaved(StatisticData_perValue.TABLE, StatisticData_perValue.KEY_VALUE, testValue.toString())
		
		//test not valid value
		responses[variableName] = JsonPrimitive("not valid")
		createObservedVariable(variableName,
			"""{"storageType": ${ObservedVariable.STORAGE_TYPE_TIMED}}"""
		).createStatistic(responses)
		assertSqlWasUpdated(StatisticData_timed.TABLE, StatisticData_timed.KEY_SUM, testValue)
	}
}