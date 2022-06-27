package dev.schlaubi.hafalsch.marudor.routes

import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Resource("stopPlace/v1")
public class StopPlace {

    /**
     * @property eva Usually 7 digits, leading zeros can be omitted
     */
    @Serializable
    @Resource("lageplan/{name}/{eva}")
    public data class Map(val name: String, val eva: String, val stopPlace: StopPlace = StopPlace())

    /**
     * @property eva Usually 7 digits, leading zeros can be omitted
     */
    @Serializable
    @Resource("{eva}")
    public data class StopPlaceByEva(val eva: String, val stopPlace: StopPlace = StopPlace()) {
        @Serializable
        @Resource("identifier")
        public data class Identifier(val parent: StopPlaceByEva) {
            public constructor(eva: String) : this(StopPlaceByEva(eva))
        }
    }

    @Serializable
    @Resource("geoSearch")
    public data class StopPlaceGeoSearch(
        @SerialName("lat") val latitude: Double,
        @SerialName("lng") val longitude: Double,
        val radius: Int? = null,
        val filterForIris: Boolean? = null,
        val max: Int? = null,
        val stopPlace: StopPlace = StopPlace()
    )

    @Serializable
    @Resource("search/{query}")
    public data class StopPlaceSearch(
        val query: String,
        val max: Int? = null,
        val filterForIris: Boolean? = null,
        val groupedBySales: Boolean? = null,
        val stopPlace: StopPlace = StopPlace()
    )

    /**
     * Currently only for VRR.
     */
    @Serializable
    @Resource("{eva}/trainOccupancy")
    public data class TrainOccupancy(val eva: String, val stopPlace: StopPlace = StopPlace())
}
