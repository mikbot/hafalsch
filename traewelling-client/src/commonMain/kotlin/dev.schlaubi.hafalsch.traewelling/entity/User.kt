package dev.schlaubi.hafalsch.traewelling.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class User(
    val id: Int,
    val name: String,
    val username: String,
    @SerialName("train_distance")
    val trainDistance: Long,
    @SerialName("train_duration")
    val trainDuration: Long,
    val points: Long,
    @SerialName("private_profile")
    val privateProfile: Boolean,
    val averageSpeed: Double
)
