package dev.schlaubi.hafalsch.marudor.entity

import dev.schlaubi.hafalsch.client.util.NumberedEnum
import dev.schlaubi.hafalsch.client.util.NumberedEnumSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.collections.Map as CollectionMap

private val qualityMessagesRange = 0..69
private val serviceMessagesRange = 70..1000 // includes 900 and 1000

@Serializable
public data class IrisMessage(
    val text: String,
    val head: String? = null,
    val timestamp: Instant? = null,
    val superseded: Boolean = false,
    val priority: Priority? = null,
    val value: Int? = null,
    val stopPlace: HafasStation? = null
) {

    public val type: Type?
        get() {
            val safeValue = value ?: return null

            return when(safeValue) {
               in qualityMessagesRange -> Type.QUALITY_MESSAGE
               in serviceMessagesRange -> Type.SERVICE_MESSAGE
               else -> null
            }
        }

    public enum class Type {
        SERVICE_MESSAGE,
        QUALITY_MESSAGE
    }

    @Serializable(with = Priority.Serializer::class)
    public enum class Priority(override val value: Int) : NumberedEnum {
        HIGH(1),
        MEDIUM(2),
        LOW(3),
        DONE(4);

        internal companion object Serializer : NumberedEnumSerializer<Priority>(enumValues()) {
            override val name: String = "Priority"
        }
    }
}

/**
 * Response of departures endpoint.
 *
 * @property departures Journeys that have not yet departed (or arrived if they end here)
 * @property lookbehind Journeys that have already departed (or arrived if they end here)
 * @property wings Map of "mediumId" to Abfahrt.
 * @property strike amount of departures/arrivals that are affected by a strike
 */
@Serializable
public data class IrisDepartures(
    val departures: List<Departure>,
    val lookbehind: List<Departure>,
    val wings: CollectionMap<String, Departure>,
    val strike: Int = 0
) {
    val allDepartures: List<Departure>
        get() = lookbehind + departures
}

@OptIn(ExperimentalContracts::class)
public fun IrisDepartures?.isNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNullOrEmpty == null)
    }

    return this == null || allDepartures.isEmpty()
}

@Serializable
public data class Departure(
    val initialDeparture: Instant,
    val arrival: StopInfo? = null,
    @SerialName("auslastung")
    val hasLoad: Boolean? = null,
    val currentStopPlace: MinimalStation,
    val departure: StopInfo? = null,
    val destination: String,
    val id: String,
    val additional: Boolean = false,
    val cancelled: Boolean = false,
    val mediumId: String,
    val messages: Messages,
    val platform: String? = null,
    val productClass: String? = null,
    val rawId: String,
    val ref: Ref? = null,
    val reihung: Boolean,
    val route: List<Stop>,
    val scheduledDestination: String,
    val scheduledPlatform: String? = null,
    val substitute: Boolean = false,
    val train: Train
) {
    @Serializable
    public data class StopInfo(
        val scheduledPlatform: String? = null,
        val platform: String? = null,
        val scheduledTime: Instant,
        val delay: Int? = null,
        val reihung: Boolean = false,
        val messages: List<HafasMessage> = emptyList(),
        val cancelled: Boolean = false,
        val wingIds: List<String> = emptyList(),
        val hidden: Boolean = false
    ) {
        val actualTime: Instant
            get() = scheduledTime + (delay ?: 0).toDuration(DurationUnit.MINUTES)
    }

    @Serializable
    public data class Stop(
        val additional: Boolean = false,
        val cancelled: Boolean = false,
        val showVia: Boolean = false,
        val name: String
    )

    @Serializable
    public data class Ref(
        val trainNumber: String,
        val trainType: String,
        val train: String
    )

    @Serializable
    public data class Messages(
        @SerialName("qos")
        val qualityOfService: List<IrisMessage>,
        val delay: List<IrisMessage>,
        val him: List<IrisMessage>,
    )
}
