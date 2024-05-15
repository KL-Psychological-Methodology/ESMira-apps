package at.jodlidev.esmira.sharedCode.merlinInterpreter

import kotlinx.serialization.Serializable
import kotlin.math.max

/*
 * Created by SelinaDev
 *
 * This file contains all classes for representing variable types in Merlin.
 */

@Serializable
sealed class MerlinType {

    abstract fun isTruthy(): Boolean
    abstract fun stringify(): String
    abstract fun binary(other: MerlinType, operator: MerlinToken): Result<MerlinType>
    abstract fun unary(operator:MerlinToken): Result<MerlinType>
    abstract fun getDebugString(): String

    fun wrapped(): MerlinArray {
        return if (this is MerlinArray) this else MerlinArray(mutableListOf(this))
    }

    fun asNumber(): MerlinNumber? {
        return when(this) {
            is MerlinNumber -> this
            is MerlinString -> value.toDoubleOrNull()?.let { MerlinNumber(it.toDouble())}
            is MerlinArray -> if (array.size == 1) array[0].asNumber() else null
            else -> null
        }
    }

    companion object {
        fun createBool(value: Boolean): MerlinNumber {
            return MerlinNumber(if (value) 1.0 else 0.0)
        }
    }
}

@Serializable
class MerlinArray (val array: MutableList<MerlinType> = mutableListOf()): MerlinType() {
    override fun isTruthy(): Boolean {
        return array.all { it.isTruthy() }
    }

    override fun stringify(): String {
        return array.joinToString(", ") { it.stringify() }
    }

    override fun binary(other: MerlinType, operator: MerlinToken): Result<MerlinType> {
        if (operator.type == MerlinTokenType.DOT_DOT) {
            return Result.success(MerlinArray((array.toList() + other.wrapped().array.toList()).toMutableList()))
        }
        return when (other) {
            is MerlinArray -> {
                try {
                    val newArray = mutableListOf<MerlinType>()
                    val thisSize = array.size
                    val otherSize = other.array.size
                    for (i in 0 until max(thisSize, otherSize)) {
                        newArray.add(array[i % thisSize].binary(other.array[i % otherSize], operator).getOrThrow())
                    }
                    Result.success(MerlinArray(newArray))
                } catch (e: MerlinRuntimeError) {
                    Result.failure(e)
                }
            }
            is MerlinObject -> Result.failure(MerlinRuntimeError(operator, "Invalid operands for operation."))
            else -> binary(other.wrapped(), operator)
        }
    }

    override fun unary(operator: MerlinToken): Result<MerlinType> {
        return try {
            Result.success(
                MerlinArray(array.map { it.unary(operator).getOrThrow() }.toMutableList())
            )
        } catch (e: MerlinRuntimeError) {
            Result.failure(e)
        }
    }

    override fun getDebugString(): String {
        return "[" + stringify() + "]"
    }

    fun isIndexInRange(index: Int): Boolean {
        return index >= 0 && index < array.size
    }

    companion object {
        fun toIndex(number: MerlinNumber): Int {
            return number.value.toInt() - 1
        }
    }
}

@Serializable
class MerlinString (val value: String): MerlinType() {

    override fun isTruthy(): Boolean {
        return value.toDoubleOrNull()?.let {it != 0.0} ?: value.isNotBlank()
    }

    override fun stringify(): String {
        return value
    }

    override fun binary(other: MerlinType, operator: MerlinToken): Result<MerlinType> {
        if (operator.type == MerlinTokenType.DOT_DOT) {
            return Result.success(MerlinString(value + other.stringify()))
        }
        return asNumber()?.binary(other, operator) ?:
            when(operator.type) {
                MerlinTokenType.PLUS -> Result.success(MerlinString(value + other.stringify()))
                MerlinTokenType.EQUAL_EQUAL -> Result.success(MerlinType.createBool(value == other.stringify()))
                MerlinTokenType.EXCLAMATION_EQUAL -> Result.success(MerlinType.createBool(value != other.stringify()))
                else -> Result.failure(MerlinRuntimeError(operator, "Invalid string operation."))
            }
    }

    override fun unary(operator: MerlinToken): Result<MerlinType> {
        return asNumber()?.unary(operator) ?: Result.failure(MerlinRuntimeError(operator, "Invalid string operation."))
    }

    override fun getDebugString(): String {
        return "\"" + stringify() + "\""
    }
}

@Serializable
class MerlinNumber (val value: Double): MerlinType() {

    override fun isTruthy(): Boolean {
        return value != 0.0
    }

    override fun stringify(): String {
        if (value.toInt() .toDouble() == value) {
            return value.toInt().toString()
        }
        return value.toString()
    }

