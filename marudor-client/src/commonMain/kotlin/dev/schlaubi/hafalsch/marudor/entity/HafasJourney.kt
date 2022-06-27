package dev.schlaubi.hafalsch.marudor.entity

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class JourneyInformation(
    val cancelled: Boolean = false,
    val changeDuration: Int? = null,
    val duration: Int,
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
    val messages: List<HafasMessage>,
    val type: String,
    val arrival: Stop.Date,
    val departure: Stop.Date,
    val wings: List<JourneyInformation> = emptyList(),
    val currentStop: Stop? = null
)


@Serializable
public data class Product(
    val name: String,
    val number: String,
    val icoX: Int,
    val cls: Int,
    val oprX: Int,
    val prodCtx: JsonObject,
    val addName: String,
    val nameS: String,
    val machId: String
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
        val scheduledTime: Instant? = null,
        val time: Instant,
        val delay: Int? = null,
        @SerialName("reihung")
        val hasWaggonOrder: Boolean,
        val messages: List<HafasMessage> = emptyList(),
        val cancelled: Boolean = false
    )
}

@Serializable
public data class Load(val first: Int? = null, val second: Int? = null)

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
    val line: String,
    val number: String,
    val type: String,
    val operator: Operator,
    val admin: String
) {
    @Serializable
    public data class Operator(
        val name: String,
        val icoX: Int
    )
}
