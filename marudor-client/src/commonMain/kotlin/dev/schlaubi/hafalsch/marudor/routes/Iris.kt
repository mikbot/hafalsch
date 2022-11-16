package dev.schlaubi.hafalsch.marudor.routes

import io.ktor.resources.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
@Resource("/iris/v2/")
public class Iris {

    @Serializable
    @Resource("abfahrten/{eva}")
    public data class Departures(
        val eva: String,
        val lookahead: Int? = null,
        val lookbehind: Int? = null,
        val iris: Iris = Iris()
    )

    @Serializable
    @Resource("wings/{rawId1}/{rawId2}")
    public data class Wings(val rawId1: String, val rawId2: String, val iris: Iris = Iris())
}

@Serializable
@Resource("journeys/v1")
public class Journeys {
    @Serializable
    @Resource("details/{trainName}")
    public data class Details(
        val trainName: String,
        val evaNumberAlongRoute: String? = null,
        val initialDepartureDate: Instant? = null,
        val journeys: Journeys = Journeys()
    )

    @Serializable
    @Resource("find/{trainName}")
    public data class Find(
        val trainName: String,
        val initialDepartureDate: Instant? = null,
        val filtered: Boolean? = null,
        val limit: Int? = null,
        val journeys: Journeys = Journeys()
    )
}
