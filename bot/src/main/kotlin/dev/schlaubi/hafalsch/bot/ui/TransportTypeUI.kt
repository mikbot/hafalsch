package dev.schlaubi.hafalsch.bot.ui

import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.marudor.entity.TransportType

val TransportType.emoji: String?
    get() =  when (this) {
        TransportType.Bus -> BUS_EMOJI
        TransportType.CityTrain -> Emojis.metro.toString()
        TransportType.HighSpeedTrain -> Emojis.bullettrainFront.toString()
        TransportType.InterCityTrain -> Emojis.train.toString()
        TransportType.InterRegionalTrain -> Emojis.lightRail.toString()
        TransportType.RegionalTrain -> Emojis.train2.toString()
        TransportType.Subway -> Emojis.tram.toString()
        TransportType.Tram -> Emojis.tram.toString()
        TransportType.Bike -> Emojis.bike.toString()
        TransportType.Car -> Emojis.blueCar.toString()
        TransportType.Ferry -> Emojis.ferry.toString()
        TransportType.Flight -> Emojis.airplane.toString()
        TransportType.Scooter -> Emojis.scooter.toString()
        TransportType.Shuttle -> Emojis.minibus.toString()
        TransportType.Taxi -> Emojis.taxi.toString()
        is TransportType.Unknown -> null
    }
