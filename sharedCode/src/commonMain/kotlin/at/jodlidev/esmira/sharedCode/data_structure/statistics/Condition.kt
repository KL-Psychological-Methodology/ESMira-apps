package at.jodlidev.esmira.sharedCode.data_structure.statistics

import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 19.05.2020.
 * not in db
 */
@Serializable
class Condition (
	val key: String = "",
	val value: String = "",
	val operator: Int = TYPE_ALL
) {
	companion object {
		const val TYPE_ALL = 0
		const val TYPE_AND = 1
		const val TYPE_OR = 2
		
		const val OPERATOR_EQUAL = 0
		const val OPERATOR_UNEQUAL = 1
		const val OPERATOR_GREATER = 2
		const val OPERATOR_LESS = 3
	}
}