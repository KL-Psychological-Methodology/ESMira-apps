package tests.data_structure

import BaseTest
import MockTools
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class StudyTokenTest : BaseDataStructureTest() {
	
	@Test
	fun save() {
		val studyToken = StudyToken(123, 456)
		studyToken.save()
		mockTools.assertSqlWasSaved(StudyToken.TABLE, StudyToken.KEY_TOKEN, 456L)
		studyToken.save()
		mockTools.assertSqlWasUpdated(StudyToken.TABLE, StudyToken.KEY_STUDY_ID, 123L)
	}
	
	@Test
	fun hasToken() {
		StudyToken.hasToken(7896L)
		mockTools.assertSqlWasSelected(StudyToken.TABLE, 0, "7896")
	}
}