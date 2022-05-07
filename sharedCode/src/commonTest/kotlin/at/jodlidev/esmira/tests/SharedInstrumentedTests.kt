package tests

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseInstrumentedTests
import kotlin.test.assertEquals

/**
 * Created by JodliDev on 05.04.2022.
 */
abstract class SharedInstrumentedTests : BaseInstrumentedTests() {
	
	open fun createAndLoadChartInfoCollection() {
		//TODO: no tests
	}
	
	abstract fun createEmptyFile(content: String): String
	open fun fileUpload_createAndDeleteFile() {
		//check existing file
		val content = "test 123"
		
		val path = createEmptyFile(content)
		val fileUploadExists = FileUpload(createStudy(), path, FileUpload.TYPES.Image)
		fileUploadExists.save()
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		assertEquals(1, DbLogic.getTemporaryFileUploads().size)
		fileUploadExists.setReadyForUpload()
		assertEquals(1, DbLogic.getPendingFileUploads().size)
		assertEquals(0, DbLogic.getTemporaryFileUploads().size)
		
		//check file content
		val byteArray = fileUploadExists.getFile()
		assertEquals(content, byteArray.decodeToString())
		
		fileUploadExists.delete()
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		assertEquals(0, DbLogic.getTemporaryFileUploads().size)
		
		
		//check non existing file
		val fileUploadDoesNotExist = FileUpload(createStudy(), "not/existing/file", FileUpload.TYPES.Image)
		fileUploadDoesNotExist.save()
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		assertEquals(1, DbLogic.getTemporaryFileUploads().size)
		fileUploadExists.setReadyForUpload()
		assertEquals(1, DbLogic.getPendingFileUploads().size)
		assertEquals(0, DbLogic.getTemporaryFileUploads().size)
		
		fileUploadDoesNotExist.delete()
		assertEquals(0, DbLogic.getPendingFileUploads().size)
		assertEquals(0, DbLogic.getTemporaryFileUploads().size) //because file does not exist, entry is deleted instead of temporary
	}
	
	open fun reportMissedInvitation_getMissedInvitations_resetMissedInvitations() {
		DbLogic.getUid() //create user
		assertEquals(0, DbLogic.getMissedInvitations())
		
		val study = createStudy("""{"id":$studyWebId, "eventUploadSettings": {"${DataSet.TYPE_INVITATION_MISSED}": true}}""")
		study.save()
		
		val questionnaire = createJsonObj<Questionnaire>()
		questionnaire.studyId = study.id //DataSet loads a study from db
		
		DbLogic.reportMissedInvitation(questionnaire, 0)
		assertEquals(1, DbLogic.getMissedInvitations())
		
		DbLogic.reportMissedInvitation(questionnaire, 0)
		assertEquals(2, DbLogic.getMissedInvitations())
		
		DbLogic.resetMissedInvitations()
		assertEquals(0, DbLogic.getMissedInvitations())
	}
}