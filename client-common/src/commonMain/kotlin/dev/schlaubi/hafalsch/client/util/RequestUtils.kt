package dev.schlaubi.hafalsch.client.util

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
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

public abstract class NumberedEnumSerializer<T>(private val values: Array<T>) :
    KSerializer<T> where T : Enum<T>, T : NumberedEnum {
    public abstract val name: String
    override val descriptor: SerialDescriptor get() = PrimitiveSerialDescriptor(name, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: T): Unit = encoder.encodeInt(value.value)
    override fun deserialize(decoder: Decoder): T {
        val value = decoder.decodeInt()

        return values.firstOrNull { it.value == value }
            ?: throw SerializationException("Could not find $name for $value")
    }
}

public suspend inline fun <reified T : Any> HttpResponse.safeBody(): T? {
    if (!status.isSuccess()) {
        return null
    }

    return body<T>()
}
