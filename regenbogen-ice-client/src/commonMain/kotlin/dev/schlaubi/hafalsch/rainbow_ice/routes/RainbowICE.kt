package dev.schlaubi.hafalsch.rainbow_ice.routes

import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle as TrainVehicleEntity
import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mapping of [Regenbogen ICE API](https://github.com/regenbogen-ice/api) / route.
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
}
