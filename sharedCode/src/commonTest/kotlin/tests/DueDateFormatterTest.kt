package tests

import BaseTest
import at.jodlidev.esmira.sharedCode.DueDateFormatter
import at.jodlidev.esmira.sharedCode.NativeLink
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by JodliDev on 26.04.2022.
 */
class DueDateFormatterTest : BaseTest() {
	@Test
	fun test() {
		val midnight = NativeLink.getMidnightMillis()
		val now = NativeLink.getNowMillis()
		val oneDay = 1000 * 60 * 60 * 24
		val soon = 1000 * 60
		
		val formatter = DueDateFormatter("soon", "today", "tomorrow", "in%1\$dDays")
		
		assertEquals("soon", formatter.get(now + soon - 1))
		assertEquals("today", formatter.get(midnight + oneDay - 1))
		assertEquals("tomorrow", formatter.get(midnight + oneDay))
		assertEquals("in2Days", formatter.get(now + oneDay*2 + 1000))
		assertEquals("in3Days", formatter.get(now + oneDay*3 + 1000))
	}
}