package dev.schlaubi.hafalsch.bot.ui

import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.marudor.entity.TransportType

val TransportType.emoji: String?
    get() = when (this) {
        TransportType.Bus -> "<:ecitaro:941801084173832213>"
        TransportType.CityTrain -> Emojis.metro.toString()
        TransportType.HighSpeedTrain -> Emojis.bullettrainFront.toString()
        TransportType.InterCityTrain -> Emojis.train.toString()
        TransportType.InterRegionalTrain -> Emojis.lightRail.toString()
        TransportType.RegionalTrain -> Emojis.train2.toString()
        TransportType.Subway -> Emojis.tram.toString()
        TransportType.Tram -> Emojis.tram.toString()
        is TransportType.Unknown -> null
    }
