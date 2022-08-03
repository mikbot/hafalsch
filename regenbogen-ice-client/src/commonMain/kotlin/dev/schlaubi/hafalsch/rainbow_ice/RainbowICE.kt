package dev.schlaubi.hafalsch.rainbow_ice

import dev.schlaubi.hafalsch.client.ClientCompanion
import dev.schlaubi.hafalsch.client.ClientResources
import dev.schlaubi.hafalsch.client.util.safeBody
import dev.schlaubi.hafalsch.rainbow_ice.entity.Station
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import dev.schlaubi.hafalsch.rainbow_ice.routes.RainbowICE as RainbowICERoute
import dev.schlaubi.hafalsch.rainbow_ice.routes.APIRoute

/**
 * Mapper of the [regenbogen-ice.de](regenbogen-ice.de) API.
 *
 * You might need to import [dev.schlaubi.hafalsch.client.invoke] to use [RainbowICEBuilder]
 */
public class RainbowICE(private val resources: ClientResources) {

    /**
     * Provides autocomplete for [query].
     */
    public suspend fun autocomplete(query: String): List<String> =
        resources.client.get(APIRoute.Autocomplete(query)).body()

    /**
     * Validates whether [query] is a valid TZn.
     *
     * @see autocomplete
     */
    public suspend fun matchTrain(query: String): Boolean = query in autocomplete(query)

    /**
     * Fetches [train information][TrainVehicle] for [query].
     *
     * @param tripLimit How many [trips][TrainVehicle.trips] to fetch
     * @param includeRoutes whether to include [TrainVehicle.Trip.stops]
     * @param includeMarudorLink whether to include [TrainVehicle.Trip.marudor]
     */
    public suspend fun fetchTrain(
        query: String,
        tripLimit: Int? = null,
        includeRoutes: Boolean? = null,
        includeMarudorLink: Boolean? = null
    ): TrainVehicle? =
        resources.client.get(APIRoute.TrainVehicle.Specific(query, tripLimit, includeRoutes, includeMarudorLink))
            .safeBody()

    /**
     * Provides station autocomplete for [query].
     *
     * **This only includes long distance travel stations**
     */
    public suspend fun stationSearch(query: String): List<Station> =
        resources.client.get(APIRoute.StationSearch(query)).body()

    /**
     *  Returns an RSS feed for the `Regenbogen ICE`.
     */
    public suspend fun rss(): HttpResponse = resources.client.get(RainbowICERoute.Rss())

    /**
     * Returns an RSS feed for the `Regenbogen ICE` at [station].
     */
    public suspend fun rss(station: String): HttpResponse =
        resources.client.get(RainbowICERoute.Rss.ForStation(station))

    public companion object : ClientCompanion<RainbowICE, RainbowICEBuilder> {
        override fun newBuilder(): RainbowICEBuilder = RainbowICEBuilder()
    }
}
