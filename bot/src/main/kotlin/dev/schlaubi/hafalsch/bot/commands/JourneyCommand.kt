package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.bot.command.optionalDate
import dev.schlaubi.hafalsch.bot.command.optionalStation
import dev.schlaubi.hafalsch.bot.command.profile
import dev.schlaubi.hafalsch.bot.core.sendStation
import dev.schlaubi.hafalsch.bot.paginator.multiButtonPaginator
import dev.schlaubi.hafalsch.bot.ui.DiscordColors
import dev.schlaubi.hafalsch.bot.ui.getLoadForValue
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.HafasJourneyMatchJourney
import dev.schlaubi.hafalsch.marudor.entity.HafasMessage
import dev.schlaubi.hafalsch.marudor.entity.Stop
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import info.debatty.java.stringsimilarity.Levenshtein
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.inject

private val levensthein = Levenshtein()

class JourneyArguments : Arguments(), KordExKoinComponent {
    private val marudor by inject<Marudor>()

    val name by string {
        name = "name"
        description = "commands.journey.arguments.name.description"

        autoComplete {
            val input = focusedOption.safeInput
            val type = input.substringBefore(' ')

            suspend fun Deferred<List<HafasJourneyMatchJourney>>.sortByRelevance() =
                await().sortedBy { levensthein.distance(input, it.train.name) }

            fun CoroutineScope.fetchSafe(input: String) = async {
                runCatching {
                    marudor.hafas.journeyMatch(input)
                }.getOrElse {
                    emptyList()
                }
            }

            coroutineScope {
                val matchingInput = fetchSafe(input)
                val matchingType = fetchSafe(type)
                val results =
                    (matchingInput.sortByRelevance() + matchingType.sortByRelevance()).distinctBy { it.train.name }

                suggestString {
                    results.take(25).forEach {
                        choice("${it.train.name} -> ${it.lastStop.station.title}", it.train.name)
                    }
                }
            }

        }
    }

    val station by optionalStation {
        name = "station"
        description = "commands.journey.arguments.station.description"
    }

    val date by optionalDate {
        name = "date"
        description = "commands.journey.arguments.date.description"
    }

    val profile by profile()
}

suspend fun Extension.journeyCommand() = publicSlashCommand(::JourneyArguments) {
    name = "journey"
    description = "commands.journey.description"
    val marudor by inject<Marudor>()

    action commandAction@{
        val journey = marudor.hafas.details(
            arguments.name, arguments.station?.eva, arguments.date, arguments.profile?.profile
        ) ?: discordError(translate("commands.journey.not_found", arrayOf(arguments.name)))

        val selectedEva = journey.currentStop?.station?.id
        val selectedIndex = selectedEva?.run { journey.stops.indexOfFirst { it.station.id == selectedEva } }
        val pagesWithOrder = journey.stops.asSequence()
            .withIndex()
            .filter { (_, stop) -> stop.arrival?.hasWaggonOrder == true || stop.departure?.hasWaggonOrder == true }
            .map(IndexedValue<*>::index)
            .toList()

        multiButtonPaginator {
            journey.stops.forEach { stop ->
                parent.page {
                    title = "${journey.train.name} - ${stop.station.title}"
                    url = marudor.hafas.detailsRedirect(journey.journeyId)

                    val hafasMessages = stop.messages.map(HafasMessage::txtN)

                    val irisMessages = stop.irisMessages.filter { it.head == null }.map {
                        "${it.timestamp.toDiscord(TimestampType.ShortTime)}: ${it.text}".cancel(
                            it.superseded
                        )
                    }


                    description = (hafasMessages + irisMessages).joinToString("\n")
                    val delay = stop.departure?.delay ?: stop.arrival?.delay
                    color = when {
                        stop.additional -> DiscordColors.GREEN
                        stop.cancelled -> DiscordColors.FUCHSIA
                        delay != null && delay > 0 -> DiscordColors.RED
                        delay != null && delay <= 0 -> DiscordColors.BLURLPLE

                        else -> DiscordColors.BLACK
                    }

                    field {
                        name = translate("journey.operator")
                        value = journey.train.operator.name
                    }

                    if (stop.arrival != null) {
                        field {
                            name = translate("journey.arrival")
                            value = stop.arrival.render()
                        }
                    }
                    if (stop.departure != null) {
                        field {
                            name = translate("journey.departure")
                            value = stop.departure.render()
                        }
                    }
                    val platform = stop.renderPlatform()
                    if (platform != null) {
                        field {
                            name = translate("journey.platform")
                            value = platform
                        }
                    }
                    val load = stop.load
                    if (load != null) {
                        field {
                            name = translate("journey.load")
                            value = """
                                ${Emojis.one}: ${getLoadForValue(load.first)}
                                ${Emojis.two}: ${getLoadForValue(load.second)}
                            """.trimIndent()
                        }
                    }
                }
            }
            ephemeralButton(2) {
                label = translate("journey.station_info")

                action {
                    respond {
                        val stationSegment = journey.stops[it.currentPageNum].station
                        val station = marudor.stopPlace.byEva(stationSegment.id)
                            ?: discordError(translate("journey.station.not_found"))

                        sendStation({ translate(it) }, marudor, station)
                    }
                }
            }

            if (pagesWithOrder.isNotEmpty()) {
                ephemeralButton(2, pagesWithOrder) {
                    label = translate("journey.waggon_order")

                    action {
                        respond {
                            content = "Wagenreihung"
                        }
                    }
                }
            }
        }.apply {
            if (selectedIndex != null) {
                currentPageNum = selectedIndex
                currentPage = pages.get(currentGroup, currentPageNum)

            }
            send()
        }
    }
}

private fun String.cancel(cancel: Boolean) = if (cancel) "~~$this~~" else this

private fun Stop.Date?.render(): String {
    if (this == null) {
        return "<unknown>"
    }

    if (scheduledTime != null && delay != null && delay != 0) {
        return "~~${scheduledTime!!.toDiscord(TimestampType.ShortTime)}~~ ${time.toDiscord(TimestampType.ShortTime)} (+$delay)"
    }

    return time.toDiscord(TimestampType.ShortTime)
}

private fun Stop.renderPlatform(): String? {
    val scheduled = arrival?.scheduledPlatform ?: departure?.scheduledPlatform
    val actual = arrival?.platform ?: departure?.platform

    if (scheduled != null) {
        return "~~$scheduled~~ $actual"
    }

    return actual
}
