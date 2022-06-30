package dev.schlaubi.hafalsch.marudor.entity

import dev.schlaubi.hafalsch.marudor.util.NumberedEnum
import dev.schlaubi.hafalsch.marudor.util.NumberedEnumSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class JourneyInformation(
    val cancelled: Boolean = false,
    val changeDuration: Int? = null,
    val duration: Int? = null,
    val finalDestination: String,
    @SerialName("jid")
    val journeyId: String,
    val product: Product? = null,
    val raw: JsonObject? = null,
    val segmentDestination: Segment,
    val segmentStart: Segment,
    val stops: List<Stop>,
    val train: Train,
    @SerialName("auslastung")
    val load: Load? = null,
    val messages: List<HafasMessage> = emptyList(),
    val tarifSet: TarifSet? = null,
    val plannedSequence: Sequence? = null,
    val type: String,
    val arrival: Stop.Date,
    val departure: Stop.Date,
    val wings: List<JourneyInformation> = emptyList(),
    val currentStop: Stop? = null
)

@Serializable
public data class TarifSet(val fares: List<Fare>)

@Serializable
public data class Fare(
    val price: Int,
    val moreExpensiveAvailable: Boolean,
    val bookable: Boolean,
    val upsell: Boolean,
    val targetContext: String
)

@Serializable
public data class Sequence(val rawType: String, val shortType: String, val type: String)

@Serializable
public data class Product(
    val name: String,
    val number: String? = null,
    val icoX: Int,
    val cls: Int,
    val oprX: Int? = null,
    val prodCtx: JsonObject? = null,
    val addName: String? = null,
    val nameS: String? = null,
    val machId: String? = null
)

@Serializable
public data class Segment(val title: String, val id: String)

@Serializable
public data class Stop(
    val arrival: Date? = null,
    val departure: Date? = null,
    val station: Segment,
    @SerialName("auslastung")
    val load: Load? = null,
    val messages: List<HafasMessage> = emptyList(),
    val additional: Boolean = false,
    val cancelled: Boolean = false,
    val irisMessages: List<IrisMessage> = emptyList()
) {
    @Serializable
    public data class Date(
        val scheduledPlatform: String? = null,
        val platform: String? = null,
        val scheduledTime: Instant,
        val time: Instant,
        val delay: Int? = null,
        @SerialName("reihung")
        val hasWaggonOrder: Boolean = false,
        val messages: List<HafasMessage> = emptyList(),
        val cancelled: Boolean = false
    )
}

@Serializable
public data class Load(val first: Load? = null, val second: Load? = null) {
    @Serializable(with = Load.Serializer::class)
    public enum class Load(override val value: Int) : NumberedEnum {
        LOW(1),
        HIGH(2),
        VERY_HIGH(3),
        SOLD_OUT(5);

        internal companion object Serializer : NumberedEnumSerializer<Load>(enumValues()) {
            override val name: String = "Load"
        }
    }
}

@Serializable
public data class HafasMessage(
    val type: String,
    val code: String,
    val icoX: Int,
    val txtN: String,
    val txtS: String? = null,
    val prio: Int? = null,
    val sidx: Int? = null
)


@Serializable
public data class Train(
    val name: String,
    val line: String? = null,
    val number: String? = null,
    val type: String? = null,
    val operator: Operator? = null,
    val admin: String? = null
)

@Serializable
public data class Operator(
    val name: String,
    val icoX: Int
)
