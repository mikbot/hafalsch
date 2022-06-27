package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.hafalsch.bot.command.station
import dev.schlaubi.hafalsch.bot.ui.emoji
import dev.schlaubi.hafalsch.marudor.Marudor
import io.ktor.http.*
import org.koin.core.component.inject

class StationArguments : Arguments() {
    val station by station {
        name = "station"
        description = "command.station.arguments.station.description"
    }
}

suspend fun Extension.stationCommand() = publicSlashCommand(::StationArguments) {
    name = "station"
    description = "command.station.description"
    val marudor by inject<Marudor>()

    action {
        val station = arguments.station

        respond {
            embed {
                title = station.name
                url = marudor.buildUrl { appendPathSegments(station.name) }

                field {
                    name = translate("station.available_transports")
                    value = station.availableTransports.mapNotNull { it.emoji }.joinToString("")
                }

                field {
                    name = translate("station.Location")
                    value =
                        "[Map](https://www.google.com/maps/search/${station.position.latitude}%2C+${station.position.longitude})"
                }
            }
        }
    }
}
