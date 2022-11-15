package dev.schlaubi.hafalsch.traewelling.entity

import kotlinx.serialization.Serializable

@Serializable
public data class User(
    val id: Int,
    val displayName: String,
    val username: String,
    val profilePicture: String?,
    val trainDistance: Long,
    val trainDuration: Long,
    val points: Long,
    val privateProfile: Boolean,
    val privacyHideDays: Int?,
    val trainSpeed: Double,
    val twitterUrl: String?,
    val mastodonUrl: String?,
    val home: Station?,
    val preventIndex: Boolean,
    val language: String?
)
