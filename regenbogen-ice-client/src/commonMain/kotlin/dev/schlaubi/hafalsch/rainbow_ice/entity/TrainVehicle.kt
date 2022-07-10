package dev.schlaubi.hafalsch.rainbow_ice.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TrainVehicle(
    val number: Int,
    val name: String,
    @SerialName("train_type")
    val trainType: String,
    val trips: List<Trip>? = null
) {
    @Serializable
    public data class Trip(
        @SerialName("group_index")
        val groupIndex: Int,
        @SerialName("vehicle_timestamp")
        val vehicleTimestamp: Instant,
        @SerialName("trip_timestamp")
        val tripTimestamp: Instant,
        @SerialName("initial_departure")
        val initialDeparture: Instant,
        @SerialName("train_type")
        val trainType: String,
        @SerialName("train_number")
        val trainNumber: Int,
        @SerialName("origin_station")
        val originStation: String,
        @SerialName("destination_station")
        val destinationStation: String,
        val marudor: String? = null,
        val stops: List<Stop>
    ) {
        @Serializable
        public data class Stop(
            val cancelled: Int,
            val station: String,
            @SerialName("scheduled_departure")
            val scheduledDeparture: Instant?,
            val departure: Instant?,
            @SerialName("scheduled_arrival")
            val scheduledArrival: Instant?,
            val arrival: Instant?
        )
    }
}
