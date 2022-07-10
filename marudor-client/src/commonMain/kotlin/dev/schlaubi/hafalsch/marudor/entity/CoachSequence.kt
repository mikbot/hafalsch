package dev.schlaubi.hafalsch.marudor.entity

import dev.schlaubi.hafalsch.client.util.NumberedEnum
import dev.schlaubi.hafalsch.client.util.NumberedEnumSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class RelativePosition(val startPercent: Double = 0.0, val stopPercent: Double = 100.0)

@Serializable
public data class CoachSequence(
    val stop: Stop,
    val product: Product,
    val sequence: Sequence,
    val multipleTrainNumbers: Boolean = false,
    val multipleDestinations: Boolean = false,
    val isRealtime: Boolean,
    val direction: Boolean? = false
) {

    @Serializable
    public data class Stop(val stopPlace: MinimalStation, val sectors: List<Sector>) {

        @Serializable
        public data class Sector(val name: String, val position: RelativePosition)
    }

    @Serializable
    public data class Product(val number: String, val type: String, val line: String? = number)

    @Serializable
    public data class Sequence(public val groups: List<CoachGroup>)
}

@Serializable
public data class CoachGroup(
    val coaches: List<Coach>,
    val name: String,
    val originName: String,
    val destinationName: String,
    val trainName: String? = null,
    val number: String,
    @SerialName("baureihe")
    val model: Model? = null
) {
    @Serializable
    public data class Model(val name: String, @SerialName("baureihe") val model: String? = null, val identifier: String)
}

@Serializable
public data class Coach(
    @SerialName("class")
    val waggonClass: WaggonClass,
    val category: String,
    val closed: Boolean = false,
    val uic: String? = null,
    val type: String? = null,
    val identificationNumber: String,
    val position: RelativePosition,
    val features: Features,
    val seats: Seats? = null
) {

    @Serializable(with = WaggonClass.Serializer::class)
    public enum class WaggonClass(override val value: Int) : NumberedEnum {
        UNKNOWN(0),
        FIRST_CLASS(1),
        SECOND_CLASS(2),
        FIRST_AND_SECOND_CLASS(3),
        NOT_FOR_PASSENGERS(4);

        internal companion object Serializer : NumberedEnumSerializer<WaggonClass>(enumValues()) {
            override val name: String = "WaggonClass"
        }
    }

    @Serializable
    public data class Features(
        val dining: Boolean = false,
        val wheelchair: Boolean = false,
        val bike: Boolean = false,
        val disabled: Boolean = false,
        val quiet: Boolean = false,
        val info: Boolean = false,
        val family: Boolean = false,
        val toddler: Boolean = false,
        val wifi: Boolean = false,
        val comfort: Boolean = false
    )

    @Serializable
    public data class Seats(
        val comfort: String? = null, val disabled: String? = null, val family: String? = null
    )
}
