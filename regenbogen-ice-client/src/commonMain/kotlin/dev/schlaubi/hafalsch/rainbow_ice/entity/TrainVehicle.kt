package dev.schlaubi.hafalsch.rainbow_ice.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representation of a train vehicle.
 *
 * @property number the tzn of the train
 * @property name the name of the train
 * @property trainType the type of the train
 * @property buildingSeries the [building series](https://en.wikipedia.org/wiki/DB_locomotive_classification) of the train
 * @property trips a list of [Trips][Trip] if requested
 */
@Serializable
public data class TrainVehicle(
    val number: Int,
    val name: String?,
    @SerialName("train_type")
    val trainType: String,
    @SerialName("building_series")
    val buildingSeries: Int,
    val trips: List<Trip>? = null
) {
    /**
     * Safe accessor for [trips] if you know you requested it.
     */
    val safeTrips: List<Trip>
        get() = trips ?: error("Please request with trip_limit > 0 to use this field")

    /**
     * Representation of a trip.
     *
     * @property groupIndex the index of this train (if connected to another train)
     * @property trainType type of the train
     * @property trainNumber train number of this trip
     * @property originStation name of the origin station
     * @property destinationStation name of the destination station
     * @property marudor link to [marudor.de](marudor.de) if requested
     * @property stops list of [Stops][Stop] the train stops at
     */
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
        val stops: List<Stop>? = null
    ) {

        /**
         * Safe accessor for [marudor] if you know it's requested.
         */
        val safeMarudor: String
            get() = marudor ?: error("Please request with include_marudor = true to use this field")

        /**
         * Safe accessor for [stops] if you know it's requested.
         */
        val safeStops: List<Stop>
            get() = stops ?: error("Please request with include_routes = true to use this field")

        /**
         * Representation of a stop.
         *
         * @property cancelled integer for whatever reason (probably 0=false,1=true)
         * @property station the station name
         * @property scheduledDeparture an [Instant] in which the train is scheduled to depart
         * @property departure an [Instant] in which the train departed
         * @property scheduledArrival an [Instant] in which the train is scheduled to arrive
         * @property arrival an [Instant] in which the train arrived
         */
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
