package dev.schlaubi.hafalsch.marudor.entity

import dev.schlaubi.hafalsch.marudor.util.NumberedEnum
import dev.schlaubi.hafalsch.marudor.util.NumberedEnumSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
public data class IrisMessage(
    val text: String,
    val head: String? = null,
    val timestamp: Instant? = null,
    val superseded: Boolean = false,
    val priority: Priority? = null,
    val value: Int,
    val stopPlace: HafasStation? = null
) {
    @Serializable(with = Priority.Serializer::class)
    public enum class Priority(override val value: Int) : NumberedEnum {
        HIGH(1),
        MEDIUM(2),
        LOW(3),
        DONE(4);
        internal companion object Serializer : NumberedEnumSerializer<Priority>(enumValues()) {
            override val name: String = "Priority"
        }
    }
}
