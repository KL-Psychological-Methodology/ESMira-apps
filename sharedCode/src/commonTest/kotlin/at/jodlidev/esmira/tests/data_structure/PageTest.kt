package tests.data_structure

import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.sharedCode.data_structure.Page
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class PageTest : BaseCommonTest() {
	
	@Test
	fun inputs() {
		val input1 = createJsonObj<Input>()
		val input2 = createJsonObj<Input>()
		val input3 = createJsonObj<Input>()
		val input4 = createJsonObj<Input>()
		val orderedInputs = arrayListOf(input1, input2, input3, input4)
		
		val page = createJsonObj<Page>()
		page.orderedInputs = orderedInputs
		assertSame(orderedInputs, page.inputs)
		
		val pageRandom = createJsonObj<Page>()
		pageRandom.randomized = true
		pageRandom.orderedInputs = orderedInputs
		assertNotSame(orderedInputs, pageRandom.inputs)
	}
	
	@Test
	fun hasScreenTracking() {
		val input1 = createJsonObj<Input>()
		val input2 = createJsonObj<Input>()
		val input3 = createJsonObj<Input>("""{"responseType": "app_usage"}""")
		val input4 = createJsonObj<Input>()
		
		val page = createJsonObj<Page>()
		page.orderedInputs = arrayListOf(input1, input2, input3, input4)
		assertTrue(page.hasScreenOrAppTracking())
		
		val pageNoScreenTracking = createJsonObj<Page>()
		pageNoScreenTracking.orderedInputs = arrayListOf(input1, input2, input4)
		assertFalse(pageNoScreenTracking.hasScreenOrAppTracking())
	}
}