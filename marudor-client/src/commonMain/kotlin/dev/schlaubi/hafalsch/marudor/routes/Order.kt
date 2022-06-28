package dev.schlaubi.hafalsch.marudor.routes

import io.ktor.resources.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Resource("/reihung")
public class Order {
    @Serializable
    @Resource("v1")
    public data class V1(val order: Order = Order()) {
        @Serializable
        @Resource("trainName/{tz}")
        public data class TrainName(val tz: String, val v1: V1 = V1())
    }

    @Serializable
    @Resource("v4")
    public data class V4(val order: Order = Order()) {
        /**
         * Returns all journeys that run on a specific date. Only works for DB Fernverkehr.
         *
         * @property trainModels Used to filter for specific Train models
         * @property identifier Used to filter for specific identifier (identifier are defined by me, not DB)
         * @property stopsAt Used to filter for runs that stop at specific stopPlaces in the specifed order
         */
        @Serializable
        @Resource("runsPerDate/{date}")
        public data class RunsPerDate(
            val date: Instant,
            @SerialName("baureihen") val trainModels: List<String>? = null,
            val identifier: List<String>? = null,
            val stopsAt: String? = null,
            val v4: V4 = V4()
        )

        @Serializable
        @Resource("wagen/{trainNumber}")
        public data class CoachSequence(
            val trainNumber: String,
            val departure: Instant,
            val evaNumber: String? = null,
            val initialDeparture: Instant? = null,
            val v4: V4 = V4()
        )
    }
}
