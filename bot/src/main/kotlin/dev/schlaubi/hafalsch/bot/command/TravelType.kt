package dev.schlaubi.hafalsch.bot.command

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import dev.schlaubi.hafalsch.traewelling.routes.Traewelling.Trains.Stationboard.TravelType as TraewellingTravelType

enum class TravelType(val parent: TraewellingTravelType, override val readableName: String) : ChoiceEnum {
    NATIONAL_EXPRESS(TraewellingTravelType.NATIONAL_EXPRESS, "traewelling.travel_type.national_express"),
    NATIONAL(TraewellingTravelType.NATIONAL, "traewelling.travel_type.national"),
    REGIONAL_EXPRESS(TraewellingTravelType.REGIONAL_EXPRESS, "traewelling.travel_type.regional_express"),
    REGIONAL(TraewellingTravelType.REGIONAL, "traewelling.travel_type.regional"),
    SUBURBAN(TraewellingTravelType.SUBURBAN, "traewelling.travel_type.suburban"),
    BUS(TraewellingTravelType.BUS, "traewelling.travel_type.bus"),
    FERRY(TraewellingTravelType.FERRY, "traewelling.travel_type.ferry"),
    SUBWAY(TraewellingTravelType.SUBWAY, "traewelling.travel_type.subway"),
    TRAM(TraewellingTravelType.TRAM, "traewelling.travel_type.tram"),
    TAXI(TraewellingTravelType.TAXI, "traewelling.travel_type.taxi");

    companion object {
        const val argumentName = "travel-type"
    }
}
