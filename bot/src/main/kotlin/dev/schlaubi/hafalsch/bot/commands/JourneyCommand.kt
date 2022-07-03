package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.hafalsch.bot.command.optionalDate
import dev.schlaubi.hafalsch.bot.command.optionalStation
import dev.schlaubi.hafalsch.bot.command.profile
import dev.schlaubi.hafalsch.bot.core.*
import dev.schlaubi.hafalsch.bot.paginator.refreshableMultiButtonPaginator
import dev.schlaubi.hafalsch.bot.ui.*
import dev.schlaubi.hafalsch.bot.util.fetchCoachSequence
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.inject

class JourneyArguments : Arguments(), KordExKoinComponent {
    private val marudor by inject<Marudor>()

    val name by string {
        name = "name"
        description = "commands.journey.arguments.name.description"

        autoComplete {
            val input = focusedOption.safeInput

            coroutineScope {
                val matches =
                    runCatching { marudor.hafas.journeyMatch(input.substringBefore(' ')) }.getOrElse { emptyList() }
                val results = matches.sortedBy {
                    it.train.name.withIndex().count { (index, value) -> input.getOrNull(index) == value }
                }

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

        val specialTrainEmote = marudor.findSpecialTrainEmote(journey)

        refreshableMultiButtonPaginator(initialData = journey) {
            retriever {
                marudor.hafas.details(
                    arguments.name, arguments.station?.eva, arguments.date, arguments.profile?.profile
                ) ?: journey
            }

            pageBuilder {
                journey.stops.forEach { stop ->
                    renderStopInfo(journey, marudor, stop, specialTrainEmote)
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
                            val stop = journey.stops[it.currentPageNum]
                            val sequence = with(marudor) { journey.fetchCoachSequence(stop) }
                            if (sequence == null) {
                                respond {
                                    content = translate("journey.coach_sequence.not_found")
                                }
                            } else {
                                sendWaggonOrder(
                                    marudor,
                                    journey,
                                    stop,
                                    interactionResponse,
                                    bundle,
                                    sequence
                                )
                            }
                        }
                    }
                }
            }

            paginatorConfigurator {
                if (selectedIndex != null) {
                    currentPageNum = selectedIndex
                    currentPage = pages.get(currentGroup, currentPageNum)

                }
            }
        }
    }
}

