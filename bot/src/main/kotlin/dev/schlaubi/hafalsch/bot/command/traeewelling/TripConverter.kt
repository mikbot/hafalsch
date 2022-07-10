package dev.schlaubi.hafalsch.bot.command.traeewelling

import com.kotlindiscord.kord.extensions.checks.interactionFor
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.schlaubi.hafalsch.bot.command.AutoCompletingArgument
import dev.schlaubi.hafalsch.bot.command.sortByRelevance
import dev.schlaubi.hafalsch.bot.command.traeewelling.TraewellingStationConverter.Companion.safeIbnr
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.mikbot.plugin.api.util.discordError
import kotlinx.datetime.Clock
import org.koin.core.component.inject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import dev.schlaubi.hafalsch.traewelling.routes.Traewelling.Trains.Stationboard.TravelType as TraewellingTravelType

private val format = DateTimeFormatter.ofPattern("HH:mm")


@Converter(
    "trip",

    types = [ConverterType.SINGLE],
    builderBuildFunctionStatements = [
        "autoComplete { with(converter) { onAutoComplete() } }"
    ]
)
class TripConverter(validator: Validator<JourneyInfo> = null) : AutoCompletingArgument<JourneyInfo>(validator) {
    private val traewelling by inject<Traewelling>()
    override val signatureTypeString: String = "Trip"

    override fun withBuilder(builder: ConverterBuilder<JourneyInfo>): SingleConverter<JourneyInfo> {
        val builderWithName = builder.apply { name = Companion.name }
        return super.withBuilder(builderWithName)
    }

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        val interaction = interactionFor(context.eventObj) as ChatInputCommandInteraction
        val stationRaw = interaction.command.options[TraewellingStationConverter.name]?.value?.toString()
        context.withToken {
            val ibnr = stationRaw?.safeIbnr(token) ?: discordError("No departures found. Invalid station?")

            val departure = text.safeJid(ibnr, token)
                ?: discordError("No departures found. Invalid station?")

            parsed = departure
        }

        return true
    }

    suspend fun AutoCompleteInteraction.onAutoComplete() {
        val stationRaw = command.options[TraewellingStationConverter.name]?.value?.toString()
        withToken {
            val ibnr = stationRaw?.safeIbnr(token)

            if (ibnr == null) {
                suggestString {
                    choice("No departures found. Invalid station?", "0")
                }
                return@onAutoComplete
            }

            val typeRaw = command.options[TravelType.argumentName]?.value?.toString()
            val type = typeRaw?.let { runCatching { enumValueOf<TraewellingTravelType>(it) }.getOrNull() }

            val departures = traewelling.trains.stationBoard(
                ibnr,
                Clock.System.now(),
                type,
                token = token
            )

            suggestString {
                departures.departures
                    .filter { it.direction != null }
                    .sortByRelevance(focusedOption.value) { it.line.name }
                    .take(25).forEach { departure ->
                        val time = departure.time?.let { format.format(OffsetDateTime.parse(it)) }
                        val timeInfo = buildString {
                            if (time != null) {
                                append('(').append(time).append(')')
                            }
                            if (departure.delay != 0) {
                                append("+(").append(departure.delay / 60).append(')')
                            }
                        }
                        choice(
                            "${departure.line.name} -> ${departure.direction} $timeInfo",
                            "$jidPrefix${departure.tripId}:${departure.line.name}:${departure.stop.id}"
                        )
                    }
            }
        }
    }

    companion object : KordExKoinComponent {
        const val name = "trip"
        const val jidPrefix = "jid:"


        private val traewelling by inject<Traewelling>()

        suspend fun String.safeJid(ibnr: Int, token: String): JourneyInfo? {
            return if (!startsWith(jidPrefix)) {
                val journey = traewelling.trains.stationBoard(
                    ibnr,
                    Clock.System.now(),
                    token = token
                ).departures.firstOrNull()

                journey?.let { JourneyInfo(it.tripId, it.line.name, it.stop.id) }
            } else {
                val (jid, line, station) = substringAfter(jidPrefix).split(':')
                JourneyInfo(jid, line, station)
            }
        }
    }
}

data class JourneyInfo(val jid: String, val lineName: String, val station: String)
