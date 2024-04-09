package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * This file stores all the Exceptions used by Merlin.
 * MerlinReturn is used internally for return values.
 * MerlinScanningError, MerlinParseError, and MerlinRuntimeError represent any errors chat can happen during the respective stages of the script.
 */

class MerlinScanningError (val char: Char, val line: Int, message: String): RuntimeException(message)

class MerlinParseError (val token: MerlinToken, message: String): RuntimeException(message)

class MerlinRuntimeError (val token: MerlinToken, message: String): RuntimeException(message)

class MerlinReturn (val value: MerlinType): RuntimeException()