package tests.data_structure

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class ErrorBoxTest : BaseCommonTest() {
	
	@Test
	fun export() {
		ErrorBox.log("title123", "msg123")
		val error = DbLogic.getErrors()[0]
		val export = error.export()
		
		assertNotEquals(-1, export.indexOf("title123"))
		assertNotEquals(-1, export.indexOf("msg123"))
	}
	
	@Test
	fun getReportHeader() {
		val header = ErrorBox.getReportHeader("comment123")
		
		assertNotEquals(-1, header.indexOf("comment123"))
		assertNotEquals(-1, header.indexOf(NativeLink.smartphoneData.appType))
		assertNotEquals(-1, header.indexOf(NativeLink.smartphoneData.appVersion))
		assertNotEquals(-1, header.indexOf(NativeLink.smartphoneData.model))
		assertNotEquals(-1, header.indexOf(NativeLink.smartphoneData.manufacturer))
		assertNotEquals(-1, header.indexOf(NativeLink.smartphoneData.osVersion))
		
	}
	
	
	@Test
	fun log() {
		ErrorBox.log("title123", "msg123")
		
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_SEVERITY, ErrorBox.SEVERITY_LOG)
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_TITLE, "title123 (${NativeLink.smartphoneData.appVersion})")
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_MSG, "msg123")
	}
	
	@Test
	fun warn() {
		ErrorBox.warn("title123", "msg123")
		
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_SEVERITY, ErrorBox.SEVERITY_WARN)
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_TITLE, "title123 (${NativeLink.smartphoneData.appVersion})")
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_MSG, "msg123")
	}
	
	@Test
	fun error() {
		ErrorBox.error("title123", "msg123")
		
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_SEVERITY, ErrorBox.SEVERITY_ERROR)
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_TITLE, "title123 (${NativeLink.smartphoneData.appVersion})")
		assertSqlWasSaved(ErrorBox.TABLE, ErrorBox.KEY_MSG, "msg123")
	}
}