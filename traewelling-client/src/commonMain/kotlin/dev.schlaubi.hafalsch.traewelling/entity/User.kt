package dev.schlaubi.hafalsch.traewelling.entity

import kotlinx.serialization.Serializable

@Serializable
public data class User(
    val id: Int,
    val displayName: String,
    val username: String,
    val profilePicture: String? = null,
    val trainDistance: Long,
    val trainDuration: Long,
    val points: Long,
    val privateProfile: Boolean,
    val privacyHideDays: Int? = null,
    val trainSpeed: Double,
    val twitterUrl: String? = null,
    val mastodonUrl: String? = null,
    val home: Station? = null,
    val preventIndex: Boolean,
    val language: String? = null
)
