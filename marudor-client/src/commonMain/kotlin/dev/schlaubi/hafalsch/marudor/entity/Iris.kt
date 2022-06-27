package dev.schlaubi.hafalsch.marudor.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
public data class IrisMessage(
    val text: String,
    val head: String? = null,
    val timestamp: Instant,
    val superseded: Boolean = false,
    val priority: Long? = null, // actually Int, but it is a String and kx.ser can parse strings as longs iirc
    val value: Int? = null
)
