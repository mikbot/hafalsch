package dev.schlaubi.hafalsch.marudor.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
public data class MinimalStation(val name: String, val evaNumber: String)

@Serializable
public data class Station(
    @SerialName("evaNumber")
    val eva: String,
    val name: String,
    val availableTransports: List<TransportType>,
    val position: Position? = null,
    val identifier: Identifier? = null
) {
    @Serializable
    public data class Identifier(
        val stationId: String? = null,
        val ifopt: String? = null,
        val ril100: String? = null,
        val alternativeRil100: List<String>? = emptyList(),
        @SerialName("evaNumber")
        val eva: String
    )
}

// Items Enum: "FERRY" "FLIGHT" "CAR" "TAXI" "SHUTTLE" "BIKE" "SCOOTER" "WALK" "UNKNOWN"
@Serializable(with = TransportType.Serializer::class)
public sealed class TransportType(public val name: String) {
    public object Tram : TransportType("TRAM")
    public object Subway : TransportType("SUBWAY")
    public object RegionalTrain : TransportType("REGIONAL_TRAIN")
    public object HighSpeedTrain : TransportType("HIGH_SPEED_TRAIN")
    public object Bus : TransportType("BUS")
    public object InterRegionalTrain : TransportType("INTER_REGIONAL_TRAIN")
    public object CityTrain : TransportType("CITY_TRAIN")
    public object InterCityTrain : TransportType("INTERCITY_TRAIN")
    public object Ferry : TransportType("FERRY")
    public object Flight : TransportType("FLIGHT")
    public object Car : TransportType("CAR")
    public object Taxi : TransportType("TAXI")
    public object Shuttle : TransportType("SHUTTLE")
    public object Bike : TransportType("BIKE")
    public object Scooter : TransportType("SCOOTER")
    public class Unknown(name: String) : TransportType(name)

    override fun toString(): String = name

    public companion object Serializer : KSerializer<TransportType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TransportType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): TransportType {
            return when (val name = decoder.decodeString()) {
                "TRAM" -> Tram
                "SUBWAY" -> Subway
                "REGIONAL_TRAIN" -> RegionalTrain
                "HIGH_SPEED_TRAIN" -> HighSpeedTrain
                "BUS" -> Bus
                "INTER_REGIONAL_TRAIN" -> InterRegionalTrain
                "CITY_TRAIN" -> CityTrain
                "INTERCITY_TRAIN" -> InterCityTrain
                "FERRY" -> Ferry
                "FLIGHT" -> Flight
                "CAR" -> Car
                "TAXI" -> Taxi
                "SHUTTLE" -> Shuttle
                "BIKE" -> Bike
                "SCOOTER" -> Scooter
                else -> Unknown(name)
            }
        }

        override fun serialize(encoder: Encoder, value: TransportType): Unit = encoder.encodeString(value.name)
    }
}

@Serializable
public class Map(@SerialName("lageplan") public val map: String? = null)
