package dev.schlaubi.hafalsch.marudor.routes

import io.ktor.resources.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import dev.schlaubi.hafalsch.marudor.routes.CoachSequence as CoachSequenceRoute

@Serializable
@Resource("/reihung")
public class CoachSequence {
    @Serializable
    @Resource("v1")
    public data class V1(val parent: CoachSequenceRoute = CoachSequenceRoute()) {
        @Serializable
        @Resource("trainName/{tz}")
        public data class TrainName(val tz: String, val v1: V1 = V1())
    }

    @Serializable
    @Resource("v4")
    public data class V4(val parent: CoachSequenceRoute = CoachSequenceRoute()) {
        /**
         * Returns all journeys that run on a specific date. Only works for DB Fernverkehr.
         *
         * @property trainModels Used to filter for specific Baureihen
         * @property identifier Used to filter for specific identifier (identifier are defined by me, not DB)
         * @property stopAt Used to filter for runs that stop at specific stopPlaces in the specifed order
         */
        @Serializable
        @Resource("runsPerDate/{date}")
        public data class RunsPerDate(
            val date: Instant,
            val trainModels: String? = null,
            val identifier: String? = null,
            val stopAt: String? = null,
            val v4: V4 = V4()
        )

        /**
         * Returns the coachSequence at a specific stop for a specific train. Works for OEBB stops and DB stops.
         *
         * Returns plannedSequence if no real time information is available
         *
         * @property departure Departure at the stop you want the coachSequence for
         * @property evaNumber needed for OEBB Reihung, usually 7 digits
         * @property initialDeparture needed for OEBB Reihung
         */
        @Serializable
        @Resource("wagen/{trainNumber}")
        public data class CoachSequence(
            val trainNumber: Int,
            val departure: Instant,
            val evaNumber: String? = null,
            val initialDeparture: Instant? = null,
            val v4: V4 = V4()
        )
    }
}
