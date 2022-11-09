package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.core.HafalschModule
import dev.schlaubi.hafalsch.bot.ui.findSpecialTrainEmote
import dev.schlaubi.hafalsch.bot.ui.formatNameWithPlan
import dev.schlaubi.hafalsch.bot.util.detailsByJourneyId
import dev.schlaubi.hafalsch.bot.util.embed
import dev.schlaubi.hafalsch.bot.util.journeyAutoComplete
import dev.schlaubi.hafalsch.marudor.entity.CoachSequence
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.hafalsch.marudor.entity.Stop
import dev.schlaubi.hafalsch.rainbow_ice.FetchTrainQuery
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.stdx.core.isNotNullOrBlank
import dev.schlaubi.stdx.coroutines.suspendLazy
import kotlinx.datetime.Instant

class TznJidArguments : Arguments() {
    val jid by string {
        name = "jid"
        description = "commands.tzn.by_jid.arguments.jid.description"
    }
}

class TznNameArguments : Arguments() {
    val name by string {
        name = "name"
        description = "commands.tzn.by_jid.arguments.name.description"

        journeyAutoComplete()
    }
}

private suspend fun CommandContext.noData(): Nothing = discordError(translate("commands.tzn.no_data"))

suspend fun HafalschModule.tznCommand() = publicSlashCommand {
    name = "tzn"
    description = "<unused>"

    publicSubCommand(::TznJidArguments) {
        name = "by-jid"
        description = "commands.tzn.by_jid.description"

        action {
            val details = marudor.detailsByJourneyId(arguments.jid) ?: noData()
            tznCommand(details)
        }

    }

    publicSubCommand(::TznNameArguments) {
        name = "by-name"
        description = "commands.tzn.by_name.description"

        action {
            val journey =
                runCatching { marudor.hafas.journeyMatch(arguments.name).firstOrNull() }.getOrNull() ?: noData()
            val details = marudor.hafas.details(
                journey.train.name,
                journey.firstStop.station.id,
                journey.firstStop.departure?.time
            ) ?: noData()

            tznCommand(details)
        }
    }
}

context (HafalschModule)
        private suspend fun JourneyInformation.findMostUpToDateInformation(
    trainNumber: Int,
    initialDeparture: Instant
): CoachSequence? {
    suspend fun tryFetch(stop: Stop?): CoachSequence? {
        if (stop == null) return null
        val departure = stop.departure?.scheduledTime
        if (departure != null) {
            return marudor.coachSequence.coachSequence(trainNumber, departure, stop.station.id, initialDeparture)
                .takeIf(CoachSequence?::isValid)
        }
        return null
    }

    suspend fun List<Stop>.findFirstMatch() = map {
        suspendLazy { tryFetch(it) }
    }.firstOrNull { it().isValid() }?.get()


    // First try to fetch the current stop
    val firstTry = currentStop ?: return null


    return tryFetch(firstTry) ?: run {
        // Then try to find the closest stop to the current stop with real time data
        val remainingStations = stops.subList(0, stops.indexOf(firstTry)).reversed()
        remainingStations.findFirstMatch()
    }
    ?: run {
        // then just find the first stop with a real-time departure
        val remainingStations = stops.subList(stops.indexOf(firstTry), stops.size)
        remainingStations.findFirstMatch()
    }
}


context(HafalschModule)
        suspend fun PublicSlashCommandContext<*>.tznCommand(details: JourneyInformation) {
    val trainNumber = details.train.number?.toIntOrNull() ?: noData()
    val initialDeparture = details.stops.firstOrNull()?.departure?.scheduledTime ?: noData()
    val coachSequence = details.findMostUpToDateInformation(trainNumber, initialDeparture) ?: noData()
    val type = coachSequence.product.type

    val embeds = buildList {
        coachSequence.sequence.groups.forEach { group ->
            val (coaches, name, _, _, _, number, model) = group
            embed {
                val a =
                    coaches.firstOrNull { it.identificationNumber.isNotNullOrBlank() }?.identificationNumber?.toInt()
                val b = coaches.lastOrNull { it.identificationNumber.isNotNullOrBlank() }?.identificationNumber?.toInt()
                title = if (a == null || b == null) {
                    translate("commands.tzn.info.title.unknown")
                } else {
                    val (begin, end) = listOf(a, b).sorted()

                    translate(
                        "commands.tzn.info.title",
                        arrayOf(begin, end)
                    )
                }


                val probableTzn = name.replace(type, "").trim().trimStart('0')
                val tznExists = rainbowICE.matchTrain(probableTzn)
                val tznJourney = rainbowICE.fetchTrain(probableTzn, 1)
                val probableJourney = tznJourney?.trips?.firstOrNull { (trainNumber) ->
                    trainNumber == number
                }
                val journeyMatches = probableJourney?.stops?.firstOrNull()?.scheduled_departure == initialDeparture

                description = when {
                    !tznExists -> translate("commands.tzn.probably_not", arrayOf(probableTzn))
                    !journeyMatches -> translate(
                        "commands.tzn.maybe",
                        arrayOf(probableTzn, tznJourney?.trips?.firstOrNull()?.name)
                    )

                    else -> translate("commands.tzn.definitely", arrayOf(probableTzn))
                }

                if (model != null) {
                    field {
                        this.name = translate("commands.tzn.model.title")
                        value = model.formatNameWithPlan()
                    }
                }
                val specialTrainEmote = group.findSpecialTrainEmote()
                if (specialTrainEmote.isNotNullOrBlank()) {
                    field {
                        this.name = translate("journey.is_best_train")
                        value = specialTrainEmote!!
                    }
                }
            }
        }
    }

    respond {
        content = translate(
            "commands.tzn.different_station",
            arrayOf(coachSequence.stop.stopPlace.name)
        ).takeIf { coachSequence.stop.stopPlace.evaNumber != details.currentStop?.station?.id }
        this.embeds.addAll(embeds)
    }
}

private fun CoachSequence?.isValid() = this != null && isRealtime && sequence.groups.isNotEmpty()

private val FetchTrainQuery.Trip.name: String
    get() = "$train_type $train_number"
