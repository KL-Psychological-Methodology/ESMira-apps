package at.jodlidev.esmira.tests.data_structure

import BaseCommonTest
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.DbUser
import kotlin.test.*

/**
 * Created by JodliDev on 24.02.2023.
 */
class DbUserTest : BaseCommonTest() {
	
	@Test
	fun setDev_isDev_getAdminAppType() {
		assertEquals(NativeLink.smartphoneData.appType, DbUser.getAdminAppType()) //no user
		DbUser.getUid() //create user
		assertEquals(NativeLink.smartphoneData.appType, DbUser.getAdminAppType())
		
		DbUser.setDev(true, "Chuck Norris")
		assertFalse(DbUser.isDev())
		assertEquals(NativeLink.smartphoneData.appType, DbUser.getAdminAppType())
		
		DbUser.setDev(true, DbLogic.ADMIN_PASSWORD)
		assertTrue(DbUser.isDev())
		assertEquals("${NativeLink.smartphoneData.appType}_dev", DbUser.getAdminAppType())
		
		DbUser.setDev(false)
		assertFalse(DbUser.isDev())
		assertEquals("${NativeLink.smartphoneData.appType}_wasDev", DbUser.getAdminAppType())
	}
	
	@Test
	fun getUid() {
		val firstUid = DbUser.getUid()
		assertEquals(firstUid, DbUser.getUid())
		
		reset()
		assertNotEquals(firstUid, DbUser.getUid())
	}
	
	
	@Test
	fun setLang_getLang() {
		DbUser.getUid() //create user
		DbUser.setLang("de")
		assertEquals("de", DbUser.getLang())
		DbUser.setLang("en")
		assertEquals("en", DbUser.getLang())
	}
}