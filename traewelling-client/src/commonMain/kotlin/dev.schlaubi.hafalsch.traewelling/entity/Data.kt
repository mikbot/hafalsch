package dev.schlaubi.hafalsch.traewelling.entity

import kotlinx.serialization.Serializable

@Serializable
public data class Data<T>(val data: T)
