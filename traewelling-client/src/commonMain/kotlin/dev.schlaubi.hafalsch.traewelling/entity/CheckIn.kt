package dev.schlaubi.hafalsch.traewelling.entity

import dev.schlaubi.hafalsch.client.util.UnixTimestamp
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
public data class CheckInRequest(
    @SerialName("tripID")
    val tripId: String,
    val lineName: String,
    val start : String,
    val destination: String,
    val body: String? = null,
    val tweet: Boolean,
    val toot: Boolean,
    val departure: Instant? = null,
    val arrival: Instant? = null
)

@Serializable
public data class CheckInResponse(
    val distance: Double,
    val duration: Int,
    val statusId: Int,
    val points: Int,
    val lineName: String?,
    val alsoOnThisConnection: List<User>
)

@Serializable
public data class Station(
    val id: Int,
    val rilIdentifier: String? = null,
    val name: String,
    val provider: String? = null
)

@Serializable
public data class Departures(
    val station: Station,
    @SerialName("when")
    val time: UnixTimestamp,
    val departures: List<Departure>
)

@Serializable
public data class Trip(
    val start: String,
    val destination: String,
    val train: Train,
    @SerialName("stopovers")
    val stopOvers: List<StopOver>
)

@Serializable
public data class MinimalStopOver(val stop: Stop) {
    @Serializable
    public data class Stop(val name: String, val id: String)
}

@Serializable
public data class Train(
    val id: Int,
    @SerialName("trip_id")
    val tripId: String,
    val category: String,
    val number: String,
    val linename: String,
    val origin: Int,
    val destination: Int,
    @SerialName("stopovers")
    val stopOvers: String
) {
    val stopOversParsed: List<MinimalStopOver>
        get() = json.decodeFromString(stopOvers)

    public companion object {
        public val json: Json = Json {
            ignoreUnknownKeys = true
        }
    }
}

@Serializable
public data class StopOver(
    val stop: HafasStation,
    val arrival: Instant? = null,
    val arrivalDelay: Int? = null,
    val arrivalPlatform: String? = null,
    val departure: Instant? = null,
    val departureDelay: Int? = null,
    val departurePlatform: String? = null
)

@Serializable
public data class Departure(
    val tripId: String,
    val stop: Stop,
    @SerialName("when")
    val time: String? = null,
    @SerialName("plannedWhen")
    val plannedTime: String,
    val delay: Int = 0,
    val platform: String? = null,
    val plannedPlatform: String? = null,
    val direction: String? = null,
    val provenance: String? = null,
    val line: Line,
    val station: Station
)

@Serializable
public data class Stop(
    val type: String,
    val id: String,
    val name: String,
    val location: Location,
    val products: Products,
    val station: HafasStation? = null,
    @SerialName("when")
    val time: UnixTimestamp? = null,
    val direction: String? = null,
    val line: Line? = null,
    val remarks: String? = null,
    val delay: Int? = null,
    val platform: String? = null
)

@Serializable
public data class HafasStation(
    val type: String,
    val id: String,
    val name: String,
    val location: Location,
    val products: Products
)

@Serializable
public data class Location(
    val type: String,
    val id: String,
    val latitude: Double,
    val longitude: Double
)

@Serializable
public data class Products(
    val nationalExpress: Boolean,
    val national: Boolean,
    val regionalExp: Boolean,
    val suburban: Boolean,
    val bus: Boolean,
    val ferry: Boolean,
    val subway: Boolean,
    val tram: Boolean,
    val taxi: Boolean
)

@Serializable
public data class Line(
    val type: String,
    val id: String,
    val fahrtNr: String,
    val name: String,
    val public: Boolean,
    val adminCode: String,
    val mode: String,
    val product: String,
    val operator: Operator
)

@Serializable
public data class Operator(
    val type: String,
    val id: String,
    val name: String
)
