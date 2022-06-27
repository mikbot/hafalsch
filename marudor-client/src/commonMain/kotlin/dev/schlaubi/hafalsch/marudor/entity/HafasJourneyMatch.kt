package dev.schlaubi.hafalsch.marudor.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class HafasJourneyMatchRequest(
    val trainName: String,
    val initialDepartureDate: Instant? = null,
    @SerialName("jnyFltrL")
    val journeyFilter: List<Filter> = emptyList(),
    val onlyRT: Boolean = false
) {
    @Serializable
    public data class Filter(val mode: String, val type: String, val value: String)
}

@Serializable
public data class HafasEnrichedJourneyMatchRequest(
    val trainName: String,
    val limit: Int = 5,
    val initialDepartureDate: Instant? = null,
    val filtered: Boolean = true
)

@Serializable
public data class HafasJourneyMatchJourney(
    val train: Train,
    val stops: List<Stop>,
    val firstStop: Stop,
    val lastStop: Stop,
    val messages: List<HafasMessage> = emptyList()
)
