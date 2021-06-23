package at.jodlidev.esmira.sharedCode

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder

/**
 * Created by JodliDev on 15.05.2020.
 */
object JsonToStringSerializer : KSerializer<String> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JsonToStringSerializer", PrimitiveKind.STRING)
	
	override fun serialize(encoder: Encoder, value: String) {
		if(value.startsWith("[") || value.startsWith("{")) {
			encoder.encodeString(value)
			println("Object has been turned into a String!")
		}
		else //will also encode Int, Float, Boolean, ... into String - can be ignored
			encoder.encodeString(value)
	}
	
	override fun deserialize(decoder: Decoder): String {
//		val output = (decoder as JsonInput).decodeJson().toString()
		val output = (decoder as JsonDecoder).decodeJsonElement().toString()
		return if(output.startsWith("\""))
			output.substring(1, output.length-1)
		else output
	}
}