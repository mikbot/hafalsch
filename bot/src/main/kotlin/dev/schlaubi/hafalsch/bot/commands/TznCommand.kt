package dev.schlaubi.hafalsch.bot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.hafalsch.bot.ui.findSpecialTrainEmote
import dev.schlaubi.hafalsch.bot.ui.formatNameWithPlan
import dev.schlaubi.hafalsch.bot.util.detailsByJourneyId
import dev.schlaubi.hafalsch.bot.util.embed
import dev.schlaubi.hafalsch.bot.util.journeyAutoComplete
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.CoachSequence
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.stdx.core.isNotNullOrBlank
import kotlinx.datetime.Instant
import org.koin.core.component.inject

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

suspend fun Extension.tznCommand() = publicSlashCommand {
    name = "tzn"
    description = "<unused>"
    val marudor by inject<Marudor>()

    publicSubCommand(::TznJidArguments) {
        name = "by-jid"
        description = "commands.tzn.by_jid.description"

        action {
            val details = marudor.detailsByJourneyId(arguments.jid) ?: noData()
            val number = details.train.number?.toIntOrNull() ?: noData()
            val sequence = marudor.coachSequence.coachSequence(
                number,
                details.departure.time,
                initialDeparture = details.stops.first().departure?.time
            )

            tznCommand(sequence, details.stops.first().departure?.time ?: details.departure.time)
        }

    }

    publicSubCommand(::TznNameArguments) {
        name = "by-name"
        description = "commands.tzn.by_name.description"

        action {
            val journey =
                runCatching { marudor.hafas.journeyMatch(arguments.name).firstOrNull() }.getOrNull() ?: noData()
            val number = journey.train.number?.toIntOrNull() ?: noData()
            val departure = journey.firstStop.departure?.time ?: noData()
            val sequence = marudor.coachSequence.coachSequence(
                number, departure, initialDeparture = departure
            )

            tznCommand(sequence, departure)
        }
    }
}

context(Extension)
        suspend fun PublicSlashCommandContext<*>.tznCommand(coachSequence: CoachSequence?, initialDeparture: Instant) {
    val rainbowICE by inject<RainbowICE>()

    if (coachSequence == null || !coachSequence.isRealtime || coachSequence.sequence.groups.isEmpty()) {
        noData()
    }

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


                val probableTzn = name.replace(type, "").trim().trim('0')
                val tznExists = rainbowICE.matchTrain(probableTzn)
                val tznJourney = rainbowICE.fetchTrain(probableTzn, 5, true)
                val probableJourney = tznJourney?.trips?.firstOrNull { journey ->
                    journey.trainNumber.toString() == number
                }
                val journeyMatches = probableJourney?.stops?.firstOrNull()?.scheduledDeparture == initialDeparture

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
        this.embeds.addAll(embeds)
    }
}

private val TrainVehicle.Trip.name: String
    get() = "$trainType $trainNumber"
