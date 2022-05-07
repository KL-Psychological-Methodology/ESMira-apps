package tests.data_structure

import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class DynamicInputDataTest : BaseCommonTest() {
	
	@Test
	fun save() {
		val input = DynamicInputData(-1, "test123", 2)
		input.save()
		assertSqlWasSaved(DynamicInputData.TABLE, DynamicInputData.KEY_ITEM_INDEX, 2)
		
		input.index = 5
		input.save()
		assertSqlWasUpdated(DynamicInputData.TABLE, DynamicInputData.KEY_ITEM_INDEX, 5)
	}
}