package dev.schlaubi.hafalsch.rainbow_ice.routes

import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Resource("")
@Serializable
public class RainbowICE {
    @Serializable
    @Resource("autocomplete/{query}")
    public data class Autocomplete(val query: String, val rainbowICE: RainbowICE = RainbowICE())

    @Serializable
    @Resource("train_vehicle")
    public data class TrainVehicle(
        @SerialName("q") val query: String,
        @SerialName("trip_limit") val tripLimit: Int? = null,
        @SerialName("include_routes") val includeRoutes: Boolean? = null,
        @SerialName("include_marudor_link") val includeMarudorLink: Boolean? = null,
        val rainbowICE: RainbowICE = RainbowICE()
    )
}
