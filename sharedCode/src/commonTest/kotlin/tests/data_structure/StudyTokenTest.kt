package tests.data_structure

import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class StudyTokenTest : BaseCommonTest() {
	
	@Test
	fun save() {
		val studyToken = StudyToken(123, 456)
		studyToken.save()
		assertSqlWasSaved(StudyToken.TABLE, StudyToken.KEY_TOKEN, 456L)
		studyToken.save()
		assertSqlWasUpdated(StudyToken.TABLE, StudyToken.KEY_STUDY_ID, 123L)
	}
	
	@Test
	fun hasToken() {
		StudyToken.hasToken(7896L)
		assertSqlWasSelected(StudyToken.TABLE, 0, "7896")
	}
}