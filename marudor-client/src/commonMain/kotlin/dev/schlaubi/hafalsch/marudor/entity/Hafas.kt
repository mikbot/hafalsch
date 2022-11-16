package dev.schlaubi.hafalsch.marudor.entity

import kotlinx.serialization.Serializable
import kotlin.collections.Map

/**
 * Additional information not provided by IRIS.
 *
 * @property occupancy map containing load for EVAs
 * @property operatorName the operator name used by hafas
 */
@Serializable
public data class HafasAdditionalJourneyInformation(
    val occupancy: Map<Int, Load> = emptyMap(),
    val operatorName: String
)
