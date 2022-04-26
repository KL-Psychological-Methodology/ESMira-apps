package tests.data_structure

import BaseTest
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Page
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class PageTest : BaseDataStructureTest() {
	
	@Test
	fun inputs() {
		val input1 = createObj<Input>()
		val input2 = createObj<Input>()
		val input3 = createObj<Input>()
		val input4 = createObj<Input>()
		val orderedInputs = arrayListOf(input1, input2, input3, input4)
		
		val page = createObj<Page>()
		page.orderedInputs = orderedInputs
		assertSame(orderedInputs, page.inputs)
		
		val pageRandom = createObj<Page>()
		pageRandom.randomized = true
		pageRandom.orderedInputs = orderedInputs
		assertNotSame(orderedInputs, pageRandom.inputs)
	}
	
	@Test
	fun hasScreenTracking() {
		val input1 = createObj<Input>()
		val input2 = createObj<Input>()
		val input3 = createObj<Input>("""{"responseType": "app_usage"}""")
		val input4 = createObj<Input>()
		
		val page = createObj<Page>()
		page.orderedInputs = arrayListOf(input1, input2, input3, input4)
		assertTrue(page.hasScreenTracking())
		
		val pageNoScreenTracking = createObj<Page>()
		pageNoScreenTracking.orderedInputs = arrayListOf(input1, input2, input4)
		assertFalse(pageNoScreenTracking.hasScreenTracking())
	}
}