package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalTimestamp
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.hafalsch.bot.command.optionalStation
import dev.schlaubi.hafalsch.bot.command.profile
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.HafasJourneyMatchJourney
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
//                val all = fetchSafe("")
                val results = (matchingInput.sortByRelevance() + matchingType.sortByRelevance())
                    .distinctBy { it.train.name }

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

    val date by optionalTimestamp {
        name = "date"
        description = "commands.journey.arguments.date.description"
    }

    val profile by profile()
}

suspend fun Extension.journeyCommand() = publicSlashCommand(::JourneyArguments) {
    name = "journey"
    description = "commands.journey.description"
    val marudor by inject<Marudor>()

    action {
        val journey = marudor.hafas.details(
            arguments.name,
            arguments.station?.eva,
            arguments.date?.instant,
            arguments.profile?.profile
        )
            ?: discordError(translate("commands.journey.not_found", arguments.name))

        val selectedEva = journey.currentStop?.station?.id
        val selectedIndex = selectedEva?.let { journey.stops.indexOfFirst { it.station.id == selectedEva } }

        editingPaginator {
            journey.stops.forEach { stop ->
                page {
                    title = "${journey.train.name} - ${stop.station.title}"
                    url = marudor.hafas.detailsRedirect(journey.journeyId)

                    val messages = stop.irisMessages
                        .filter { it.head == null }
                        .joinToString("\n") {
                            "${it.timestamp.toDiscord(TimestampType.LongTime)}: ${it.text}".cancel(
                                it.superseded
                            )
                        }

                    description = messages

                    if (stop.arrival != null) {
                        field {
                            name = "Arrival"
                            value = stop.arrival.render()
                        }
                    }
                    if (stop.departure != null) {
                        field {
                            name = "Departure"
                            value = stop.departure.render()
                        }
                    }
                    val platform = stop.renderPlatform()
                    if (platform != null) {
                        field {
                            name = "Platform"
                            value = platform
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
        return "~~${scheduledTime!!.toDiscord(TimestampType.LongTime)}~~ ${time.toDiscord(TimestampType.LongTime)} (+$delay)"
    }

    return time.toDiscord(TimestampType.LongTime)
}

private fun Stop.renderPlatform(): String? {
    val scheduled = arrival?.scheduledPlatform ?: departure?.scheduledPlatform
    val actual = arrival?.platform ?: departure?.platform

    if (scheduled != null) {
        return "~~$scheduled~~ $actual"
    }

    return actual
}
