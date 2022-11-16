package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.hafalsch.rainbow_ice.FindCoachQuery
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.mikbot.plugin.api.util.discordError
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.inject

class CoachArguments : Arguments() {
    val uic by string {
        name = "uic"
        description = "commands.coach.arguments.uic.description"
    }
}

suspend fun Extension.coachCommand() = publicSlashCommand(::CoachArguments) {
    name = "commands.coach.name"
    description = "commands.coach.description"

    val rainbow by inject<RainbowICE>()

    action {
        val coach = rainbow.searchCoach(arguments.uic).coach.firstOrNull()
            ?: discordError(translate("commands.coach.bot_found"))

        respond {
            embed {
                title = translate("commands.coach.title", arrayOf(arguments.uic))
                field {
                    name = translate("commands.coach.type")
                    value = coach.type
                    inline = true
                }
                field {
                    name = translate("commands.coach.category")
                    value = coach.category
                    inline = true
                }
                field {
                    name = translate("commands.coach.class")
                    value = coach.`class`.toString()
                    inline = true
                }

                val now = Clock.System.now()
                val orderedLinks = coach.coach_links.sortedBy(FindCoachQuery.Coach_link::departure)
                val currentTrip = orderedLinks.firstOrNull {
                    it.trip.initial_departure.asInstant() < now && it.trip.stops.last().scheduled_arrival!!.asInstant() > now
                }
                val nextTrip = orderedLinks.lastOrNull {
                    it.trip.initial_departure.asInstant() > now
                }
                val pastTrips = orderedLinks.filter {
                    it.trip.stops.last().scheduled_arrival!!.asInstant() < now
                }

                if (nextTrip != null) {
                    field {
                        name = translate("command.coach.next_trip")
                        value = journey(nextTrip.trip)
                    }
                }

                if (currentTrip != null) {
                    field {
                        name = translate("command.coach.current_trip")
                        value = journey(currentTrip.trip)
                    }
                }
                if (pastTrips.isNotEmpty()) {
                    field {
                        name = translate("command.coach.past_trips")
                        value = pastTrips.take(5).map {
                            journey(it.trip)
                        }.joinToString("\n") { "- $it" }
                    }
                }
            }
        }
    }
}

private suspend fun CommandContext.journey(journey: FindCoachQuery.Trip) =
    translate(
        "commands.coach.trip",
        arrayOf(
            journey.train_type,
            journey.train_number,
            journey.bahn_expert,
            journey.origin_station,
            journey.initial_departure.departure,
            journey.destination_station,
            journey.stops.last().scheduled_arrival?.departure
        )
    )

private val Any.departure: String
    get() = asInstant().toMessageFormat(DiscordTimestampStyle.ShortDateTime)

private fun Any.asInstant() = Instant.parse(toString())
