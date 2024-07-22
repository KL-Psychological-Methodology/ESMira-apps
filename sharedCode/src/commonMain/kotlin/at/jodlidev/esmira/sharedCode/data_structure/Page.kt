package at.jodlidev.esmira.sharedCode.data_structure

import at.jodlidev.esmira.sharedCode.merlinInterpreter.MerlinRunner
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
	var skipAfterSecs = 0
	var header: String = ""
	var footer: String = ""
	var relevance: String = ""
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

	@Transient private lateinit var _activeInputs: List<Input>
	val activeInputs: List<Input> get() {
		if(!this::_activeInputs.isInitialized) {
			_activeInputs = inputs.filter { input ->
				if (input.relevance.isNotEmpty())
					MerlinRunner.runForBool(
						input.relevance,
						input.questionnaire,
						"item relevance script of item ${input.name}",
						true
					)
				else
					true
			}
		}
		return _activeInputs
	}
	
	fun hasScreenTracking(): Boolean {
		for(input in inputs) {
			if(input.hasScreenTracking())
				return true
		}
		return false
	}
	fun hasScreenOrAppTracking(): Boolean {
		for(input in inputs) {
			if(input.hasScreenOrAppTracking())
				return true
		}
		return false
	}
}