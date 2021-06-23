package at.jodlidev.esmira.sharedCode.data_structure.statistics

import at.jodlidev.esmira.sharedCode.JsonToStringSerializer
import at.jodlidev.esmira.sharedCode.data_structure.ObservedVariable
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 15.05.2020.
 */
@Serializable
class StatisticBox (
	var observedVariables: Map<String, List<ObservedVariable>> = HashMap(),
//	@Serializable(with = JsonToStringSerializer::class) var observed_variables: String,
	@Serializable(with = JsonToStringSerializer::class) var charts: String
)