package dev.schlaubi.hafalsch.marudor.routes

import io.ktor.resources.*
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
@Resource("hafas")
public class Hafas {

    @Serializable
    @Resource("v1")
    public data class V1(val hafas: Hafas = Hafas()) {
        /**
         * This redirects to the current Details Page with a provided HAFAS TripId / JourneyId / JID.
         */
        @Serializable
        @Resource("detailsRedirect/{tripId}")
        public data class DetailsRedirect(val tripId: String, val profile: HafasProfile? = null, val v1: V1 = V1())

        @Serializable
        @Resource("irisCompatibleAbfahrten/{eva}")
        public data class IrisCompatibleAbfahrten(
            val eva: String,
            val lookahead: Int? = null,
            val lookbehind: Int? = null,
        )
    }

    @Serializable
    @Resource("v3")
    public data class V3(val hafas: Hafas = Hafas()) {
        @Serializable
        @Resource("additionalInformation/{trainName}")
        public data class AdditionalInformation(
            val trainName: String,
            val evaNumberAlongRoute: String? = null,
            val initialDepartureDate: Instant? = null,
            val v3: V3 = V3()
        )
    }
}


@Serializable(with = HafasProfile.Serializer::class)
public enum class HafasProfile(public val serialName: String) {
    DB("db"),
    OEBB("oebb"),
    BVG("bvg"),
    HVV("hvv"),
    RMV("rmv"),
    SNCB("sncb"),
    AVV("avv"),
    NAHSH("nahsh"),
    INSA("insa"),
    ANACHB("anachb"),
    VAO("vao"),
    SBB("sbb"),
    DBNETZ("dbnetz"),
    PKP("pkp"),
    DBREGIO("dbregio"),
    SMARTRBL("smartrbl"),
    VBN("vbn");

    public companion object {
        public val DEFAULT: HafasProfile = DB
    }

    public object Serializer : KSerializer<HafasProfile> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HafasProfile", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): HafasProfile {
            val name = decoder.decodeString()
            return values().firstOrNull { it.serialName == name }
                ?: throw SerializationException("Could not find hafas profile $name")
        }

        override fun serialize(encoder: Encoder, value: HafasProfile): Unit = encoder.encodeString(value.serialName)
    }
}
