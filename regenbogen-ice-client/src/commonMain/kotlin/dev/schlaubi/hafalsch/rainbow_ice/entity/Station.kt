package dev.schlaubi.hafalsch.rainbow_ice.entity

import kotlinx.serialization.Serializable

/**
 * Representation of a train station.
 *
 * @property evaNumber the eva (or IBNR) of the station
 * @property name the name of the station
 */
@Serializable
public data class Station(val evaNumber: Int, val name: String)
