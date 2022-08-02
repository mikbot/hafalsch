package dev.schlaubi.hafalsch.rainbow_ice.routes

import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle as TrainVehicleEntity
import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mapping of [Regenbogen ICE API](https://github.com/regenbogen-ice/api).
 */
@Resource("")
@Serializable
public class RainbowICE {

    /**
     * Returns an RSS feed for the `Regenbogen ICE`.
     */
    @Serializable
    @Resource("rss")
    public data class Rss(val rainbowICE: RainbowICE = RainbowICE()) {

        /**
         * Returns an RSS feed for the `Regenbogen ICE` at [station].
         */
        @Serializable
        @Resource("{station}")
        public data class ForStation(val station: String, val rss: Rss = Rss())
    }

    /**
     * Provides station autocomplete for [query].
     *
     * **This only includes long distance travel stations**
     */
    @Serializable
    @Resource("stationSearch/{query}")
    public data class StationSearch(val query: String, val rainbowICE: RainbowICE = RainbowICE())

    /**
     * Provides train autocomplete for [query] (TZn and name).
     */
    @Serializable
    @Resource("autocomplete/{query}")
    public data class Autocomplete(val query: String, val rainbowICE: RainbowICE = RainbowICE())

    /**
     * Meta-class for `/train_vehicle` route.
     */
    @Serializable
    @Resource("train_vehicle")
    public data class TrainVehicle(val rainbowICE: RainbowICE = RainbowICE()) {
        /**
         * Fetches [train information][TrainVehicleEntity] for [query].
         *
         * @property tripLimit How many [trips][TrainVehicleEntity.trips] to fetch
         * @property includeRoutes whether to include [TrainVehicleEntity.trips]
         * @property includeMarudorLink whether to include [TrainVehicleEntity.Trip.marudor]
         */
        @Serializable
        @Resource("")
        public data class Specific(
            @SerialName("q") val query: String,
            @SerialName("trip_limit") val tripLimit: Int? = null,
            @SerialName("include_routes") val includeRoutes: Boolean? = null,
            @SerialName("include_marudor_link") val includeMarudorLink: Boolean? = null,
            val trainVehicle: TrainVehicle = TrainVehicle()
        )
    }
}
