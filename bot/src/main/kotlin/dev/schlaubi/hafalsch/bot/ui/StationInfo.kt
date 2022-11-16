package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.bot.core.*
import dev.schlaubi.hafalsch.bot.paginator.MultiButtonPaginatorBuilder
import dev.schlaubi.hafalsch.bot.util.fetchCoachSequence
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.*
import dev.schlaubi.stdx.core.isNotNullOrEmpty


context(UIContext)
fun MultiButtonPaginatorBuilder.renderStopInfo(
    journey: JourneyInformation,
    stop: Stop,
    specialTrainEmote: String?,
    additionalJourneyInformation: HafasAdditionalJourneyInformation? = null
) {
    parent.page {
        val journeyName = "${journey.train.name} - ${stop.station.title.replaceStationNames()}"
        title = if (journey.cancelled) translate("journey.wannabe", journeyName) else journeyName
        url = marudor.hafas.detailsRedirect(journey.journeyId)

        val hafasMessages = stop.messages.map(HafasMessage::txtN)

        val irisMessages = stop.irisMessages
            .filterRelevant()
            .format()


        description = (hafasMessages + irisMessages).joinToString("\n")
        val delay = stop.departure?.delay ?: stop.arrival?.delay
        color = when {
            stop.additional -> DiscordColors.GREEN
            stop.cancelled -> DiscordColors.FUCHSIA
            delay != null && delay > 0 -> DiscordColors.RED
            delay != null && delay <= 0 -> DiscordColors.BLURLPLE

            else -> DiscordColors.BLACK
        }

        if (specialTrainEmote.isNotNullOrEmpty()) {
            field {
                name = translate("journey.is_best_train")
                value = specialTrainEmote.toString()
            }
        }

        if (journey.train.operator != null) {
            field {
                name = translate("journey.operator")
                value = (additionalJourneyInformation?.operatorName ?: journey.train.operator?.name).toString()
            }
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


        val load = additionalJourneyInformation?.occupancy?.get(stop.station.id.toInt())
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

suspend fun Marudor.findSpecialTrainEmote(journey: JourneyInformation): String? {
    val stop = journey.stops.firstOrNull { it.departure?.hasWaggonOrder == true }
        ?: return null
    val order = journey.fetchCoachSequence(stop) ?: return null
    return order.findSpecialTrainEmote()
}

fun CoachSequence.findSpecialTrainEmote(): String {
    return sequence.groups
        .asSequence()
        .mapNotNull(CoachGroup::findSpecialTrainEmote)
        .joinToString("")
}

fun CoachGroup.findSpecialTrainEmote(): String? {
    val tzn = name.substringAfter("ICE").toIntOrNull() ?: return null
    val emoji = when (tzn) {
        rainbowICE -> Emojis.rainbow
        europeICE -> Emojis.flagEu
        germanyICE -> Emojis.flagDe
        maskICE -> Emojis.mask
        femaleICE -> Emojis.womanOfficeWorker
        else -> return null
    }
    return emoji.unicode
}

fun Stop.Date?.render(): String {
    if (this == null) {
        return "<unknown>"
    }

    if (delay != null && delay != 0) {
        return "~~${scheduledTime.toDiscord(TimestampType.ShortTime)}~~ ${time.toDiscord(TimestampType.ShortTime)} (+$delay)"
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
