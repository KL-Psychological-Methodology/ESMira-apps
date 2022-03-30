package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import java.io.*

/**
 * Created by JodliDev on 17.03.2022.
 */
actual class FileOpener {
	actual fun deleteFile(path: String): Boolean {
		val file = File(path)
		return file.delete()
	}
	
	actual fun getFile(path: String): ByteArray {
		val file = File(path)
		
		try {
			val input: InputStream = FileInputStream(file)
			val output = ByteArrayOutputStream()
			
			var nextByte: Int = input.read()
			while(nextByte != -1) {
				output.write(nextByte)
				nextByte = input.read()
			}
			
			val r = output.toByteArray()
			
			input.close()
			output.flush()
			output.close()
			
			return r
		}
		catch(e: IOException) {
			ErrorBox.error("Could not load File", "Error while reading File", e)
			return ByteArray(0)
		}
	}
	
	actual fun getFileSize(path: String): Long {
		val file = File(path)
		return file.length()
	}
}