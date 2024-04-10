package at.jodlidev.esmira.sharedCode.merlinInterpreter

/*
 * Created by SelinaDev
 *
 * The variable environment used in the interpreter. MerlinEnvironments can reference an enclosing environment, thereby creating a tree of environments.
 * New environments are generated, e.g., in blocks or function calls.
 * Note that variables and functions are stored separately.
 * Assign statements will assign to this or the first ancestor environment in which a variable of that name is present, otherwise will implicitly define the variable in the current environment.
 * Explicit definitions (and thus shadowing) only happen in function calls (with parameter variables) and in for loops (with the loop variable).
 * Functions are always directly defined in the current environment, and can therefore shadow other functions.
 */

class MerlinEnvironment (private val enclosing: MerlinEnvironment? = null) {
    private val values = HashMap<String, MerlinType>()
    private val functions = HashMap<String, MerlinFunction>()
    var currentReturnValue: MerlinType = MerlinNone


    fun get(name: MerlinToken): MerlinType {
        return get(name.lexeme)
    }

    fun get(name: String): MerlinType {
        if (values.containsKey(name)) {
            return values[name]!!
        }

        enclosing?.let { return enclosing.get(name) }

        return MerlinNone
    }

    private fun find(name: MerlinToken): Boolean {
        if (values.containsKey(name.lexeme)) return true
        return enclosing?.find(name) ?: false
    }

    fun assign(name: MerlinToken, value: MerlinType, isInInit: Boolean = false) {
        if (!tryAssign(name, value, isInInit)) {
            values[name.lexeme] = value // implicit definition
        }
    }

    private  fun tryAssign(name: MerlinToken, value: MerlinType, isInInit: Boolean): Boolean {
        if (values.containsKey(name.lexeme)) {
            if (!isInInit) values[name.lexeme] = value
            return true
        }

        return enclosing?.tryAssign(name, value, isInInit) ?: false
    }

    fun define(name: String, value: MerlinType) {
        values[name] = value
    }

    fun defineFunction(name: String, function: MerlinFunction) {
        functions[name] = function
    }

    fun getFunction(name: MerlinToken): MerlinFunction? {
        return functions.getOrElse(name.lexeme) {
            enclosing?.let {
                return enclosing.getFunction(name)
            }
        }
    }
}