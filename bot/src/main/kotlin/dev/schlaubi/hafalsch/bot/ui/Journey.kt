package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.command.HafasProfileChoice
import dev.schlaubi.hafalsch.bot.core.sendStation
import dev.schlaubi.hafalsch.bot.paginator.refreshableMultiButtonPaginator
import dev.schlaubi.hafalsch.bot.util.fetchCoachSequence
import dev.schlaubi.hafalsch.marudor.entity.Station
import dev.schlaubi.mikbot.plugin.api.util.discordError
import kotlinx.datetime.Instant

interface JourneySource {
    val name: String
    val station: Station?
    val date: Instant?
    val profile: HafasProfileChoice?
}

data class JourneyData(
    override val name: String,
    override val station: Station?,
    override val date: Instant?,
    override val profile: HafasProfileChoice?
) : JourneySource

suspend fun UIContext.journey(journeySource: JourneySource, followUp: Boolean = false) {
    val journey = marudor.hafas.details(
        journeySource.name, journeySource.station?.eva, journeySource.date, journeySource.profile?.profile
    ) ?: discordError(translate("commands.journey.not_found", journeySource.name))

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
                journeySource.name, journeySource.station?.eva, journeySource.date, journeySource.profile?.profile
            ) ?: journey
        }

        pageBuilder {
            doFollowUp = followUp
            journey.stops.forEach { stop ->
                renderStopInfo(journey,  stop, specialTrainEmote)
            }
            ephemeralButton(2) {
                label = translate("journey.station_info")

                action {
                    respond {
                        val stationSegment = journey.stops[it.currentPageNum].station
                        val station = marudor.stopPlace.byEva(stationSegment.id)
                            ?: discordError(translate("journey.station.not_found"))

                        sendStation(station)
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
                                journey,
                                stop,
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
