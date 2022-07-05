package dev.schlaubi.hafalsch.bot.command

import com.kotlindiscord.kord.extensions.checks.interactionFor
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.schlaubi.hafalsch.bot.command.TraewellingStationConverter.Companion.safeIbnr
import dev.schlaubi.hafalsch.bot.command.TripConverter.Companion.safeJid
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.koin.core.component.inject

@Converter(
    "exit",

    types = [ConverterType.SINGLE]
)
class ExitConverter(validator: Validator<String> = null) : AutoCompletingArgument<String>(validator) {
    private val traewelling by inject<Traewelling>()
    override val signatureTypeString: String = "Exit"

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        parsed = if (text.startsWith(ibnrPrefix)) {
            text.substringAfter(ibnrPrefix)
        } else {
            context.withToken {
                val interaction = interactionFor(context.eventObj) as ChatInputCommandInteraction
                val stationRaw = interaction.command.options[TraewellingStationConverter.name]?.value?.toString()
                val tripRaw = interaction.command.options[TripConverter.name]?.value?.toString()

                val ibnr = stationRaw?.safeIbnr(token) ?: discordError("Please specify a station")

                val (jid, lineName, stopId) = tripRaw?.safeJid(ibnr, token) ?: discordError("No exits found. Invalid trip?")

                val trip = traewelling.trains.trip(
                    jid,
                    lineName,
                    stopId,
                    token
                )

                trip.stopOvers.last().stop.id
            }
        }

        return true
    }

    override suspend fun AutoCompleteInteraction.onAutoComplete() {
        val stationRaw = command.options[TraewellingStationConverter.name]?.value?.toString()
        val tripRaw = command.options[TripConverter.name]?.value?.toString()
        withToken {
            val ibnr = stationRaw?.safeIbnr(token)

            if (ibnr == null) {
                suggestString {
                    choice("Please specify a station", "0")
                }
                return@onAutoComplete
            }

            val (jid, lineName, stopId) = tripRaw?.safeJid(ibnr, token) ?: run {
                suggestString {
                    choice("No exits found. Invalid trip?", "0")
                }
                return@onAutoComplete
            }

            val trip = traewelling.trains.trip(
                jid,
                lineName,
                stopId,
                token
            )

            suggestString {
                trip.stopOvers.take(25).forEach {
                    choice(it.stop.name, "$ibnrPrefix${it.stop.id}")
                }
            }
        }
    }

    companion object {
        const val ibnrPrefix = "ibnr:"
    }
}
