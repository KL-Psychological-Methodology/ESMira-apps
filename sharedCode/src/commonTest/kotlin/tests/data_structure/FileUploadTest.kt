package tests.data_structure

import BaseTest
import MockTools
import at.jodlidev.esmira.sharedCode.data_structure.*
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class FileUploadTest : BaseDataStructureTest() {
	
	@Test
	fun save() {
		val path = "path/to/file"
		val fileUpload = FileUpload(createStudy(), path, FileUpload.TYPES.Image)
		fileUpload.save()
		mockTools.assertSqlWasSaved(FileUpload.TABLE, FileUpload.KEY_FILE_PATH, path)
	}
	
	@Test
	fun setReadyForUpload() {
		val path = "path/to/file"
		val fileUpload = FileUpload(createStudy(), path, FileUpload.TYPES.Image)
		fileUpload.save()
		fileUpload.setReadyForUpload()
		
		mockTools.assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, false)
	}
	
	@Test
	fun setTemporary() {
		val path = "path/to/file"
		val fileUpload = FileUpload(createStudy(), path, FileUpload.TYPES.Image)
		fileUpload.save()
		fileUpload.setTemporary()
		
		mockTools.assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, true)
	}
	
	// delete() and getFile() are tested in DatabaseSharedTestLogic.fileUpload_createAndDeleteFile()
}