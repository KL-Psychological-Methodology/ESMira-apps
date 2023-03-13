package at.jodlidev.esmira.sharedCode

import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import java.io.*

/**
 * Created by JodliDev on 17.03.2022.
 */
actual object FileOpener {
	actual fun deleteFile(path: String): Boolean {
		val file = File(path)
		return if(!file.delete())
			!file.exists()
		else
			true
	}
	
	/**
	 * @throws FileNotFoundException
	 * @throws SecurityException
	 */
	actual fun getFile(path: String): ByteArray {
		val file = File(path)
		
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
	
	actual fun getFileSize(path: String): Long {
		val file = File(path)
		return file.length()
	}
}