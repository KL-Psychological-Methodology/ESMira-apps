package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * The MerlinFunction represents something that can be called as a function within the Interpreter.
 * The MerlinScriptFunction implements this interface and represents user-defined functions created within a script.
 * However, native functions can be created by creating objects implementing MerlinFunction, and storing them in the Environment used by the Interpreter.
 */

interface MerlinFunction {
    fun arity(): Int

    fun call(interpreter: MerlinInterpreter, arguments: List<MerlinType>): MerlinType
}

class MerlinScriptFunction (private val declaration: MerlinStmt.Function): MerlinFunction {
    override fun arity(): Int {
        return declaration.params.size
    }

    override fun call(interpreter: MerlinInterpreter, arguments: List<MerlinType>): MerlinType {
        val environment = MerlinEnvironment(interpreter.globals)
        for (i in 0 until declaration.params.size) {
            environment.define(declaration.params[i].lexeme, arguments[i])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (r: MerlinReturn) {
            return r.value
        }
        return environment.currentReturnValue
    }
}