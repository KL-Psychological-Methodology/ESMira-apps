package tests.data_structure

import BaseTest
import MockTools
import kotlin.test.BeforeTest

/**
 * Created by JodliDev on 26.04.2022.
 */
abstract class BaseDataStructureTest : BaseTest() {
	internal val mockTools = MockTools()
	
	@BeforeTest
	fun resetMock() {
		reset()
	}
	
	override fun reset() {
		super.reset()
		mockTools.reset()
	}
}