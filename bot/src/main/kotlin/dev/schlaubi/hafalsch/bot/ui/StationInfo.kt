package dev.schlaubi.hafalsch.bot.ui

import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.bot.commands.JourneyArguments
import dev.schlaubi.hafalsch.bot.core.*
import dev.schlaubi.hafalsch.bot.paginator.MultiButtonPaginatorBuilder
import dev.schlaubi.hafalsch.bot.util.fetchCoachSequence
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.HafasMessage
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.hafalsch.marudor.entity.Stop
import dev.schlaubi.stdx.core.isNotNullOrEmpty


context(PublicSlashCommandContext<JourneyArguments>)
        fun MultiButtonPaginatorBuilder.renderStopInfo(
    journey: JourneyInformation,
    marudor: Marudor,
    stop: Stop,
    specialTrainEmote: String?
) {
    parent.page {
        val journeyName = "${journey.train.name} - ${stop.station.title.replaceStationNames()}"
        title = if (journey.cancelled) translate(
            "journey.wannabe",
            arrayOf(journeyName)
        ) else journeyName
        url = marudor.hafas.detailsRedirect(journey.journeyId)

        val hafasMessages = stop.messages.map(HafasMessage::txtN)

        val irisMessages = stop.irisMessages
            .filter { it.head == null }
            .map {
                buildString {
                    if (it.timestamp != null) {
                        append(it.timestamp!!.toDiscord(TimestampType.ShortTime)).append(':')
                    }
                    append(it.text.cancel(it.superseded))
                }
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

        if (specialTrainEmote.isNotNullOrEmpty()) {
            field {
                name = translate("journey.is_best_train")
                value = specialTrainEmote.toString()
            }
        }

        if (journey.train.operator != null) {
            field {
                name = translate("journey.operator")
                value = journey.train.operator!!.name
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

suspend fun Marudor.findSpecialTrainEmote(journey: JourneyInformation): String? {
    val stop = journey.stops.firstOrNull { it.departure?.hasWaggonOrder == true }
        ?: return null
    val order = journey.fetchCoachSequence(stop) ?: return null
    return order.sequence.groups
        .asSequence()
        .mapNotNull {
            it.name.substringAfter("ICE").toIntOrNull()
        }
        .mapNotNull {
            when (it) {
                rainbowICE -> Emojis.rainbow
                europeICE -> Emojis.flagEu
                germanyICE -> Emojis.flagDe
                maskICE -> Emojis.mask
                femaleICE -> Emojis.womanOfficeWorker
                else -> null
            }
        }
        .map(DiscordEmoji::unicode)
        .joinToString("")
}

private fun String.cancel(cancel: Boolean) = if (cancel) "~~$this~~" else this

private fun Stop.Date?.render(): String {
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
