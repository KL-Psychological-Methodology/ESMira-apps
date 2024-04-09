package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * This class encapsulates the MerlinInterpreter and everything it depends on.
 * Interpretation works like this: SourceCode -> Scanner -> Tokens -> Parser -> AST -> Interpreter
 * The Runner abstracts these intermediate steps and is able to manage the errors that can occur in each step.
 */

class MerlinRunner () {
    private val errors = mutableListOf<MerlinError>()
    private val interpreter = MerlinInterpreter()

    fun run(source: String): MerlinType? {
        // Scanning
        val scanner = MerlinScanner(source)
        val tokens: List<MerlinToken>
        try {
            tokens = scanner.scanTokens()
        } catch (e: MerlinScanningError) {
            logScanningError(e)
            return null
        }

        // Parsing
        val parser = MerlinParser(tokens)
        val statements: List<MerlinStmt>
        try {
            statements = parser.parse()
        } catch (e: MerlinParseError) {
            logParsingError(e)
            return null
        }

        // Interpreting
        try {
            interpreter.interpret(statements)
        } catch (r: MerlinReturn) {
            return r.value
        } catch (e: MerlinRuntimeError) {
            logRuntimeError(e)
        }
        return null
    }

    fun runForBool(source: String, default: Boolean): Boolean {
        return run(source)?.isTruthy() ?: default
    }

    fun runForString(source: String): String {
        return run(source)?.stringify() ?: ""
    }


    private fun logScanningError(error: MerlinScanningError) {
        // TODO
    }

    private fun logParsingError(error: MerlinParseError) {
        // TODO
    }

    private fun logRuntimeError(error: MerlinRuntimeError) {
        // TODO
    }
}

class MerlinError (val type: String, val message: String)