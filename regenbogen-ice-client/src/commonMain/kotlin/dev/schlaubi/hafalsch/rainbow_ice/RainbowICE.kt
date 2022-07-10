package dev.schlaubi.hafalsch.rainbow_ice

import dev.schlaubi.hafalsch.client.ClientCompanion
import dev.schlaubi.hafalsch.client.ClientResources
import dev.schlaubi.hafalsch.client.util.safeBody
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import dev.schlaubi.hafalsch.rainbow_ice.routes.RainbowICE as RainbowICERoute

public class RainbowICE(private val resources: ClientResources) {

    /**
     * Provides autocomplete for [query].
     */
    public suspend fun autocomplete(query: String): List<String> =
        resources.client.get(RainbowICERoute.Autocomplete(query)).body()

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
     * @param includeRoutes whether to include [TrainVehicle.trips]
     * @param includeMarudorLink whether to include [TrainVehicle.Trip.marudor]
     */
    public suspend fun fetchTrain(
        query: String,
        tripLimit: Int? = null,
        includeRoutes: Boolean? = null,
        includeMarudorLink: Boolean? = null
    ): TrainVehicle? =
        resources.client.get(RainbowICERoute.TrainVehicle(query, tripLimit, includeRoutes, includeMarudorLink))
            .safeBody()

    public companion object : ClientCompanion<RainbowICE, RainbowICEBuilder> {
        override fun newBuilder(): RainbowICEBuilder = RainbowICEBuilder()
    }
}
