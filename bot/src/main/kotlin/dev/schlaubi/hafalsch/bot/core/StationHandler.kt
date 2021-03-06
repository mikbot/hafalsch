package dev.schlaubi.hafalsch.bot.core

import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.hafalsch.bot.ui.UIContext
import dev.schlaubi.hafalsch.bot.ui.emoji
import dev.schlaubi.hafalsch.marudor.entity.Station
import dev.schlaubi.hafalsch.marudor.entity.TransportType
import io.ktor.http.*
import kotlin.time.Duration.Companion.minutes

context(UIContext)suspend fun FollowupMessageCreateBuilder.sendStation(station: Station) {
    embed {
        title = station.name
        url = marudor.buildUrl { appendPathSegments(station.name) }

        val map = marudor.stopPlace.map(station)

        field {
            name = translate("station.available_transports")
            value = station.availableTransports.mapNotNull(TransportType::emoji).joinToString("")
        }

        if (station.position != null) {
            field {
                name = translate("station.location")
                value =
                    "[Map](https://www.google.com/maps/search/${station.position?.latitude}%2C+${station.position?.longitude})"
            }
        }

        if (map?.map != null) {
            field {
                name = translate("station.map")
                value = (map.map ?: return@field)
            }
        }
    }

    components(1.minutes) {
        ephemeralButton {
            label = translate("station.buttons.legend")
            bundle = this@UIContext.bundle

            action {
                respond {
                    content = station.availableTransports
                        // joinToString is not an inline function
                        .map {
                            "${it.emoji}: ${
                                translate(
                                    "station.transport.${it.name.lowercase()}",
                                    bundle
                                )
                            }"
                        }
                        .joinToString("\n")
                }
            }
        }
    }
}