    override fun binary(other: MerlinType, operator: MerlinToken): Result<MerlinType> {
        return when(other) {
            is MerlinNumber -> when(operator.type) {
                MerlinTokenType.PLUS -> Result.success(MerlinNumber(value + other.value))
                MerlinTokenType.MINUS -> Result.success(MerlinNumber(value - other.value))
                MerlinTokenType.STAR -> Result.success(MerlinNumber(value * other.value))
                MerlinTokenType.SLASH -> Result.success(MerlinNumber(value / other.value))
                MerlinTokenType.EQUAL_EQUAL -> Result.success(createBool(value == other.value))
                MerlinTokenType.EXCLAMATION_EQUAL -> Result.success(createBool(value != other.value))
                MerlinTokenType.GREATER -> Result.success(createBool(value > other.value))
                MerlinTokenType.GREATER_EQUAL -> Result.success(createBool(value >= other.value))
                MerlinTokenType.LESS -> Result.success(createBool(value < other.value))
                MerlinTokenType.LESS_EQUAL -> Result.success(createBool(value <= other.value))
                MerlinTokenType.AND -> Result.success(createBool(isTruthy() && other.isTruthy()))
                MerlinTokenType.OR -> Result.success(createBool(isTruthy() || other.isTruthy()))
                else -> Result.failure(MerlinRuntimeError(operator, "Invalid binary operator."))
            }
            is MerlinString -> other.asNumber()?.let { binary(it, operator) } ?: Result.failure(MerlinRuntimeError(operator, "Unable to convert String to number."))
            is MerlinNone -> Result.success(MerlinNone)
            is MerlinArray -> wrapped().binary(other, operator)
            else -> Result.failure(MerlinRuntimeError(operator, "Invalid operands for operation."))
        }
    }

    override fun unary(operator: MerlinToken): Result<MerlinType> {
        return when(operator.type) {
            MerlinTokenType.MINUS -> Result.success(MerlinNumber(- value))
            MerlinTokenType.EXCLAMATION -> Result.success(createBool((!isTruthy())))
            else -> Result.failure(MerlinRuntimeError(operator, "Invalid unary operator."))
        }
    }

    override fun getDebugString(): String {
        return stringify()
    }
}

@Serializable
class MerlinObject (): MerlinType() {
    private val fields = HashMap<String, MerlinType>()

    fun get(name: MerlinToken): MerlinType {
        return get(name.lexeme)
    }

    fun get(name: String): MerlinType {
        return fields.getOrElse(name) { MerlinNone }
    }

    fun has(name: String): Boolean {
        return fields.containsKey(name)
    }

    fun set(name: MerlinToken, value: MerlinType) {
        fields[name.lexeme] = value
    }

    override fun isTruthy(): Boolean {
        return fields.isNotEmpty()
    }

    override fun stringify(): String {
        val objectString = StringBuilder()
        objectString.append("{")
        objectString.append(
            fields.entries.joinToString(", ") { "${it.key}: ${it.value.stringify()}" }
        )
        objectString.append("}")
        return objectString.toString()
    }

    override fun binary(other: MerlinType, operator: MerlinToken): Result<MerlinType> {
        return Result.failure(MerlinRuntimeError(operator, "Invalid operand for operation."))
    }

    override fun unary(operator: MerlinToken): Result<MerlinType> {
        return Result.failure(MerlinRuntimeError(operator, "Invalid operand for operation."))
    }

    override fun getDebugString(): String {
        val objectString = StringBuilder()
        objectString.append("{")
        objectString.append(
            fields.entries.joinToString(", ") { "\"${it.key}\": ${it.value.getDebugString()}" }
        )
        objectString.append("}")
        return objectString.toString()
    }
}

@Serializable
object MerlinNone: MerlinType() {

    override fun isTruthy(): Boolean {
        return false
    }

    override fun stringify(): String {
        return ""
    }

    override fun binary(other: MerlinType, operator: MerlinToken): Result<MerlinType> {
        return when(other) {
            is MerlinNone -> when(operator.type) {
                MerlinTokenType.EQUAL_EQUAL -> Result.success(MerlinType.createBool(true))
                MerlinTokenType.EXCLAMATION_EQUAL -> Result.success(MerlinType.createBool(false))
                else -> Result.success(MerlinNone)
            }
            is MerlinArray -> wrapped().binary(other, operator)
            else -> Result.success(MerlinNone)
        }
    }

    override fun unary(operator: MerlinToken): Result<MerlinType> {
        return Result.success(MerlinNone)
    }

    override fun getDebugString(): String {
        return "NONE"
    }
}