package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 17.03.2022.
 */
expect class FileOpener {
	fun deleteFile(path: String): Boolean
	fun getFile(path: String): ByteArray
	fun getFileSize(path: String): Long
}