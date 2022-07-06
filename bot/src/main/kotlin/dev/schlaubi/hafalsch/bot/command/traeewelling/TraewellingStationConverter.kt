package dev.schlaubi.hafalsch.bot.command.traeewelling

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.schlaubi.hafalsch.bot.command.AutoCompletingArgument
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import org.koin.core.component.inject

@Converter(
    "traewellingStation",

    types = [ConverterType.SINGLE]
)
class TraewellingStationConverter(validator: Validator<Int> = null) : AutoCompletingArgument<Int>(validator) {
    private val traewelling by inject<Traewelling>()
    override val signatureTypeString: String = "Station"
    override fun withBuilder(builder: ConverterBuilder<Int>): SingleConverter<Int> {
        val builderWithName = builder.apply { name = Companion.name }
        return super.withBuilder(builderWithName)
    }

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        context.withToken {
            val station = text.safeIbnr(token)
                ?: discordError(context.translate("arguments.station.not_found", arrayOf(text)))
            parsed = station
        }

        return true
    }

    override suspend fun AutoCompleteInteraction.onAutoComplete() {
        withToken {
            val safeInput = focusedOption.safeInput

            if (safeInput.isEmpty()) {
                suggest<String>(emptyList())
            } else {
                val response = traewelling.trains.autocomplete(safeInput, token)

                suggestString {
                    response
                        .asSequence()
                        .take(25).forEach {
                            val identifier = if (it.rilIdentifier != null) {
                                "${it.name} (${it.rilIdentifier})"
                            } else {
                                it.name
                            }
                            choice(identifier, "$ibnrPrefix${it.id}")
                        }
                }
            }
        }
    }

    companion object : KordExKoinComponent {
        const val name = "station"
        const val ibnrPrefix = "ibnr:"

        private val traewelling by inject<Traewelling>()

        suspend fun String.safeIbnr(token: String): Int? {
            return if (!startsWith(ibnrPrefix)) {
                traewelling.trains.autocomplete(this, token).firstOrNull()?.id
            } else {
                substringAfter(ibnrPrefix).toIntOrNull()
            }
        }
    }
}
