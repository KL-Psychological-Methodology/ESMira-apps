package tests.data_structure

import at.jodlidev.esmira.sharedCode.data_structure.*
import BaseCommonTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class FileUploadTest : BaseCommonTest() {
	
	@Test
	fun save() {
		val path = "path/to/file"
		val fileUpload = FileUpload(createStudy(), path, FileUpload.TYPES.Image)
		fileUpload.save()
		assertSqlWasSaved(FileUpload.TABLE, FileUpload.KEY_FILE_PATH, path)
	}
	
	@Test
	fun setReadyForUpload() {
		val path = "path/to/file"
		val fileUpload = FileUpload(createStudy(), path, FileUpload.TYPES.Image)
		fileUpload.save()
		fileUpload.setReadyForUpload()
		
		assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, 0)
	}
	
	@Test
	fun setTemporary() {
		val path = "path/to/file"
		val fileUpload = FileUpload(createStudy(), path, FileUpload.TYPES.Image)
		fileUpload.save()
		fileUpload.setTemporary()
		
		assertSqlWasUpdated(FileUpload.TABLE, FileUpload.KEY_IS_TEMPORARY, 1)
	}
	
	// delete() and getFile() are tested in DatabaseSharedTestLogic.fileUpload_createAndDeleteFile()
}