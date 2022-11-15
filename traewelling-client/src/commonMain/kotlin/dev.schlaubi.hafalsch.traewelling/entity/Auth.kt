package dev.schlaubi.hafalsch.traewelling.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class LoginRequest(val login: String, val password: String)

@Serializable
public data class TokenResponse(val token: String, @SerialName("expires_at") val expiresAt: Instant)

@Serializable
public data class SignUpResponse(
    val token: String,
    val message: String,
    @SerialName("expires_at") val expiresAt: Instant
)

@Serializable
public data class SignUpRequest(
    val username: String,
    val name: String,
    val email: String,
    val password: String,
    @SerialName("confirm_password")
    val confirmPassword: String
)

@Serializable
public data class LogoutResponse(val message: String)
