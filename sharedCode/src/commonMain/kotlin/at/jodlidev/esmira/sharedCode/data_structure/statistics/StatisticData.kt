package at.jodlidev.esmira.sharedCode.data_structure.statistics

import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 12.02.2020.
 */
//abstract class StatisticData internal constructor() {
//	var exists = false
//	var id: Long = -1
//	var studyId: Long = -1
//	var observedId: Long = -1
//	var sum: Double = 0.0
//	var count: Int = 0
//	var observableIndex = 0 //is not saved in db
//	lateinit var variableName: String //is not saved in db
//
//	abstract fun save()
//	abstract fun getType(): Int
//}
@Serializable
abstract class StatisticData internal constructor() {
	@Transient var exists = false
	@Transient var id: Long = -1
	@Transient var studyId: Long = -1
	@Transient var observedId: Long = -1
	
	//inherited properties in Serializable cause troubles when building for IOS
	// Workaround is to make them abstract
	// https://github.com/Kotlin/kotlinx.serialization/issues/768
	abstract var sum: Double
	abstract var count: Int
	
	@Transient var observableIndex = 0 //is not saved in db
	@Transient lateinit var variableName: String //is not saved in db

	abstract fun save()
	abstract fun getType(): Int
}