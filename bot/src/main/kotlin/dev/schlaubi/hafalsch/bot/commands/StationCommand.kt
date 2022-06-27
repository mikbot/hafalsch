package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.hafalsch.bot.command.station
import dev.schlaubi.hafalsch.bot.ui.emoji
import dev.schlaubi.hafalsch.marudor.Marudor
import io.ktor.http.*
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes

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

                val map = marudor.stopPlace.map(station)

                field {
                    name = translate("station.available_transports")
                    value = station.availableTransports.mapNotNull { it.emoji }.joinToString("")
                }

                field {
                    name = translate("station.location")
                    value =
                        "[Map](https://www.google.com/maps/search/${station.position.latitude}%2C+${station.position.longitude})"
                }

                if (map?.map != null) {
                    field {
                        name = translate("station.map")
                        value = map.map!!
                    }
                }
            }

            components(1.minutes) {
                ephemeralButton {
                    label = translate("station.buttons.legend")

                    action {
                        respond {
                            content = station.availableTransports
                                // joinToString is not an inline function
                                .map {
                                    "${it.emoji}: ${
                                        translate(
                                            "station.transport.${it.name.lowercase()}",
                                            this@publicSlashCommand.resolvedBundle
                                        )
                                    }"
                                }
                                .joinToString("\n")
                        }
                    }
                }
            }
        }
    }
}
