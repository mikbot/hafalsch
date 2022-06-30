package dev.schlaubi.hafalsch.marudor.entity

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class Position(@JsonNames("lat") val latitude: Double, @JsonNames("lng") val longitude: Double)

public typealias Coordinates = Position
