package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * This file stores all the Exceptions used by Merlin.
 * MerlinReturn is used internally for return values.
 * MerlinScanningError, MerlinParseError, and MerlinRuntimeError represent any errors chat can happen during the respective stages of the script.
 */

class MerlinScanningError (val char: Char, val line: Int, message: String): RuntimeException(message) {
    fun getFormattedError(): String {
        return "At char '${char}' on line ${line}: ${message ?: ""}"
    }
}

class MerlinParseError (val token: MerlinToken, message: String): RuntimeException(message) {
    fun getFormattedError(): String {
        return "At token '${token.lexeme}' on line ${token.line}: ${message ?: ""}"
    }
}

class MerlinRuntimeError (val token: MerlinToken, message: String): RuntimeException(message) {
    fun getFormattedError(): String {
        return "At token '${token.lexeme}' on line ${token.line}: ${message ?: ""}"
    }
}

class MerlinReturn (val value: MerlinType): RuntimeException()