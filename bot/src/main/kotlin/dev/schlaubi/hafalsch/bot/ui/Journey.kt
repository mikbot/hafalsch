package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.core.sendStation
import dev.schlaubi.hafalsch.bot.paginator.MultiButtonPaginatorBuilder
import dev.schlaubi.hafalsch.bot.paginator.refreshableMultiButtonPaginator
import dev.schlaubi.hafalsch.bot.util.fetchCoachSequence
import dev.schlaubi.hafalsch.marudor.entity.HafasAdditionalJourneyInformation
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.hafalsch.marudor.entity.Station
import dev.schlaubi.mikbot.plugin.api.util.discordError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Instant

interface JourneySource {
    val name: String
    val station: Station?
    val date: Instant?
}

data class JourneyData(
    override val name: String,
    override val station: Station?,
    override val date: Instant?
) : JourneySource

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun UIContext.journey(journeySource: JourneySource, followUp: Boolean = false) {
    val journey = marudor.journeys.details(
        journeySource.name, journeySource.station?.eva, journeySource.date
    ) ?: discordError(translate("commands.journey.not_found", journeySource.name))

    val selectedEva = journey.currentStop?.station?.id
    val selectedIndex = selectedEva?.run { journey.stops.indexOfFirst { it.station.id == selectedEva } }
    val pagesWithOrder = journey.stops.asSequence()
        .withIndex()
        .filter { (_, stop) -> stop.arrival?.hasWaggonOrder == true || stop.departure?.hasWaggonOrder == true }
        .map(IndexedValue<*>::index)
        .toList()

    val specialTrainEmote = marudor.findSpecialTrainEmote(journey)

    coroutineScope {
        val additionalInformation = async {
            marudor.hafas.additionalInformation(
                journeySource.name, journeySource.station?.eva, journeySource.date
            )
        }

        val paginator = refreshableMultiButtonPaginator(initialData = journey) {
            retriever {
                marudor.journeys.details(
                    journeySource.name, journeySource.station?.eva, journeySource.date
                ) ?: journey
            }

            pageBuilder {
                doFollowUp = followUp
                val additional = if (additionalInformation.isCompleted) {
                    additionalInformation.getCompleted()
                } else {
                    null
                }
                addJourneys(journey, specialTrainEmote, pagesWithOrder, additional)
            }

            paginatorConfigurator {
                if (selectedIndex != null) {
                    currentPageNum = selectedIndex
                    currentPage = pages.get(currentGroup, currentPageNum)
                }
            }
        }

        val fullInformation = additionalInformation.await()
        if (fullInformation != null) {
            val newPages = journeyPages(journey, specialTrainEmote, pagesWithOrder, additionalInformation.await())
            paginator.updatePages(newPages)
        }
    }
}

context (UIContext)
fun journeyPages(
    journey: JourneyInformation,
    specialTrainEmote: String?,
    pagesWithOrder: List<Int>,
    additionalJourneyInformation: HafasAdditionalJourneyInformation? = null
): Pages {
    val builder = MultiButtonPaginatorBuilder("", PaginatorBuilder())
    builder.addJourneys(journey, specialTrainEmote, pagesWithOrder, additionalJourneyInformation)
    return builder.parent.pages
}

context (UIContext)
fun MultiButtonPaginatorBuilder.addJourneys(
    journey: JourneyInformation,
    specialTrainEmote: String?,
    pagesWithOrder: List<Int>,
    additionalJourneyInformation: HafasAdditionalJourneyInformation? = null
) {
    journey.stops.forEach { stop ->
        renderStopInfo(journey, stop, specialTrainEmote, additionalJourneyInformation)
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
