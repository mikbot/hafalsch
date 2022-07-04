package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.components.types.emoji
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.bot.command.station
import dev.schlaubi.hafalsch.bot.paginator.multiButtonPaginator
import dev.schlaubi.hafalsch.bot.ui.*
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.Departure
import dev.schlaubi.hafalsch.marudor.entity.isNullOrEmpty
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.inject


private val numbers = listOf(Emojis.one, Emojis.two, Emojis.three, Emojis.four, Emojis.five)

class DeparturesArguments : Arguments() {
    val station by station {
        name = "station"
        description = "commands.departures.arguments.station.description"
    }

    val lookahead by optionalInt {
        name = "lookahead"
        description = "commands.departures.arguments.lookahead.description"

        minValue = 0
        maxValue = 480
    }

    val lookbehind by defaultingInt {
        name = "lookbehind"
        description = "commands.departures.arguments.lookbehind.description"

        defaultValue = 0
        minValue = 0
        maxValue = 480
    }
}

suspend fun Extension.departuresCommand() = publicSlashCommand(::DeparturesArguments) {
    name = "commands.departures.name"
    description = "commands.departures.description"

    val marudor by inject<Marudor>()

    action {
        val departures = marudor.iris.departures(arguments.station.eva, arguments.lookahead, arguments.lookbehind)

        if (departures.isNullOrEmpty()) {
            respond {
                content = translate("commands.departures.empty")
            }
            return@action
        }

        val orderedDepartures = departures!!.allDepartures.chunked(5).map {
            val item = it.firstOrNull()
            val departureTime = (item?.departure?.actualTime ?: item?.arrival?.actualTime) ?: Instant.DISTANT_PAST

            departureTime to it
        }
        val now = Clock.System.now()
        val selectedIndex = orderedDepartures.indexOfFirst { (firstDeparture) -> firstDeparture > now }

        multiButtonPaginator {
            orderedDepartures.forEach { (_, currentDepartures) ->
                parent.page {
                    title = translate("departures.title", arrayOf(arguments.station.name.replaceStationNames()))

                    description = currentDepartures.joinToString("\n") { departure ->
                        val via = departure.route.asSequence().filter(Departure.Stop::showVia)
                            .joinToString(",") { (additional, cancelled, _, name1) ->
                                name1.cancel(cancelled).bold(additional).replaceStationNames()
                            }

                        val text = buildString {
                            if (departure.departure == null) {
                                append(departure.arrival!!.render()).append(' ')
                            } else {
                                append(departure.departure!!.render()).append(' ')
                            }

                            val name = if (departure.cancelled) {
                                runBlocking { translate("journey.wannabe", arrayOf(departure.train.name)) }
                            } else {
                                departure.train.name
                            }

                            append(name).append(": **").append(departure.destination.replaceStationNames())
                                .append("**")
                            if (via.isNotBlank()) {
                                append(" (").append(via).append(")")
                            }
                        }

                        text
                    }
                }
            }

            val allButLast = 0 until orderedDepartures.lastIndex
            numbers.forEachIndexed { index, (unicode) ->
                val pages = if (orderedDepartures.last().second.lastIndex < index) {
                    allButLast.toList()
                } else {
                    null
                }
                publicButton(2, pages) {
                    emoji(unicode)

                    action {
                        val journey = orderedDepartures[it.currentPageNum].second[index]

                        asUIContext {
                            journey(JourneyData(journey.train.name, arguments.station, null, null), true)
                        }
                    }
                }
            }
        }.apply {
            currentPageNum = selectedIndex
            currentPage = pages.get(currentGroup, currentPageNum)

            send()
        }
    }
}

fun Departure.StopInfo.render(): String {
    if (delay != null && delay != 0) {
        return "~~${scheduledTime.toDiscord(TimestampType.ShortTime)}~~ ${actualTime.toDiscord(TimestampType.ShortTime)} (+$delay)"
    }

    return scheduledTime.toDiscord(TimestampType.ShortTime)
}
