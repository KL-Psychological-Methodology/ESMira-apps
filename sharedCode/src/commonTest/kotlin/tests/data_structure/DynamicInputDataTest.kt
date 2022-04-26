package tests.data_structure

import BaseTest
import MockTools
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class DynamicInputDataTest : BaseDataStructureTest() {
	
	@Test
	fun save() {
		val input = DynamicInputData(-1, "test123", 2)
		input.save()
		mockTools.assertSqlWasSaved(DynamicInputData.TABLE, DynamicInputData.KEY_ITEM_INDEX, 2)
		
		input.index = 5
		input.save()
		mockTools.assertSqlWasUpdated(DynamicInputData.TABLE, DynamicInputData.KEY_ITEM_INDEX, 5)
	}
}