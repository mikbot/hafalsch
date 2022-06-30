package dev.schlaubi.hafalsch.marudor.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public interface NumberedEnum {
    public val value: Int
}

internal abstract class NumberedEnumSerializer<T>(private val values: Array<T>) :
    KSerializer<T> where T : Enum<T>, T : NumberedEnum {
    abstract val name: String
    override val descriptor: SerialDescriptor get() = PrimitiveSerialDescriptor(name, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeInt(value.value)
    override fun deserialize(decoder: Decoder): T {
        val value = decoder.decodeInt()

        return values.firstOrNull { it.value == value }
            ?: throw SerializationException("Could not find $name for $value")
    }
}
