package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.posix.memcpy

/**
 * Created by JodliDev on 17.03.2022.
 */
actual object FileOpener {
	private fun getPath(fileName: String): String {
		val documentPath = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)[0] as NSURL
		return "${documentPath.path}/$fileName"
	}
	actual fun deleteFile(path: String): Boolean {
		memScoped {
			val errorPtr: ObjCObjectVar<NSError?> = alloc<ObjCObjectVar<NSError?>>()
			NSFileManager.defaultManager.removeItemAtPath(getPath(path), errorPtr.ptr)
			if(errorPtr.value == null) {
				return true
			}
			else {
				ErrorBox.error("ios FileOpener", "Error while deleting file: ${errorPtr.value}")
				return false
			}
		}
	}

	actual fun getFile(path: String): ByteArray {
		val data = NSFileManager.defaultManager.contentsAtPath(getPath(path)) ?: return ByteArray(0)
		val bytes = ByteArray(data.length.toInt())
		bytes.usePinned { pinned ->
			memcpy(pinned.addressOf(0), data.bytes, data.length)
		}
		return bytes
	}
	
	actual fun getFileSize(path: String): Long {
		val fileSize = NSFileManager.defaultManager.attributesOfItemAtPath(getPath(path),null) as NSDictionary
		return fileSize.fileSize().toLong()
//		return 0
//		val attributes = NSFileManager.defaultManager.attributesOfFileSystemForPath(path, null)
//		attributes?.get(FileAttributeKey.size)
//
//		NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:URL error:&attributesError];
//
//		NSNumber *fileSizeNumber = [fileAttributes objectForKey:NSFileSize];
//		long long fileSize = [fileSizeNumber longLongValue];
//
//
//
//		val file = File(path)
//		return file.length()
	}
}