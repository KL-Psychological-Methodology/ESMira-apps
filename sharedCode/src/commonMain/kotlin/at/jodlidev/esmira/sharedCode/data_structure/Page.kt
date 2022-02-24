package at.jodlidev.esmira.sharedCode.data_structure

import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable


/**
 * Created by JodliDev on 16.04.2019.
 * is just an interpreter for creating appropriate Views
 * it is not saved into db
 */
@Serializable
class Page internal constructor( ) {
	var randomized: Boolean = false
	var header: String = ""
	var footer: String = ""
	@SerialName("inputs") var orderedInputs: List<Input> = ArrayList()
	
	@Transient private lateinit var _inputs: List<Input>
	val inputs: List<Input> get() {
		if(!this::_inputs.isInitialized) {
			_inputs = if(randomized)
				orderedInputs.shuffled()
			else
				orderedInputs
		}
		return _inputs
	}
	
	fun hasScreenTracking(): Boolean {
		for(input in inputs) {
			if(input.hasScreenTracking())
				return true
		}
		return false
	}
}