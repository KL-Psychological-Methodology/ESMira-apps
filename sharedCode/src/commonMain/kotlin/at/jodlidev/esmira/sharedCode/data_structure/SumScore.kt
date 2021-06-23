package at.jodlidev.esmira.sharedCode.data_structure

import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 10.08.2020.
 */
@Serializable
class SumScore internal constructor() {
	var name: String = "unknown"
	var addList: List<String> = ArrayList()
	var subtractList: List<String> = ArrayList()
}