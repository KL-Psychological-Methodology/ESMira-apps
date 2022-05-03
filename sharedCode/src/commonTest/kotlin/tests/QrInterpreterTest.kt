package tests

import BaseTest
import at.jodlidev.esmira.sharedCode.DueDateFormatter
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.QrInterpreter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Created by JodliDev on 26.04.2022.
 */
class QrInterpreterTest : BaseTest() {
	private class TestLine(
		val url: String,
		val serverUrl: String,
		val accessKey: String = "",
		val studyId: Long = 0,
		val qId: Long = 0
	) {
		override fun toString(): String {
			return "$url, $serverUrl, $accessKey, $studyId, $qId"
		}
	}
	@Test
	fun check() {
		val testData = arrayOf(
			TestLine("jodli.dev/test1", "jodli.dev/", "test1"),
			TestLine("esmira://jodli.dev/test1", "https://jodli.dev/", "test1"),
			TestLine("https://jodli.dev/test1", "https://subdomain.jodli.dev/", "test1"),
			TestLine("https://subdomain.jodli.dev/test1", "https://subdomain.jodli.dev/", "test1"),
			TestLine("https://jodli.dev/subfolder/test1", "https://jodli.dev/subfolder/", "test1"),
			TestLine("https://大智若愚.i❤.ws/sub-földer/test1", "https://大智若愚.i❤.ws/sub-földer/", "test1"),
			
			TestLine("jodli.dev/123", "jodli.dev/", "", 123),
			TestLine("jodli.dev/123-test1", "jodli.dev/", "test1", 123),
			
			TestLine("jodli.dev/app-test1", "jodli.dev/", "test1"),
			TestLine("jodli.dev/app-123", "jodli.dev/", "", 123),
			TestLine("jodli.dev/app-123-test1", "jodli.dev/", "test1", 123),
			
			TestLine("jodli.dev/survey-12345", "jodli.dev/", "", 0, 12345),
			TestLine("jodli.dev/survey-12345-test1", "jodli.dev/", "test1", 0, 12345),
		)
		
		val interpreter = QrInterpreter()
		var data: QrInterpreter.ConnectData?
		
		for(line in testData) {
			data = interpreter.check(line.url)
			assertNotEquals(null, data, "check() with $line returned null")
			assertEquals(line.accessKey, data?.accessKey, "${data?.accessKey} not found in $line")
			assertEquals(line.studyId, data?.studyId, "${data?.studyId} not found in $line")
			assertEquals(line.qId, data?.qId, "${data?.qId} not found in $line")
		}
	}
}