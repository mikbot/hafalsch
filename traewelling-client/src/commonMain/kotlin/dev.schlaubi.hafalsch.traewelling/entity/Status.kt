package dev.schlaubi.hafalsch.traewelling.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class StatusesList(
    val statuses: List<Status>
)

@Serializable
public data class Status(
    val id: Int,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant,
    val body: String?,
    val type: String,
    @SerialName("event_id")
    val eventId: Int?,
    val favorited: Boolean,
    val user: User,
    @SerialName("train_checkin")
    val trainCheckin: CheckIn,
    val event: Event?
)

@Serializable
public data class CheckIn(
    val id: Int,
    @SerialName("status_id")
    val statusId: Int,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("trip_id")
    val tripId: String,
    val origin: Station,
    val destination: Station,
    val distance: Double,
    val departure: Instant,
    val arrival: Instant,
    val points: Int,
    val duration: Int,
    @SerialName("origin_stopover")
    val originStopOver: MinimalHafasStopOver,
    @SerialName("destination_stopover")
    val destinationStopOver: MinimalHafasStopOver,
    val speed: Double
)

@Serializable
public data class MinimalHafasStopOver(
    val id: Int,
    @SerialName("train_station")
    val trainStation: TrainStation
) {
    @Serializable
    public data class TrainStation(
        val id: Int, val ibnr: Int, val name: String
    )
}

@Serializable
public data class Event(
    val id: Int,
    val name: String,
    val slug: String,
    val hashtag: String,
    val host: String,
    val url: String,
    @SerialName("train_station")
    val trainStation: String? = null,
    val begin: Instant,
    val end: Instant
)
