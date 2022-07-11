package dev.schlaubi.hafalsch.marudor.routes

import io.ktor.resources.*
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
@Resource("hafas")
public class Hafas {
    @Serializable
    @Resource("experimental")
    public data class Experimental(val hafas: Hafas = Hafas()) {
        @Serializable
        @Resource("irisCompatibleAbfahrten/{eva}")
        public data class IrisCompatibleAbfahrten(
            val eva: String,
            val lookahead: Int? = null,
            val lookbehind: Int? = null,
            val experimental: Experimental = Experimental()
        )
    }

    @Serializable
    @Resource("v1")
    public data class V1(val hafas: Hafas = Hafas()) {
        @Serializable
        @Resource("enrichedJourneyMatch")
        public data class EnrichedJourneyMatch(val v1: V1 = V1())

        /**
         * Can be used to find all stopPlaces within a radius around a specific geolocation.
         */
        @Serializable
        @Resource("geoStation")
        public data class GeoStation(
            @SerialName("lat") val latitude: Double,
            @SerialName("lng") val longitude: Double,
            @SerialName("maximumDistance") val maximumDistance: Double? = null,
            val profile: HafasProfile? = null,
            val v1: V1 = V1()
        )

        @Serializable
        @Resource("journeyGeoPos")
        public data class JourneyGeoPosition(val profile: HafasProfile? = null, val v1: V1 = V1())

        /**
         * Used to find the position of a specific journey. Based on predictions, not GPS.
         */
        @Serializable
        @Resource("positionForTrain/{trainName}")
        public data class PositionForTrain(val trainName: String, val profile: HafasProfile? = null, val v1: V1 = V1())

        /**
         * Only uses this for non DB profiles. If you need DB stopPlace search use Operation tagged with StopPlace.
         * Used to search for stopPlaces based on a name.
         */
        @Serializable
        @Resource("stopPlace/{searchTerm}")
        public data class StopPlaceSearch(
            val searchTerm: String,
            val type: Type? = null,
            val profile: HafasProfile? = null,
            val v1: V1 = V1()
        ) {
            @Serializable
            public enum class Type {
                /**
                 * returns only StopPlaces,
                 */
                S,

                /**
                 * also returns Point of Interests
                 */
                ALL
            }
        }

        /**
         * This redirects to the current Details Page with a provided HAFAS TripId / JourneyId / JID.
         */
        @Serializable
        @Resource("detailsRedirect/{tripId}")
        public data class DetailsRedirect(val tripId: String, val profile: HafasProfile? = null, val v1: V1 = V1())
    }

    @Serializable
    @Resource("v2")
    public data class V2(val hafas: Hafas = Hafas()) {
        /**
         * Used to get all arrivals at a specific stopPlace.
         *
         * @property station Usually 7 digits, leading zeros can be omitted
         */
        @Serializable
        @Resource("arrivalStationBoard")
        public data class ArrivalStationBoard(
            val station: String,
            val date: Instant? = null,
            val profile: HafasProfile? = null,
            val v2: V2 = V2()
        )

        /**
         * Used to get all arrivals at a specific stopPlace.
         *
         * @property start name of the start stop
         * @property destination name of the destination stop
         * @property plannedDepartureTime planned Departure time of the stop you want the occpuancy for

         */
        @Serializable
        @Resource("auslastung/{start}/{destination}/{trainNumber}/{plannedDepartureTime}")
        public data class Auslastung(
            val start: String,
            val destination: String,
            val trainNumber: String,
            val plannedDepartureTime: String,
            val v2: V2 = V2()
        )

        /**
         * Used to get all departures at a specific stopPlace. Optionally filterable to get only Journeys
         * that travel via a specific stopPlace
         *
         * @property station Usually 7 digits, leading zeros can be omitted
         * @property direction Usually 7 digits, leading zeros can be omitted
         */
        @Serializable
        @Resource("departureStationBoard")
        public data class DepartureStationBoard(
            val station: String,
            val direction: String,
            val date: Instant? = null,
            val profile: HafasProfile? = null,
            val v2: V2 = V2()
        )


        /**
         * This combines several HAFAS endpoint as well as IRIS data to get the best possible information for a specific journey.
         *
         * @property station EvaNumber of a stop of your train, might not work for profiles other than DB
         * @property date This is the initialDepartureDate of your desired journey
         */
        @Serializable
        @Resource("details/{trainName}")
        public data class Details(
            val trainName: String,
            val station: String? = null,
            val date: Instant? = null,
            val profile: HafasProfile? = null,
            val v2: V2 = V2()
        )

        /**
         * Used to find a specific journey based on name, date and HAFAS filter.
         */
        @Serializable
        @Resource("journeyMatch")
        public data class JourneyMatch(val profile: HafasProfile? = null, val v2: V2 = V2())

        /**
         * provides Details for a specific Journey.
         */
        @Serializable
        @Resource("journeyDetails")
        public data class JourneyDetails(
            @SerialName("jid") val journeyId: String,
            val profile: HafasProfile? = null,
            val v2: V2 = V2()
        )

        /**
         * Advanced HAFAS Method, not used by marudor.de and therefore quite undocumented.
         */
        @Serializable
        @Resource("searchOnTrip")
        public data class SearchOnTrip(val profile: HafasProfile? = null, val v2: V2 = V2())
    }

    @Serializable
    @Resource("v3")
    public data class V3(val hafas: Hafas = Hafas()) {
        @Serializable
        @Resource("tripSearch")
        public data class TripSearch(val profile: HafasProfile, val v3: V3 = V3())
    }
}


@Serializable(with = HafasProfile.Serializer::class)
public enum class HafasProfile(public val serialName: String) {
    DB("db"),
    OEBB("oebb"),
    BVG("bvg"),
    HVV("hvv"),
    RMV("rmv"),
    SNCB("sncb"),
    AVV("avv"),
    NAHSH("nahsh"),
    INSA("insa"),
    ANACHB("anachb"),
    VAO("vao"),
    SBB("sbb"),
    DBNETZ("dbnetz"),
    PKP("pkp"),
    DBREGIO("dbregio"),
    SMARTRBL("smartrbl"),
    VBN("vbn");

    public companion object {
        public val DEFAULT: HafasProfile = DB
    }

    public object Serializer : KSerializer<HafasProfile> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HafasProfile", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): HafasProfile {
            val name = decoder.decodeString()
            return values().firstOrNull { it.serialName == name }
                ?: throw SerializationException("Could not find hafas profile $name")
        }

        override fun serialize(encoder: Encoder, value: HafasProfile): Unit = encoder.encodeString(value.serialName)
    }
}
